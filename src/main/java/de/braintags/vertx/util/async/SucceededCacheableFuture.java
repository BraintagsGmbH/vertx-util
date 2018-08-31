package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class SucceededCacheableFuture<T> extends SucceededSharedFuture<T> implements CacheableFuture<T> {

  private long expires;

  public SucceededCacheableFuture(final long expires, final T result) {
    super(result);
    this.expires = expires;
  }

  @Override
  public long expires() {
    return expires;
  }

  @Override
  public void reduceExpire(final long expires) {
    this.expires = Math.min(this.expires, expires);
  }

  @Override
  public Handler<CacheableResult<T>> cacheHandler() {
    return res -> handle(res);
  }

  @Override
  public <U> CacheableFuture<U> compose(final Function<T, Future<U>> mapper) {
    try {
      return CacheableFuture.wrap(expires(), mapper.apply(result));
    } catch (Throwable e) {
      return CacheableFuture.failedFuture(e);
    }
  }

  @Override
  public <U> CacheableFuture<U> map(final Function<T, U> mapper) {
    try {
      return CacheableFuture.succeededFuture(expires(), mapper.apply(result));
    } catch (Throwable e) {
      return CacheableFuture.failedFuture(e);
    }
  }

  @Override
  public <V> CacheableFuture<V> map(final V value) {
    try {
      return CacheableFuture.succeededFuture(expires(), value);
    } catch (Throwable e) {
      return CacheableFuture.failedFuture(e);
    }
  }

  @Override
  public <V> CacheableFuture<V> mapEmpty() {
    try {
      return CacheableFuture.succeededFuture(expires());
    } catch (Throwable e) {
      return CacheableFuture.failedFuture(e);
    }
  }

  @Override
  public <V> CacheableFuture<V> chain(final Function<Void, Future<V>> mapper) {
    try {
      return CacheableFuture.wrap(expires(), mapper.apply(null));
    } catch (Throwable e) {
      return CacheableFuture.failedFuture(e);
    }
  }

  @Override
  public <V> CacheableFuture<V> then(final Function<AsyncResult<T>, Future<V>> mapper) {
    try {
      return CacheableFuture.wrap(expires(), mapper.apply(this));
    } catch (Throwable e) {
      return CacheableFuture.failedFuture(e);
    }
  }

  @Override
  public void complete(final long expires, final T result) {
    // will throw exception
    complete(result);
  }

  @Override
  public CacheableFuture<T> recover(final Function<Throwable, Future<T>> mapper) {
    return this;
  }

  @Override
  public CacheableFuture<T> otherwise(final Function<Throwable, T> mapper) {
    return this;
  }

  @Override
  public CacheableFuture<T> otherwiseEmpty() {
    return this;
  }

  @Override
  public CacheableFuture<T> addCacheHandler(Handler<CacheableFuture<T>> handler) {
    handler.handle(this);
    return this;
  }

}
