package de.braintags.vertx.util.async;

public class CacheableResultImpl<T> implements CacheableResult<T> {

  private final T result;
  private final long expires;
  private final Throwable cause;

  public CacheableResultImpl(final T result, final long expires) {
    this.result = result;
    this.expires = expires;
    this.cause = null;
  }

  public CacheableResultImpl(final Throwable cause) {
    this.result = null;
    this.expires = CacheableResult.EXPIRED;
    this.cause = cause;
  }

  @Override
  public long expires() {
    return expires;
  }

  @Override
  public T result() {
    return result;
  }

  @Override
  public Throwable cause() {
    return cause;
  }

  @Override
  public boolean succeeded() {
    return cause == null;
  }

  @Override
  public boolean failed() {
    return cause != null;
  }

}
