package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.handlers.AddListingHandler;
import edu.brown.cs.student.main.server.handlers.AddUserHandler;
import edu.brown.cs.student.main.server.handlers.DeleteListingHandler;
import edu.brown.cs.student.main.server.handlers.GetListingsByIdHandler;
import edu.brown.cs.student.main.server.handlers.GetListingsHandler;
import edu.brown.cs.student.main.server.handlers.GetUserHandler;
import edu.brown.cs.student.main.server.handlers.GetUserListingsHandler;
import edu.brown.cs.student.main.server.handlers.UpdateListingHandler;
import edu.brown.cs.student.main.server.handlers.UpdateUserHandler;
import edu.brown.cs.student.main.server.storage.MockStorage;
import edu.brown.cs.student.main.server.storage.RealStorage;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class APITests {
  static StorageInterface dbHandler;
  private static MockStorage mockStorage;

  @BeforeAll
  public static void setupOnce() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
    dbHandler = new RealStorage();
  }

  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  @BeforeEach
  public void setup() {
    Spark.get("add-listing", new AddListingHandler(dbHandler));
    Spark.get("add-user", new AddUserHandler(dbHandler));
    Spark.get("delete-listing", new DeleteListingHandler(dbHandler));
    Spark.get("get-listing-by-id", new GetListingsByIdHandler(dbHandler));
    Spark.get("get-listings", new GetListingsHandler(dbHandler));
    Spark.get("get-user", new GetUserHandler(dbHandler));
    Spark.get("get-user-listings", new GetUserListingsHandler(dbHandler));
    Spark.get("update-listing", new UpdateListingHandler(dbHandler));
    Spark.get("update-user", new UpdateUserHandler(dbHandler));

    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("/add-listing");
    Spark.unmap("/add-user");
    Spark.unmap("/add-delete-listing");
    Spark.unmap("/get-listing-by-id");
    Spark.unmap("/get-listings");
    Spark.unmap("/get-user");
    Spark.unmap("/get-user-listings");
    Spark.unmap("/update-listing");
    Spark.unmap("/update-user");
    Spark.stop();
    Spark.awaitStop();
  }

  /* =========================================================== AddListingHandler tests ===========================================================*/
  // testing successful api call for AddListing endpoint
  @Test
  public void testAddListingSuccess() throws IOException {
    // sellerId, title, available, description, price, category, condition, imageUrl, tags
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=100&title=pencil&available=true&description=yellow+pencil&price=0.01&category=Other&condition=New&image_url=pencil.jpg&tags=fiction,thriller");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));
    assertEquals("100", responseBody.get("sellerId"));
    assertEquals("pencil", responseBody.get("title"));
    assertEquals(true, responseBody.get("available"));
    assertEquals("yellow pencil", responseBody.get("description"));
    assertEquals(0.01, responseBody.get("price"));
    assertEquals("Other", responseBody.get("category"));
    assertEquals("pencil.jpg", responseBody.get("imageUrl"));
    assertEquals(Arrays.asList("fiction", "thriller"), responseBody.get("tags"));
    assertEquals("New", responseBody.get("condition"));
  }

  // testing unsuccessful api call for AddListing endpoint bc params blank
  @Test
  public void testAddListingParamsEmpty() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=&title=&available=&description=descriptionnnnn&price=69.69&category=&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Seller ID is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc missing seller id
  @Test
  public void testAddListingMissingSellerId() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=&title=titleeeeee&available=&description=descriptionnnnn&price=s&category=&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Seller ID is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc missing title
  @Test
  public void testAddListingMissingTitle() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=6&title=&available=&description=descriptionnnnn&price=s&category=&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Title is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc missing description
  @Test
  public void testAddListingMissingDescription() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=6&title=adaa&available=&description=&price=s&category=&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Description is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc missing price
  @Test
  public void testAddListingMissingPrice() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=6&title=adaa&available=&description=assadsa&price=&category=&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Price is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc price negative
  @Test
  public void testAddListingNegativePrice() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=6&title=adaa&available=&description=assadsa&price=-4&category=&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Price cannot be negative", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc wrong price input
  @Test
  public void testAddListingWrongPrice() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=6&title=adaa&available=&description=assadsa&price=s&category=&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: For input string: \"s\"", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc missing category
  @Test
  public void testAddListingMissingCategory() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=6&title=adaa&available=&description=assadsa&price=5&category=&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Category is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc missing condition
  @Test
  public void testAddListingMissingCondition() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=6&title=adaa&available=&description=assadsa&price=5&category=Other&condition=&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Condition is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddListing endpoint bc missing image
  @Test
  public void testAddListingMissingImage() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-listing?seller_id=6&title=adaa&available=&description=assadsa&price=5&category=Other&condition=New&image_url=&tags=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Image URL is required", responseBody.get("error"));
  }

  /* =========================================================== AddUserHandler tests ===========================================================*/
  // testing successful api call for AddUser endpoint
  // might fail sometimes bc random number chosen could have been chosen already in previous run
  @Test
  public void testAddUserSuccess() throws IOException {
    Random random = new Random();
    int randomNumber = random.nextInt(200) + 0;
    String email = "BOBJOE" + randomNumber + "@risd.edu";

    HttpURLConnection loadConnection =
        tryRequest(
            "add-user?clerk_id=user_"
                + randomNumber
                + "&email="
                + email
                + "&name=bob&phone_number=1234567890&school=risd");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));
    assertEquals("user_" + randomNumber, responseBody.get("clerk_id"));
    assertEquals("BOBJOE" + randomNumber + "@risd.edu", responseBody.get("email"));
    assertEquals("bob", responseBody.get("name"));
    assertEquals("1234567890", responseBody.get("phone number"));
    assertEquals("risd", responseBody.get("school"));
  }

  // testing unsuccessful api call for AddUser endpoint bc params blank
  @Test
  public void testAddUserParamsEmpty() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("add-user?clerk_id=&email=&name=&phone_number=&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Email is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddUser endpoint bc missing email
  @Test
  public void testAddUserMissingEmail() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("add-user?clerk_id=&email=&name=joe&phone_number=56723&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Email is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddUser endpoint bc wrong email input type
  @Test
  public void testAddUserWrongEmail() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("add-user?clerk_id=&email=abc@gmail.com&name=joe&phone_number=56723&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals(
        "Invalid input: Email must end with @risd.edu or @brown.edu", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddUser endpoint bc missing phone number
  @Test
  public void testAddUserMissingPhoneNumber() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("add-user?clerk_id=&email=abc@risd.edu&name=joe&phone_number=&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Phone number is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddUser endpoint bc wrong phone number format
  @Test
  public void testAddUserWrongPhoneNumber() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("add-user?clerk_id=&email=abc@risd.edu&name=joe&phone_number=56723&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Invalid phone number format", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddUser endpoint bc missing name
  @Test
  public void testAddUserMissingName() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("add-user?clerk_id=&email=abc@risd.edu&name=&phone_number=56723&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Name is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddUser endpoint bc wrong name format
  @Test
  public void testAddUserWrongName() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("add-user?clerk_id=&email=abc@risd.edu&name=d&phone_number=56723&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals(
        "Invalid input: Name must be at least 2 characters long", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddUser endpoint bc missing school
  @Test
  public void testAddUserMissingSchool() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-user?clerk_id=&email=abc@risd.edu&name=ph&phone_number=(123)-456-7878&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: School is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for AddUser endpoint bc wrong school
  @Test
  public void testAddUserWrongSchool() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "add-user?clerk_id=&email=abc@risd.edu&name=ph&phone_number=(123)-456-7878&school=RISDDDDD");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: School must be Brown or RISD", responseBody.get("error"));
  }

  /* =========================================================== DeleteListingHandler tests ===========================================================*/
  // unable to test a successful api call of DeleteListingHandler because that requires control over
  // listing id, but supabase controls that

  // testing unsuccessful api call for DeleteListing endpoint bc params blank
  @Test
  public void testDeleteListingParamsEmpty() throws IOException {
    HttpURLConnection loadConnection = tryRequest("delete-listing?listing_id=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Listing ID is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for DeleteListing endpoint bc invalid param inputted
  @Test
  public void testDeleteListingParamsInvalid() throws IOException {
    HttpURLConnection loadConnection = tryRequest("delete-listing?listing_id=1");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Listing not found", responseBody.get("error"));
  }

  /* =========================================================== GetListingsByIdHandler tests ===========================================================*/
  // testing successful api call for GetListingsById endpoint
  // WILL FAIL IF LISTINGS CHANGED
  @Test
  public void testGetListingsByIdSuccess() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-listing-by-id?listing_id=160");

    // Expected data setup
    Map<String, Object> expectedData = new HashMap<>();
    expectedData.put("id", 160); // id as integer
    expectedData.put("seller_id", "user_2qIsp60kbn35x0c0636nJtYNCSX");
    expectedData.put("title", "Soda");
    expectedData.put("description", "sodaaaaa");
    expectedData.put("price", 20.0);
    expectedData.put("category", "Decor");
    expectedData.put("condition", "Fair");
    expectedData.put(
        "image_url",
        "https://qguaazfosybrxefngxta.supabase.co/storage/v1/object/public/images/1734396066987-can-soda.jpg");
    expectedData.put("tags", new String[] {"soda"});
    expectedData.put("available", true);
    assertEquals(200, loadConnection.getResponseCode());

    // Parse the response body
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));

    // Extract the listing from the response body
    Map<String, Object> actualListing = (Map<String, Object>) responseBody.get("listing");

    if (actualListing.get("id") instanceof Double) {
      actualListing.put("id", ((Double) actualListing.get("id")).intValue());
    }

    List<String> actualTagsList = (List<String>) actualListing.get("tags");
    String[] actualTags = actualTagsList.toArray(new String[0]);

    assertEquals(expectedData.get("id"), actualListing.get("id"));
    assertEquals(expectedData.get("seller_id"), actualListing.get("seller_id"));
    assertEquals(expectedData.get("title"), actualListing.get("title"));
    assertEquals(expectedData.get("description"), actualListing.get("description"));
    assertEquals(expectedData.get("price"), actualListing.get("price"));
    assertEquals(expectedData.get("category"), actualListing.get("category"));
    assertEquals(expectedData.get("condition"), actualListing.get("condition"));
    assertEquals(expectedData.get("image_url"), actualListing.get("image_url"));
    assertArrayEquals((String[]) expectedData.get("tags"), actualTags);
    assertEquals(expectedData.get("available"), actualListing.get("available"));
  }

  // testing unsuccessful api call for GetListingsById endpoint bc params blank
  @Test
  public void testGetListingsByIdParamsEmpty() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-listing-by-id?listing_id=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Listing ID is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for GetListingsById endpoint bc params blank
  @Test
  public void testGetListingsByIdWrongListingId() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-listing-by-id?listing_id=-4");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Listing not found", responseBody.get("error"));
  }

  /* =========================================================== GetListingsHandler tests ===========================================================*/
  // THESE TESTS CAN FAIL IF LISTINGS CHANGE AROUND
  // testing successful api call for GetListings endpoint
  @Test
  public void testGetListingsSuccess() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-listings?");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));
    Double number = (Double) responseBody.get("number of listings obtained");
    assertTrue(number > 0);
  }

  // testing successful api call for GetListings endpoint, searching by title
  @Test
  public void testGetListingsTitleSuccess() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-listings?title=lamp");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));
    assertEquals(1.0, responseBody.get("number of listings obtained"));
  }

  // testing successful api call for GetListings endpoint, searching by category
  @Test
  public void testGetListingsCategorySuccess() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-listings?category=Decor");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));
    assertEquals(1.0, responseBody.get("number of listings obtained"));
  }

  // testing unsuccessful api call for GetListings endpoint bc min price negative
  @Test
  public void testGetListingsNegativeMinPrice() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-listings?minPrice=-1");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Price cannot be negative", responseBody.get("error"));
  }

  // testing unsuccessful api call for GetListings endpoint bc max price negative
  @Test
  public void testGetListingsNegativeMaxPrice() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-listings?maxPrice=-1");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Price cannot be negative", responseBody.get("error"));
  }

  /* =========================================================== GetUserHandler tests ===========================================================*/
  // testing successful api call for GetUser endpoint
  @Test
  public void testGetUserSuccess() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("get-user?clerk_id=user_2qIsp60kbn35x0c0636nJtYNCSX");

    assertEquals(200, loadConnection.getResponseCode());

    // Parse the response body
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;

    assertEquals("success", responseBody.get("response_type"));

    // Extract the 'user_data' object from the response
    Map<String, Object> userData = (Map<String, Object>) responseBody.get("user_data");

    assertEquals("user_2qIsp60kbn35x0c0636nJtYNCSX", userData.get("clerk_id"));
    assertEquals("Brown", userData.get("school"));
    assertEquals("Sarah Liao", userData.get("name"));
    assertEquals("111-111-1111", userData.get("phone_number"));
    assertEquals(20.0, userData.get("id")); // Assuming 'id' is an integer
    assertEquals("sarah_liao@brown.edu", userData.get("email"));

    assertEquals(0, ((List<?>) userData.get("tags")).size());
  }

  // testing unsuccessful api call for GetUser endpoint bc missing clerk id
  @Test
  public void testGetUserMissingClerkId() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-user?clerk_id=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Clerk ID is required", responseBody.get("error"));
  }

  /* =========================================================== GetUserListingsHandler tests ===========================================================*/
  // testing successful api call for GetUserListings endpoint
  @Test
  public void testGetUserListingsSuccess() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("get-user-listings?seller_id=user_2qIsp60kbn35x0c0636nJtYNCSX");

    assertEquals(200, loadConnection.getResponseCode());

    // Parse the response body
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;

    assertEquals("success", responseBody.get("response_type"));

    // Extract the 'listings' field
    List<Map<String, Object>> listings = (List<Map<String, Object>>) responseBody.get("listings");

    Map<String, Object> listing = listings.get(0);

    assertEquals(177.0, listing.get("id"));
    assertEquals("lamp", listing.get("title"));
    assertEquals("New", listing.get("condition"));
    assertEquals(20.0, listing.get("price"));
    assertEquals("its a lamp!", listing.get("description"));
    assertEquals("Furniture", listing.get("category"));
    assertEquals(true, listing.get("available"));
    assertEquals(
        "https://qguaazfosybrxefngxta.supabase.co/storage/v1/object/public/images/1734396290137-lamp.webp",
        listing.get("image_url"));

    // Check that 'tags' is an array with the expected value
    List<String> tags = (List<String>) listing.get("tags");
    assertEquals(4, tags.size());
    assertEquals("from amazon", tags.get(0));
    assertEquals("lamp", tags.get(1));
    assertEquals("unopened", tags.get(2));
    assertEquals("unused", tags.get(3));
  }

  // testing unsuccessful api call for GetUserListings endpoint bc missing seller id
  @Test
  public void testGetUserMissingSellerId() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-user-listings?seller_id=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Seller ID is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for GetUserListings endpoint bc wrong seller id
  @Test
  public void testGetUserWrongSellerId() throws IOException {
    HttpURLConnection loadConnection = tryRequest("get-user-listings?seller_id=66");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("No listings found for the given seller ID", responseBody.get("error"));
  }

  /* =========================================================== UpdateListingHandler tests ===========================================================*/
  // testing successful api call for UpdateListing endpoint
  @Test
  public void testUpdateListingSuccess() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest(
            "update-listing?listing_id=177&title=Updated+Lamp&price=20.00&tags=from+amazon,lamp,unopened,unused&image_url=https://qguaazfosybrxefngxta.supabase.co/storage/v1/object/public/images/1734396290137-lamp.webp");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));
    assertEquals("Listing updated successfully", responseBody.get("message"));
    assertEquals("Updated Lamp", responseBody.get("title"));
    assertEquals(
        "https://qguaazfosybrxefngxta.supabase.co/storage/v1/object/public/images/1734396290137-lamp.webp",
        responseBody.get("image_url"));
    assertEquals("from amazon,lamp,unopened,unused", responseBody.get("tags"));
  }

  @Test
  public void testResetListingSuccess() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("update-listing?listing_id=177&title=lamp" + "&available=true");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));
    assertEquals("Listing updated successfully", responseBody.get("message"));
    assertEquals("lamp", responseBody.get("title"));
  }

  // testing unsuccessful api call for UpdateListing endpoint bc missing listing id
  @Test
  public void testUpdateListingMissingListingId() throws IOException {
    HttpURLConnection loadConnection = tryRequest("update-listing?listing_id=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Listing ID is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for UpdateListing endpoint bc wrong listing id
  @Test
  public void testUpdateListingWrongListingId() throws IOException {
    HttpURLConnection loadConnection = tryRequest("update-listing?listing_id=-9");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Listing could not be updated", responseBody.get("error"));
  }

  // testing unsuccessful api call for UpdateListing endpoint bc negative price
  @Test
  public void testUpdateListingNegativePrice() throws IOException {
    HttpURLConnection loadConnection = tryRequest("update-listing?listing_id=160&price=-6");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Price cannot be negative", responseBody.get("error"));
  }

  /* =========================================================== UpdateUserHandler tests ===========================================================*/
  // testing successful api call for UpdateUser endpoint
  // might fail sometimes bc random number chosen could have been chosen already in previous run
  @Test
  public void testUpdateUserSuccess() throws IOException {
    Random random = new Random();
    int randomNumber = random.nextInt(20) + 0;
    String name = "bob" + randomNumber;

    HttpURLConnection loadConnection =
        tryRequest(
            "update-user?clerk_id=12345&name=" + name + "&phone_number=1234443333&school=brown");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("success", responseBody.get("response_type"));
    assertEquals("User updated successfully", responseBody.get("message"));
    assertEquals("12345", responseBody.get("clerk_id"));
    assertEquals(name, responseBody.get("name"));
    assertEquals("1234443333", responseBody.get("phone_number"));
    assertEquals("brown", responseBody.get("school"));
  }

  // testing unsuccessful api call for UpdateUser endpoint bc all params blank
  @Test
  public void testUpdateUserParamsEmpty() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("update-user?clerk_id=&name=&phone_number=&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Name is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for UpdateUser endpoint bc wrong name input
  @Test
  public void testUpdateUserWrongName() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("update-user?clerk_id=&name=a&phone_number=&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals(
        "Invalid input: Name must be at least 2 characters long", responseBody.get("error"));
  }

  // testing unsuccessful api call for UpdateUser endpoint bc missing phone number
  @Test
  public void testUpdateUserMissingPhoneNumber() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("update-user?clerk_id=&namebob=&phone_number=&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: Phone number is required", responseBody.get("error"));
  }

  // testing unsuccessful api call for UpdateUser endpoint bc missing school
  @Test
  public void testUpdateUserMissingSchool() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("update-user?clerk_id=&namebob=&phone_number=111-111-2222&school=");

    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assert responseBody != null;
    assertEquals("failure", responseBody.get("response_type"));
    assertEquals("Invalid input: School is required", responseBody.get("error"));
  }
}
