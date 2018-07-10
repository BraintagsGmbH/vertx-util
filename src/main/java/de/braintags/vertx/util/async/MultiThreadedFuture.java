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
 * Future of an asynchronous process whose result can be shared between multiple threads.
 *
 * @author mpluecker
 *
 * @param <T>
 */
public interface MultiThreadedFuture<T> extends CacheableFuture<T> {

  public static <T> MultiThreadedFuture<T> toMultiThreaded(final Future<T> future) {
    if (future instanceof MultiThreadedFuture) {
      return (MultiThreadedFuture<T>) future;
    } else {
      MultiThreadedFuture<T> res = MultiThreadedFuture.future();
      future.setHandler(res);
      return res;
    }
  }

  public static <T> MultiThreadedFuture<T> future() {
    return new MultiThreadedFutureImpl<>();
  }

  public static <T> MultiThreadedFuture<T> succeededFuture() {
    return succeededFuture(CacheableFuture.EXPIRED, null);
  }

  public static <T> MultiThreadedFuture<T> succeededFuture(final long expires) {
    return succeededFuture(expires, null);
  }

  public static <T> MultiThreadedFuture<T> succeededFuture(final long expires, final T result) {
    return new SucceededMultiThreadedFuture<>(expires, result);
  }

  public static <T> MultiThreadedFuture<T> failedFuture(final Throwable cause) {
    return new MultiThreadedFutureImpl<>(cause);
  }

  @Override
  void complete(long expires, T result);

  @Override
  void reduceExpire(long expires);

  @Override
  <U> MultiThreadedFuture<U> compose(final Function<T, Future<U>> mapper);

  @Override
  <U> MultiThreadedFuture<U> map(final Function<T, U> mapper);

  @Override
  <V> MultiThreadedFuture<V> map(final V value);

  @Override
  <V> MultiThreadedFuture<V> mapEmpty();

  @Override
  <V> MultiThreadedFuture<V> then(Function<AsyncResult<T>, Future<V>> mapper);

  @Override
  <V> MultiThreadedFuture<V> chain(Function<Void, Future<V>> mapper);

  @Override
  MultiThreadedFuture<T> recover(final Function<Throwable, Future<T>> mapper);

  @Override
  MultiThreadedFuture<T> otherwise(Function<Throwable, T> mapper);

  @Override
  MultiThreadedFuture<T> otherwiseEmpty();

  MultiThreadedFuture<T> addHandler(Handler<AsyncResult<T>> handler);


  public static <T> MultiThreadedFuture<T> wrap(final long expires, final Future<T> future) {
    MultiThreadedFuture<T> f = MultiThreadedFuture.future();
    future.setHandler(res -> {
      if (res.succeeded())
        f.complete(expires, res.result());
      else
        f.fail(res.cause());
    });
    return f;
  }

}
