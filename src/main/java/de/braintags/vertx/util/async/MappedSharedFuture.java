package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

public class MappedSharedFuture<U, T> extends SharedFutureImpl<U> implements SharedFuture<U> {

  private final Function<T, U> mapper;

  public MappedSharedFuture(final Future<T> src, final Function<T, U> mapper) {
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
