package de.braintags.vertx.util;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StreamUtil {

  private StreamUtil() {

  }

  /**
   * A collector that only returns a single element. Throws an {@link IllegalStateException} if the stream has 0 or more
   * than 1 element.
   * 
   * @return the single element of the stream
   */
  public static <T> Collector<T, ?, T> singletonCollector() {
    return Collectors.collectingAndThen(Collectors.toList(), list -> {
      if (list.size() != 1) {
        throw new IllegalStateException("Expected exactly one element, but got " + list.size());
      }
      return list.get(0);
    });
  }

  /**
   * A collector that returns an optional element, but fails with an {@link IllegalStateException} if the stream
   * contains more than one element.
   * 
   * @return an optional element of the stream
   */
  public static <T> Collector<T, ?, Optional<T>> optionalCollector() {
    return Collectors.collectingAndThen(Collectors.toList(), list -> {
      if (list.size() > 1) {
        throw new IllegalStateException("Expected at most one element, but got " + list.size());
      }
      return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    });
  }

  public static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }
}
