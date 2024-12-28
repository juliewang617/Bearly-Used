package edu.brown.cs.student.main.server.storage;

import edu.brown.cs.student.main.server.classes.Listing;
import edu.brown.cs.student.main.server.classes.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StorageInterface {

  Long createUser(String clerkId, String email, String name, String phoneNumber, String school)
      throws IllegalArgumentException, SQLException;

  // all-purpose to get either all listings or filtered listings
  List<Listing> getListings(
      String title,
      String category,
      Float minPrice,
      Float maxPrice,
      List<String> tags,
      Sorter sorter);

  Long createListing(
      String sellerId,
      String title,
      boolean isAvailable,
      String description,
      float price,
      String category,
      String condition,
      String imageUrl,
      List<String> tags)
      throws IllegalArgumentException, SQLException;

  Optional<Listing> getListingById(Long listingId);

  Listing obtainListing(Long listingId);

  boolean updateListing(Long listingId, Listing updatedListing);

  boolean deleteListing(Long listingId);

  Map<String, Object> getUser(String clerkId) throws Exception;

  boolean updateUser(String userId, User updatedUser);

  List<Map<String, Object>> getListingsBySellerId(String sellerId) throws Exception;
}
