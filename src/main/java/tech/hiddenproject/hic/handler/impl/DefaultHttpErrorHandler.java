package tech.hiddenproject.hic.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.hiddenproject.hic.handler.HttpHandler;

/**
 * Default {@link HttpHandler}
 *
 * @author Danila Rassokhin
 */
public class DefaultHttpErrorHandler implements HttpHandler {

  private static final Logger log = LoggerFactory.getLogger(DefaultHttpErrorHandler.class);

  @Override
  public void handler(Integer statusCode, String responseBody) {
    log.error("Error occurred during request: {}", statusCode);
  }
}
