package de.braintags.vertx.util.async;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

public class SharedCompositeFuture {

  public static SharedFuture<Void> allVoid(final Collection<? extends Future<?>> futures) {
    return SharedFuture.wrap(CompositeFuture.all(new ArrayList(futures))).mapEmpty();
  }

  public static <T> SharedFuture<List<T>> all(final List<? extends Future<T>> futures) {
    return SharedFuture
        .wrap(CompositeFuture.all((List) futures).map(v -> futures.stream().map(Future::result).collect(toList())));
  }

  public static SharedFuture<CompositeFuture> join(final List<? extends SharedFuture<?>> futures) {
    return SharedFuture.wrap(CompositeFuture.join((List) futures));
  }

}
