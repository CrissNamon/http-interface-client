package tech.hiddenproject.hic.exception;

/**
 * @author Danila Rassokhin
 */
public class HttpClientException extends RuntimeException {

  public HttpClientException(String message) {
    super(message);
  }

  public HttpClientException(Throwable cause) {
    super(cause);
  }
}
