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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
    return all(Arrays.asList(futures));
  }

  /**
   * Like {@link #all(Future...))} but with a list of futures.
   * <p>
   *
   * When the list is empty, the returned future will be already completed.
   */
  static CacheableFuture<Void> all(final Collection<CacheableFuture<?>> futures) {
    CacheableFuture<Void> f = CacheableFuture.future();
    SharedCompositeFuture.allVoid(futures).setHandler(handler(f, futures));
    return f;
  }

  static <T> CacheableFuture<List<T>> a(final List<CacheableFuture<T>> futures) {
    CacheableFuture<List<T>> f = CacheableFuture.future();
    SharedCompositeFuture.all(futures).setHandler(handler(f, futures));
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
  static CacheableFuture<Void> join(final List<? extends CacheableFuture<?>> futures) {
    CacheableFuture<Void> f = CacheableFuture.future();
    CacheableFuture<?>[] array = futures.toArray(new CacheableFuture[futures.size()]);
    CompositeFutureImpl.join(array).setHandler(handler(f, array));
    return f;
  }
  
  static <T> CacheableFuture<List<T>> join2(final List<? extends CacheableFuture<T>> futures) {
    return CacheableFuture.wrap(futures.stream().min(Comparator.comparing(CacheableFuture::expires)).get().expires(),
        CompositeFuture.join((List) futures).map(v -> futures.stream().map(Future::result).collect(toList())));
  }
}
