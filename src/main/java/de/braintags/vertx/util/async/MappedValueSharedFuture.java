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
