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
package de.braintags.vertx.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class ErrorObjectTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#ErrorObject(io.vertx.core.Handler)}.
   */
  @Test
  public void testErrorObject() {
    ErrorObject err = new ErrorObject<>(null);
    assertNull(err.getThrowable());
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#isError()}.
   */
  @Test
  public void testIsError() {
    ErrorObject err = new ErrorObject<>(null);
    assertFalse(err.isError());
    err.setThrowable(new IllegalArgumentException());
    assertTrue(err.isError());
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#getThrowable()}.
   */
  @Test
  public void testGetThrowable() {
    ErrorObject err = new ErrorObject<>(null);
    assertFalse(err.isError());
    err.setThrowable(new IllegalArgumentException());
    assertNotNull(err.getThrowable());
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#getRuntimeException()}.
   */
  @Test
  public void testGetRuntimeException() {
    ErrorObject err = new ErrorObject<>(null);
    err.setThrowable(new IllegalArgumentException());
    assertNotNull(err.getRuntimeException());
    assertTrue(err.getRuntimeException() instanceof RuntimeException);
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#setThrowable(java.lang.Throwable)}.
   */
  @Test
  public void testSetThrowable() {
    ErrorObject err = new ErrorObject<>(null);
    err.setThrowable(new IllegalArgumentException());
    assertNotNull(err.getRuntimeException());
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#toFuture()}.
   */
  @SuppressWarnings("rawtypes")
  @Test
  public void testToFuture() {
    ErrorObject err = new ErrorObject<>(null);
    err.setThrowable(new IllegalArgumentException());
    Future f = err.toFuture();
    assertNotNull(f.cause());
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#handleError()}.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void testHandleError() {
    StringBuffer buffer = new StringBuffer();
    ErrorObject err = new ErrorObject<>(new Handler() {

      @Override
      public void handle(Object event) {
        buffer.append("test");
      }
    });
    err.setThrowable(new IllegalArgumentException());
    assertEquals("test", buffer.toString());
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#isErrorHandled()}.
   */
  @Test
  public void testIsErrorHandled() {
    StringBuffer buffer = new StringBuffer();
    ErrorObject err = new ErrorObject<>(new Handler() {

      @Override
      public void handle(Object event) {
        buffer.append("test");
      }
    });
    err.setThrowable(new IllegalArgumentException());
    assertTrue(err.isErrorHandled());
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ErrorObject#getHandler()}.
   */
  @Test
  public void testGetHandler() {
    StringBuffer buffer = new StringBuffer();
    ErrorObject err = new ErrorObject<>(new Handler() {

      @Override
      public void handle(Object event) {
        buffer.append("test");
      }
    });
    assertNotNull(err.getHandler());
  }

}
