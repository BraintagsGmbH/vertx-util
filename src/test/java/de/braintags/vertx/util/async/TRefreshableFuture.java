package de.braintags.vertx.util.async;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import de.braintags.vertx.BtVertxTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class TRefreshableFuture extends BtVertxTestBase {

  private static int called1 = 0;
  private static int called2 = 0;
  private static int called3 = 0;

  @Test
  public void testNoRefresh(final TestContext context) {
    called1 = 0;
    RefreshableFuture<Integer> refreshable = new RefreshableFuture<>(TimeUnit.SECONDS.toMillis(10), () -> {
      CacheableFuture<Integer> cacheable = CacheableFuture
          .succeededFuture(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5), ++called1);
      return cacheable;
    });

    CacheableFuture<Integer> future = refreshable.get();
    assertThat(future.result(), is(1));
  }

  @Test
  public void testSoftRefresh(final TestContext context) throws InterruptedException {
    called2 = 0;
    RefreshableFuture<Integer> refreshable = new RefreshableFuture<>(TimeUnit.SECONDS.toMillis(10), () -> {
      CacheableFuture<Integer> cacheable = CacheableFuture.future();
      cacheable.reduceExpire(System.currentTimeMillis() + 1);
      if (called2 > 0)
        vertx.setTimer(30, id -> {
          cacheable.complete(++called2);
        });
      else
        cacheable.complete(++called2);
      return cacheable;
    });

    CacheableFuture<Integer> future = refreshable.get();
    assertThat(future.isComplete(), is(true));
    assertThat(future.result(), is(1));
    Async async1 = context.async();
    // wait for soft limit
    vertx.setTimer(20, id -> {
      // soft refresh should have started, but not completed, so first result should still be given
      CacheableFuture<Integer> future2 = refreshable.get();
      assertThat(future2.result(), is(1));
      async1.complete();
    });

    async1.await();

    Async async2 = context.async();
    // wait for the soft refresh to complete
    vertx.setTimer(20, id -> {
      CacheableFuture<Integer> future3 = refreshable.get();
      assertThat(future3.result(), is(2));
      async2.complete();
    });

    async2.await();
  }

  @Test
  public void testHardRefresh(final TestContext context) throws InterruptedException {
    called3 = 0;
    RefreshableFuture<Integer> refreshable = new RefreshableFuture<>(5, () -> {
      CacheableFuture<Integer> cacheable = CacheableFuture.future();
      cacheable.reduceExpire(System.currentTimeMillis() + 1);
      vertx.setTimer(30, id -> {
        cacheable.complete(++called3);
      });
      return cacheable;
    });

    // wait for hard limit expiration
    Async async = context.async();
    vertx.setTimer(60, id -> {
      CacheableFuture<Integer> future2;
      future2 = refreshable.get();
      System.out.println(future2.result());
      assertThat(future2.isComplete(), is(false));
      future2.setHandler(result -> {
        assertThat(result.result(), is(2));
        async.complete();
      });
    });
    async.await();
  }

}
