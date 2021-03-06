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

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class HandlerTest_Future {
  int loops;

  public HandlerTest_Future(int loops) {
    this.loops = loops;
  }

  Future<Integer> loop() {
    List<Future> fl = new ArrayList<>();
    for (int i = 0; i < loops; i++) {
      fl.add(handle(i));
    }
    Future f = Future.future();
    CompositeFuture.all(fl).setHandler(res -> {
      if (res.failed()) {
        f.fail("error");
      } else {
        f.complete(res.result().size());
      }
    });
    return f;
  }

  static Future<Integer> handle(int current) {
    System.out.print(current);
    return Future.succeededFuture(current);
  }

  public static void main(String[] args) {
    Future<Integer> ht = new HandlerTest_Future(50000).loop();
    ht.setHandler(result -> {
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
