package tech.hiddenproject.hic.decoder.impl;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import tech.hiddenproject.hic.decoder.BodyDecoder;

/**
 * Default {@link BodyDecoder} uses Google GSON library.
 *
 * @author Danila Rassokhin
 */
public class JSONBodyDecoder implements BodyDecoder {

  private static final Gson GSON = new Gson();

  public static String encode(Object entity) {
    return GSON.toJson(entity);
  }

  @Override
  public <T> T decode(String rawBody, Type targetClass) {
    return GSON.fromJson(rawBody, targetClass);
  }
}
