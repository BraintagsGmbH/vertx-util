package de.braintags.vertx.util.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public abstract class AbstractFuture<T> implements Future<T> {

  private static final Logger logger = LoggerFactory.getLogger(AbstractFuture.class);

  /**
   * Set the result. Any handler will be called, if there is one
   */
  @Override
  public void complete(final T result) {
    if (!tryComplete(result))
      if (isTimeouted()) {
        logger.error(
            "Timeouted operation, timeouted for "
                + (System.currentTimeMillis() - ((AsyncTimeoutException) cause()).getTimeStamp()) + " ms",
            new IllegalStateException("Result is already timeouted"));
      } else
        throw new IllegalStateException("Result is already complete: " + (succeeded() ? "succeeded" : "failed"));
  }

  protected boolean isTimeouted() {
    return cause() != null && (cause() instanceof AsyncTimeoutException);
  }

  @Override
  public void complete() {
    complete(null);
  }

  /**
   * Set the failure. Any handler will be called, if there is one
   */
  @Override
  public void fail(final Throwable throwable) {
    if (!tryFail(throwable)) {
      if (isTimeouted()) {
        logger.error("error in timeouted future, timeouted for "
            + (System.currentTimeMillis() - ((AsyncTimeoutException) cause()).getTimeStamp()) + " ms", throwable);
      } else {
        throw new IllegalStateException("Result is already complete: " + (succeeded() ? "succeeded" : "failed"));
      }
    }
  }

  @Override
  public void fail(final String failureMessage) {
    fail(new NoStackTraceThrowable(failureMessage));
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#tryComplete()
   */
  @Override
  public boolean tryComplete() {
    return tryComplete(null);
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#tryFail(java.lang.String)
   */
  @Override
  public boolean tryFail(final String failureMessage) {
    return tryFail(new NoStackTraceThrowable(failureMessage));
  }

  /*
   * (non-Javadoc)
   *
   * @see io.vertx.core.Future#handle(io.vertx.core.AsyncResult)
   */
  @Override
  public void handle(final AsyncResult<T> asyncResult) {
    if (asyncResult.succeeded())
      complete(asyncResult.result());
    else
      fail(asyncResult.cause());
  }
}
