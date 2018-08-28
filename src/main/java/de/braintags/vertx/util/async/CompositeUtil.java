/*-
 * #%L
 * Vert.x utilities from Braintags
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util.async;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

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

  private CompositeUtil() {
    // hide constructor
  }

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
   * @param func
   *          the function that should be executed for each object of the iterator
   * @param handler
   *          returns all futures created during the execution. WILL ALWAYS RETURN SUCCESS! Check each future to
   *          determine if the operation really was successful
   */
  @Deprecated
  public static <T, U, F extends Future<U>> void executeChunked(final Iterator<T> iterator, final int chunkSize,
      final Function<T, F> func, final Handler<AsyncResult<List<F>>> handler) {
    executeChunked(iterator, chunkSize, 0, null, func, handler);
  }

  public static <T, U, F extends Future<U>> SharedFuture<List<F>> executeChunkedWithFuture(final Iterator<T> iterator,
      final int chunkSize,
      final Function<T, F> func) {
    SharedFuture<List<F>> f = SharedFuture.future();
    executeChunked(iterator, chunkSize, 0, null, func, f);
    return f;
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
   *          the amount of time in MS to wait between chunks, if <= 0 the next chunk will start immediately
   * @param vertx
   *          a vertx instance, needed to set a timer to wait between chunks if waitDuration > 0
   * @param func
   *          the function that should be executed for each object of the iterator
   * @param handler
   *          returns all futures created during the execution. WILL ALWAYS RETURN SUCCESS! Check each future to
   *          determine if the operation really was successful
   */
  public static <T, U, F extends Future<U>> void executeChunked(final Iterator<T> iterator, final int chunkSize,
      final long waitDuration, final Vertx vertx,
      final Function<T, F> func, final Handler<AsyncResult<List<F>>> handler) {
    List<F> totalFutures = new ArrayList<>();
    if (chunkSize <= 0) {
      throw new IllegalArgumentException("'chunkSize' must be > 0");
    }

    if (!iterator.hasNext()) {
      // empty list, don't even start
      handler.handle(Future.succeededFuture(totalFutures));
    } else {
      executeChunk(iterator, chunkSize, waitDuration, vertx, func, totalFutures, result -> {
        handler.handle(Future.succeededFuture(totalFutures));
      });
    }
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
  private static <U, T, F extends Future<U>> void executeChunk(final Iterator<T> iterator, final int chunkSize,
      final long waitDuration, final Vertx vertx, final Function<T, F> biConsumer,
      final List<F> totalFutures,
      final Handler<AsyncResult<Void>> handler) {
    List<Future> futures = new ArrayList<>();
    while (iterator.hasNext()) {
      T object = iterator.next();
      F future = biConsumer.apply(object);
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
