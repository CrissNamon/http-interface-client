package tech.hiddenproject.hic.data.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import tech.hiddenproject.hic.data.MultipartData;
import tech.hiddenproject.hic.exception.HttpClientException;

/**
 * @author Danila Rassokhin
 */
public class MultipartFile implements MultipartData {

  private final Path file;

  public MultipartFile(Path file) {
    this.file = file;
  }

  @Override
  public byte[] serialize() {
    try {
      return Files.readAllBytes(file);
    } catch (IOException e) {
      throw new HttpClientException(e);
    }
  }

  @Override
  public String getName() {
    return file.getFileName().toString();
  }

  @Override
  public String getType() {
    try {
      return Files.probeContentType(file);
    } catch (IOException e) {
      throw new HttpClientException(e);
    }
  }
}
