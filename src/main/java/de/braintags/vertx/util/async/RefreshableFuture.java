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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * This class acts as a self-refreshing cache for futures. <br>
 * If the limit of the cacheable future expires, the future will be recreated in the background and the new future
 * will be set once it is completed.<br>
 * If the hard limit is reached, the new, not yet completed future will be returned.
 * 
 * 
 * @author sschmitt
 *
 * @param <T>
 */
public class RefreshableFuture<T, F extends CacheableFuture<T>> {

  private static final Logger logger = LoggerFactory.getLogger(RefreshableFuture.class);

  private final long hardLimit;
  private final Function<T, Boolean> shouldRefreshFilter;
  private final Supplier<F> supplier;
  private final AtomicBoolean refreshing = new AtomicBoolean(false);

  private volatile long hardExpires;
  private F currentFuture;

  /**
   * Create a new refreshable future
   * 
   * @param hardLimit
   *          the limit in ms, after which a call to {@link #get()} will be guaranteed to result in a new future
   * @param supplier
   *          the supplier to create a new future, will be used to refresh the future repeatedly, when necessary
   */
  public RefreshableFuture(final long hardLimit, final Supplier<F> supplier) {
    this(hardLimit, supplier, null);
  }

  /**
   * Create a new refreshable future
   * 
   * @param hardLimit
   *          the limit in ms, after which a call to {@link #get()} will be guaranteed to result in a new future
   * @param supplier
   *          the supplier to create a new future, will be used to refresh the future repeatedly, when necessary
   * @param shouldRefreshFilter
   *          an optional filter. If it returns false for the result of the current future, it will not be refreshed, no
   *          matter the expiration values. If it returns true, the normal expiration rules are applied to the future.
   */
  public RefreshableFuture(final long hardLimit, final Supplier<F> supplier,
      final Function<T, Boolean> shouldRefreshFilter) {
    this.hardLimit = hardLimit;
    this.shouldRefreshFilter = shouldRefreshFilter;
    this.currentFuture = supplier.get();
    this.hardExpires = System.currentTimeMillis() + hardLimit;
    this.supplier = supplier;
  }

  /**
   * Checks if the future is still valid, and will return either
   * <ul>
   * <li>a still valid future</li>
   * <li>the last valid result, while the future is being refreshed in the backgroudn</li>
   * <li>a new future that is still being completed</li>
   * <ul>
   * 
   * @return a valid future
   */
  public F get() {
    if (!currentFuture.isComplete() || (shouldRefreshFilter != null && currentFuture.succeeded()
        && !shouldRefreshFilter.apply(currentFuture.result())))
      return currentFuture;

    long currentTimeMillis = System.currentTimeMillis();
    if (currentTimeMillis > hardExpires) {
      hardRefresh();
    } else if (currentTimeMillis > currentFuture.expires()) {
      softRefresh();
    }
    return currentFuture;
  }

  private void softRefresh() {
    if (refreshing.compareAndSet(false, true)) {
      F newFuture = supplier.get();
      newFuture.setHandler(res -> {
        if (res.succeeded()) {
          this.hardExpires = System.currentTimeMillis() + hardLimit;
          this.currentFuture = newFuture;
        } else {
          logger.error("Error soft-refreshing future", res.cause());
        }
        refreshing.set(false);
      });
    }
  }

  private synchronized void hardRefresh() {
    if (System.currentTimeMillis() > hardExpires) {
      F newFuture = supplier.get();
      this.hardExpires = System.currentTimeMillis() + hardLimit;
      this.currentFuture = newFuture;
      refreshing.set(false);
    }
  }
}
