package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class representing a AddListingHandler object.
 *
 * <p>Handles add-listing request to our server, which is the request used to add a new Listing
 * object to the database. Implements Route: Route is the SparkJava interface for request handlers.
 */
public class AddListingHandler implements Route {

  public StorageInterface dbHandler;

  public AddListingHandler(StorageInterface dbHandler) {
    this.dbHandler = dbHandler;
  }

  /**
   * Method that handles add-listing request
   *
   * @param request - request from user
   * @param response - the response
   * @return the response map, represented as a Map from String to Object
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      String sellerId =
          validateSellerId(request.queryParams("seller_id")); // should be the clerk id
      String title = validateTitle(request.queryParams("title"));
      boolean available = Boolean.parseBoolean(request.queryParams("available"));
      String description = validateDescription(request.queryParams("description"));
      float price = validatePrice(request.queryParams("price"));
      String category = validateCategory(request.queryParams("category"));
      String condition = validateCondition(request.queryParams("condition"));
      String imageUrl = validateImageUrl(request.queryParams("image_url"));
      // parse tags
      List<String> tags = parseTags(request.queryParams("tags"));

      // EXAMPLE QUERY:
      // http://localhost:3232/add-listing?seller_id=1&title=Book&available=true&description=Great+book
      // &price=15.0&category=Books&condition=New&image_url=book.jpg&tags=fiction,thriller]

      Long listingId =
          this.dbHandler.createListing(
              sellerId, title, available, description, price, category, condition, imageUrl, tags);

      responseMap.put("response_type", "success");
      responseMap.put("listing_id", listingId);
      responseMap.put("sellerId", sellerId);
      responseMap.put("title", title);
      responseMap.put("available", available);
      responseMap.put("description", description);
      responseMap.put("price", price);
      responseMap.put("category", category);
      responseMap.put("condition", condition);
      responseMap.put("imageUrl", imageUrl);
      responseMap.put("tags", tags);
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

  private String validateSellerId(String sellerIdStr) {
    if (sellerIdStr == null || sellerIdStr.isEmpty()) {
      throw new IllegalArgumentException("Seller ID is required");
    }
    return sellerIdStr;
  }

  private String validateTitle(String title) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Title is required");
    }
    return title.trim();
  }

  private String validateDescription(String description) {
    if (description == null || description.trim().isEmpty()) {
      throw new IllegalArgumentException("Description is required");
    }
    return description.trim();
  }

  private float validatePrice(String priceStr) {
    if (priceStr == null || priceStr.isEmpty()) {
      throw new IllegalArgumentException("Price is required");
    }
    float price = Float.parseFloat(priceStr);
    if (price < 0) {
      throw new IllegalArgumentException("Price cannot be negative");
    }
    return price;
  }

  private String validateCategory(String category) {
    if (category == null || category.trim().isEmpty()) {
      throw new IllegalArgumentException("Category is required");
    }
    return category.trim();
  }

  private String validateCondition(String condition) {
    if (condition == null || condition.trim().isEmpty()) {
      throw new IllegalArgumentException("Condition is required");
    }
    return condition.trim();
  }

  private String validateImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.trim().isEmpty()) {
      throw new IllegalArgumentException("Image URL is required");
    }
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
