/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.braintags.vertx.BtVertxTestBase;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class JsonReadStreamTest extends BtVertxTestBase {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(JsonReadStreamTest.class);
  private static final int count = 10;
  private List<TestClass> objectList;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    objectList = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      objectList.add(new TestClass(i));
    }
  }

  @Test
  public void testStream_Embedded_JsonConformNonPretty(final TestContext context) throws IOException {
    JsonReadStream<TestClass> jr = new JsonReadStream<>(objectList, false, true);
    execute(context, jr, true, count, 0, false, true);
  }

  @Test
  public void testStream_Embedded_JsonConformPretty(final TestContext context) throws IOException {
    JsonReadStream<TestClass> jr = new JsonReadStream<>(objectList, true, true);
    execute(context, jr, true, count, 0, true, true);
  }

  @Test
  public void testStream_Embedded_NonJsonConformNonPretty(final TestContext context) throws IOException {
    JsonReadStream<TestClass> jr = new JsonReadStream<>(objectList, false, false);
    execute(context, jr, true, count, 0, false, false);
  }

  @Test
  public void testStream_Embedded_NonJsonConformPretty(final TestContext context) throws IOException {
    JsonReadStream<TestClass> jr = new JsonReadStream<>(objectList, true, false);
    execute(context, jr, true, count, 0, true, false);
  }

  private void execute(final TestContext context, final JsonReadStream<TestClass> qr,
      final boolean expectBufferWritten, final int succeededCount, final int failedCount,
      final boolean pretty, final boolean jsonConform) {
    BufferWriteStream ws = new BufferWriteStream();
    execute(context, qr, ws);
    if (expectBufferWritten) {
      context.assertTrue(ws.buffer.length() > 0, "buffer was not written");
    } else {
      context.assertFalse(ws.buffer.length() > 0, "buffer should not be written");
    }
    String textResult = ws.buffer.toString();
    LOGGER.debug(textResult);
    context.assertEquals(succeededCount, ws.count, "not all instances were written");

    if (jsonConform) {
      context.assertTrue(textResult.startsWith("["), "json conform does not start with array");
      context.assertTrue(textResult.endsWith("]"), "json conform does not end with array");
    } else {
      context.assertTrue(textResult.startsWith("{"), "json NOT conform does not start with {");
      context.assertTrue(textResult.endsWith("}"), "json NOT conform does not start with }");
    }
    try {
      JsonArray arr = new JsonArray(ws.buffer);
      if (jsonConform) {
        context.assertEquals(succeededCount, arr.size(), "the result is incomplete");
      } else {
        context.fail("expected an exception cause no well formatted json");
      }
    } catch (Exception e) {
      if (jsonConform) {
        context.fail(e);
      }

    }
    if (pretty) {
      context.assertTrue(ws.buffer.toString().contains("  "), "the result seems not to be pretty");
    } else {
      context.assertFalse(ws.buffer.toString().contains("  "), "the result seems to be pretty, but should not");
    }
  }

  private void execute(final TestContext context, final JsonReadStream<TestClass> readStream, final WriteStream ws) {
    Async async = context.async();
    readStream.endHandler(end -> async.complete());
    readStream.exceptionHandler(new ErrorHandler(context, async));
    Pump p = Pump.pump(readStream, ws);
    p.start();
    async.await();
  }

  public static class ErrorHandler implements Handler<Throwable> {
    private final TestContext context;
    private final Async async;

    ErrorHandler(final TestContext context, final Async async) {
      this.context = context;
      this.async = async;
    }

    @Override
    public void handle(final Throwable cause) {
      async.complete();
      context.fail(cause);
    }

  }

  public static class TestClass {
    public String prop1;
    public String prop2;

    public TestClass(final int i) {
      this.prop1 = "my property " + i;
      this.prop2 = "my property2 " + i;
    }

  }

  /**
   * Writes content into a Buffer and counts the write actions
   * 
   * 
   * @author Michael Remme
   *
   */
  public static class BufferWriteStream implements WriteStream<Buffer> {
    private final Buffer buffer = Buffer.buffer();
    private Handler<Throwable> exceptionHandler;
    private int count;

    @Override
    public WriteStream<Buffer> exceptionHandler(final Handler<Throwable> handler) {
      this.exceptionHandler = handler;
      return this;
    }

    @Override
    public WriteStream<Buffer> write(final Buffer data) {
      ++count;
      buffer.appendBuffer(data);
      return this;
    }

    @Override
    public WriteStream<Buffer> setWriteQueueMaxSize(final int maxSize) {
      return this;
    }

    @Override
    public boolean writeQueueFull() {
      return false;
    }

    @Override
    public WriteStream<Buffer> drainHandler(final Handler<Void> handler) {
      return this;
    }

    @Override
    public void end() {
    }

  }

}
