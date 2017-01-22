/*
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

import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Handler;

/**
 * A helper to count loops inside an asynchron call. It extends {@link ResultObject} to enable error and result handling
 * inside a loop directly
 * 
 * @param <E>
 *          the underlaying class, which shall be delivered to the Handler as {@link AsyncResult}
 * 
 * @author Michael Remme
 * @deprecated use {@link CompositeFuture} instead
 */

@Deprecated
public class CounterObject<E> extends ResultObject<E> {
  private final AtomicInteger count;

  /**
   * Craetes a new instance with the given count and a handler
   * 
   * @param count
   *          the count to be used for countdown
   * @param handler
   *          if a handler is set, it is automatically informed, if an error occured
   */
  public CounterObject(int count, Handler<AsyncResult<E>> handler) {
    super(handler);
    if (count == 0)
      throw new UnsupportedOperationException("handle zero elements");
    this.count = new AtomicInteger(count);
  }

  /**
   * Reduces the counter by 1 and returns true, if the counter reached 0
   * 
   * @return true, if zero
   */
  public boolean reduce() {
    return count.decrementAndGet() == 0;
  }

  /**
   * Returns true, if the internal counter is 0 ( zero )
   * 
   * @return wether the internal counter is zero
   */
  public boolean isZero() {
    return count.get() == 0;
  }

  /**
   * Get the current counter
   * 
   * @return the counter
   */
  public int getCount() {
    return count.get();
  }
}
