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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import de.braintags.vertx.util.DebugDetection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Implementation of future which is thread safe and supports multiple handlers (set handler is add handler);
 *
 * @author mpluecker
 *
 * @param <T>
 */
public class SharedFutureImpl<T> extends AbstractFuture<T> implements SharedFuture<T> {

  private static final Logger logger = LoggerFactory.getLogger(SharedFutureImpl.class);

  protected boolean failed;
  protected boolean succeeded;
  protected T result;
  protected Throwable throwable;

  private Handler<AsyncResult<T>> handler;
  private List<Handler<AsyncResult<T>>> additionalHandlers;

  private @Nullable Context context;

  /**
   * Create a FutureResult that hasn't completed yet
   */
  SharedFutureImpl() {
  }

  /**
   * Create a SharedFuture that has already succeeded
   *
   * @param result
   *          The result
   */
  SharedFutureImpl(final T result) {
    this();
    complete(result);
  }

  /**
   * Create a SharedFuture that has already failed
   *
   * @param cause
   *          The cause
   */
  SharedFutureImpl(final Throwable cause) {
    this();
    fail(cause);
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
    return succeeded;
  }

  /**
   * Did it fail?
   */
  @Override
  public boolean failed() {
    return failed;
  }

  /**
   * Has it completed?
   */
  @Override
  public boolean isComplete() {
    return failed || succeeded;
  }

  @Override
  public SharedFuture<T> setHandler(final Handler<AsyncResult<T>> handler) {
    boolean callHandler = isComplete();
    if (callHandler) {
      handler.handle(this);
    } else {
      if (this.handler != null) {
        if (!DebugDetection.isTest() || context == Vertx.currentContext()) {
          if (additionalHandlers == null) {
            additionalHandlers = new ArrayList<>(4);
          }
          additionalHandlers.add(handler);
        } else {
          IllegalStateException e = new IllegalStateException(
              "handler already set on context " + context + " - unable to set on context " + Vertx.currentContext());
          handler.handle(Future.failedFuture(e));
          throw e;
        }
      } else {
        this.handler = handler;
        this.context = DebugDetection.isTest() ? Vertx.currentContext() : null;
      }
    }
    return this;
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#tryFail(java.lang.Throwable)
   */
  @Override
  public boolean tryFail(final Throwable cause) {
    if (isComplete())
      return false;
    this.throwable = cause;
    failed = true;
    callHandlers();
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#tryComplete(java.lang.Object)
   */
  @Override
  public boolean tryComplete(final T result) {
    if (isComplete())
      return false;
    this.result = result;
    succeeded = true;
    callHandlers();
    return true;
  }

  protected void callHandlers() {
    if (handler != null) {
      handler.handle(this);
      handler = null;
    }

    if (additionalHandlers != null) {
      int size = additionalHandlers.size();
      for (int i = 0; i < size; i++) {
        additionalHandlers.get(i).handle(this);
      }
      additionalHandlers = null;
    }
  }

  @Override
  public <U> SharedFuture<U> compose(final Function<T, Future<U>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        try {
          return SharedFuture.toCacheable(mapper.apply(this.result()));
        } catch (Throwable e) {
          return SharedFuture.failedFuture(e);
        }
      } else {
        return (SharedFuture<U>) this;
      }
    } else {
      return new ComposedSharedFuture<>(this, mapper);
    }
  }

  @Override
  public <U> SharedFuture<U> map(final Function<T, U> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        try {
          return SharedFuture.succeededFuture(mapper.apply(this.result()));
        } catch (Throwable e) {
          return SharedFuture.failedFuture(e);
        }
      } else {
        return (SharedFuture<U>) this;
      }
    } else {
      return new MappedSharedFuture<>(this, mapper);
    }
  }

  @Override
  public <V> SharedFuture<V> map(final V value) {
    if (isComplete()) {
      if (succeeded()) {
        return SharedFuture.succeededFuture(value);
      } else {
        return (SharedFuture<V>) this;
      }
    } else {
      return new MappedValueSharedFuture<>(this, value);
    }
  }

  @Override
  public <V> SharedFuture<V> mapEmpty() {
    return map((V) null);
  }

  @Override
  public <V> SharedFuture<V> chain(final Function<Void, Future<V>> mapper) {
    return new SharedFutureChain<>(this, mapper);
  }

  @Override
  public <V> SharedFuture<V> then(final Function<AsyncResult<T>, Future<V>> mapper) {
    return new SharedFutureThen<>(this, mapper);
  }

  @Override
  public SharedFuture<T> recover(final Function<Throwable, Future<T>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        return this;
      } else {
        try {
          return SharedFuture.toCacheable(mapper.apply(cause()));
        } catch (Throwable e) {
          return SharedFuture.failedFuture(e);
        }
      }
    } else {
      return new RecoverSharedFuture<>(this, mapper);
    }
  }

  @Override
  public SharedFuture<T> otherwise(final Function<Throwable, T> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    if (isComplete()) {
      if (succeeded()) {
        return this;
      } else {
        try {
          return SharedFuture.succeededFuture(mapper.apply(cause()));
        } catch (Throwable e) {
          return SharedFuture.failedFuture(e);
        }
      }
    } else {
      return new OtherwiseSharedFuture<>(this, mapper);
    }
  }

  @Override
  public SharedFuture<T> otherwiseEmpty() {
    return otherwise(err -> null);
  }

}
