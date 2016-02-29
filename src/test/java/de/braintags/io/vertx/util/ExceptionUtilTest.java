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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class ExceptionUtilTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ExceptionUtil#createRuntimeException(java.lang.Throwable)}.
   */
  @Test
  public void testCreateRuntimeException() {
    assertTrue(ExceptionUtil.createRuntimeException(new IOException()) instanceof RuntimeException);
    RuntimeException re = new RuntimeException();
    assertSame(re, ExceptionUtil.createRuntimeException(re));
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ExceptionUtil#getStackTrace(java.lang.Throwable)}.
   */
  @Test
  public void testGetStackTraceThrowable() {
    assertNotNull(ExceptionUtil.getStackTrace(new RuntimeException()));
  }

  /**
   * Test method for
   * {@link de.braintags.io.vertx.util.ExceptionUtil#getStackTrace(java.lang.Throwable, java.lang.String)}.
   */
  @Test
  public void testGetStackTraceThrowableString() {
    assertNotNull(ExceptionUtil.getStackTrace(new RuntimeException(), null));
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ExceptionUtil#getStackTrace(java.lang.Throwable, int)}.
   */
  @Test
  public void testGetStackTraceThrowableInt() {
    assertNotNull(ExceptionUtil.getStackTrace(new RuntimeException(), 2));
  }

  /**
   * Test method for
   * {@link de.braintags.io.vertx.util.ExceptionUtil#appendStackTrace(java.lang.Throwable, java.lang.StringBuilder, java.lang.String, int)}
   * .
   */
  @Test
  public void testAppendStackTraceThrowableStringBuilderStringInt() {
    StringBuilder b = new StringBuilder();
    ExceptionUtil.appendStackTrace(new RuntimeException(), b, null, 2);
    assertTrue(b.length() > 0);
  }

  /**
   * Test method for
   * {@link de.braintags.io.vertx.util.ExceptionUtil#appendStackTrace(java.lang.Throwable, java.lang.StringBuilder)}.
   */
  @Test
  public void testAppendStackTraceThrowableStringBuilder() {
    StringBuilder b = new StringBuilder();
    ExceptionUtil.appendStackTrace(new RuntimeException(), b);
    assertTrue(b.length() > 0);
  }

}
