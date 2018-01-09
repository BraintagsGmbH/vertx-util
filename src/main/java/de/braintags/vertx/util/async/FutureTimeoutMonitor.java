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

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import io.vertx.core.Context;
import io.vertx.core.Future;

/**
 * Monitors a list of {@link Future futures} and fails them if they do not complete in the time given.
 * In order to be efficient the monitor works in generations which are created at a rate specified by the
 * tolerance of the timeout. A generation is checked as soon as the generation itself timeouts.
 * 
 * @author mpluecker
 *
 */
public class FutureTimeoutMonitor {

  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(FutureTimeoutMonitor.class);

  private final Queue<TimeoutGeneration> generations;
  private final Thread monitorThread;
  private final int timeoutMs;
  private final int toleranceMs;
  private final Function<Future<?>, Exception> timeoutException;

  private volatile TimeoutGeneration currentGeneration;

  /**
   * @param name
   *          Name for the monitoring thread.
   * @param timeoutMs
   *          Timeout for the futures in milliseconds.
   * @param toleranceMs
   *          Tolerance in timeout (50 <= toleranceMs <= timeoutMs). If you decrease this value, the futures timeout
   *          more accurate, but the overhead for monitoring is increased.
   * @param timeoutException
   *          Function to create the cause for the futures to fail with timeout.
   * 
   */
  public FutureTimeoutMonitor(final String name, final int timeoutMs, final int toleranceMs,
      final Function<Future<?>, Exception> timeoutException) {
    this.timeoutMs = timeoutMs;
    this.toleranceMs = toleranceMs;
    this.timeoutException = timeoutException;
    if (toleranceMs > timeoutMs) {
      throw new IllegalArgumentException("toleranceMs must be smaller than timeoutMs");
    }

    if (toleranceMs < 50) {
      throw new IllegalArgumentException("toleranceMs must be larger or equal to 50");
    }

    monitorThread = new Thread(this::monitorThread);
    monitorThread.setName(name);
    monitorThread.setDaemon(true);
    generations = new ArrayDeque<>();
    currentGeneration = new TimeoutGeneration();
    monitorThread.start();
  }

  public void addFuture(final Context context, final Future<?> future) {
    currentGeneration.add(new FutureWithContext(context, future));
  }

  private void monitorThread() {
    while (!Thread.interrupted()) {
      try {
        TimeoutGeneration gen = currentGeneration;
        currentGeneration = new TimeoutGeneration();
        generations.add(gen);

        long timeout = System.currentTimeMillis() - timeoutMs;
        while (!generations.isEmpty() && generations.peek().getCreationTime() < timeout) {
          TimeoutGeneration timoutGen = generations.peek();
          for (FutureWithContext f : timoutGen) {
            if (!f.getFuture().isComplete()) {
              f.getContext().runOnContext(v -> {
                try {
                  Future<?> future = f.getFuture();
                  Exception exception = timeoutException.apply(future);
                  future.tryFail(exception);
                } catch (Exception e2) {
                  LOGGER.error(e2);
                }
              });
            }
          }
          generations.poll();
        }
        Thread.sleep(toleranceMs);
      } catch (InterruptedException e) {
        LOGGER.error(e);
        return;
      } catch (Exception e) {
        LOGGER.error(e);
      }
    }
  }

  private static class TimeoutGeneration implements Iterable<FutureWithContext> {
    private final ConcurrentLinkedQueue<FutureWithContext> futures;
    private final long creationTimeMs;

    public TimeoutGeneration() {
      this.creationTimeMs = System.currentTimeMillis();
      futures = new ConcurrentLinkedQueue<>();
    }

    public long getCreationTime() {
      return creationTimeMs;
    }

    public void add(final FutureWithContext futureWithContext) {
      futures.add(futureWithContext);
    }

    @Override
    public Iterator<FutureWithContext> iterator() {
      return futures.iterator();
    }
  }

  private static class FutureWithContext {

    private final Context context;
    private final Future<?> future;

    public FutureWithContext(final Context context, final Future<?> future) {
      this.context = context;
      this.future = future;
    }

    public Context getContext() {
      return context;
    }

    public Future<?> getFuture() {
      return future;
    }

  }
}
