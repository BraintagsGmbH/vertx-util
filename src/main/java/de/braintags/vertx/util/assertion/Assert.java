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
package de.braintags.vertx.util.assertion;

/**
 * static helper for assertions
 * 
 * @author Michael Remme
 * 
 */
public class Assert {

  private Assert() {
  }

  /**
   * Throw IllegalArgumentException if the value is null.
   *
   * @param name
   *          the parameter name
   * @param value
   *          the value that should not be null
   * @param <T>
   *          the value type
   * @return the value
   * @throws java.lang.IllegalArgumentException
   *           if value is null
   */
  public static <T> T notNull(final String name, final T value) {
    if (value == null) {
      throw new IllegalArgumentException(name + " can not be null");
    }
    return value;
  }

  /**
   * Throw IllegalArgumentException if the condition if false.
   *
   * @param name
   *          the name of the state that is being checked
   * @param condition
   *          the condition about the parameter to check
   * @throws java.lang.IllegalArgumentException
   *           if the condition is false
   */
  public static void isTrueArgument(final String name, final boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("state should be: " + name);
    }
  }
}
