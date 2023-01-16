package tech.hiddenproject.hic.data.impl;

import java.lang.reflect.Type;
import tech.hiddenproject.hic.data.HttpResponse;
import tech.hiddenproject.hic.decoder.BodyDecoder;

/**
 * Represents response for http request.
 *
 * @author Danila Rassokhin
 */
public class Response<T> implements HttpResponse<T> {

  private final T body;

  private final String rawBody;

  private final Integer statusCode;

  public Response(T body, Integer statusCode, String rawBody) {
    this.body = body;
    this.statusCode = statusCode;
    this.rawBody = rawBody;
  }

  public static <T> Response<T> create(T body, Integer statusCode, String rawBody) {
    return new Response<>(body, statusCode, rawBody);
  }

  public static boolean isError(Integer statusCode) {
    return statusCode >= 300;
  }

  public T get() {
    return body;
  }

  public String raw() {
    return rawBody;
  }

  public <E> E decodedRaw(Type bodyClass, BodyDecoder bodyDecoder) {
    return bodyDecoder.decode(raw(), bodyClass);
  }

  public boolean isError() {
    return statusCode >= 300;
  }

  public Integer status() {
    return statusCode;
  }

}
