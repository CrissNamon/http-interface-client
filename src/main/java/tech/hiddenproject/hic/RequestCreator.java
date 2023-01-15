package tech.hiddenproject.hic;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.StringSubstitutor;
import tech.hiddenproject.hic.data.RequestMethod;
import tech.hiddenproject.hic.exception.HttpClientException;

/**
 * @author Danila Rassokhin
 */
public class RequestCreator {

  /**
   * Creates {@link HttpRequest}.
   *
   * @param baseUrl          Base url
   * @param path             Resource path
   * @param requestMethod    {@link RequestMethod}
   * @param queryParameters  {@link tech.hiddenproject.hic.annotation.Query} parameters
   * @param pathParameters   {@link tech.hiddenproject.hic.annotation.Path} parameters
   * @param headerParameters {@link tech.hiddenproject.hic.annotation.Header} parameters
   * @param body             Encoded request body
   * @return {@link HttpRequest}
   */
  public static HttpRequest create(String baseUrl, String path,
                                   RequestMethod requestMethod,
                                   Map<String, String> queryParameters,
                                   Map<String, String> pathParameters,
                                   Map<String, String> headerParameters,
                                   String body) {
    StringSubstitutor stringSubstitutor = new StringSubstitutor(pathParameters, "{", "}");
    String url = stringSubstitutor.replace(path);
    String encodedQuery = queryParameters.size() == 0 ? "" : "?" + urlEncodeUTF8(queryParameters);
    HttpRequest.Builder httpRequestBuilder = baseRequest(
        baseUrl + url + encodedQuery, headerParameters);
    BodyPublisher bodyPublisher =
        body.length() == 0 ? BodyPublishers.noBody() : BodyPublishers.ofString(body);
    switch (requestMethod) {
      case GET:
        httpRequestBuilder = httpRequestBuilder.GET();
        break;
      case POST:
        httpRequestBuilder = httpRequestBuilder.POST(bodyPublisher);
        break;
      case PUT:
        httpRequestBuilder = httpRequestBuilder.PUT(bodyPublisher);
        break;
      case DELETE:
        httpRequestBuilder = httpRequestBuilder.DELETE();
        break;
    }
    return httpRequestBuilder.build();
  }

  private static HttpRequest.Builder baseRequest(String url, Map<String, String> headers) {
    try {
      String[] headersArray = headers.entrySet().stream()
          .flatMap(header -> Stream.of(header.getKey(), header.getValue()))
          .collect(Collectors.toList())
          .toArray(new String[]{});
      return HttpRequest.newBuilder()
          .uri(new URI(url))
          .version(HttpClient.Version.HTTP_2)
          .headers(headersArray);
    } catch (Exception e) {
      throw new HttpClientException(e);
    }
  }

  private static String urlEncodeUTF8(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  private static String urlEncodeUTF8(Map<?, ?> map) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (sb.length() > 0) {
        sb.append("&");
      }
      sb.append(String.format("%s=%s", urlEncodeUTF8(entry.getKey().toString()),
                              urlEncodeUTF8(entry.getValue().toString())
      ));
    }
    return sb.toString();
  }
}
