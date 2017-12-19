package de.braintags.vertx.util.async;

public interface OneTimeFutureCallable<T extends SharedFuture<?>> {

  public T get();

}
