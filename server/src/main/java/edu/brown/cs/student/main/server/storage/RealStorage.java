package edu.brown.cs.student.main.server.storage;

import edu.brown.cs.student.main.server.classes.Listing;
import edu.brown.cs.student.main.server.classes.User;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** A handler for the Postgres database */
public class RealStorage implements StorageInterface {
  private String JDBC;

  public RealStorage() {
    Dotenv dotenv = Dotenv.load();
    this.JDBC = dotenv.get("JDBC");
  }

  // Validate email
  private void validateEmail(String email) {
    if (email == null || !Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
      throw new IllegalArgumentException("Invalid email format");
    }
  }

  // Validate price
  private void validatePrice(float price) {
    if (price < 0) {
      throw new IllegalArgumentException("Price cannot be negative");
    }
  }

  /* USER FUNCTIONS */

  // Creates a user
  @Override
  public Long createUser(
      String clerkId, String email, String name, String phoneNumber, String school)
      throws SQLException {
    validateEmail(email);

    // SQL parameterization
    String sql =
        "INSERT INTO users (email, name, phone_number, school, clerk_id) VALUES (?, ?, ?, ?, ?) RETURNING id";

    try {
      Connection connection = DriverManager.getConnection(this.JDBC);
      PreparedStatement statement = connection.prepareStatement(sql);

      statement.setString(1, email);
      statement.setString(2, name);
      statement.setString(3, phoneNumber);
      statement.setString(4, school);
      statement.setString(5, clerkId);

      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          Long userId = result.getLong(1);
          System.out.println("User created successfully with clerk ID: " + clerkId);
          return userId;
        } else {
          throw new SQLException("No ID obtained for created user");
        }
      }
    } catch (SQLException e) {
      System.err.println("Error creating user: " + e.getMessage());
      throw e;
    }
  }

  @Override
  public boolean updateUser(String clerkId, User updatedUser) {
    // Construct SQL update query
    StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET ");
    boolean hasUpdates = false;

    try (Connection connection = DriverManager.getConnection(this.JDBC)) {
      // Create a list to hold parameters
      List<Object> params = new ArrayList<>();

      // Add fields to update
      if (updatedUser.getName() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("name = ?");
        params.add(updatedUser.getName());
        hasUpdates = true;
      }

      if (updatedUser.getPhoneNumber() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("phone_number = ?");
        params.add(updatedUser.getPhoneNumber());
        hasUpdates = true;
      }

      if (updatedUser.getSchool() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("school = ?");
        params.add(updatedUser.getSchool());
        hasUpdates = true;
      }

      sqlBuilder.append(" WHERE clerk_id = ?");
      params.add(clerkId);

      // debugging
      System.out.println("SQL Query: " + sqlBuilder);
      System.out.println("Parameters: " + params);

      try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
        for (int i = 0; i < params.size(); i++) {
          statement.setObject(i + 1, params.get(i));
        }

        int rowsAffected = statement.executeUpdate();

        if (rowsAffected > 0) {
          System.out.println("User updated successfully: " + clerkId);
          return true;
        } else {
          System.err.println("No User found with ID: " + clerkId);
          return false;
        }
      }
    } catch (SQLException e) {
      System.err.println("Error updating user: " + e.getMessage());
      return false;
    }
  }

  /* LISTING FUNCTIONS */
  @Override
  public List<Listing> getListings(
      String title,
      String category,
      Float minPrice,
      Float maxPrice,
      List<String> tags,
      Sorter sorter) {

    try {
      List<Listing> listings = new ArrayList<>();

      // "Where 1=1" is the same as WHERE TRUE so it lets us append additional filters with AND
      StringBuilder sqlBuilder =
          new StringBuilder("SELECT * FROM listings WHERE 1=1 AND available = TRUE");
      List<Object> params = new ArrayList<>();

      // Apply filters dynamically
      if (title != null && !title.trim().isEmpty()) {
        sqlBuilder.append(" AND LOWER(title) LIKE ?");
        params.add("%" + title.trim().toLowerCase() + "%");
      }

      if (category != null) {
        sqlBuilder.append(" AND category = ?");
        params.add(category);
      }

      if (minPrice != null) {
        sqlBuilder.append(" AND price >= ?");
        params.add(minPrice);
      }

      if (maxPrice != null) {
        sqlBuilder.append(" AND price <= ?");
        params.add(maxPrice);
      }

      // check tags
      if (tags != null && !tags.isEmpty()) {
        sqlBuilder.append(" AND (");
        for (int i = 0; i < tags.size(); i++) {
          if (i > 0) {
            sqlBuilder.append(" OR ");
          }
          sqlBuilder.append("? = ANY(tags)");
          params.add(tags.get(i));
        }
        sqlBuilder.append(")");
      }

      if (sorter != null) {
        sqlBuilder.append(" ORDER BY ");
        switch (sorter) {
          case PRICE_ASC:
            sqlBuilder.append("price ASC");
            break;
          case PRICE_DESC:
            sqlBuilder.append("price DESC");
            break;
          default:
            throw new IllegalArgumentException("Unsupported sorter");
        }
      }

      try (Connection connection = DriverManager.getConnection(this.JDBC);
          PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {

        System.out.println("SQL QUERY: " + sqlBuilder.toString());

        // Set query parameters
        for (int i = 0; i < params.size(); i++) {
          statement.setObject(i + 1, params.get(i));
        }

        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            // Map ResultSet to Listing object
            Listing listing =
                new Listing(
                    resultSet.getLong("id"),
                    resultSet.getString("seller_id"),
                    resultSet.getString("title"),
                    resultSet.getString("description"),
                    resultSet.getFloat("price"),
                    resultSet.getString("category"),
                    resultSet.getString("condition"),
                    resultSet.getString("image_url"),
                    Arrays.asList((String[]) resultSet.getArray("tags").getArray()),
                    resultSet.getBoolean("available"));
            listings.add(listing);
          }
        }

      } catch (SQLException e) {
        throw new RuntimeException("Error fetching listings: " + e.getMessage());
      }

      return listings;
    } catch (Exception e) {
      throw new RuntimeException("unknown error: " + e.getMessage());
    }
  }

  public Long createListing(
      String sellerId, // this is their clerk id
      String title,
      boolean isAvailable,
      String description,
      float price,
      String category,
      String condition,
      String imageUrl,
      List<String> tags)
      throws IllegalArgumentException, SQLException {

    validatePrice(price);

    // SQL parameterization
    String sql =
        "INSERT INTO listings "
            + "(seller_id, title, available, description, price, category, condition, image_url, tags) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    System.out.println(sql);

    try {
      Connection connection = DriverManager.getConnection(this.JDBC);
      PreparedStatement statement = connection.prepareStatement(sql);

      statement.setString(1, sellerId);
      statement.setString(2, title);
      statement.setBoolean(3, isAvailable);
      statement.setString(4, description);
      statement.setFloat(5, price);
      statement.setString(6, category);
      statement.setString(7, condition);
      statement.setString(8, imageUrl);

      if (tags != null) {
        System.out.println(
            "Tags being passed to createArrayOf: " + Arrays.toString(tags.toArray()));
        statement.setArray(9, connection.createArrayOf("text", tags.toArray()));
      }

      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          Long listingId = result.getLong(1);
          System.out.println("Listing created successfully with ID: " + listingId);
          return listingId;
        } else {
          System.err.println("Failed to retrieve created listing ID");
          throw new SQLException("No ID obtained for created listing");
        }
      }
    } catch (SQLException e) {
      System.err.println("Error connection to SQL Database: " + e.getMessage());
      throw e;
    }
  }

  @Override
  public Map<String, Object> getUser(String clerkId) throws Exception {
    Map<String, Object> userData = new HashMap<>();
    String sql = "SELECT * FROM users WHERE clerk_id = ?";

    try (Connection connection = DriverManager.getConnection(this.JDBC);
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, clerkId);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          userData.put("id", rs.getLong("id"));
          userData.put("clerk_id", rs.getString("clerk_id"));
          userData.put("email", rs.getString("email"));
          userData.put("name", rs.getString("name"));
          userData.put("phone_number", rs.getString("phone_number"));
          userData.put("school", rs.getString("school"));
          userData.put("tags", cleanTags(rs.getString("interests")));
        }
      }
    }
    return userData;
  }

  @Override
  public List<Map<String, Object>> getListingsBySellerId(String sellerId) throws Exception {
    List<Map<String, Object>> listings = new ArrayList<>();
    String sql = "SELECT * FROM listings WHERE seller_id = ?";

    try (Connection connection = DriverManager.getConnection(this.JDBC);
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, sellerId);

      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> listing = new HashMap<>();
          listing.put("id", rs.getLong("id"));
          listing.put("title", rs.getString("title"));
          listing.put("description", rs.getString("description"));
          listing.put("price", rs.getDouble("price"));
          listing.put("available", rs.getBoolean("available"));
          listing.put("category", rs.getString("category"));
          listing.put("condition", rs.getString("condition"));
          listing.put("image_url", rs.getString("image_url"));

          String tagsJson = rs.getString("tags");
          listing.put("tags", cleanTags(tagsJson));
          listings.add(listing);
        }
      }
    }

    return listings;
  }

  private List<String> cleanTags(String tagsJson) {
    if (tagsJson == null || tagsJson.trim().isEmpty()) {
      return new ArrayList<>();
    }

    String cleanedTags = tagsJson.replaceAll("[\\{\\}\\[\\]\\\"]", "").replace("\\", "");
    return Arrays.stream(cleanedTags.split(","))
        .map(String::trim)
        .filter(tag -> !tag.isEmpty())
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Listing> getListingById(Long listingId) {
    return Optional.empty();
  }

  @Override
  public Listing obtainListing(Long listingId) {
    Listing listing = null;
    String sql = "SELECT * FROM listings WHERE id = ?";

    try (Connection connection = DriverManager.getConnection(this.JDBC);
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setLong(1, listingId);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {

          // create Listing object from ResultSet
          listing =
              new Listing(
                  resultSet.getLong("id"),
                  resultSet.getString("seller_id"),
                  resultSet.getString("title"),
                  resultSet.getString("description"),
                  resultSet.getFloat("price"),
                  resultSet.getString("category"),
                  resultSet.getString("condition"),
                  resultSet.getString("image_url"),

                  // base64Image,
                  Arrays.asList((String[]) resultSet.getArray("tags").getArray()),
                  resultSet.getBoolean("available"));
        }
      }
      return listing;

    } catch (SQLException e) {
      System.err.println("Error obtaining listing: " + e.getMessage());
      return null;
    }
  }

  @Override
  public boolean updateListing(Long listingId, Listing updatedListing) {
    // Validate price if price is being updated
    if (updatedListing.getPrice() != null) {
      validatePrice(updatedListing.getPrice());
    }

    // Construct SQL update query
    StringBuilder sqlBuilder = new StringBuilder("UPDATE listings SET ");
    boolean hasUpdates = false;

    try (Connection connection = DriverManager.getConnection(this.JDBC)) {
      // Create a list to hold parameters
      List<Object> params = new ArrayList<>();

      // Add fields to update
      if (updatedListing.getTitle() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("title = ?");
        params.add(updatedListing.getTitle());
        hasUpdates = true;
      }

      if (updatedListing.getDescription() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("description = ?");
        params.add(updatedListing.getDescription());
        hasUpdates = true;
      }

      if (updatedListing.getPrice() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("price = ?");
        params.add(updatedListing.getPrice());
        hasUpdates = true;
      }

      if (updatedListing.getCategory() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("category = ?");
        params.add(updatedListing.getCategory());
        hasUpdates = true;
      }

      if (updatedListing.getCondition() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("condition = ?");
        params.add(updatedListing.getCondition());
        hasUpdates = true;
      }

      if (updatedListing.getImageUrl() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("image_url = ?");
        params.add(updatedListing.getImageUrl());
        hasUpdates = true;
      }

      if (updatedListing.getAvailable() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("available = ?");
        params.add(updatedListing.getAvailable());
        hasUpdates = true;
      }

      if (updatedListing.getTags() != null) {
        sqlBuilder.append(hasUpdates ? ", " : "").append("tags = ?");
        params.add(connection.createArrayOf("TEXT", updatedListing.getTags().toArray()));
        hasUpdates = true;
      }

      if (!hasUpdates) {
        System.err.println("No fields to update");
        return false;
      }

      sqlBuilder.append(" WHERE id = ?");
      params.add(listingId);

      try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
        for (int i = 0; i < params.size(); i++) {
          statement.setObject(i + 1, params.get(i));
        }

        int rowsAffected = statement.executeUpdate();

        if (rowsAffected > 0) {
          System.out.println("Listing updated successfully: " + listingId);
          return true;
        } else {
          System.err.println("No listing found with ID: " + listingId);
          return false;
        }
      }
    } catch (SQLException e) {
      System.err.println("Error updating listing: " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean deleteListing(Long listingId) {
    String sql = "DELETE FROM listings WHERE id = ?";

    try (Connection connection = DriverManager.getConnection(this.JDBC);
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setLong(1, listingId);

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected > 0) {
        System.out.println("Listing deleted successfully: " + listingId);
        return true;
      } else {
        System.err.println("No listing found with ID: " + listingId);
        return false;
      }
    } catch (SQLException e) {
      System.err.println("Error deleting listing: " + e.getMessage());
      return false;
    }
  }
}
