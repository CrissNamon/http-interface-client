package tech.hiddenproject.hic.example;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import tech.hiddenproject.hic.annotation.Body;
import tech.hiddenproject.hic.annotation.DELETE;
import tech.hiddenproject.hic.annotation.Field;
import tech.hiddenproject.hic.annotation.GET;
import tech.hiddenproject.hic.annotation.POST;
import tech.hiddenproject.hic.annotation.PUT;
import tech.hiddenproject.hic.annotation.Part;
import tech.hiddenproject.hic.annotation.Path;
import tech.hiddenproject.hic.annotation.Query;
import tech.hiddenproject.hic.data.MultipartData;
import tech.hiddenproject.hic.data.RequestContent;
import tech.hiddenproject.hic.data.impl.Response;

/**
 * @author Danila Rassokhin
 */
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
  CompletableFuture<List<Book>> getBooksAsync();

  @GET("/book/error")
  Response<List<Book>> getBooksWrapped();

  @POST(value = "/book", contentType = RequestContent.FORM_ENCODED)
  Book createBookFromForm(@Field("title") String title);

  @POST(value = "/book", contentType = RequestContent.MULTIPART)
  Book createBookFromFile(@Part("file") MultipartData file);

}
