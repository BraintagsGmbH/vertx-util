/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2016 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.util.async;

import io.vertx.core.AsyncResult;

/**
 * Utility class to handle {@link AsyncResult}
 * 
 * @author Michael Remme
 * 
 */
public final class DefaultAsyncResult<T> implements AsyncResult<T> {

  private final Throwable cause;
  private final T result;

  public DefaultAsyncResult(Throwable cause, T result) {
    this.cause = cause;
    this.result = result;
  }

  /**
   * Create a new instance as succeeded result
   * 
   * @param result
   * @return
   */
  public static <T> AsyncResult<T> succeed(T result) {
    return new DefaultAsyncResult<>(null, result);
  }

  /**
   * Creates a new instance as AsyncResult Void
   * 
   * @return
   */
  public static AsyncResult<Void> succeed() {
    return succeed(null);
  }

  /**
   * Creates a new instance of failed result
   * 
   * @param cause
   * @return
   */
  public static <T> AsyncResult<T> fail(Throwable cause) {
    if (cause == null) {
      throw new IllegalArgumentException("cause argument cannot be null");
    }

    return new DefaultAsyncResult<>(cause, null);
  }

  /**
   * Creates a new instance of failed result
   * 
   * @param result
   * @return
   */
  public static <T> AsyncResult<T> fail(AsyncResult<?> result) {
    return fail(result.cause());
  }

  @Override
  public T result() {
    return result;
  }

  @Override
  public Throwable cause() {
    return cause;
  }

  @Override
  public boolean succeeded() {
    return cause == null;
  }

  @Override
  public boolean failed() {
    return cause != null;
  }
}
