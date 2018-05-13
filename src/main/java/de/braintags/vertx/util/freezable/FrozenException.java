package de.braintags.vertx.util.freezable;

public class FrozenException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FrozenException(final String message) {
    super(message);
  }

}
