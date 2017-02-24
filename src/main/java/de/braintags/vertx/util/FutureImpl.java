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
package de.braintags.vertx.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.NoStackTraceThrowable;

/**
 * 
 * Abstract implementation of {@link Future}
 * 
 * @author Michael Remme
 * @param T
 *          the result, which the Future shall transport
 *
 */
public class FutureImpl<T> implements Future<T> {
  private boolean failed;
  private boolean succeeded;
  private Handler<AsyncResult<T>> handler;
  private T result;
  private Throwable throwable;

  /**
   * Create a FutureResult that hasn't completed yet
   */
  protected FutureImpl() {
  }

  /**
   * Create a VoidResult that has already completed
   * 
   * @param t
   *          The Throwable or null if succeeded
   */
  FutureImpl(Throwable t) {
    if (t == null) {
      complete(null);
    } else {
      fail(t);
    }
  }

  FutureImpl(String failureMessage) {
    this(new NoStackTraceThrowable(failureMessage));
  }

  /**
   * Create a FutureResult that has already succeeded
   * 
   * @param result
   *          The result
   */
  FutureImpl(T result) {
    complete(result);
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
  public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
    this.handler = handler;
    checkCallHandler();
    return this;
  }

  /**
   * Set the result. Any handler will be called, if there is one
   */
  @Override
  public void complete(T result) {
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
  public void fail(Throwable throwable) {
    if (!tryFail(throwable))
      throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
  }

  @Override
  public void fail(String failureMessage) {
    fail(new NoStackTraceThrowable(failureMessage));
  }

  private void checkCallHandler() {
    if (handler != null && isComplete()) {
      handler.handle(this);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.vertx.core.Future#tryComplete(java.lang.Object)
   */
  @Override
  public boolean tryComplete(T result) {
    if (isComplete())
      return false;
    this.result = result;
    succeeded = true;
    checkCallHandler();
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
  public boolean tryFail(Throwable cause) {
    if (isComplete())
      return false;
    this.throwable = cause;
    failed = true;
    checkCallHandler();
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.vertx.core.Future#tryFail(java.lang.String)
   */
  @Override
  public boolean tryFail(String failureMessage) {
    return tryFail(new NoStackTraceThrowable(failureMessage));
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.vertx.core.Future#handle(io.vertx.core.AsyncResult)
   */
  @Override
  public void handle(AsyncResult<T> asyncResult) {
    if (asyncResult.succeeded())
      complete(asyncResult.result());
    else
      fail(asyncResult.cause());
  }

}
