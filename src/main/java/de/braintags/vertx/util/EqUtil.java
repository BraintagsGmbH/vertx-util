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

import java.util.Objects;

public class EqUtil {

  /**
   * @deprecated use Objects.equals() instead
   */
  @Deprecated
  public static boolean eq(Object o1, Object o2) {
    return Objects.equals(o1, o2);
  }

}
