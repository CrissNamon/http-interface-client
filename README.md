## HTTP interface client

### About

Simple library to perform http requests using interfaces with no implementations.

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
  Book createBook(@Body Book newBook);

  @PUT("/book/{id}")
  Book updateBook(@Path("id") Integer id, @Body Book book);

  @DELETE("/book/{id}")
  void deleteBook(@Path("id") Integer id);

  @GET("/book")
  AsyncResponse<List<Book>> getBooksAsync();
}
```

Create ``WebClient`` instance from created interface:

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
        // Set WebClient exception handler
        .exceptionHandler(new DefaultExceptionHandler())
        // Add new header
        .header("Authorization", Main::authorizationBearerToken)
        // Set HttpRequest handler
        .httpHandler(Response::isError, new DefaultHttpErrorHandler())
        .create();
  }

  public static String authorizationBearerToken() {
    return "Bearer token";
  }

}
```

Then just call methods from interface:

```java
// Get all books
List<Book> allBooks = bookClient.getBooks(1);
System.out.println(allBooks);

// Get book with id = 3
Book someBook = bookClient.getBook(3);
System.out.println(someBook);

//Create new book
Book newBook = new Book("New book");
System.out.println(bookClient.createBook(newBook));

// Update book with id = 5
System.out.println(bookClient.updateBook(5, newBook));

// Get all books async
bookClient.getBooksAsync().get()
    .whenComplete((books, throwable) -> System.out.println(books))
    .get();

// Get wrapped response
Response<List<Book>> allBooksWrapped = bookClient.getBooksWrapped();
System.out.println("Is wrapped error: " + allBooksWrapped.isError());
System.out.println("Raw unwrapped: " + allBooksWrapped.raw());
System.out.println("Unwrapped: " + allBooksWrapped.get());

// Call wrong resource
bookClient.nosuchmethod();
```
