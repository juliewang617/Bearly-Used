package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a DeleteListingHandler object.
 *
 * <p>Handles delete-listing request to our server, which is the request used to delete a Listing
 * object from the database. Implements Route: Route is the SparkJava interface for request
 * handlers.
 */
public class DeleteListingHandler implements Route {

  public StorageInterface dbHandler;

  public DeleteListingHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
  }

  /**
   * Method that handles delete-listing request
   *
   * @param request - request from user
   * @param response - the response
   * @return the response map, represented as a Map from String to Object
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      // Requires the listing ID to know which listing to delete
      Long listingId = validateListingId(request.queryParams("listing_id"));

      // EXAMPLE QUERY:
      // http://localhost:3232/delete-listing?listing_id=1

      boolean deleted = this.dbHandler.deleteListing(listingId);

      if (deleted) {
        responseMap.put("response_type", "success");
        responseMap.put("message", "Listing deleted successfully");
      } else {
        responseMap.put("response_type", "failure");
        responseMap.put("error", "Listing not found");
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

  // validation method for user input

  private Long validateListingId(String listingIdStr) {
    if (listingIdStr == null || listingIdStr.isEmpty()) {
      throw new IllegalArgumentException("Listing ID is required");
    }
    return Long.parseLong(listingIdStr);
  }
}
