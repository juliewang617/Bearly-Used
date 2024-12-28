package edu.brown.cs.student.main.server.handlers;

import com.google.gson.Gson;
import edu.brown.cs.student.main.server.classes.Listing;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a GetListingsByIdHandler object.
 *
 * <p>Handles get-listing-by-id request to our server, which is the request used to get a specific
 * Listing object via its unique id from the database. Implements Route: Route is the SparkJava
 * interface for request handlers.
 */
public class GetListingsByIdHandler implements Route {
  public StorageInterface dbHandler;
  private Gson gson;

  public GetListingsByIdHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
    this.gson = new Gson();
  }

  /**
   * Method that handles get-listing-by-id request
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

      // EXAMPLE QUERY:
      // http://localhost:3232/get-listing-by-id?listing_id=2

      Listing listing = this.dbHandler.obtainListing(listingId);

      if (listing != null) {
        responseMap.put("response_type", "success");
        responseMap.put("listing", listing);

        // Manual serialization to JSON
        String jsonResponse = gson.toJson(responseMap);
        response.status(200); // Set the HTTP status code
        response.type("application/json"); // Set the response type to JSON
        return jsonResponse; // Return the serialized JSON string
      } else {
        responseMap.put("response_type", "failure");
        responseMap.put("error", "Listing not found");
        return gson.toJson(responseMap);
      }
    } catch (Exception e) {
      responseMap.put("response_type", "failure");
      responseMap.put("error", "Invalid input: " + e.getMessage());
      return gson.toJson(responseMap);
    }
  }

  // validation method for user input
  private Long validateListingId(String listingIdStr) {
    if (listingIdStr == null || listingIdStr.isEmpty()) {
      throw new IllegalArgumentException("Listing ID is required");
    }
    return Long.parseLong(listingIdStr);
  }
}
