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

class RecoverSharedFuture<T> extends SharedFutureImpl<T> implements SharedFuture<T> {

  private final Function<Throwable, Future<T>> mapper;

  public RecoverSharedFuture(final Future<T> src, final Function<Throwable, Future<T>> mapper) {
    this.mapper = mapper;
    src.setHandler(this::chainFuture);
  }

  private void chainFuture(final AsyncResult<T> res) {
    if (res.succeeded()) {
      complete(res.result());
    } else {
      Future<T> mapped;
      try {
        mapped = mapper.apply(res.cause());
      } catch (Throwable e) {
        fail(e);
        return;
      }
      mapped.setHandler(this);
    }
  }

}
