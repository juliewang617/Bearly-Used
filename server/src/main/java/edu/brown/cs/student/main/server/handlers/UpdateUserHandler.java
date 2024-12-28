package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.classes.User;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a UpdateUserHandler object.
 *
 * <p>Handles update-user request to our server, which is the request used to update a User object
 * from the database. Implements Route: Route is the SparkJava interface for request handlers.
 */
public class UpdateUserHandler implements Route {
  public StorageInterface dbHandler;

  public UpdateUserHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
  }

  /**
   * Method that handles update-user request
   *
   * @param request - request from user
   * @param response - the response
   * @return the response map, represented as a Map from String to Object
   */
  @Override
  public Object handle(Request request, Response response) {
    System.out.println("Received request to update user");
    Map<String, Object> responseMap = new HashMap<>();

    try {

      String clerkId = request.queryParams("clerk_id");
      String name = request.queryParams("name");
      String phoneNumber = request.queryParams("phone_number");
      String school = request.queryParams("school");

      // EXAMPLE QUERY:
      // http://localhost:3232/update-user?clerk_id=9&name=robbie&phone_number=123-444-3333&school=risd

      User newUser =
          new User(
              clerkId,
              name != null ? validateName(name) : null,
              phoneNumber != null ? validatePhoneNumber(phoneNumber) : null,
              school != null ? validateSchool(school) : null);

      boolean updated = this.dbHandler.updateUser(clerkId, newUser);

      if (updated) {
        responseMap.put("response_type", "success");
        responseMap.put("message", "User updated successfully");
        responseMap.put("clerk_id", clerkId);
        responseMap.put("name", name);
        responseMap.put("phone_number", phoneNumber);
        responseMap.put("school", school);
      } else {
        responseMap.put("response_type", "failure");
        responseMap.put("error", "User could not be updated");
      }
    } catch (IllegalArgumentException e) {
      // Handle input validation errors
      responseMap.put("response_type", "failure");
      responseMap.put("error", "Invalid input: " + e.getMessage());
    } catch (Exception e) {
      responseMap.put("response_type", "failure");
      responseMap.put("error", "Unexpected error: " + e.getMessage());
    }

    return Utils.toMoshiJson(responseMap);
  }

  // validation methods for user input

  private String validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }
    if (name.trim().length() < 2) {
      throw new IllegalArgumentException("Name must be at least 2 characters long");
    }

    return name.trim();
  }

  private String validatePhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("Phone number is required");
    }
    // Regex for various formats:
    // (123) 456-7890, 123-456-7890, 1234567890
    if (!Pattern.matches(
        "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$",
        phoneNumber.replaceAll("\\s", ""))) {
      throw new IllegalArgumentException("Invalid phone number format");
    }

    return phoneNumber.trim();
  }

  private String validateSchool(String school) {
    if (school == null || school.trim().isEmpty()) {
      throw new IllegalArgumentException("School is required");
    }
    if (!(school.equalsIgnoreCase("brown") || school.equalsIgnoreCase("RISD"))) {
      throw new IllegalArgumentException("School must be Brown or RISD");
    }

    return school.trim();
  }
}
