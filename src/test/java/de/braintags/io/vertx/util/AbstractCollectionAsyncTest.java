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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.braintags.io.vertx.BtVertxTestBase;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class AbstractCollectionAsyncTest extends BtVertxTestBase {

  /**
   * Test method for {@link de.braintags.io.vertx.util.AbstractCollectionAsync#AbstractCollectionAsync()}.
   */
  @Test
  public void testAbstractCollectionAsync() {
    AC ac = new AC();
    assertFalse(ac.isEmpty());
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.AbstractCollectionAsync#isEmpty()}.
   */
  @Test
  public void testIsEmpty() {
    AC ac = new AC();
    assertFalse(ac.isEmpty());
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.AbstractCollectionAsync#add(java.lang.Object)}.
   */
  @Test
  public void testAdd() {
    AC ac = new AC();
    try {
      ac.add("teststring");
      fail("should no tbe supported");
    } catch (UnsupportedOperationException e) {
      // correct
    }
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.AbstractCollectionAsync#remove(java.lang.Object)}.
   */
  @Test
  public void testRemove() {
    AC ac = new AC();
    try {
      ac.remove("testString");
      fail("should no tbe supported");
    } catch (UnsupportedOperationException e) {
      // correct
    }
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.AbstractCollectionAsync#clear()}.
   */
  @Test
  public void testClear() {
    AC ac = new AC();
    try {
      ac.clear();
      fail("should no tbe supported");
    } catch (UnsupportedOperationException e) {
      // correct
    }
  }

  class AC extends AbstractCollectionAsync<String> {

    /*
     * (non-Javadoc)
     * 
     * @see de.braintags.io.vertx.util.CollectionAsync#size()
     */
    @Override
    public int size() {
      return 5;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.braintags.io.vertx.util.CollectionAsync#iterator()
     */
    @Override
    public IteratorAsync<String> iterator() {
      return null;
    }
  }
}
