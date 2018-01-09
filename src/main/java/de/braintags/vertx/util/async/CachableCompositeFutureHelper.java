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

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.CompositeFutureImpl;

/**
 * @author jkerkenhoff
 *
 */
public interface CachableCompositeFutureHelper {
  /**
   * Return a composite future, succeeded when all futures are succeeded, failed when any future is failed.
   * <p/>
   * The returned future fails as soon as one future fails.
   *
   * @param futures
   *          the futures
   * @return the composite future
   */
  static CacheableFuture<Void> all(final CacheableFuture<?>... futures) {
    CacheableFuture<Void> f = CacheableFuture.future();
    CompositeFutureImpl.all(futures).setHandler(handler(f, futures));
    return f;
  }

  static Handler<AsyncResult<CompositeFuture>> handler(final CacheableFuture<Void> f,
      final CacheableFuture<?>... futures) {
    return res -> {
      for (CacheableFuture<?> resolvable : futures) {
        f.reduceExpire(resolvable.expires());
      }
      if (res.succeeded()) {
        f.complete(CacheableResult.INFINITE, null);
      } else {
        f.fail(res.cause());
      }
    };
  }

  /**
   * Like {@link #all(Future...))} but with a list of futures.
   * <p>
   *
   * When the list is empty, the returned future will be already completed.
   */
  static CacheableFuture<Void> all(final List<CacheableFuture<?>> futures) {
    CacheableFuture<Void> f = CacheableFuture.future();
    CacheableFuture<?>[] array = futures.toArray(new CacheableFuture[futures.size()]);
    CompositeFutureImpl.all(array).setHandler(handler(f, array));
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
  static CacheableFuture<Void> join(final CacheableFuture<?>... futures) {
    CacheableFuture<Void> f = CacheableFuture.future();
    CompositeFutureImpl.join(futures).setHandler(handler(f, futures));
    return f;
  }

  /**
   * Like {@link #join(Future...)} but with a list of futures.
   * <p>
   *
   * When the list is empty, the returned future will be already completed.
   */
  static CacheableFuture<Void> join(final List<Future> futures) {
    CacheableFuture<Void> f = CacheableFuture.future();
    CacheableFuture<?>[] array = futures.toArray(new CacheableFuture[futures.size()]);
    CompositeFutureImpl.join(array).setHandler(handler(f, array));
    return f;
  }
}
