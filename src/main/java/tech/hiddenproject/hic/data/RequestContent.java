package tech.hiddenproject.hic.data;

/**
 * @author Danila Rassokhin
 */
public enum RequestContent {

  APPLICATION_JSON("application/json"),
  TEXT_PLAIN("text/plain"),
  MULTIPART("multipart/form-data"),
  FORM_ENCODED("application/x-www-form-urlencoded");


  final String contentType;

  RequestContent(String contentType) {
    this.contentType = contentType;
  }

  public String getContentType() {
    return contentType;
  }
}
