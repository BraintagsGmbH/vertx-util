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

class ComposedSharedFuture<U, T> extends SharedFutureImpl<U> implements SharedFuture<U> {

  private final Function<T, Future<U>> mapper;

  public ComposedSharedFuture(final Future<T> src, final Function<T, Future<U>> mapper) {
    this.mapper = mapper;
    src.setHandler(this::chainFuture);
  }

  private void chainFuture(final AsyncResult<T> res) {
    if (res.succeeded()) {
      Future<U> result;
      try {
        result = mapper.apply(res.result());
      } catch (Exception e) {
        fail(e);
        return;
      }
      result.setHandler(this);
    } else {
      this.fail(res.cause());
    }
  }

}
