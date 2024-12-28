package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.classes.Listing;
import edu.brown.cs.student.main.server.storage.Sorter;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a GetListingsHandler object.
 *
 * <p>Handles get-listings request to our server, which is the request used to get all Listing
 * object from the database. Implements Route: Route is the SparkJava interface for request
 * handlers.
 */
public class GetListingsHandler implements Route {

  public StorageInterface dbHandler;

  public GetListingsHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
  }

  /**
   * Method that handles get-listings request
   *
   * @param request - request from user
   * @param response - the response
   * @return the response map, represented as a Map from String to Object
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      // EXAMPLE QUERY to get all listings
      // "http://localhost:3232/get-listings"

      String title = request.queryParams("title");
      String category = request.queryParams("category");
      String minPrice = request.queryParams("minPrice");
      String maxPrice = request.queryParams("maxPrice");
      String tagsParam = request.queryParams("tags");
      String sorterStr = request.queryParams("sorter");

      validatePrice(minPrice);
      validatePrice(maxPrice);

      List<String> tagsList = parseTags(tagsParam);

      Sorter sorter = null;

      if (sorterStr != null) {
        if (sorterStr.equals("PRICE_ASC")) {
          sorter = Sorter.PRICE_ASC;
        } else if (sorterStr.equals("PRICE_DESC")) {
          sorter = Sorter.PRICE_DESC;
        } else {
          throw new IllegalArgumentException(
              "Invalid input for sorter. It is either PRICE_ASC or PRICE_DESC");
        }
      }

      List<Listing> res =
          this.dbHandler.getListings(
              title,
              category,
              (minPrice == null ? null : Float.valueOf(minPrice)),
              (maxPrice == null ? null : Float.valueOf(maxPrice)),
              tagsList,
              sorter);

      responseMap.put("response_type", "success");
      responseMap.put("result", res);
      responseMap.put("number of listings obtained", res.size());

    } catch (IllegalArgumentException e) {
      responseMap.put("response_type", "failure");
      responseMap.put("error", "Invalid input: " + e.getMessage());
    } catch (Exception e) {
      responseMap.put("response_type", "failure");
      responseMap.put("error", "Unexpected error: " + e.getMessage());
    }

    return Utils.toMoshiJson(responseMap);
  }

  // validation methods for user input

  private Long validateListingId(String listingIdStr) {
    if (listingIdStr == null || listingIdStr.isEmpty()) {
      throw new IllegalArgumentException("Listing ID is required");
    }
    return Long.parseLong(listingIdStr);
  }

  private void validatePrice(String priceStr) {
    float price = (priceStr == null ? 0 : Float.parseFloat(priceStr));
    if (price < 0) {
      throw new IllegalArgumentException("Price cannot be negative");
    }
  }

  private List<String> parseTags(String tagsParam) {
    if (tagsParam == null || tagsParam.trim().isEmpty()) {
      return List.of();
    }
    return Arrays.stream(tagsParam.split(","))
        .map(String::trim)
        .filter(tag -> !tag.isEmpty())
        .toList();
  }
}
