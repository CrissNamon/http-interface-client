package tech.hiddenproject.hic.handler.impl;

import tech.hiddenproject.hic.exception.HttpClientException;
import tech.hiddenproject.hic.handler.ClientExceptionHandler;

/**
 * Default {@link ClientExceptionHandler}
 *
 * @author Danila Rassokhin
 */
public class DefaultExceptionHandler implements ClientExceptionHandler {

  @Override
  public void handle(Throwable throwable) {
    throw new HttpClientException(throwable);
  }
}
