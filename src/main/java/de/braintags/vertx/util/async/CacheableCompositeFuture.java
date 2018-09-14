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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public interface CacheableCompositeFuture {
  /**
   * Return a composite future, succeeded when all futures are succeeded, failed when any future is failed.
   * <p/>
   * The returned future fails as soon as one future fails.
   *
   * @param futures
   *          the futures
   * @return the composite future
   */
  static CacheableFuture<Void> allVoid(final CacheableFuture<?>... futures) {
    return allVoid(Arrays.asList(futures));
  }

  /**
   * Like {@link #allVoid(Future...))} but with a list of futures.
   * <p>
   *
   * When the list is empty, the returned future will be already completed.
   */
  static CacheableFuture<Void> allVoid(final Collection<? extends CacheableFuture<?>> futures) {
    if (futures.isEmpty()) {
      return CacheableFuture.succeededFuture(CacheableFuture.INFINITE);
    }
    CacheableFuture<Void> f = CacheableFuture.future();
    SharedCompositeFuture.allVoid(futures).setHandler(handler(f, futures));
    return f;
  }

  /**
   * Return a composite future, succeeded when all futures are succeeded, failed when any future is failed.
   * <p/>
   * The returned future fails as soon as one future fails.
   *
   * @param futures
   *          the futures
   * @return the composite future
   */
  static <T> CacheableFuture<List<T>> all(final List<CacheableFuture<T>> futures) {
    if (futures.isEmpty()) {
      return CacheableFuture.succeededFuture(CacheableFuture.INFINITE, Collections.emptyList());
    }
    CacheableFuture<List<T>> f = CacheableFuture.future();
    SharedCompositeFuture.all(futures).setHandler(handler(f, futures));
    return f;
  }

  /**
   * Like {@link #all(Future...))} but with a list of futures.
   * <p>
   *
   * When the list is empty, the returned future will be already completed.
   */
  static <T> CacheableFuture<List<T>> all(final CacheableFuture<T>... futures) {
    return all(Arrays.asList(futures));
  }

  /**
   * Return a composite future, succeeded when all futures are succeeded, failed when any future is failed.
   * <p/>
   * It always waits until all its futures are completed and will not fail as soon as one future
   * fails.
   *
   * @param futures
   *          the futures
   * @return the composite future
   */
  static CacheableFuture<Void> joinVoid(final CacheableFuture<?>... futures) {
    return joinVoid(Arrays.asList(futures));
  }

  /**
   * Like {@link #joinVoid(Future...)} but with a list of futures.
   * <p>
   *
   * When the list is empty, the returned future will be already completed.
   */
  static CacheableFuture<Void> joinVoid(final List<? extends CacheableFuture<?>> futures) {
    if (futures.isEmpty()) {
      return CacheableFuture.succeededFuture(CacheableFuture.INFINITE);
    }
    CacheableFuture<Void> f = CacheableFuture.future();
    SharedCompositeFuture.joinVoid(futures).setHandler(handler(f, futures));
    return f;
  }
  
  /**
   * Return a composite future, succeeded when all futures are succeeded, failed when any future is failed.
   * <p/>
   * It always waits until all its futures are completed and will not fail as soon as one future
   * fails.
   *
   * @param futures
   *          the futures
   * @return the composite future
   */
  static <T> CacheableFuture<List<T>> join(final CacheableFuture<T>... futures) {
    return join(Arrays.asList(futures));
  }

  /**
   * Like {@link #joinVoid(Future...)} but with a list of futures.
   * <p>
   *
   * When the list is empty, the returned future will be already completed.
   */
  static <T> CacheableFuture<List<T>> join(final List<? extends CacheableFuture<T>> futures) {
    if (futures.isEmpty()) {
      return CacheableFuture.succeededFuture(CacheableFuture.INFINITE, Collections.emptyList());
    }
    CacheableFuture<List<T>> f = CacheableFuture.future();
    SharedCompositeFuture.join(futures).setHandler(handler(f, futures));
    return f;
  }

  static <T> Handler<AsyncResult<T>> handler(final CacheableFuture<T> f,
      final Iterable<? extends CacheableFuture<?>> futures) {
    return res -> {
      for (CacheableFuture<?> resolvable : futures) {
        f.reduceExpire(resolvable.expires());
      }
      if (res.succeeded()) {
        f.complete(CacheableResult.INFINITE, res.result());
      } else {
        f.fail(res.cause());
      }
    };
  }
}
