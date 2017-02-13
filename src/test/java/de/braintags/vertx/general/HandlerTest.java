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
package de.braintags.vertx.general;

import de.braintags.vertx.util.CounterObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class HandlerTest {
  int loops;

  public HandlerTest(int loops) {
    this.loops = loops;
  }

  void handle(Handler<AsyncResult<Integer>> handler) {
    CounterObject co = new CounterObject<>(loops, handler);
    for (int i = 0; i < loops; i++) {
      handle(i, result -> {
        if (result.failed()) {
          handler.handle(result);
          return;
        } else {
          if (co.reduce()) {
            handler.handle(Future.succeededFuture(loops));
          }
        }
      });
    }
  }

  void handle(Integer element, Handler<AsyncResult<Integer>> handler) {
    handler.handle(Future.succeededFuture(element));
  }

  public static void main(String[] args) {
    HandlerTest ht = new HandlerTest(50000);
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
