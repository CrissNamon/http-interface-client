package tech.hiddenproject.hic.data;

/**
 * @author Danila Rassokhin
 */
public interface MultipartData {

  byte[] serialize();

  String getName();

  String getType();

}
