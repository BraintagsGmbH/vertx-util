package de.braintags.vertx.util.async;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

public class SharedCompositeFuture {

  public static SharedFuture<Void> allVoid(final Future<?>... futures) {
    return allVoid(Arrays.asList(futures));
  }

  public static SharedFuture<Void> allVoid(final Collection<? extends Future<?>> futures) {
    if (futures.isEmpty()) {
      return SharedFuture.succeededFuture();
    }
    return SharedFuture.wrap(CompositeFuture.all(new ArrayList(futures))).mapEmpty();
  }

  public static <T> SharedFuture<List<T>> all(final List<? extends Future<? extends T>> futures) {
    if (futures.isEmpty()) {
      return SharedFuture.succeededFuture(Collections.emptyList());
    }
    return SharedFuture
        .wrap(CompositeFuture.all((List) futures).map(v -> futures.stream().map(Future::result).collect(toList())));
  }

  public static SharedFuture<Void> joinVoid(final Collection<? extends Future<?>> futures) {
    if (futures.isEmpty()) {
      return SharedFuture.succeededFuture();
    }
    return SharedFuture.wrap(CompositeFuture.join(new ArrayList(futures))).mapEmpty();
  }

  public static <T> SharedFuture<List<T>> join(final List<? extends Future<T>> futures) {
    if (futures.isEmpty()) {
      return SharedFuture.succeededFuture(Collections.emptyList());
    }
    return SharedFuture
        .wrap(CompositeFuture.join((List) futures).map(v -> futures.stream().map(Future::result).collect(toList())));
  }

}
