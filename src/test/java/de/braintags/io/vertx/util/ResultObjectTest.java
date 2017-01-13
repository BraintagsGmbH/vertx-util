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
package de.braintags.io.vertx.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Handler;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class ResultObjectTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ResultObject#ResultObject(io.vertx.core.Handler)}.
   */
  @Test
  public void testResultObject() {
    ResultObject ro = new ResultObject<>(null);
    assertNull(ro.getResult());
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ResultObject#getResult()}.
   */
  @Test
  public void testGetResult() {
    ResultObject ro = new ResultObject<>(null);
    ro.setResult(new Object());
    assertNotNull(ro.getResult());
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ResultObject#setResult(java.lang.Object)}.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void testSetResult() {
    StringBuffer buffer = new StringBuffer();
    ResultObject ro = new ResultObject<>(new Handler() {

      @Override
      public void handle(Object event) {
        buffer.append("test");
      }
    });
    ro.setResult(new Object());
    assertNotNull(ro.getResult());
    assertEquals("test", buffer.toString());
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ResultObject#isResultDefined()}.
   */
  @Test
  public void testIsResultDefined() {
    ResultObject ro = new ResultObject<>(null);
    ro.setResult(new Object());
    assertTrue(ro.isResultDefined());
  }

}
