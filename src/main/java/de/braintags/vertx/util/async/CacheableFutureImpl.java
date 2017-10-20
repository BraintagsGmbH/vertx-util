package de.braintags.vertx.util.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * Result of an Service invocation that can be cached.
 * This results specifies how long it can be cached via the expires field.
 * 
 * @author mpluecker
 *
 * @param <T>
 */
public class CacheableFutureImpl<T> extends SharedFuture<T> implements CacheableFuture<T> {

  private long expires = CacheableFuture.INFINITE;

  public CacheableFutureImpl() {
    super();
  }

  public CacheableFutureImpl(final long expires, final T result) {
    super(result);
    this.expires = expires;
  }

  public CacheableFutureImpl(final Throwable cause) {
    super(cause);
    this.expires = CacheableResult.EXPIRED;
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
  public void complete(final long expires, final T result) {
    reduceExpire(expires);
    if (!tryComplete(expires, result))
      throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
  }

  public synchronized boolean tryComplete(final long expires, final T result) {
    if (isComplete())
      return false;
    reduceExpire(expires);
    this.result = result;
    succeeded = true;
    callHandlers();
    return true;
  }

  public boolean tryComplete(final long expires) {
    return tryComplete(expires, null);
  }

  @Override
  public void complete() {
    reduceExpire(CacheableFuture.EXPIRED);
    super.complete();
  }

  @Override
  public synchronized boolean tryComplete(final T result) {
    reduceExpire(CacheableFuture.EXPIRED);
    return super.tryComplete(result);
  }

  @Override
  public Handler<CacheableResult<T>> cacheHandler() {
    return res -> {
      if (res.succeeded()) {
        complete(res.expires(), res.result());
      } else {
        super.handle(res);
      }
    };
  }

  @Override
  public void handle(final AsyncResult<T> asyncResult) {
    if (asyncResult.succeeded() && asyncResult instanceof CacheableResult) {
      complete(((CacheableResult<?>) asyncResult).expires(), asyncResult.result());
    } else {
      super.handle(asyncResult);
    }
  }


}
