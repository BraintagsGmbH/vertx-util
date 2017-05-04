/*
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
package de.braintags.vertx.util.lock;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * An implementation for an asynchronous read / write lock. A {@link LockedExecution} can be called with a read or write
 * lock following the semantics of {@link ReadWriteLock}. The LockedExecution must not block (same as for tasks executed
 * on the event loop) and call the provided handler as soon as the lock can be released.
 * 
 * @author mpluecker
 */
public class AsyncReadWriteLock {

  private static final Logger                    LOGGER = LoggerFactory.getLogger(AsyncReadWriteLock.class);

  private StampedLock                            lock;
  private final ConcurrentLinkedQueue<AsyncLock> pending;

  public AsyncReadWriteLock() {
    this.lock = new StampedLock();
    pending = new ConcurrentLinkedQueue<>();
  }

  /**
   * Execute the {@link LockedExecution} using a read lock.
   * 
   * @param execution
   */
  public void readLock(LockedExecution execution) {
    execute(true, execution);
  }

  /**
   * Execute the {@link LockedExecution} using a write lock.
   * 
   * @param execution
   */
  public void writeLock(LockedExecution execution) {
    execute(false, execution);
  }

  /**
   * Execute the {@link LockedExecution} using the given lock.
   * 
   * @param readLock
   *          true for a read lock, false for a write lock
   * @param execution
   *          the handler executed with the lock.
   */
  public void execute(boolean readLock, LockedExecution execution) {
    AsyncLock asyncLock = new AsyncLock(readLock, execution);
    if (pending.isEmpty()) {
      long lockStamp = obtainLock(readLock);
      if (lockStamp != 0) {
        executeWorkerLocked(lockStamp, asyncLock);
      } else {
        enqueue(asyncLock);
      }
    } else {
      enqueue(asyncLock);
    }
  }

  private long obtainLock(boolean readLock) {
    return readLock ? lock.tryReadLock() : lock.tryWriteLock();
  }

  /**
   * Enqueues the lock for execution after the pending locks. Fair execution for all locks.
   */
  private void enqueue(AsyncLock asyncLock) {
    pending.add(asyncLock);
    // maybe the reason blocking the execution is gone?
    dequeue();
  }

  /**
   * Tries to execute the first pending execution in the queue. Checks if it is already executed. Can be called
   * infinitely often (also in multiple threads).
   */
  private void dequeue() {
    AsyncLock pend = pending.peek();
    if (pend == null) {
      // empty list
      return;
    }

    // obtain lock
    long lockStamp = obtainLock(pend.readLock);
    if (lockStamp != 0) {
      // check if this thread is the one handling the first pending execution
      if (pending.remove(pend)) {
        executeWorkerLocked(lockStamp, pend);
        dequeue();
      } else {
        // there is another thread dequeuing, let him do the rest of the work
        lock.unlock(lockStamp);
      }
    }
  }

  /**
   * Actually executed the execution. All locks must be obtained when calling this method.
   * 
   * @param lockStamp
   */
  private void executeWorkerLocked(long lockStamp, AsyncLock asyncLock) {
    try {
      long currentLock = asyncLock.lockStamp.getAndUpdate(currentValue -> currentValue == 0 ? lockStamp : currentValue);
      if (currentLock != 0) {
        throw new IllegalStateException(
            "by construction it should not happen that there is a second execution of the same LockedExecution");
      }
      asyncLock.execution.perform(() -> executionDone(asyncLock));
    } catch (Exception e) {
      LOGGER.error("error executing execution", e);
      executionDone(asyncLock);
    }
  }

  /**
   * An execution has finished its work and the next execution can be called.
   * 
   * @param asyncLock
   */
  private void executionDone(AsyncLock asyncLock) {
    // this is done to make the lock robust against multiple calls to the handler
    long lockStamp = asyncLock.lockStamp.getAndSet(0);
    if (lockStamp != 0) {
      lock.unlock(lockStamp);
      dequeue();
    }
  }
}
