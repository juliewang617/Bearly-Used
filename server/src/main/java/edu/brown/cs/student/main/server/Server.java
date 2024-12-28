package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import edu.brown.cs.student.main.server.handlers.AddListingHandler;
import edu.brown.cs.student.main.server.handlers.AddUserHandler;
import edu.brown.cs.student.main.server.handlers.DefaultHandler;
import edu.brown.cs.student.main.server.handlers.DeleteListingHandler;
import edu.brown.cs.student.main.server.handlers.GetListingsByIdHandler;
import edu.brown.cs.student.main.server.handlers.GetListingsHandler;
import edu.brown.cs.student.main.server.handlers.GetUserHandler;
import edu.brown.cs.student.main.server.handlers.GetUserListingsHandler;
import edu.brown.cs.student.main.server.handlers.UpdateListingHandler;
import edu.brown.cs.student.main.server.handlers.UpdateUserHandler;
import edu.brown.cs.student.main.server.storage.RealStorage;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import spark.Filter;
import spark.Spark;

/** Top Level class for our project, utilizes spark to create and maintain our server. */
public class Server {

  /** Sets up server endpoints */
  public static void setUpServer() {
    int port = 3232;
    Spark.port(port);

    after(
        (Filter)
            (request, response) -> {
              response.header("Access-Control-Allow-Origin", "*");
              response.header("Access-Control-Allow-Methods", "*");
              response.header("Access-Control-Allow-Headers", "*");
            });

    StorageInterface dbHandler;

    try {
      dbHandler = new RealStorage();
      Spark.get("add-user", new AddUserHandler(dbHandler));
      Spark.get("update-user", new UpdateUserHandler(dbHandler));
      Spark.get("add-listing", new AddListingHandler(dbHandler));
      Spark.get("update-listing", new UpdateListingHandler(dbHandler));
      Spark.get("delete-listing", new DeleteListingHandler(dbHandler));
      Spark.get("get-user", new GetUserHandler(dbHandler));
      Spark.get("get-user-listings", new GetUserListingsHandler(dbHandler));
      Spark.get("get-listings", new GetListingsHandler(dbHandler));
      Spark.get("get-listing-by-id", new GetListingsByIdHandler(dbHandler));
      Spark.get("*", new DefaultHandler());

      Spark.notFound(
          (request, response) -> {
            response.status(404); // Not Found
            System.out.println("ERROR");
            return "404 Not Found - The requested endpoint does not exist.";
          });
      Spark.init();
      Spark.awaitInitialization();

    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Error starting server");
      System.exit(1);
    }
  }

  /**
   * Runs Server.
   *
   * @param args none
   */
  public static void main(String[] args) {
    setUpServer();
  }
}
