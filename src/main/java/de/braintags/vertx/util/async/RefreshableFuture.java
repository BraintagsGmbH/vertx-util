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
public class RefreshableFuture<T> {

  private static final Logger logger = LoggerFactory.getLogger(RefreshableFuture.class);

  private final long hardLimit;
  private final Function<T, Boolean> shouldRefreshFilter;
  private final Supplier<CacheableFuture<T>> supplier;
  private final AtomicBoolean refreshing = new AtomicBoolean(false);

  private long softExpires;
  private long hardExpires;
  private CacheableFuture<T> currentFuture;

  /**
   * Create a new refreshable future
   * 
   * @param hardLimit
   *          the limit in ms, after which a call to {@link #get()} will be guaranteed to result in a new future
   * @param supplier
   *          the supplier to create a new future, will be used to refresh the future repeatedly, when necessary
   */
  public RefreshableFuture(final long hardLimit, final Supplier<CacheableFuture<T>> supplier) {
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
  public RefreshableFuture(final long hardLimit, final Supplier<CacheableFuture<T>> supplier,
      final Function<T, Boolean> shouldRefreshFilter) {
    this.hardLimit = hardLimit;
    this.shouldRefreshFilter = shouldRefreshFilter;
    this.currentFuture = supplier.get();
    this.softExpires = currentFuture.expires();
    this.hardExpires = System.currentTimeMillis() + hardLimit;
    assert this.softExpires <= this.hardExpires;
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
  public CacheableFuture<T> get() {
    if (shouldRefreshFilter != null && currentFuture.succeeded() && !shouldRefreshFilter.apply(currentFuture.result()))
      return currentFuture;

    if (System.currentTimeMillis() > hardExpires) {
      hardRefresh();
    } else if (System.currentTimeMillis() > softExpires) {
      softRefresh();
    }
    return currentFuture;
  }

  private void softRefresh() {
    if (refreshing.compareAndSet(false, true)) {
      CacheableFuture<T> newFuture = supplier.get();
      newFuture.setHandler(res -> {
        if (res.succeeded()) {
          this.softExpires = newFuture.expires();
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
      CacheableFuture<T> newFuture = supplier.get();
      this.softExpires = newFuture.expires();
      this.hardExpires = System.currentTimeMillis() + hardLimit;
      this.currentFuture = newFuture;
      refreshing.set(false);
    }
  }
}
