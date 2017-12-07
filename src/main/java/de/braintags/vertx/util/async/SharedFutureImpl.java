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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.NoStackTraceThrowable;

/**
 * Implementation of future which is thread safe and supports multiple handlers (set handler is add handler);
 *
 * @author mpluecker
 *
 * @param <T>
 */
public class SharedFutureImpl<T> implements SharedFuture<T> {

  protected boolean failed;
  protected boolean succeeded;
  private final List<Handler<AsyncResult<T>>> handlers;
  protected T result;
  protected Throwable throwable;

  /**
   * Create a FutureResult that hasn't completed yet
   */
  public SharedFutureImpl() {
    handlers = new ArrayList<>();
  }

  /**
   * Create a SharedFuture that has already succeeded
   *
   * @param result
   *          The result
   */
  public SharedFutureImpl(final T result) {
    this();
    complete(result);
  }

  /**
   * Create a SharedFuture that has already failed
   *
   * @param cause
   *          The cause
   */
  public SharedFutureImpl(final Throwable cause) {
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

  /**
   * Set a handler for the result. It will get called when it's complete
   */
  @Override
  public SharedFuture<T> setHandler(final Handler<AsyncResult<T>> handler) {
    return addHandler(handler);
  }

  @Override
  public SharedFuture<T> addHandler(final Handler<AsyncResult<T>> handler) {
    boolean handleImmediately;
    synchronized (this) {
      if (handler != null && isComplete()) {
        handleImmediately = true;
      } else {
        handlers.add(handler);
        handleImmediately = false;
      }
    }
    if (handleImmediately) {
      handler.handle(this);
    }
    return this;
  }

  /**
   * Set the result. Any handler will be called, if there is one
   */
  @Override
  public void complete(final T result) {
    if (!tryComplete(result))
      throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
  }

  @Override
  public void complete() {
    complete(null);
  }

  /**
   * Set the failure. Any handler will be called, if there is one
   */
  @Override
  public void fail(final Throwable throwable) {
    if (!tryFail(throwable))
      throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
  }

  @Override
  public void fail(final String failureMessage) {
    fail(new NoStackTraceThrowable(failureMessage));
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#tryComplete(java.lang.Object)
   */
  @Override
  public synchronized boolean tryComplete(final T result) {
    if (isComplete())
      return false;
    this.result = result;
    succeeded = true;
    callHandlers();
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#tryComplete()
   */
  @Override
  public boolean tryComplete() {
    return tryComplete(null);
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
    this.throwable = cause;
    failed = true;
    callHandlers();
    return true;
  }

  protected synchronized void callHandlers() {
    for (Handler<AsyncResult<T>> handler : handlers) {
      handler.handle(this);
    }
    handlers.clear();
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#tryFail(java.lang.String)
   */
  @Override
  public boolean tryFail(final String failureMessage) {
    return tryFail(new NoStackTraceThrowable(failureMessage));
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#handle(io.vertx.core.AsyncResult)
   */
  @Override
  public void handle(final AsyncResult<T> asyncResult) {
    if (asyncResult.succeeded())
      complete(asyncResult.result());
    else
      fail(asyncResult.cause());
  }

  @Override
  public <U> SharedFuture<U> compose(final Function<T, Future<U>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    return new ComposedSharedFuture<>(this, mapper);
  }

  @Override
  public <U> SharedFuture<U> map(final Function<T, U> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    return new MappedSharedFuture<>(this, mapper);
  }

  @Override
  public <V> SharedFuture<V> map(final V value) {
    return new MappedValueSharedFuture<>(this, value);
  }

  @Override
  public <V> SharedFuture<V> mapEmpty() {
    return map((V) null);
  }

  @Override
  public SharedFuture<T> recover(final Function<Throwable, Future<T>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    SharedFuture<T> ret = SharedFuture.future();
    setHandler(ar -> {
      if (ar.succeeded()) {
        ret.complete(result());
      } else {
        Future<T> mapped;
        try {
          mapped = mapper.apply(ar.cause());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        mapped.setHandler(ret);
      }
    });
    return ret;
  }

  @Override
  public SharedFuture<T> otherwise(final Function<Throwable, T> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    SharedFuture<T> ret = SharedFuture.future();
    setHandler(ar -> {
      if (ar.succeeded()) {
        ret.complete(result());
      } else {
        T value;
        try {
          value = mapper.apply(ar.cause());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        ret.complete(value);
      }
    });
    return ret;
  }

}
