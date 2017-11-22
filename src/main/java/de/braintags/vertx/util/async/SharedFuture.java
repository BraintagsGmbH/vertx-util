package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * Future of an asynchronous process whose result can be cached.
 * This results specifies how long it can be cached via the expires field.
 *
 * @author mpluecker
 *
 * @param <T>
 */
public interface SharedFuture<T> extends Future<T> {

  public static <T> SharedFuture<T> toCacheable(final Future<T> future) {
    if (future instanceof SharedFuture) {
      return (SharedFuture<T>) future;
    } else {
      SharedFuture<T> res = SharedFuture.future();
      future.setHandler(res);
      return res;
    }
  }

  public static <T> SharedFuture<T> future() {
    return new SharedFutureImpl<>();
  }

  public static <T> SharedFuture<T> succeededFuture(final T result) {
    return new SharedFutureImpl<>(result);
  }

  @SuppressWarnings("unchecked")
  public static <T> SharedFuture<T> succeededFuture() {
    return (SharedFuture<T>) new SharedFutureImpl<>((Void) null);
  }

  public static <T> SharedFuture<T> failedFuture(final Throwable cause) {
    return new SharedFutureImpl<>(cause);
  }

  @Override
  <U> SharedFuture<U> compose(final Function<T, Future<U>> mapper);

  @Override
  <U> SharedFuture<U> map(final Function<T, U> mapper);

  @Override
  <V> SharedFuture<V> map(final V value);

  @Override
  SharedFuture<T> recover(final Function<Throwable, Future<T>> mapper);

  @Override
  SharedFuture<T> otherwise(Function<Throwable, T> mapper);

  @Override
  <V> SharedFuture<V> mapEmpty();

  public static <T> SharedFuture<T> wrap(final Future<T> future) {
    SharedFuture<T> f = new SharedFutureImpl<>();
    future.setHandler(f);
    return f;
  }

  @Override
  SharedFuture<T> setHandler(Handler<AsyncResult<T>> handler);

  SharedFuture<T> addHandler(Handler<AsyncResult<T>> asyncAssertSuccess);

}
