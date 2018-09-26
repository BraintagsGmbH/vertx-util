/*-
 * #%L
 * Vert.x utilities from Braintags
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * Result of an Service invocation that can be cached.
 * This results specifies how long it can be cached via the expires field.
 *
 * @author mpluecker
 *
 * @param <T>
 */
public class CacheableFutureImpl<T> extends SharedFutureImpl<T> implements CacheableFuture<T> {

  private long expires = CacheableFuture.INFINITE;

  CacheableFutureImpl() {
    super();
  }

  CacheableFutureImpl(final Throwable cause) {
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

  protected void reduceExpireFromResult(final AsyncResult<?> res) {
    if (res.succeeded() && res instanceof CacheableResult) {
      reduceExpire(((CacheableResult<?>) res).expires());
    } else {
      reduceExpire(CacheableResult.EXPIRED);
    }
  }

  @Override
  public void complete(final long expires, final T result) {
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
    if (isComplete())
      return false;
    reduceExpire(CacheableFuture.EXPIRED);
    return super.tryComplete(result);
  }

  @Override
  public boolean tryFail(final Throwable cause) {
    if (isComplete())
      return false;
    reduceExpire(CacheableFuture.EXPIRED);
    return super.tryFail(cause);
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

  @Override
  public <U> CacheableFuture<U> compose(final Function<T, Future<U>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        try {
          CacheableFuture<U> res = CacheableFuture.toCacheable(mapper.apply(this.result()));
          res.reduceExpire(expires());
          return res;
        } catch (Throwable e) {
          return CacheableFuture.failedFuture(e);
        }
      } else {
        return (CacheableFuture<U>) this;
      }
    } else {
      return new ComposedCacheableFuture<>(this, mapper);
    }

  }

  @Override
  public <U> CacheableFuture<U> map(final Function<T, U> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        try {
          return CacheableFuture.succeededFuture(expires(), mapper.apply(this.result()));
        } catch (Throwable e) {
          return CacheableFuture.failedFuture(e);
        }
      } else {
        return (CacheableFuture<U>) this;
      }
    } else {
      return new MappedCacheableFuture<>(this, mapper);
    }
  }

  @Override
  public <V> CacheableFuture<V> map(final V value) {
    if (isComplete()) {
      if (succeeded()) {
        return CacheableFuture.succeededFuture(expires(), value);
      } else {
        return (CacheableFuture<V>) this;
      }
    } else {
      return new MappedValueCacheableFuture<>(this, value);
    }
  }

  @Override
  public CacheableFuture<T> recover(final Function<Throwable, Future<T>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        return this;
      } else {
        try {
          return CacheableFuture.toCacheable(mapper.apply(cause()));
        } catch (Throwable e) {
          return CacheableFuture.failedFuture(e);
        }
      }
    } else {
      return new RecoverCacheableFuture<>(this, mapper);
    }
  }

  @Override
  public <V> CacheableFuture<V> mapEmpty() {
    return map((V) null);
  }

  @Override
  public <V> CacheableFuture<V> chain(final Function<Void, Future<V>> mapper) {
    return new CacheableFutureChain<>(this, mapper);
  }

  @Override
  public <V> CacheableFuture<V> then(final Function<AsyncResult<T>, Future<V>> mapper) {
    return new CacheableFutureThen<>(this, mapper);
  }

  @Override
  public CacheableFuture<T> otherwise(final Function<Throwable, T> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        return this;
      } else {
        try {
          return CacheableFuture.succeededFuture(expires(), mapper.apply(cause()));
        } catch (Throwable e) {
          return CacheableFuture.failedFuture(e);
        }
      }
    } else {
      return new OtherwiseCacheableFuture<>(this, mapper);
    }
  }

  @Override
  public CacheableFuture<T> otherwiseEmpty() {
    return otherwise(err -> null);
  }

  @Override
  public CacheableFuture<T> addCacheHandler(final Handler<CacheableFuture<T>> handler) {
    setHandler(res -> {
      if (res.succeeded()) {
        handler.handle((CacheableFuture<T>) res);
      }
    });
    return this;
  }

}
