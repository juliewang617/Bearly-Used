package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a AddUserHandler object.
 *
 * <p>Handles add-user request to our server, which is the request used to add a new User object to
 * the database. Implements Route: Route is the SparkJava interface for request handlers.
 */
public class AddUserHandler implements Route {

  public StorageInterface dbHandler;

  public AddUserHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
  }

  /**
   * Method that handles add-user request
   *
   * @param request - request from user
   * @param response - the response
   * @return the response map, represented as a Map from String to Object
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      String clerkId = request.queryParams("clerk_id");
      String email = validateEmail(request.queryParams("email"));
      String name = validateName(request.queryParams("name"));
      String phoneNumber = validatePhoneNumber(request.queryParams("phone_number"));
      String school = validateSchool(request.queryParams("school"));

      // EXAMPLE QUERY:
      // http://localhost:3232/add-user?clerk_id=12345&email=abc@gmail.com&name=bob&phone_number=1234567890&school=brown

      Long userId = this.dbHandler.createUser(clerkId, email, name, phoneNumber, school);

      responseMap.put("response_type", "success");
      responseMap.put("user_id", userId);
      responseMap.put("clerk_id", clerkId);
      responseMap.put("email", email);
      responseMap.put("name", name);
      responseMap.put("phone number", phoneNumber);
      responseMap.put("school", school);
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

  private String validateEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email is required");
    }
    // Check that email ends with "@risd.edu" or "@brown.edu"
    String lowerEmail = email.toLowerCase();
    if (!lowerEmail.endsWith("@risd.edu") && !lowerEmail.endsWith("@brown.edu")) {
      throw new IllegalArgumentException("Email must end with @risd.edu or @brown.edu");
    }

    return email.trim();
  }

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
