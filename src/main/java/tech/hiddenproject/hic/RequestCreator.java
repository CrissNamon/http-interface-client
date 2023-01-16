package tech.hiddenproject.hic;

import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.StringSubstitutor;
import tech.hiddenproject.hic.data.MultipartData;
import tech.hiddenproject.hic.data.RequestContent;
import tech.hiddenproject.hic.data.RequestMethod;
import tech.hiddenproject.hic.exception.HttpClientException;
import tech.hiddenproject.hic.util.IfTrueConditional;

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
                                   RequestContent contentType,
                                   Map<String, String> queryParameters,
                                   Map<String, String> pathParameters,
                                   Map<String, String> headerParameters,
                                   Map<String, MultipartData> multipartData,
                                   Map<String, String> formData,
                                   String body) {
    StringSubstitutor stringSubstitutor = new StringSubstitutor(pathParameters, "{", "}");
    String url = stringSubstitutor.replace(path);
    String encodedQuery = queryParameters.size() == 0 ? "" : "?" + urlEncodeUTF8(queryParameters);
    headerParameters.put("Content-Type", contentType.getContentType());
    HttpRequest.Builder baseRequest = baseRequest(
        baseUrl + url + encodedQuery, headerParameters);
    BodyPublisher bodyPublisher = IfTrueConditional.create()
        .ifTrue(contentType, RequestContent.MULTIPART::equals)
        .then(() -> onMultipartData(multipartData))
        .ifTrue(contentType, RequestContent.FORM_ENCODED::equals)
        .then(() -> onFormEncoded(formData))
        .ifTrue(() -> body.length() == 0).then(BodyPublishers.noBody())
        .orElseGet(() -> BodyPublishers.ofString(body));
    HttpRequest.Builder requestBuilder = IfTrueConditional.create()
        .ifTrue(requestMethod, RequestMethod.GET::equals).then(() -> baseRequest.GET())
        .ifTrue(requestMethod, RequestMethod.POST::equals)
        .then(() -> baseRequest.POST(bodyPublisher))
        .ifTrue(requestMethod, RequestMethod.PUT::equals).then(() -> baseRequest.PUT(bodyPublisher))
        .ifTrue(requestMethod, RequestMethod.DELETE::equals).then(() -> baseRequest.DELETE())
        .orElseThrows(() -> new HttpClientException("Request method is undefined"));
    return requestBuilder.build();
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

  private static BodyPublisher onMultipartData(Map<String, MultipartData> data) {
    BigInteger boundary = new BigInteger(35, new Random());
    List<byte[]> bytes = data.entrySet().stream()
        .map(entry -> encodeMultipart(boundary, entry.getKey(), entry.getValue()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    bytes.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
    bytes.forEach(arr -> System.out.println(new String(arr)));
    return BodyPublishers.ofByteArrays(bytes);
  }

  private static List<byte[]> encodeMultipart(BigInteger boundary, String name,
                                              MultipartData multipartData) {
    return List.of(
        ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
            .getBytes(StandardCharsets.UTF_8),
        ("\"" + name + "\"; filename=\"" + multipartData.getName() +
            "\"\r\nContent-Type: " + multipartData.getType() + "\r\n\r\n")
            .getBytes(StandardCharsets.UTF_8),
        multipartData.serialize(),
        "\r\n".getBytes(StandardCharsets.UTF_8)
    );
  }

  private static BodyPublisher onFormEncoded(Map<?, ?> formData) {
    return BodyPublishers.ofString(urlEncodeUTF8(formData));
  }
}
