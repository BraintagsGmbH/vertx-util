package de.braintags.vertx.util.async;

@FunctionalInterface
public interface OneTimeFuture<T extends SharedFuture<?>> {

  public T get();

}
