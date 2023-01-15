package tech.hiddenproject.hic.data;

/**
 * @author Danila Rassokhin
 */
public interface HttpResponse<T> {

  /**
   * @return Decoded response body
   */
  T get();

}
