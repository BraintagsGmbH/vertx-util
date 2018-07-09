package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class SucceededMultiThreadedFuture<T> extends SucceededCacheableFuture<T> implements MultiThreadedFuture<T> {

  public SucceededMultiThreadedFuture(final long expires, final T result) {
    super(expires, result);
  }

  @Override
  public MultiThreadedFuture<T> addHandler(final Handler<AsyncResult<T>> handler) {
    handler.handle(this);
    return this;
  }

  @Override
  public <U> MultiThreadedFuture<U> compose(final Function<T, Future<U>> mapper) {
    try {
      return MultiThreadedFuture.wrap(expires(), mapper.apply(result));
    } catch (Throwable e) {
      return MultiThreadedFuture.failedFuture(e);
    }
  }

  @Override
  public <U> MultiThreadedFuture<U> map(final Function<T, U> mapper) {
    try {
      return MultiThreadedFuture.succeededFuture(expires(), mapper.apply(result));
    } catch (Throwable e) {
      return MultiThreadedFuture.failedFuture(e);
    }
  }

  @Override
  public <V> MultiThreadedFuture<V> map(final V value) {
    try {
      return MultiThreadedFuture.succeededFuture(expires(), value);
    } catch (Throwable e) {
      return MultiThreadedFuture.failedFuture(e);
    }
  }

  @Override
  public <V> MultiThreadedFuture<V> mapEmpty() {
    try {
      return MultiThreadedFuture.succeededFuture(expires());
    } catch (Throwable e) {
      return MultiThreadedFuture.failedFuture(e);
    }
  }

  @Override
  public <V> MultiThreadedFuture<V> chain(final Function<Void, Future<V>> mapper) {
    try {
      return MultiThreadedFuture.wrap(expires(), mapper.apply(null));
    } catch (Throwable e) {
      return MultiThreadedFuture.failedFuture(e);
    }
  }

  @Override
  public <V> MultiThreadedFuture<V> then(final Function<AsyncResult<T>, Future<V>> mapper) {
    try {
      return MultiThreadedFuture.wrap(expires(), mapper.apply(this));
    } catch (Throwable e) {
      return MultiThreadedFuture.failedFuture(e);
    }
  }

  @Override
  public MultiThreadedFuture<T> recover(final Function<Throwable, Future<T>> mapper) {
    return this;
  }

  @Override
  public MultiThreadedFuture<T> otherwise(final Function<Throwable, T> mapper) {
    return this;
  }

  @Override
  public MultiThreadedFuture<T> otherwiseEmpty() {
    return this;
  }

}
