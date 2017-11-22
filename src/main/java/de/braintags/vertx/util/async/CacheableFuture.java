package de.braintags.vertx.util.async;

import java.util.function.Function;

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
public interface CacheableFuture<T> extends SharedFuture<T>, CacheableResult<T> {

  public static <T> CacheableFuture<T> toCacheable(final Future<T> future) {
    if (future instanceof CacheableFuture) {
      return (CacheableFuture<T>) future;
    } else {
      CacheableFuture<T> res = CacheableFuture.future();
      future.setHandler(res);
      return res;
    }
  }

  public static <T> CacheableFuture<T> future() {
    return new CacheableFutureImpl<>();
  }

  public static <T> CacheableFuture<T> succeededFuture(final long expires, final T result) {
    return new CacheableFutureImpl<>(expires, result);
  }

  public static <T> CacheableFuture<T> failedFuture(final Throwable cause) {
    return new CacheableFutureImpl<>(cause);
  }

  void complete(long expires, T result);

  void reduceExpire(long expires);

  Handler<CacheableResult<T>> cacheHandler();

  @Override
  <U> CacheableFuture<U> compose(final Function<T, Future<U>> mapper);

  @Override
  <U> CacheableFuture<U> map(final Function<T, U> mapper);

  @Override
  <V> CacheableFuture<V> map(final V value);

  @Override
  CacheableFuture<T> recover(final Function<Throwable, Future<T>> mapper);

  @Override
  <V> CacheableFuture<V> mapEmpty();

  public static <T> CacheableFuture<T> wrap(final long expires, final Future<T> future) {
    CacheableFuture<T> f = CacheableFuture.future();
    future.setHandler(res -> {
      if (res.succeeded())
        f.complete(expires, res.result());
      else
        f.fail(res.cause());
    });
    return f;
  }

}
