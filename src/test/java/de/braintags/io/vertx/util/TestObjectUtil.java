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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author mremme
 * 
 */
public class TestObjectUtil {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ObjectUtil#isEqual(java.lang.Object, java.lang.Object)}.
   */
  @Test
  public void testIsEqual() {
    assertTrue(ObjectUtil.isEqual(null, null));
    assertFalse(ObjectUtil.isEqual(new Object(), null));
    assertFalse(ObjectUtil.isEqual(null, new Object()));
    assertFalse(ObjectUtil.isEqual(new Object(), new Object()));
    assertFalse(ObjectUtil.isEqual(new String(), new Object()));
    assertTrue(ObjectUtil.isEqual("test", "test"));
  }

}
