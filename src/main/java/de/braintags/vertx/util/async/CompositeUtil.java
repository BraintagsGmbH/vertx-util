package de.braintags.vertx.util.async;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Utility class for {@link CompositeFuture}
 * 
 * @author sschmitt
 * 
 */
public class CompositeUtil {

  /**
   * Like a composite future with the "join" method, but executes only a given amount, and waits until that amount is
   * finished before starting the next chunk.
   * 
   * Because a normal "join" execution would execute for all objects, no matter if there is a failure, this method will
   * ignore any failures inside chunks and complete until the iterator is empty.
   * This is why the handler returns a list of all futures. The caller MUST check each future for failure to determine
   * if (and how many) errors occurred during execution. The handler itself will ALWAYS be successful.
   * 
   * @param iterator
   *          the iterator of all data that should be processed
   * @param chunkSize
   *          the size of each individual chunk. A chunk may be smaller if there is not enough data to fill it
   *          completely
   * @param biConsumer
   *          the method that should be executed for each object of the iterator, combined with the completer of
   *          the future for the object
   * @param handler
   *          returns all futures created during the execution. WILL ALWAYS RETURN SUCCESS! Check each future to
   *          determine if the operation really was successful
   */
  public static <T, U> void executeChunked(Iterator<T> iterator, int chunkSize,
      BiConsumer<T, Handler<AsyncResult<U>>> biConsumer, Handler<AsyncResult<List<Future<U>>>> handler) {
    executeChunked(iterator, chunkSize, 0, null, biConsumer, handler);
  }

  /**
   * Like a composite future with the "join" method, but executes only a given amount, and waits until that amount is
   * finished, plus a custom sleep period, before starting the next chunk.
   * 
   * Because a normal "join" execution would execute for all objects, no matter if there is a failure, this method will
   * ignore any failures inside chunks and complete until the iterator is empty.
   * This is why the handler returns a list of all futures. The caller MUST check each future for failure to determine
   * if (and how many) errors occurred during execution. The handler itself will ALWAYS be successful.
   * 
   * @param iterator
   *          the iterator of all data that should be processed
   * @param chunkSize
   *          the size of each individual chunk. A chunk may be smaller if there is not enough data to fill it
   *          completely
   * @param waitDuration
   *          the amount of time in MS to wait between chunks
   * @param vertx
   *          a vertx instance, needed to set a timer to wait between chunks if waitDuration > 0
   * @param biConsumer
   *          the method that should be executed for each object of the iterator, combined with the completer of
   *          the future for the object
   * @param handler
   *          returns all futures created during the execution. WILL ALWAYS RETURN SUCCESS! Check each future to
   *          determine if the operation really was successful
   */
  public static <T, U> void executeChunked(Iterator<T> iterator, int chunkSize, long waitDuration, Vertx vertx,
      BiConsumer<T, Handler<AsyncResult<U>>> biConsumer, Handler<AsyncResult<List<Future<U>>>> handler) {
    List<Future<U>> totalFutures = new ArrayList<>();
    executeChunk(iterator, chunkSize, waitDuration, vertx, biConsumer, totalFutures, result -> {
      handler.handle(Future.succeededFuture(totalFutures));
    });
  }

  /**
   * Recursive method that executes a given chunk and starts the next one after finishing
   * 
   * @param iterator
   *          the iterator of all data that should be processed
   * @param chunkSize
   *          the size of a single chunk
   * @param waitDuration
   *          the amount of time in MS to wait between chunks
   * @param vertx
   *          a vertx instance, needed to set a timer to wait between chunks if waitDuration > 0
   * @param biConsumer
   *          the method that will be executed for each object, and its future completer
   * @param totalFutures
   *          a list that will contain all futures created during the execution
   * @param handler
   *          returns when the iterator is empty and all futures have completed
   */
  @SuppressWarnings("rawtypes")
  private static <U, T> void executeChunk(Iterator<T> iterator, int chunkSize, long waitDuration, Vertx vertx,
      BiConsumer<T, Handler<AsyncResult<U>>> biConsumer, List<Future<U>> totalFutures,
      Handler<AsyncResult<Void>> handler) {
    List<Future> futures = new ArrayList<>();
    while (iterator.hasNext()) {
      T object = iterator.next();
      Future<U> future = Future.future();
      biConsumer.accept(object, future.completer());
      futures.add(future);
      totalFutures.add(future);
      if (futures.size() == chunkSize || !iterator.hasNext()) {
        CompositeFuture.join(futures).setHandler(result -> {
          if (iterator.hasNext()) {
            if (vertx != null && waitDuration > 0) {
              vertx.setTimer(waitDuration, id -> {
                executeChunk(iterator, chunkSize, waitDuration, vertx, biConsumer, totalFutures, handler);
              });
            } else {
              executeChunk(iterator, chunkSize, waitDuration, vertx, biConsumer, totalFutures, handler);
            }
          } else {
            handler.handle(Future.succeededFuture());
          }
        });
        break;
      }
    }
  }

}
