package de.braintags.vertx.util.async;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class MultiThreadedFutureImpl<T> extends AbstractFuture<T> implements MultiThreadedFuture<T> {

  private volatile List<Pair<Context, Handler<? super MultiThreadedFuture<T>>>> handlers;
  protected volatile FutureState state = FutureState.RUNNING;
  protected volatile T result;
  protected volatile Throwable throwable;

  MultiThreadedFutureImpl() {
    super();
    handlers = new ArrayList<>();
  }

  MultiThreadedFutureImpl(final Throwable cause) {
    handlers = new ArrayList<>();
    this.expires = CacheableResult.EXPIRED;
  }

  private long expires = CacheableFuture.INFINITE;

  @Override
  public long expires() {
    return expires;
  }

  @Override
  public void reduceExpire(final long expires) {
    this.expires = Math.min(this.expires, expires);
  }

  /**
   * The result of the operation. This will be null if the operation failed.
   */
  @Override
  public T result() {
    return result;
  }

  /**
   * An exception describing failure. This will be null if the operation succeeded.
   */
  @Override
  public Throwable cause() {
    return throwable;
  }

  /**
   * Did it succeeed?
   */
  @Override
  public boolean succeeded() {
    return state == FutureState.SUCCEEDED;
  }

  /**
   * Did it fail?
   */
  @Override
  public boolean failed() {
    return state == FutureState.FAILED;
  }

  /**
   * Has it completed?
   */
  @Override
  public boolean isComplete() {
    return state != FutureState.RUNNING;
  }

  @Override
  public void complete() {
    complete(CacheableFuture.EXPIRED, null);
  }

  @Override
  public void complete(final long expires, final T result) {
    if (!tryComplete(expires, result))
      throw new IllegalStateException("Result is already complete: " + (succeeded() ? "succeeded" : "failed"));
  }

  @Override
  public boolean tryComplete() {
    return tryComplete(CacheableFuture.EXPIRED, null);
  }

  @Override
  public boolean tryComplete(final T value) {
    return tryComplete(CacheableFuture.EXPIRED, value);
  }

  public synchronized boolean tryComplete(final long expires, final T result) {
    if (isComplete())
      return false;
    reduceExpire(expires);
    // important: set result first
    this.result = result;
    state = FutureState.SUCCEEDED;
    callHandlers();
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#tryFail(java.lang.Throwable)
   */
  @Override
  public synchronized boolean tryFail(final Throwable cause) {
    if (isComplete())
      return false;
    // important: set throwable first
    this.throwable = cause;
    state = FutureState.FAILED;
    callHandlers();
    return true;
  }

  public boolean tryComplete(final long expires) {
    return tryComplete(expires, null);
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
  /**
   * Set a handler for the result. It will get called when it's complete
   */
  @Override
  public SharedFuture<T> setHandler(final Handler<AsyncResult<T>> handler) {
    return addHandler(handler);
  }

  protected synchronized void callHandlers() {
    Context currentContext = Vertx.currentContext();
    for (Pair<Context, Handler<? super MultiThreadedFuture<T>>> handler : handlers) {
      if (handler.getKey() == null || currentContext == handler.getKey()) {
        handler.getValue().handle(this);
      } else {
        handler.getKey().runOnContext(v -> {
          handler.getValue().handle(this);
        });
      }
    }
    handlers = null;
  }

  @Override
  public MultiThreadedFuture<T> addHandler(final Handler<? super MultiThreadedFuture<T>> handler) {
    if (handler == null) {
      throw new NullPointerException("null handler not allowed");
    }
    boolean handleImmediately = isComplete();
    if (!handleImmediately) {
      Context currentContext = Vertx.currentContext();
      synchronized (this) {
        handleImmediately = isComplete();
        if (!handleImmediately) {
          handlers.add(Pair.of(currentContext, handler));
        }
      }
    }
    if (handleImmediately) {
      handler.handle(this);
    }
    return this;
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
  public <U> MultiThreadedFuture<U> compose(final Function<T, Future<U>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        try {
          MultiThreadedFuture<U> res = MultiThreadedFuture.toMultiThreaded(mapper.apply(this.result()));
          res.reduceExpire(expires());
          return res;
        } catch (Throwable e) {
          return MultiThreadedFuture.failedFuture(e);
        }
      } else {
        return (MultiThreadedFuture<U>) this;
      }
    } else {
      return new ComposedMultiThreadedFuture<>(this, mapper);
    }

  }

  @Override
  public <U> MultiThreadedFuture<U> map(final Function<T, U> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        try {
          return MultiThreadedFuture.succeededFuture(expires(), mapper.apply(this.result()));
        } catch (Throwable e) {
          return MultiThreadedFuture.failedFuture(e);
        }
      } else {
        return (MultiThreadedFuture<U>) this;
      }
    } else {
      return new MappedMultiThreadedFuture<>(this, mapper);
    }
  }

  @Override
  public <V> MultiThreadedFuture<V> map(final V value) {
    if (isComplete()) {
      if (succeeded()) {
        return MultiThreadedFuture.succeededFuture(expires(), value);
      } else {
        return (MultiThreadedFuture<V>) this;
      }
    } else {
      return new MappedValueMultiThreadedFuture<>(this, value);
    }
  }

  @Override
  public MultiThreadedFuture<T> recover(final Function<Throwable, Future<T>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        return this;
      } else {
        try {
          return MultiThreadedFuture.toMultiThreaded(mapper.apply(cause()));
        } catch (Throwable e) {
          return MultiThreadedFuture.failedFuture(e);
        }
      }
    } else {
      return new RecoverMultiThreadedFuture<>(this, mapper);
    }
  }

  @Override
  public <V> MultiThreadedFuture<V> mapEmpty() {
    return map((V) null);
  }

  @Override
  public <V> MultiThreadedFuture<V> chain(final Function<Void, Future<V>> mapper) {
    return new MultiThreadedFutureChain<>(this, mapper);
  }

  @Override
  public <V> MultiThreadedFuture<V> then(final Function<AsyncResult<T>, Future<V>> mapper) {
    return new MultiThreadedFutureThen<>(this, mapper);
  }

  @Override
  public MultiThreadedFuture<T> otherwise(final Function<Throwable, T> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        return this;
      } else {
        try {
          return MultiThreadedFuture.succeededFuture(expires(), mapper.apply(cause()));
        } catch (Throwable e) {
          return MultiThreadedFuture.failedFuture(e);
        }
      }
    } else {
      return new OtherwiseMultiThreadedFuture<>(this, mapper);
    }
  }

  @Override
  public MultiThreadedFuture<T> otherwiseEmpty() {
    return otherwise(err -> null);
  }
  
  @Override
  public MultiThreadedFuture<T> addCacheHandler(Handler<CacheableFuture<T>> handler) {
    addHandler(handler);
    return this;
  }
}
