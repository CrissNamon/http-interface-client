## HTTP interface client

### About

Simple library to perform http requests using interfaces with no implementations.

### Documentation

See [Wiki](https://github.com/CrissNamon/http-interface-client/wiki) for more documentation and examples.

### How to use

Create interface and methods for requests.
Annotate methods and their parameters with library annotations:

```java
public interface BookClient {

  @GET("/book")
  List<Book> getBooks(@Query("page") Integer page);

  @GET("/book/{id}")
  Book getBook(@Path("id") Integer id);

  @POST("/book")
  Book createBook(@Header("Content-Type") String contentType, @Body Book newBook);

  @PUT("/book/{id}")
  Book updateBook(@Path("id") Integer id, @Body Book book);

  @DELETE("/book/{id}")
  void deleteBook(@Path("id") Integer id);

  @GET("/book")
  CompletableFuture<List<Book>> getBooksAsync();

  @POST(value = "/book", contentType = RequestContent.FORM_ENCODED)
  Book createBookFromForm(@Field("title") String title);

  @POST(value = "/book", contentType = RequestContent.MULTIPART)
  Book createBookFromFile(@Part("file") MultipartData file);
}
```

Create ``WebClient`` instance from created interface and then just call interface methods:

```java
public class Main {

  public static void main(String... args) throws ExecutionException, InterruptedException {

    // Create WebClient
    BookClient bookClient = WebClient.of(BookClient.class)
        // Set base url
        .baseUrl("https://63c306edb0c286fbe5f7e9d4.mockapi.io/api/v1")
        // Set body decoder (optional, GSON by default)
        .decoder(new JSONBodyDecoder())
        // Set body encoder (optional, GSON by default) 
        .encoder(new JSONBodyEncoder())
        // Set WebClient exception handler (optional)
        .exceptionHandler(new DefaultExceptionHandler())
        // Add new header (optional)
        .header("Authorization", Main::authorizationBearerToken)
        // Set HttpRequest handler (optional)
        .httpHandler(Response::isError, new DefaultHttpErrorHandler())
        .create();

    // Get all books
    List<Book> allBooks = bookClient.getBooks(1);
    System.out.println(allBooks);

    // Get book with id = 3
    Book someBook = bookClient.getBook(3);
    System.out.println(someBook);

    //Create new book
    Book newBook = new Book("New book");
    System.out.println(bookClient.createBook("application/json", newBook));

    // Update book with id = 5
    System.out.println(bookClient.updateBook(5, newBook));

    // Get all books async
    bookClient.getBooksAsync()
        .whenComplete((books, throwable) -> System.out.println(books))
        .get();

    // Get wrapped response
    Response<List<Book>> allBooksWrapped = bookClient.getBooksWrapped();
    System.out.println("Is wrapped error: " + allBooksWrapped.isError());
    System.out.println("Raw unwrapped: " + allBooksWrapped.raw());
    System.out.println("Unwrapped: " + allBooksWrapped.get());

    // Create book with Content-Type: application/x-www-form-urlencoded
    bookClient.createBookFromForm("The peripheral");

    // Create book with Content-Type: multipart/form-data
    File file = new File("file.txt");
    MultipartData multipartFile = new MultipartFile(file.toPath());
    bookClient.createBookFromFile(multipartFile);
  }

  public static String authorizationBearerToken() {
    return "Bearer token";
  }

}
```
