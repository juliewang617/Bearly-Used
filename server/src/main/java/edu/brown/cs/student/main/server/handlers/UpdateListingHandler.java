package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.classes.Listing;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a UpdateListingHandler object.
 *
 * <p>Handles update-listing request to our server, which is the request used to update a Listing
 * object from the database. Implements Route: Route is the SparkJava interface for request
 * handlers.
 */
public class UpdateListingHandler implements Route {

  public StorageInterface dbHandler;

  public UpdateListingHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
  }

  /**
   * Method that handles update-listing request
   *
   * @param request - request from user
   * @param response - the response
   * @return the response map, represented as a Map from String to Object
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      Long listingId = validateListingId(request.queryParams("listing_id"));
      String sellerId = request.queryParams("seller_id");
      String title = request.queryParams("title");
      String description = request.queryParams("description");
      String priceStr = request.queryParams("price");
      String availableStr = request.queryParams("available");
      String category = request.queryParams("category");
      String condition = request.queryParams("condition");
      String imageUrl = request.queryParams("image_url");
      String tagsParam = request.queryParams("tags");

      // EXAMPLE QUERY:
      // http://localhost:3232/update-listing?listing_id=1&title=Updated+Book&price=20.0

      Listing newList =
          new Listing(
              listingId,
              sellerId,
              title != null ? validateTitle(title) : null,
              description != null ? validateDescription(description) : null,
              priceStr != null ? validatePrice(priceStr) : null,
              category != null ? validateCategory(category) : null,
              condition != null ? validateCondition(condition) : null,
              imageUrl != null ? validateImageUrl(imageUrl) : null,
              tagsParam != null ? parseTags(tagsParam) : null,
              Boolean.parseBoolean(availableStr));

      boolean updated = this.dbHandler.updateListing(listingId, newList);

      if (updated) {
        responseMap.put("response_type", "success");
        responseMap.put("message", "Listing updated successfully");
        responseMap.put("listing_id", listingId);
        responseMap.put("seller_id", sellerId);
        responseMap.put("title", title);
        responseMap.put("description", description);
        responseMap.put("price", priceStr);
        responseMap.put("category", category);
        responseMap.put("condition", condition);
        responseMap.put("image_url", imageUrl);
        responseMap.put("tags", tagsParam);
      } else {
        responseMap.put("response_type", "failure");
        responseMap.put("error", "Listing could not be updated");
      }
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

  private String validateTitle(String title) {
    return title.trim();
  }

  private String validateDescription(String description) {
    return description.trim();
  }

  private float validatePrice(String priceStr) {
    float price = Float.parseFloat(priceStr);
    if (price < 0) {
      throw new IllegalArgumentException("Price cannot be negative");
    }
    return price;
  }

  private String validateCategory(String category) {
    return category.trim();
  }

  private String validateCondition(String condition) {
    return condition.trim();
  }

  private String validateImageUrl(String imageUrl) {
    return imageUrl.trim();
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
