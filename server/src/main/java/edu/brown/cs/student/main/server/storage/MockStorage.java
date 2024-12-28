package edu.brown.cs.student.main.server.storage;

import edu.brown.cs.student.main.server.classes.Listing;
import edu.brown.cs.student.main.server.classes.User;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Mocked storage */
public class MockStorage implements StorageInterface {
  private final Map<String, User> userStorage = new HashMap<>();
  private final Map<Long, Listing> listingStorage = new HashMap<>();
  private Long nextListingId = 1L;

  @Override
  public Long createUser(
      String clerkId, String email, String name, String phoneNumber, String school) {
    User user = new User(clerkId, name, phoneNumber, school);
    userStorage.put(clerkId, user);
    return (long) userStorage.size();
  }

  @Override
  public boolean updateUser(String clerkId, User updatedUser) {
    if (userStorage.containsKey(clerkId)) {
      User user = userStorage.get(clerkId);
      if (updatedUser.getName() != null) user.setName(updatedUser.getName());
      if (updatedUser.getPhoneNumber() != null) user.setPhoneNumber(updatedUser.getPhoneNumber());
      if (updatedUser.getSchool() != null) user.setSchool(updatedUser.getSchool());
      return true;
    }
    return false;
  }

  @Override
  public List<Listing> getListings(
      String title,
      String category,
      Float minPrice,
      Float maxPrice,
      List<String> tags,
      Sorter sorter) {
    return listingStorage.values().stream()
        .filter(
            listing ->
                (title == null || listing.getTitle().toLowerCase().contains(title.toLowerCase()))
                    && (category == null || category.equals(listing.getCategory()))
                    && (minPrice == null || listing.getPrice() >= minPrice)
                    && (maxPrice == null || listing.getPrice() <= maxPrice)
                    && (tags == null
                        || tags.isEmpty()
                        || tags.stream().anyMatch(tag -> listing.getTags().contains(tag))))
        .sorted(
            (a, b) -> {
              if (sorter == null) return 0;
              switch (sorter) {
                case PRICE_ASC:
                  return Float.compare(a.getPrice(), b.getPrice());
                case PRICE_DESC:
                  return Float.compare(b.getPrice(), a.getPrice());
                default:
                  return 0;
              }
            })
        .collect(Collectors.toList());
  }

  @Override
  public Long createListing(
      String sellerId,
      String title,
      boolean isAvailable,
      String description,
      float price,
      String category,
      String condition,
      String imageUrl,
      List<String> tags) {
    List<String> tagList = tags != null ? tags : Collections.emptyList();
    Listing listing =
        new Listing(
            nextListingId++,
            sellerId,
            title,
            description,
            price,
            category,
            condition,
            imageUrl,
            tagList,
            isAvailable);
    this.listingStorage.put(listing.getId(), listing);
    return listing.getId();
  }

  @Override
  public Optional<Listing> getListingById(Long listingId) {
    return Optional.ofNullable(listingStorage.get(listingId));
  }

  @Override
  public Listing obtainListing(Long listingId) {
    return this.listingStorage.get(listingId);
  }

  @Override
  public boolean updateListing(Long listingId, Listing updatedListing) {
    if (listingStorage.containsKey(listingId)) {
      Listing listing = listingStorage.get(listingId);
      if (updatedListing.getTitle() != null) listing.setTitle(updatedListing.getTitle());
      if (updatedListing.getDescription() != null)
        listing.setDescription(updatedListing.getDescription());
      if (updatedListing.getPrice() != null) listing.setPrice(updatedListing.getPrice());
      if (updatedListing.getCategory() != null) listing.setCategory(updatedListing.getCategory());
      if (updatedListing.getCondition() != null)
        listing.setCondition(updatedListing.getCondition());
      if (updatedListing.getImageUrl() != null) listing.setImageUrl(updatedListing.getImageUrl());
      if (updatedListing.getTags() != null) listing.setTags(updatedListing.getTags());
      listing.setAvailable(updatedListing.getAvailable());
      return true;
    }
    return false;
  }

  @Override
  public boolean deleteListing(Long listingId) {
    if (listingStorage.remove(listingId) != null) {
      return true;
    }
    return false;
  }

  @Override
  public Map<String, Object> getUser(String clerkId) throws Exception {
    User user = userStorage.get(clerkId);
    if (user != null) {
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("clerkId", user.getClerkId());
      userMap.put("name", user.getName());
      userMap.put("phoneNumber", user.getPhoneNumber());
      userMap.put("school", user.getSchool());
      return userMap;
    }
    return Collections.emptyMap();
  }

  @Override
  public List<Map<String, Object>> getListingsBySellerId(String sellerId) throws Exception {
    return listingStorage.values().stream()
        .filter(listing -> sellerId.equals(listing.getSellerId()))
        .map(
            listing -> {
              Map<String, Object> listingMap = new HashMap<>();
              listingMap.put("id", listing.getId());
              listingMap.put("title", listing.getTitle());
              listingMap.put("description", listing.getDescription());
              listingMap.put("price", listing.getPrice());
              listingMap.put("available", listing.getAvailable());
              listingMap.put("category", listing.getCategory());
              listingMap.put("condition", listing.getCondition());
              listingMap.put("imageUrl", listing.getImageUrl());
              listingMap.put("tags", listing.getTags());
              return listingMap;
            })
        .collect(Collectors.toList());
  }

  public boolean contains(String title) {
    return listingStorage.values().stream().anyMatch(listing -> listing.getTitle().equals(title));
  }
}
