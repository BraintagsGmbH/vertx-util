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

/**
 * Timeout exception for asynchronous processes
 *
 */
public class AsyncTimeoutException extends RuntimeException {

  private static final long serialVersionUID = -6178852666496645693L;

  private final long timeStamp = System.currentTimeMillis();

  public AsyncTimeoutException() {
    super();
  }

  public AsyncTimeoutException(final String message) {
    super(message);
  }

  public AsyncTimeoutException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public AsyncTimeoutException(final Throwable cause) {
    super(cause);
  }

  protected AsyncTimeoutException(final String message, final Throwable cause, final boolean enableSuppression,
      final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public long getTimeStamp() {
    return timeStamp;
  }
}
