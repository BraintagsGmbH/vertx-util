package de.braintags.io.vertx.util.asyncLock;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.braintags.io.vertx.util.asyncLock.AsyncReadWriteLock;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class AsyncReadWriteLockTest {

  @Test
  public void simpleTest(TestContext context) {
    AsyncReadWriteLock lock = new AsyncReadWriteLock();
    ArrayList<Integer> result = new ArrayList<>();

    int threadCount = 0;

    ExecutorService es = Executors.newFixedThreadPool(3);
    threadCount += executeLockLevel(lock, 5, 0, true, result, es, false, false);
    threadCount += executeLockLevel(lock, 1, 1, false, result, es, false, false);
    threadCount += executeLockLevel(lock, 10, 2, true, result, es, false, false);
    threadCount += executeLockLevel(lock, 1, 3, false, result, es, true, true);
    threadCount += executeLockLevel(lock, 1, 4, false, result, es, false, true);
    threadCount += executeLockLevel(lock, 1, 5, false, result, es, false, true);
    threadCount += executeLockLevel(lock, 3, 6, true, result, es, true, false);
    threadCount += executeLockLevel(lock, 3, 7, true, result, es, false, true);

    for (int tryCount = 0; tryCount < 100 && result.size() < threadCount; tryCount++) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    context.assertEquals(result.size(), threadCount);

    int current = -1;
    for (int i = 0; i < result.size(); i++) {
      Integer v = result.get(i);
      context.assertTrue(current <= v);
      current = v;
    }
  }

  /**
   * Executes count threads on lock in with read or write lock. Each thread adds the level to the result list.
   * Threads are executed on the executor service es and can simulate an exception or illegal behaviour calling
   * the finish handler twice.
   * @param lock
   * @param count
   * @param level
   * @param readLock
   * @param resultList
   * @param es
   * @param simulateException
   * @param simulateIllegalHandler
   * @return
   */
  protected int executeLockLevel(AsyncReadWriteLock lock, int count, int level, boolean readLock,
      ArrayList<Integer> resultList, ExecutorService es, boolean simulateException, boolean simulateIllegalHandler) {
    for (int i = 0; i < count; i++) {
      lock.execute(readLock, handler -> {
        Runnable runnable = () -> {
          try {
            Thread.sleep((int) (Math.random() * 30));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          synchronized (resultList) {
            resultList.add(level);
          }
          handler.run();
          if (simulateIllegalHandler) {
            handler.run();
          }
        };
        if (simulateException) {
          throw new RuntimeException("test execption in worker");
        }

        es.submit(runnable);
      });
    }

    if (simulateException) {
      return 0;
    } else {
      return count;
    }
  }
}
