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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import de.braintags.vertx.BtVertxTestBase;
import io.vertx.core.Future;
import io.vertx.ext.unit.TestContext;

/**
 * Unit test for {@link CompositeUtil}
 * 
 * @author sschmitt
 * 
 */
public class TCompositeUtil extends BtVertxTestBase {

  /**
   * Ensure the second execution waits for the given amount of time before starting
   */
  @Test
  public void testTimer(final TestContext context) {
    List<String> testList = new ArrayList<>();
    testList.add("Test1");
    testList.add("Test2");

    long start = System.currentTimeMillis();
    CompositeUtil.executeChunked(testList.iterator(), 1, 500, vertx, value -> {
      if ("Test2".equals(value)) {
        assertThat("Execution should have waited at least 500ms", System.currentTimeMillis() - start,
            greaterThanOrEqualTo(500l));
      }
      return Future.succeededFuture();
    }, context.asyncAssertSuccess());
  }

  /**
   * Test that an invalid chunk size leads to an exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidChunkSize(final TestContext context) {
    List<String> testList = new ArrayList<>();
    testList.add("Test1");
    testList.add("Test2");

    CompositeUtil.executeChunked(testList.iterator(), 0, value -> Future.succeededFuture())
        .setHandler(context.asyncAssertSuccess());
  }

  /**
   * Ensure that all futures are executed, even if one fails in the middle of the execution
   */
  @Test
  public void testCompleteResult(final TestContext context) {
    List<String> testList = new ArrayList<>();
    for (int i = 1; i <= 10; i++)
      testList.add("Test" + i);

    CompositeUtil.executeChunked(testList.iterator(), 2, value -> {
      if ("Test5".equals(value))
        return Future.failedFuture("Test fail");
      else
        return Future.succeededFuture((Void) null);
    }).setHandler(context.asyncAssertSuccess(result -> {
      assertThat(result.size(), is(10));
      int failed = 0;
      int success = 0;
      for (Future<Void> f : result) {
        if (f.failed())
          failed++;
        else
          success++;

      }
      assertThat("There should be only one failed future", failed, is(1));
      assertThat("There should be exactly 9 successful futures", success, is(9));
    }));
  }

  /**
   * Ensure that no errors occur if the iterator is empty
   */
  @Test
  public void testEmptyList(final TestContext context) {
    List<String> testList = new ArrayList<>();

    CompositeUtil.executeChunked(testList.iterator(), 2, value -> {
      Assert.fail("Should not have entered here with an empty list");
      return Future.succeededFuture();
    }).setHandler(context.asyncAssertSuccess());
  }

  /**
   * Ensure that chunks are executed one after the other, and never in parallel
   */
  @Test
  public void testSequentialExecution(final TestContext context) {
    List<Integer> testList = new ArrayList<>();
    for (int i = 1; i <= 10; i++)
      testList.add(i);

    Map<Integer, Boolean> executionMap = new HashMap<>();
    for (int testValue : testList) {
      executionMap.put(testValue, false);
    }

    CompositeUtil.executeChunked(testList.iterator(), 1, value -> {
      for (Entry<Integer, Boolean> entry : executionMap.entrySet()) {
        if (entry.getKey() < value)
          assertThat(entry.getKey() + " should have finished processing before " + value, entry.getValue(), is(true));
        else
          assertThat(entry.getKey() + " should not have been processed before " + value, entry.getValue(), is(false));
      }
      executionMap.put(value, true);
      return Future.succeededFuture();
    }).setHandler(context.asyncAssertSuccess());
  }

}
