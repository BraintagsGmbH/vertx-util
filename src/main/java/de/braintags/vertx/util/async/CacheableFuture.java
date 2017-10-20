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
public interface CacheableFuture<T> extends Future<T>, CacheableResult<T> {

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
  default <U> CacheableFuture<U> compose(final Function<T, Future<U>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    CacheableFuture<U> ret = CacheableFuture.future();
    setHandler(ar -> {
      ret.reduceExpire(this.expires());
      if (ar.succeeded()) {
        Future<U> apply;
        try {
          apply = mapper.apply(ar.result());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        apply.setHandler(ret);
      } else {
        ret.fail(ar.cause());
      }
    });
    return ret;
  }

  @Override
  default <U> CacheableFuture<U> map(final Function<T, U> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    CacheableFuture<U> ret = CacheableFuture.future();
    setHandler(ar -> {
      ret.reduceExpire(this.expires());
      if (ar.succeeded()) {
        U mapped;
        try {
          mapped = mapper.apply(ar.result());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        ret.complete(mapped);
      } else {
        ret.fail(ar.cause());
      }
    });
    return ret;
  }

  @Override
  default <V> CacheableFuture<V> map(final V value) {
    CacheableFuture<V> ret = CacheableFuture.future();
    setHandler(ar -> {
      ret.reduceExpire(this.expires());
      if (ar.succeeded()) {
        ret.complete(value);
      } else {
        ret.fail(ar.cause());
      }
    });
    return ret;
  }

  @Override
  default CacheableFuture<T> recover(final Function<Throwable, Future<T>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    CacheableFuture<T> ret = CacheableFuture.future();
    setHandler(ar -> {
      ret.reduceExpire(this.expires());
      if (ar.succeeded()) {
        ret.complete(result());
      } else {
        Future<T> mapped;
        try {
          mapped = mapper.apply(ar.cause());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        mapped.setHandler(ret);
      }
    });
    return ret;
  }

  @Override
  default <V> CacheableFuture<V> mapEmpty() {
    return map((V) null);
  }

}
