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

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

class CacheableFutureThen<U, T> extends CacheableFutureImpl<U> implements CacheableFuture<U> {

  private final Function<AsyncResult<T>, Future<U>> mapper;

  public CacheableFutureThen(final Future<T> src, final Function<AsyncResult<T>, Future<U>> mapper) {
    this.mapper = mapper;
    src.setHandler(this::chainFuture);
  }

  private void chainFuture(final AsyncResult<T> res) {
    reduceExpireFromResult(res);
    Future<U> result;
    try {
      result = mapper.apply(res);
    } catch (Throwable e) {
      fail(e);
      return;
    }
    result.setHandler(this);
  }

}
