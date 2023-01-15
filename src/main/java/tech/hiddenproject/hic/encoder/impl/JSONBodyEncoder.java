package tech.hiddenproject.hic.encoder.impl;

import com.google.gson.Gson;
import tech.hiddenproject.hic.encoder.BodyEncoder;

/**
 * Default {@link BodyEncoder} uses Google GSON library.
 *
 * @author Danila Rassokhin
 */
public class JSONBodyEncoder implements BodyEncoder {

  private final Gson gson = new Gson();

  @Override
  public String encode(Object obj) {
    return gson.toJson(obj);
  }
}
