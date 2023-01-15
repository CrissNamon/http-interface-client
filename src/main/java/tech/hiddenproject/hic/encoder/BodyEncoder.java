package tech.hiddenproject.hic.encoder;

/**
 * @author Danila Rassokhin
 */
public interface BodyEncoder {

  /**
   * Encodes Java object into string representation.
   *
   * @param obj Object to encode
   * @return String representation
   */
  String encode(Object obj);

}
