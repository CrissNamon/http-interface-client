package tech.hiddenproject.hic;

import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.hiddenproject.hic.annotation.Body;
import tech.hiddenproject.hic.annotation.DELETE;
import tech.hiddenproject.hic.annotation.Field;
import tech.hiddenproject.hic.annotation.GET;
import tech.hiddenproject.hic.annotation.Header;
import tech.hiddenproject.hic.annotation.POST;
import tech.hiddenproject.hic.annotation.PUT;
import tech.hiddenproject.hic.annotation.Part;
import tech.hiddenproject.hic.annotation.Path;
import tech.hiddenproject.hic.annotation.Query;
import tech.hiddenproject.hic.data.MultipartData;
import tech.hiddenproject.hic.data.RequestContent;
import tech.hiddenproject.hic.data.RequestMethod;
import tech.hiddenproject.hic.data.impl.Response;
import tech.hiddenproject.hic.decoder.BodyDecoder;
import tech.hiddenproject.hic.encoder.BodyEncoder;
import tech.hiddenproject.hic.exception.HttpClientException;
import tech.hiddenproject.hic.handler.ClientExceptionHandler;
import tech.hiddenproject.hic.handler.HttpHandler;
import tech.hiddenproject.hic.util.BooleanOptional;
import tech.hiddenproject.hic.util.IfTrueConditional;
import tech.hiddenproject.hic.util.ObjectUtils;

/**
 * Intercepts calls to {@link WebClient} interfaces.
 *
 * @author Danila Rassokhin
 */
public class RequestInvocationInterceptor implements InvocationHandler {

  private static final Logger log = LoggerFactory.getLogger(RequestInvocationInterceptor.class);

  private final HttpClient httpClient;

  private final Supplier<String> baseUrl;

  private final BodyDecoder defaultDecoder;

  private final BodyEncoder defaultEncoder;

  private final ClientExceptionHandler clientExceptionHandler;

  private final Map<String, Supplier<String>> headers;

  private final Map<Predicate<Integer>, HttpHandler> httpHandler;

  public RequestInvocationInterceptor(Supplier<String> baseUrl, BodyDecoder bodyDecoder,
                                      BodyEncoder bodyEncoder,
                                      ClientExceptionHandler clientExceptionHandler,
                                      Map<String, Supplier<String>> headers,
                                      Map<Predicate<Integer>, HttpHandler> httpHandler,
                                      HttpClient httpClient) {
    this.baseUrl = baseUrl;
    this.httpClient = httpClient;
    this.defaultDecoder = bodyDecoder;
    this.defaultEncoder = bodyEncoder;
    this.clientExceptionHandler = clientExceptionHandler;
    this.headers = headers;
    this.httpHandler = httpHandler;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    Map<String, String> queryUrlParameters = collectParameters(
        method, Query.class, args, Query::value);
    Map<String, String> headers = collectParameters(method, Header.class, args, Header::value);
    Map<String, String> currentHeaderSuppliers = this.headers.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, header -> header.getValue().get()));
    headers.putAll(currentHeaderSuppliers);
    Map<String, String> pathUrlParameters = collectParameters(
        method, Path.class, args, Path::value);
    Map<String, String> bodyParameters = collectParameters(method, Body.class, args, body -> "",
                                                           defaultEncoder::encode
    );
    Map<String, MultipartData> multipartParameters = collectParameters(method, Part.class, args,
                                                                       Part::value, this::cast
    );
    Map<String, String> formDataParameters = collectParameters(method, Field.class, args,
                                                               Field::value
    );

    GET get = AnnotationProcessor.extractMethodAnnotation(method, GET.class);
    POST post = AnnotationProcessor.extractMethodAnnotation(method, POST.class);
    PUT put = AnnotationProcessor.extractMethodAnnotation(method, PUT.class);
    DELETE delete = AnnotationProcessor.extractMethodAnnotation(method, DELETE.class);

    BooleanOptional.of(ObjectUtils.isMoreThanNull(get, post, put, delete))
        .ifTrueThrow(() -> new HttpClientException(
            "More than one @GET, @POST, @PUT or @DELETE annotations found"));
    String pathUrl = IfTrueConditional.create()
        .ifTrue(get, Objects::nonNull).then(() -> get.value())
        .ifTrue(post, Objects::nonNull).then(() -> post.value())
        .ifTrue(put, Objects::nonNull).then(() -> put.value())
        .ifTrue(delete, Objects::nonNull).then(() -> delete.value())
        .orElse("");
    RequestMethod requestMethod = IfTrueConditional.create()
        .ifTrue(get, Objects::nonNull).then(RequestMethod.GET)
        .ifTrue(post, Objects::nonNull).then(RequestMethod.POST)
        .ifTrue(put, Objects::nonNull).then(RequestMethod.PUT)
        .ifTrue(delete, Objects::nonNull).then(RequestMethod.DELETE)
        .orElseThrows(() -> new HttpClientException(
            "No @GET, @POST, @PUT or @DELETE annotations found on called method"));
    RequestContent contentType = IfTrueConditional.create()
        .ifTrue(get, Objects::nonNull).then(() -> get.contentType())
        .ifTrue(post, Objects::nonNull).then(() -> post.contentType())
        .ifTrue(put, Objects::nonNull).then(() -> put.contentType())
        .ifTrue(delete, Objects::nonNull).then(() -> delete.contentType())
        .orElse(RequestContent.APPLICATION_JSON);

    HttpRequest httpRequest = RequestCreator.create(baseUrl.get(), pathUrl, requestMethod,
                                                    contentType, queryUrlParameters,
                                                    pathUrlParameters, headers, multipartParameters,
                                                    formDataParameters,
                                                    bodyParameters.values().stream().findFirst()
                                                        .orElse("")
    );
    log.debug("Sending {} request for: {}", requestMethod, httpRequest.uri().toString());
    return IfTrueConditional.create()
        .ifTrue(method, this::isAsyncRequest).then(() -> sendRequestAsync(httpRequest, method))
        .orElseGet(() -> sendRequest(httpRequest, method));
  }

  private Object sendRequest(HttpRequest httpRequest, Method method) {
    try {
      HttpResponse<String> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
      httpHandler.entrySet().stream()
          .filter(handler -> handler.getKey().test(httpResponse.statusCode()))
          .forEach(handler -> handler.getValue()
              .handler(httpResponse.statusCode(), httpResponse.body()));
      log.info(httpResponse.body());
      return IfTrueConditional.create()
          .ifTrue(method, this::hasResult).then(() -> wrapResponse(httpResponse, method))
          .orElse(null);
    } catch (JsonSyntaxException | IOException | InterruptedException e) {
      clientExceptionHandler.handle(e);
    }
    return null;
  }

  private CompletableFuture sendRequestAsync(HttpRequest httpRequest, Method method) {
    Type genericType = ((ParameterizedType) method.getGenericReturnType())
        .getActualTypeArguments()[0];
    return httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenApply(body -> defaultDecoder.decode(body, genericType));
  }

  private boolean isAsyncRequest(Method method) {
    return method.getReturnType().equals(CompletableFuture.class);
  }

  private boolean hasResult(Method method) {
    return !method.getReturnType().equals(void.class);
  }

  private boolean isWrapped(Method method) {
    return method.getReturnType().equals(Response.class);
  }

  private Object wrapResponse(HttpResponse<String> httpResponse, Method method) {
    if (isWrapped(method)) {
      Type genericType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
      Object response = Response.isError(httpResponse.statusCode()) ? null
          : defaultDecoder.decode(httpResponse.body(), genericType);
      return Response.create(response, httpResponse.statusCode(), httpResponse.body());
    }
    return Response.isError(httpResponse.statusCode()) ? null
        : defaultDecoder.decode(httpResponse.body(), method.getReturnType());
  }

  private <T extends Annotation, V> Map<String, V> collectParameters(Method method,
                                                                     Class<T> annotationClass,
                                                                     Object[] args,
                                                                     Function<T, String> keyExtractor,
                                                                     Function<Object, V> valueExtractor) {
    Map<T, Object> parameters = AnnotationProcessor.extractMethodParametersWithAnnotation(
        method, annotationClass, args);
    return parameters.entrySet().stream()
        .collect(Collectors.toMap(
            entry -> keyExtractor.apply(entry.getKey()),
            entry -> valueExtractor.apply(entry.getValue())
        ));
  }

  private <T extends Annotation> Map<String, String> collectParameters(Method method,
                                                                       Class<T> annotationClass,
                                                                       Object[] args,
                                                                       Function<T, String> keyExtractor) {
    return collectParameters(method, annotationClass, args, keyExtractor, Object::toString);
  }

  private <T> T cast(Object obj) {
    return (T) obj;
  }
}
