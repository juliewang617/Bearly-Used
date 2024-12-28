package edu.brown.cs.student.main.server.handlers;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Default handler for the server. This class is responsible for handling requests that do not match
 * any specific endpoint. It provides a default message instructing the client on how to use the
 * available endpoints.
 */
public class DefaultHandler implements Route {

  /**
   * Handles requests that do not match a specific endpoint. This method returns a default message
   * instructing the client on how to use the server's available endpoints and the necessary
   * parameters for each endpoint.
   *
   * @param request The incoming HTTP request.
   * @param response The HTTP response object that will be sent back to the client.
   * @return A string message explaining the available endpoints and their usage.
   * @throws Exception If an error occurs during the handling of the request.
   */
  @Override
  public Object handle(Request request, Response response) {
    return ("Please enter with the necessary " + "parameters");
  }
}
