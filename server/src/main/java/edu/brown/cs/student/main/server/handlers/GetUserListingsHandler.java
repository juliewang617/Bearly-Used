package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a GetUserListingsHandler object.
 *
 * <p>Handles get-user-listings request to our server, which is the request used to get the Listing
 * objects made by a specifc User object via the user's Clerk id from the database. Implements
 * Route: Route is the SparkJava interface for request handlers.
 */
public class GetUserListingsHandler implements Route {

  private final StorageInterface dbHandler;

  public GetUserListingsHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
  }

  /**
   * Method that handles get-user-listing request
   *
   * @param request - request from user
   * @param response - the response
   * @return the response map, represented as a Map from String to Object
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      String sellerIdParam = request.queryParams("seller_id");
      // validation here
      if (sellerIdParam == null || sellerIdParam.trim().isEmpty()) {
        throw new IllegalArgumentException("Seller ID is required");
      }

      // Fetch listings from the database for specified seller_id
      List<Map<String, Object>> listings = this.dbHandler.getListingsBySellerId(sellerIdParam);

      if (listings == null || listings.isEmpty()) {
        responseMap.put("response_type", "failure");
        responseMap.put("error", "No listings found for the given seller ID");
      } else {
        responseMap.put("response_type", "success");
        responseMap.put("listings", listings);
      }
    } catch (NumberFormatException e) {
      responseMap.put("response_type", "failure");
      responseMap.put("error", "Invalid seller ID format");
    } catch (IllegalArgumentException e) {
      responseMap.put("response_type", "failure");
      responseMap.put("error", "Invalid input: " + e.getMessage());
    } catch (Exception e) {
      responseMap.put("response_type", "failure");
      responseMap.put("error", "Unexpected error: " + e.getMessage());
    }

    return Utils.toMoshiJson(responseMap);
  }
}
