package tech.hiddenproject.hic.handler;

/**
 * Handler to catch http response with given status.
 *
 * @author Danila Rassokhin
 */
public interface HttpHandler {

  /**
   * Handles response for given http status.
   *
   * @param statusCode   HTTP status code to catch
   * @param responseBody Raw http response
   */
  void handler(Integer statusCode, String responseBody);

}
