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

class MultiThreadedFutureChain<U> extends MultiThreadedFutureImpl<U> implements MultiThreadedFuture<U> {

  private final Function<Void, Future<U>> mapper;

  public MultiThreadedFutureChain(final Future<?> src, final Function<Void, Future<U>> mapper) {
    this.mapper = mapper;
    src.setHandler(this::chainFuture);
  }

  private void chainFuture(final AsyncResult<?> ar) {
    reduceExpireFromResult(ar);
    Future<U> mapped;
    try {
      mapped = mapper.apply(null);
    } catch (Throwable e) {
      fail(e);
      return;
    }
    mapped.setHandler(res -> {
      if (ar.failed()) {
        fail(ar.cause());
      } else {
        handle(res);
      }
    });
  }

}
