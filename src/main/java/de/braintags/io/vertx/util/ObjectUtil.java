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

import java.util.Objects;

/**
 * Helper class on objects
 * 
 * @author mremme
 * 
 */
public class ObjectUtil {

  private ObjectUtil() {
  }

  /**
   * Compares two objects by respecting null
   * 
   * @param o1
   *          compare object 1
   * @param o2
   *          compare object 2
   * @return true, if one object is null and not the other or if they are not equal
   */
  public static boolean isEqual(Object o1, Object o2) {
    return Objects.equals(o1, o2);
  }

}
