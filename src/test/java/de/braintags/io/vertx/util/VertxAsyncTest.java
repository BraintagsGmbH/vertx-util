/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.braintags.io.vertx.BtVertxTestBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * Testing for vertx-async from https://github.com/gchauvet/vertx-async
 * 
 * @author Michael Remme
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VertxAsyncTest extends BtVertxTestBase {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(VertxAsyncTest.class);

  private int COUNT = 50000;
  private static final List<String> toRemove = Arrays.asList("1", "2", "3");

  /**
   * Test method for {@link de.braintags.io.vertx.util.CounterObject#CounterObject(int, io.vertx.core.Handler)}.
   */
  @Test
  public void testAsyncCounter() {
    final List<String> sourceList = generateList();
    List<String> workList = new ArrayList<>(sourceList);

    LOGGER.info("list length: " + workList.size());
    Handler<AsyncResult<String>> handler = new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        int lastElementPosition = sourceList.indexOf(result.result());
        LOGGER.info("last position of element " + result.result() + " is " + lastElementPosition);
      }
    };

    CounterObject<String> co = new CounterObject<>(workList.size(), handler);
    workList.forEach(entry -> {
      entry.toUpperCase().chars().filter(s -> toRemove.contains(s)).findAny();
      if (co.reduce()) {
        co.setResult(entry);
      }
    });

  }

  private List<String> generateList() {
    List<String> sourceList = Arrays.asList("eins", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun",
        "zehn", "elf", "zweiundzwanzig", "vierzehg", "achtzig");
    List<String> returnList = new ArrayList<>(sourceList);
    for (int i = 0; i < COUNT; i++) {
      for (String entry : sourceList) {
        returnList.add(entry + String.valueOf(i));
      }
    }
    return returnList;
  }

}
