package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

public class ComposedCacheableFuture<U, T> extends CacheableFutureImpl<U> implements CacheableFuture<U> {

  private final Function<T, Future<U>> mapper;

  public ComposedCacheableFuture(final Future<T> src, final Function<T, Future<U>> mapper) {
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
