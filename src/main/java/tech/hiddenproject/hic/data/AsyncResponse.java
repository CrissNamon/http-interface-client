package tech.hiddenproject.hic.data;

import java.util.concurrent.CompletableFuture;

/**
 * Represents response for async http request.
 *
 * @author Danila Rassokhin
 */
public class AsyncResponse<T> implements HttpResponse<CompletableFuture<T>> {

  private final CompletableFuture<T> body;

  public AsyncResponse(CompletableFuture<T> body) {
    this.body = body;
  }

  public static <T> AsyncResponse<T> create(CompletableFuture<T> body) {
    return new AsyncResponse<>(body);
  }

  @Override
  public CompletableFuture<T> get() {
    return body;
  }
}
