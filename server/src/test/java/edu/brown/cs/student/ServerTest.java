package edu.brown.cs.student;

/** Tests the keyword and bounding box endpoints */
public class ServerTest {
  //  private final Type mapStringObject =
  //      Types.newParameterizedType(Map.class, String.class, Object.class);
  //
  //  private Map<String, Object> deserializeResponse(HttpURLConnection connection) throws
  // IOException {
  //    assertEquals(200, connection.getResponseCode());
  //    Moshi moshi = new Moshi.Builder().build();
  //    return (Map<String, Object>)
  //        moshi.adapter(mapStringObject).fromJson(new
  // Buffer().readFrom(connection.getInputStream()));
  //  }
  //
  //  /** Shared state for all tests. */
  //  @BeforeAll
  //  public static void setup_before_everything() {
  //    Spark.port(0);
  //    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  //  }
  //
  //  @BeforeEach
  //  public void setup() {}
  //
  //  @AfterEach
  //  public void teardown() {
  //    //    Spark.unmap("boundingbox");
  //    //    Spark.unmap("keyword");
  //    //    Spark.awaitStop(); // don't proceed until the server is stopped
  //  }
  //
  //  /**
  //   * Helper to start a connection to a specific API endpoint/params
  //   *
  //   * @param apiCall the call string, including endpoint
  //   * @return the connection for the given URL, just after connecting
  //   * @throws IOException if the connection fails for some reason
  //   */
  //  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
  //    // Configure the connection (but don't actually send the request yet)
  //    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
  //    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
  //
  //    clientConnection.setRequestMethod("GET");
  //
  //    clientConnection.connect();
  //    return clientConnection;
  //  }
}
