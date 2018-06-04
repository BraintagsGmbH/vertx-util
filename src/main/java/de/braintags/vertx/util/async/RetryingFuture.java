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

import java.util.concurrent.Callable;
import java.util.function.Function;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * Retries an execution for number of times if it fails
 * 
 * @author mpluecker
 *
 */
public class RetryingFuture<F extends Future<?>> extends SharedFutureImpl<F> {

  private final Callable<F> execution;
  private int currentTry = -1;
  private final Vertx vertx;
  private final Function<Integer, Integer> retryDelayMs;
  private final Function<Integer, Boolean> retryDecider;

  /**
   * @param retryDecider
   *          Function that decides if another retry is made. The function is called with the current number of retries
   *          (first retry is called with 0).
   * @param retryDelayMs
   *          Function from retry to delay. The function is called with the current number of retries (first retry is
   *          called with 0).
   */
  public RetryingFuture(final Vertx vertx, final Callable<F> execution, final Function<Integer, Boolean> retryDecider,
      final Function<Integer, Integer> retryDelayMs) {
    this.vertx = vertx;
    this.execution = execution;
    this.retryDecider = retryDecider;
    this.retryDelayMs = retryDelayMs;
    execute();
  }

  private void execute() {
    try {
      currentTry++;
      F current = execution.call();
      current.setHandler(res -> {
        if (res.succeeded()) {
          complete(current);
        } else {
          retry(res.cause());
        }
      });
    } catch (Throwable e) {
      fail(e);
    }
  }

  private void retry(final Throwable cause) {
    if (retryDecider.apply(currentTry)) {
      vertx.setTimer(retryDelayMs.apply(currentTry), timerId -> execute());
    } else {
      fail(cause);
    }
  }

}
