package tech.hiddenproject.hic.decoder;

import java.lang.reflect.Type;

/**
 * Decodes raw http response into Java object.
 *
 * @author Danila Rassokhin
 */
public interface BodyDecoder {

  /**
   * Decodes raw http response into Java object.
   *
   * @param rawBody     Http response
   * @param targetClass Target Java type
   * @param <T>         Decoded type
   * @return Decoded response
   */
  <T> T decode(String rawBody, Type targetClass);

}
