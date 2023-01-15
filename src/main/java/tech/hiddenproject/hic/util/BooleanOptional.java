package tech.hiddenproject.hic.util;

import java.util.function.Supplier;

/**
 * @author Danila Rassokhin
 */
public class BooleanOptional {

  private final boolean value;

  public BooleanOptional(boolean value) {
    this.value = value;
  }

  public static BooleanOptional of(boolean value) {
    return new BooleanOptional(value);
  }

  public <X extends Throwable> void ifTrueThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (value) {
      throw exceptionSupplier.get();
    }
  }

  public <T> T choose(T t, T f) {
    if (value) {
      return t;
    }
    return f;
  }
}
