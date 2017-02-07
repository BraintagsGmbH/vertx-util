package de.braintags.vertx.general;

import de.braintags.vertx.util.CounterObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class HandlerTest_staticMethod {
  int loops;

  public HandlerTest_staticMethod(int loops) {
    this.loops = loops;
  }

  void handle(Handler<AsyncResult<Integer>> handler) {
    CounterObject co = new CounterObject<>(loops, handler);
    for (int i = 0; i < loops; i++) {
      handle(i, result -> handleResult(handler, co, loops, result));
    }
  }

  static void handleResult(Handler<AsyncResult<Integer>> handler, CounterObject co, int loops,
      AsyncResult<Integer> result) {
    if (result.failed()) {
      handler.handle(result);
      return;
    } else {
      if (co.reduce()) {
        handler.handle(Future.succeededFuture(loops));
      }
      System.out.print(result.result());
    }
  }

  void handle(Integer element, Handler<AsyncResult<Integer>> handler) {
    handler.handle(Future.succeededFuture(element));
  }

  public static void main(String[] args) {
    HandlerTest_staticMethod ht = new HandlerTest_staticMethod(50000);
    ht.handle(result -> {
      if (result.failed()) {
        System.out.println(result.cause());
      } else {
        System.out.println(" ");
        System.out.println("========================");
        System.out.println("loops: " + result.result());
      }
    });

  }

}
