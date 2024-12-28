package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a GetUserHandler object.
 *
 * <p>Handles get-user request to our server, which is the request used to get a specifc User object
 * via the user's Clerk id from the database. Implements Route: Route is the SparkJava interface for
 * request handlers.
 */
public class GetUserHandler implements Route {

  private StorageInterface dbHandler;

  public GetUserHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
  }

  /**
   * Method that handles get-user request
   *
   * @param request - request from user
   * @param response - the response
   * @return the response map, represented as a Map from String to Object
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      String userIdParam = request.queryParams("clerk_id");
      // validate id here
      if (userIdParam == null || userIdParam.trim().isEmpty()) {
        throw new IllegalArgumentException("Clerk ID is required");
      }

      Map<String, Object> userData = this.dbHandler.getUser(userIdParam);

      if (userData == null || userData.isEmpty()) {
        responseMap.put("response_type", "failure");
        responseMap.put("error", "User not found");
      } else {
        responseMap.put("response_type", "success");
        responseMap.put("user_data", userData);
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
}
