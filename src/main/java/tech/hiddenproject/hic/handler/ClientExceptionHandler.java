package tech.hiddenproject.hic.handler;

/**
 * Handler to catch client exceptions.
 *
 * @author Danila Rassokhin
 */
public interface ClientExceptionHandler {

  /**
   * Handles client exception.
   *
   * @param throwable Exception to handle
   */
  void handle(Throwable throwable);

}
