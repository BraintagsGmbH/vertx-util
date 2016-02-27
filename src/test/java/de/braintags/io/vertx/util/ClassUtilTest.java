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

import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class ClassUtilTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getConstructor(java.lang.Class, java.lang.Class<?>[])}.
   */
  @Test
  public void testGetConstructor() {
    Constructor constr = ClassUtil.getConstructor(StringBuffer.class, String.class);
    assertNotNull(constr);
  }

  // /**
  // * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getDeclaredAndInheritedMethods(java.lang.Class)}.
  // */
  // @Test
  // public void testGetDeclaredAndInheritedMethods() {
  // fail("Not yet implemented");
  // }
  //
  // /**
  // * Test method for
  // * {@link de.braintags.io.vertx.util.ClassUtil#getTypeArgument(java.lang.Class, java.lang.reflect.TypeVariable)}.
  // */
  // @Test
  // public void testGetTypeArgument() {
  // fail("Not yet implemented");
  // }
  //
  // /**
  // * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getClass(java.lang.reflect.Type)}.
  // */
  // @Test
  // public void testGetClassType() {
  // fail("Not yet implemented");
  // }
  //
  // /**
  // * Test method for {@link de.braintags.io.vertx.util.ClassUtil#toClass(java.lang.reflect.Type)}.
  // */
  // @Test
  // public void testToClass() {
  // fail("Not yet implemented");
  // }
  //
  // /**
  // * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getParameterizedType(java.lang.reflect.Field, int)}.
  // */
  // @Test
  // public void testGetParameterizedType() {
  // fail("Not yet implemented");
  // }
  //
  // /**
  // * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getParameterizedClass(java.lang.Class)}.
  // */
  // @Test
  // public void testGetParameterizedClassClassOfQ() {
  // fail("Not yet implemented");
  // }
  //
  // /**
  // * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getParameterizedClass(java.lang.Class, int)}.
  // */
  // @Test
  // public void testGetParameterizedClassClassOfQInt() {
  // fail("Not yet implemented");
  // }
  //
  // /**
  // * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getDeclaredField(java.lang.Class, java.lang.String)}.
  // */
  // @Test
  // public void testGetDeclaredField() {
  // fail("Not yet implemented");
  // }

}
