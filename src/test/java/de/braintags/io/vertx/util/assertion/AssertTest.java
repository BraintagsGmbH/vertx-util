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
package de.braintags.io.vertx.util.assertion;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class AssertTest {

  /**
   * Test method for {@link de.braintags.io.vertx.util.assertion.Assert#notNull(java.lang.String, java.lang.Object)}.
   */
  @Test
  public void testNotNull() {
    de.braintags.io.vertx.util.assertion.Assert.notNull("testname", new Object());
    // no exception - good
    try {
      de.braintags.io.vertx.util.assertion.Assert.notNull("testname", null);
      fail("Exception expected");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.assertion.Assert#isTrueArgument(java.lang.String, boolean)}.
   */
  @Test
  public void testIsTrueArgument() {
    de.braintags.io.vertx.util.assertion.Assert.isTrueArgument("must be true", true);
    // no exception - good
    try {
      de.braintags.io.vertx.util.assertion.Assert.isTrueArgument("must be true", false);
      fail("Exception expected");
    } catch (IllegalArgumentException e) {
      // expected
    }

  }

}
