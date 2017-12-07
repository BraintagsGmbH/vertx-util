package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

public class MappedCacheableFuture<U, T> extends CacheableFutureImpl<U> implements CacheableFuture<U> {

  private final Function<T, U> mapper;

  public MappedCacheableFuture(final Future<T> src, final Function<T, U> mapper) {
    this.mapper = mapper;
    src.setHandler(this::chainFuture);
  }

  private void chainFuture(final AsyncResult<T> res) {
    if (res.succeeded()) {
      U result;
      try {
        result = mapper.apply(res.result());
      } catch (Exception e) {
        fail(e);
        return;
      }
      complete(result);
    } else {
      fail(res.cause());
    }
  }

}
