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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

class MappedValueSharedFuture<U, T> extends SharedFutureImpl<U> implements SharedFuture<U> {

  private final U value;

  public MappedValueSharedFuture(final Future<T> src, final U value) {
    this.value = value;
    src.setHandler(this::chainFuture);
  }

  private void chainFuture(final AsyncResult<T> res) {
    if (res.succeeded()) {
      complete(value);
    } else {
      fail(res.cause());
    }
  }

}
