package tech.hiddenproject.hic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import tech.hiddenproject.hic.decoder.BodyDecoder;
import tech.hiddenproject.hic.decoder.impl.JSONBodyDecoder;
import tech.hiddenproject.hic.encoder.BodyEncoder;
import tech.hiddenproject.hic.encoder.impl.JSONBodyEncoder;
import tech.hiddenproject.hic.handler.ClientExceptionHandler;
import tech.hiddenproject.hic.handler.HttpHandler;
import tech.hiddenproject.hic.handler.impl.DefaultExceptionHandler;

/**
 * @author Danila Rassokhin
 */
public class WebClient<T> {

  private final Class<T> clientClass;

  private final Map<String, Supplier<String>> headers = new HashMap<>();

  private final Map<Predicate<Integer>, HttpHandler> httpHandler = new HashMap<>();

  private Supplier<String> baseUrl;

  private BodyDecoder defaultDecoder = new JSONBodyDecoder();

  private BodyEncoder defaultEncoder = new JSONBodyEncoder();

  private ClientExceptionHandler clientExceptionHandler = new DefaultExceptionHandler();

  private HttpClient httpClient = HttpClient.newBuilder().build();

  private WebClient(Class<T> clientClass) {
    this.clientClass = clientClass;
  }

  /**
   * Creates new webclient from given class.
   *
   * @param clientClass Class to create webclient from
   * @param <T>         Type of client
   * @return WebClient builder
   */
  public static <T> WebClient<T> of(Class<T> clientClass) {
    return new WebClient<>(clientClass);
  }

  /**
   * Sets base url.
   *
   * @param baseUrl Base url
   * @return WebClient builder
   */
  public WebClient<T> baseUrl(String baseUrl) {
    this.baseUrl = () -> baseUrl;
    return this;
  }

  /**
   * Sets base url.
   *
   * @param baseUrl Base url supplier, to get url dynamically before request.
   * @return WebClient builder
   */
  public WebClient<T> baseUrl(Supplier<String> baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  /**
   * Sets response body decoder.
   *
   * @param bodyDecoder {@link BodyDecoder}
   * @return WebClient builder
   */
  public WebClient<T> decoder(BodyDecoder bodyDecoder) {
    this.defaultDecoder = bodyDecoder;
    return this;
  }

  /**
   * Sets request body encoder.
   *
   * @param bodyEncoder {@link BodyEncoder}
   * @return WebClient builder
   */
  public WebClient<T> encoder(BodyEncoder bodyEncoder) {
    this.defaultEncoder = bodyEncoder;
    return this;
  }

  /**
   * Sets {@link ClientExceptionHandler}
   *
   * @param clientExceptionHandler {@link ClientExceptionHandler}
   * @return WebClient builder
   */
  public WebClient<T> exceptionHandler(ClientExceptionHandler clientExceptionHandler) {
    this.clientExceptionHandler = clientExceptionHandler;
    return this;
  }

  /**
   * Adds new header for all requests.
   *
   * @param name  Header name
   * @param value Header value
   * @return WebClient builder
   */
  public WebClient<T> header(String name, String value) {
    this.headers.put(name, () -> value);
    return this;
  }

  /**
   * Adds new header supplier for all requests, to get header value dynamically before request.
   *
   * @param name  Header name
   * @param value Header value
   * @return WebClient builder
   */
  public WebClient<T> header(String name, Supplier<String> value) {
    this.headers.put(name, value);
    return this;
  }

  /**
   * Sets {@link HttpHandler} for given status code predicate
   *
   * @param statusCodePredicate Predicate to decide if response must be intercepted
   * @param responseHandler     Handler to intercept response
   * @return WebClient builder
   */
  public WebClient<T> httpHandler(Predicate<Integer> statusCodePredicate,
                                  HttpHandler responseHandler) {
    this.httpHandler.put(statusCodePredicate, responseHandler);
    return this;
  }

  /**
   * Sets {@link HttpClient} to send requests.
   *
   * @param httpClient {@link HttpClient}
   * @return WebClient builder
   */
  public WebClient<T> httpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
    return this;
  }

  /**
   * @return WebClient instance
   */
  @SuppressWarnings("unchecked")
  public T create() {
    return (T) Proxy.newProxyInstance(clientClass.getClassLoader(), new Class[]{clientClass},
                                      getDefaultHandler()
    );
  }

  private InvocationHandler getDefaultHandler() {
    return new RequestInvocationInterceptor(baseUrl, defaultDecoder, defaultEncoder,
                                            clientExceptionHandler, headers, httpHandler, httpClient
    );
  }
}
