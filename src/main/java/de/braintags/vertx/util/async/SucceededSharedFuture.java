package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class SucceededSharedFuture<T> implements SharedFuture<T> {

  protected final T result;

  /**
   * Create a future that has already succeeded
   * @param result the result
   */
  SucceededSharedFuture(final T result) {
    this.result = result;
  }

  @Override
  public boolean isComplete() {
    return true;
  }

  @Override
  public SharedFuture<T> setHandler(final Handler<AsyncResult<T>> handler) {
    handler.handle(this);
    return this;
  }

  @Override
  public void complete(final T result) {
    throw new IllegalStateException("Result is already complete: succeeded");
  }

  @Override
  public void complete() {
    throw new IllegalStateException("Result is already complete: succeeded");
  }

  @Override
  public void fail(final Throwable cause) {
    throw new IllegalStateException("Result is already complete: succeeded");
  }

  @Override
  public void fail(final String failureMessage) {
    throw new IllegalStateException("Result is already complete: succeeded");
  }

  @Override
  public boolean tryComplete(final T result) {
    return false;
  }

  @Override
  public boolean tryComplete() {
    return false;
  }

  @Override
  public boolean tryFail(final Throwable cause) {
    return false;
  }

  @Override
  public boolean tryFail(final String failureMessage) {
    return false;
  }

  @Override
  public T result() {
    return result;
  }

  @Override
  public Throwable cause() {
    return null;
  }

  @Override
  public boolean succeeded() {
    return true;
  }

  @Override
  public boolean failed() {
    return false;
  }

  @Override
  public void handle(final AsyncResult<T> asyncResult) {
    throw new IllegalStateException("Result is already complete: succeeded");
  }

  @Override
  public String toString() {
    return "SharedFuture{result=" + result + "}";
  }

  @Override
  public <U> SharedFuture<U> compose(final Function<T, Future<U>> mapper) {
    try {
      return SharedFuture.toCacheable(mapper.apply(result));
    } catch (Throwable e) {
      return SharedFuture.failedFuture(e);
    }
  }

  @Override
  public <U> SharedFuture<U> map(final Function<T, U> mapper) {
    try {
      return SharedFuture.succeededFuture(mapper.apply(result));
    } catch (Throwable e) {
      return SharedFuture.failedFuture(e);
    }
  }

  @Override
  public <V> SharedFuture<V> map(final V value) {
    try {
      return SharedFuture.succeededFuture(value);
    } catch (Throwable e) {
      return SharedFuture.failedFuture(e);
    }
  }

  @Override
  public <V> SharedFuture<V> mapEmpty() {
    try {
      return SharedFuture.succeededFuture();
    } catch (Throwable e) {
      return SharedFuture.failedFuture(e);
    }
  }

  @Override
  public <V> SharedFuture<V> chain(final Function<Void, Future<V>> mapper) {
    try {
      return SharedFuture.toCacheable(mapper.apply(null));
    } catch (Throwable e) {
      return SharedFuture.failedFuture(e);
    }
  }

  @Override
  public <V> SharedFuture<V> then(final Function<AsyncResult<T>, Future<V>> mapper) {
    try {
      return SharedFuture.toCacheable(mapper.apply(this));
    } catch (Throwable e) {
      return SharedFuture.failedFuture(e);
    }
  }

  @Override
  public SharedFuture<T> recover(final Function<Throwable, Future<T>> mapper) {
    return this;
  }

  @Override
  public SharedFuture<T> otherwise(final Function<Throwable, T> mapper) {
    return this;
  }

  @Override
  public SharedFuture<T> otherwiseEmpty() {
    return this;
  }

}
