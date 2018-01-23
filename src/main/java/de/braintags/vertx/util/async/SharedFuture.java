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
 * Future of an asynchronous process which can have multiple handlers.
 * All methods that complete the future (e.g., {@link SharedFuture#complete()}) may not
 * throw an exception if the future has been failed with an {@link AsyncTimeoutException}.
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
  <V> SharedFuture<V> mapEmpty();

  @Override
  SharedFuture<T> recover(final Function<Throwable, Future<T>> mapper);

  @Override
  SharedFuture<T> otherwise(Function<Throwable, T> mapper);

  @Override
  SharedFuture<T> otherwiseEmpty();

  public static <T> SharedFuture<T> wrap(final Future<T> future) {
    SharedFuture<T> f = new SharedFutureImpl<>();
    future.setHandler(f);
    return f;
  }

  @Override
  SharedFuture<T> setHandler(Handler<AsyncResult<T>> handler);

  SharedFuture<T> addHandler(Handler<AsyncResult<T>> asyncAssertSuccess);

}
