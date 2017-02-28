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
package de.braintags.vertx.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.braintags.vertx.util.EqUtil;

public class EqUtilTest {

  /**
   * Test method for {@link de.braintags.vertx.util.EqUtil#eq(Object, Object)}.
   */
  @Test
  public void testEq() {
    String hello = "Hello";
    String world = " world!";
    String helloWorld = "Hello world!";

    assertFalse(EqUtil.eq(hello, world));
    assertFalse(EqUtil.eq(hello, null));
    assertFalse(EqUtil.eq(null, world));
    
    String o1 = hello + world;
    assertFalse(o1 == helloWorld);
    
    assertTrue(EqUtil.eq(null, null));
    assertTrue(EqUtil.eq(o1, helloWorld));
    assertTrue(EqUtil.eq(helloWorld, o1));
  }
  
}
