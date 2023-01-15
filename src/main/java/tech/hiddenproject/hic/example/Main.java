package tech.hiddenproject.hic.example;

import java.util.List;
import java.util.concurrent.ExecutionException;
import tech.hiddenproject.hic.WebClient;
import tech.hiddenproject.hic.data.Response;
import tech.hiddenproject.hic.decoder.impl.JSONBodyDecoder;
import tech.hiddenproject.hic.encoder.impl.JSONBodyEncoder;
import tech.hiddenproject.hic.handler.impl.DefaultExceptionHandler;
import tech.hiddenproject.hic.handler.impl.DefaultHttpErrorHandler;

/**
 * @author Danila Rassokhin
 */
public class Main {

  public static void main(String... args) throws ExecutionException, InterruptedException {

    BookClient bookClient = WebClient.of(BookClient.class)
        .baseUrl("https://63c306edb0c286fbe5f7e9d4.mockapi.io/api/v1")
        .decoder(new JSONBodyDecoder())
        .encoder(new JSONBodyEncoder())
        .exceptionHandler(new DefaultExceptionHandler())
        .header("Authorization", Main::authorizationBearerToken)
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
    System.out.println(bookClient.createBook(newBook));

    // Update book with id = 5
    System.out.println(bookClient.updateBook(5, newBook));

    // Get all books async
    bookClient.getBooksAsync().whenComplete((books, throwable) -> System.out.println(books))
        .get();

    // Get wrapped response
    Response<List<Book>> allBooksWrapped = bookClient.getBooksWrapped();
    System.out.println("Is wrapped error: " + allBooksWrapped.isError());
    System.out.println("Raw unwrapped: " + allBooksWrapped.raw());
    System.out.println("Unwrapped: " + allBooksWrapped.get());

    // Call wrong resource
    bookClient.nosuchmethod();
  }

  public static String authorizationBearerToken() {
    return "Bearer token";
  }

}
