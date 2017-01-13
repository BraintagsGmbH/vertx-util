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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class CounterObjectTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.CounterObject#CounterObject(int, io.vertx.core.Handler)}.
   */
  @Test
  public void testCounterObject() {
    CounterObject co = new CounterObject<>(5, null);
    assertEquals(co.getCount(), 5);
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.CounterObject#reduce()}.
   */
  @Test
  public void testReduce() {
    CounterObject co = new CounterObject<>(5, null);
    co.reduce();
    assertEquals(co.getCount(), 4);
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.CounterObject#isZero()}.
   */
  @Test
  public void testIsZero() {
    CounterObject co = new CounterObject<>(1, null);
    assertTrue(co.reduce());
    assertTrue(co.isZero());
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.CounterObject#getCount()}.
   */
  @Test
  public void testGetCount() {
    CounterObject co = new CounterObject<>(5, null);
    co.reduce();
    assertEquals(co.getCount(), 4);
  }

}
