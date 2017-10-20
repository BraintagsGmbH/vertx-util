package de.braintags.vertx.util.async;

import java.util.function.Function;

import io.vertx.core.AsyncResult;

/**
 * Result of an Service invocation that can be cached.
 * This results specifies how long it can be cached via the expires field.
 * 
 * @author mpluecker
 *
 * @param <T>
 */
public interface CacheableResult<T> extends AsyncResult<T> {

  public static final long EXPIRED = -1;
  public static final long INFINITE = Long.MAX_VALUE;

  long expires();

  public static <T> CacheableResult<T> succeededResult(final T result, final long expires) {
    return new CacheableResultImpl<>(result, expires);
  }

  public static <T> CacheableResult<T> failedResult(final Throwable cause) {
    return new CacheableResultImpl<>(cause);
  }


  @Override
  default <U> CacheableResult<U> map(final Function<T, U> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }

    try {
      U mappedResult;
      if (succeeded()) {
        mappedResult = mapper.apply(CacheableResult.this.result());
      } else {
        mappedResult = null;
      }
      return new CacheableResult<U>() {
        @Override
        public U result() {
          if (succeeded()) {
            return mappedResult;
          } else {
            return null;
          }
        }

        @Override
        public Throwable cause() {
          return CacheableResult.this.cause();
        }

        @Override
        public boolean succeeded() {
          return CacheableResult.this.succeeded();
        }

        @Override
        public boolean failed() {
          return CacheableResult.this.failed();
        }

        @Override
        public long expires() {
          return CacheableResult.this.expires();
        }
      };
    } catch (Exception e) {
      return CacheableResult.failedResult(e);
    }
  }

  @Override
  default <V> CacheableResult<V> map(final V value) {
    return map(t -> value);
  }

  @Override
  default <V> CacheableResult<V> mapEmpty() {
    return map((V) null);
  }

}
