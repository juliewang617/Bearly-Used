package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.server.classes.Listing;
import edu.brown.cs.student.main.server.classes.User;
import edu.brown.cs.student.main.server.storage.MockStorage;
import edu.brown.cs.student.main.server.storage.Sorter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MockTests {
  private MockStorage storage;

  @BeforeEach
  void setUp() {
    storage = new MockStorage();
  }

  @Test
  void testCreateUser() throws Exception {
    Long userId = storage.createUser("clerk1", "user@example.com", "Bob", "1234567890", "Brown");
    assertEquals(1L, userId);

    Map<String, Object> user = storage.getUser("clerk1");
    assertEquals("Bob", user.get("name"));
    assertEquals("Brown", user.get("school"));
  }

  @Test
  void testUpdateUser() throws Exception {
    storage.createUser("clerk1", "user@example.com", "Bob", "1234567890", "Brown");

    User updatedUser = new User("clerk1", "John", null, "RISD");
    boolean updated = storage.updateUser("clerk1", updatedUser);

    assertTrue(updated);

    Map<String, Object> user = storage.getUser("clerk1");
    assertEquals("John", user.get("name"));
    assertEquals("RISD", user.get("school"));
  }

  @Test
  void testCreateListing() {
    Long listingId =
        storage.createListing(
            "clerk1",
            "Bike",
            true,
            "Sexy bike",
            100.0f,
            "Other",
            "New",
            "bike.jpg",
            List.of("outdoor", "transport"));

    assertEquals(1L, listingId);

    Optional<Listing> listing = storage.getListingById(listingId);
    assertTrue(listing.isPresent());
    assertEquals("Bike", listing.get().getTitle());
    assertEquals("Other", listing.get().getCategory());
  }

  @Test
  void testUpdateListing() {
    Long listingId =
        storage.createListing(
            "clerk1",
            "Bike",
            true,
            "Sexy bike",
            100.0f,
            "Other",
            "New",
            "bike.jpg",
            List.of("outdoor", "transport"));

    Listing updatedListing =
        new Listing(
            listingId,
            "clerk1",
            "Mountain Bike",
            "Sexier bike",
            150.0f,
            "Other",
            "New",
            "bike_updated.jpg",
            List.of("mountain", "outdoor"),
            true);
    boolean updated = storage.updateListing(listingId, updatedListing);

    assertTrue(updated);

    Optional<Listing> listing = storage.getListingById(listingId);
    assertTrue(listing.isPresent());
    assertEquals("Mountain Bike", listing.get().getTitle());
    assertEquals(150.0f, listing.get().getPrice());
  }

  @Test
  void testDeleteListing() {
    Long listingId =
        storage.createListing(
            "clerk1",
            "Bike",
            true,
            "Sexy bike",
            100.0f,
            "Other",
            "New",
            "bike.jpg",
            List.of("outdoor", "transport"));

    boolean deleted = storage.deleteListing(listingId);

    assertTrue(deleted);

    Optional<Listing> listing = storage.getListingById(listingId);
    assertFalse(listing.isPresent());
  }

  @Test
  void testGetListingsWithFilters() {
    storage.createListing(
        "clerk1",
        "Bike",
        true,
        "Sexy bike",
        100.0f,
        "Other",
        "New",
        "bike.jpg",
        List.of("outdoor"));
    storage.createListing(
        "clerk2",
        "Helmet",
        true,
        "Sexy helmet",
        50.0f,
        "Other",
        "New",
        "helmet.jpg",
        List.of("safety"));

    List<Listing> filteredListings = storage.getListings("Bike", null, null, null, null, null);
    assertEquals(1, filteredListings.size());
    assertEquals("Bike", filteredListings.get(0).getTitle());

    filteredListings = storage.getListings(null, "Other", 30.0f, 60.0f, null, null);
    assertEquals(1, filteredListings.size());
    assertEquals("Helmet", filteredListings.get(0).getTitle());

    filteredListings = storage.getListings(null, "Other", null, null, null, null);
    assertEquals(2, filteredListings.size());
    assertEquals("Bike", filteredListings.get(0).getTitle());
    assertEquals("Helmet", filteredListings.get(1).getTitle());
  }

  @Test
  void testGetListingsSorted() {
    storage.createListing(
        "clerk1",
        "Bike",
        true,
        "Sexy bike",
        100.0f,
        "Other",
        "New",
        "bike.jpg",
        List.of("outdoor"));
    storage.createListing(
        "clerk2",
        "Helmet",
        true,
        "Sexy helmet",
        50.0f,
        "Other",
        "New",
        "helmet.jpg",
        List.of("safety"));

    List<Listing> sortedListings =
        storage.getListings(null, null, null, null, null, Sorter.PRICE_ASC);
    assertEquals(2, sortedListings.size());
    assertEquals("Helmet", sortedListings.get(0).getTitle());

    sortedListings = storage.getListings(null, null, null, null, null, Sorter.PRICE_DESC);
    assertEquals(2, sortedListings.size());
    assertEquals("Bike", sortedListings.get(0).getTitle());
  }
}
