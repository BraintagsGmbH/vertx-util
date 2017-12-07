package de.braintags.vertx.util.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

public class MappedValueCacheableFuture<U, T> extends CacheableFutureImpl<U> implements CacheableFuture<U> {

  private final U value;

  public MappedValueCacheableFuture(final Future<T> src, final U value) {
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
