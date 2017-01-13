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
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

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

  /**
   * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getDeclaredAndInheritedMethods(java.lang.Class)}.
   */
  @Test
  public void testGetDeclaredAndInheritedMethods() {
    List<Method> methods = ClassUtil.getDeclaredAndInheritedMethods(String.class);
    assertNotNull(methods);
    assertTrue(!methods.isEmpty());
  }

  /**
   * Test method for
   * {@link de.braintags.io.vertx.util.ClassUtil#getTypeArgument(java.lang.Class, java.lang.reflect.TypeVariable)}.
   */
  @Test
  public void testGetTypeArgument() throws Exception {
    Field field = Super1.class.getDeclaredField("field");
    Class<?> fieldClass = field.getType();
    TypeVariable<?> tv = (TypeVariable<?>) field.getGenericType();
    Class<?> typeArgument = ClassUtil.getTypeArgument(Sub.class, tv);
    System.out.println("fieldClass: " + fieldClass);
    System.out.println("TypeVariable: " + tv);
    System.out.println("typeArgument: " + typeArgument);
    assertEquals("Wrong Result", Integer.class, typeArgument);
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getClass(java.lang.reflect.Type)}.
   */
  @Test
  public void testGetClassType() throws Exception {
    Field field = FieldClass.class.getDeclaredField("list");
    Class<?> fieldClass = field.getType();
    ParameterizedType pt = (ParameterizedType) field.getGenericType();
    Class<?> typeArgument = ClassUtil.getClass(pt);
    assertEquals("Wrong Result", List.class, typeArgument);
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ClassUtil#toClass(java.lang.reflect.Type)}.
   */
  @Test
  public void testToClass() throws Exception {
    Field field = FieldClass.class.getDeclaredField("list");
    Class<?> fieldClass = field.getType();
    ParameterizedType pt = (ParameterizedType) field.getGenericType();
    final Type[] types = pt.getActualTypeArguments();
    Class realType = ClassUtil.toClass(pt);
    assertEquals("Wrong Result", List.class, realType);
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getParameterizedType(java.lang.reflect.Field, int)}.
   */
  @Test
  public void testGetParameterizedType() throws Exception {
    Field field = FieldClass.class.getDeclaredField("list");
    Type type = ClassUtil.getParameterizedType(field, 0);
    assertEquals("Wrong Result", String.class, type);
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getParameterizedClass(java.lang.Class)}.
   */
  @Test
  public void testGetParameterizedClassClassOfQ() {
    Class<?> cls = ClassUtil.getParameterizedClass(Sub.class);
    assertEquals("Wrong Result", Integer.class, cls);
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getParameterizedClass(java.lang.Class, int)}.
   */
  @Test
  public void testGetParameterizedClassClassOfQInt() {
    Class<?> cls = ClassUtil.getParameterizedClass(Sub.class, 0);
    assertEquals("Wrong Result", Integer.class, cls);
  }

  /**
   * Test method for {@link de.braintags.io.vertx.util.ClassUtil#getDeclaredField(java.lang.Class, java.lang.String)}.
   */
  @Test
  public void testGetDeclaredField() {
    Field field = ClassUtil.getDeclaredField(Sub.class, "field");
    assertNotNull(field);
  }

  private static class FieldClass {
    private List<String> list;
  }

  private static class Super1<T extends Object> {
    private T field;
  }

  private static class Super2<T extends Serializable> extends Super1<T> {
  }

  private static class Super3<T extends Number> extends Super2<T> {
  }

  private static class Sub extends Super3<Integer> {
  }

}
