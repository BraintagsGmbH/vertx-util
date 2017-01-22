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
package de.braintags.vertx.util.codec;

public class UserObject {
  public String testString = "testString";
  public int count = 34;

  @Override
  public boolean equals(Object o) {
    UserObject compare = (UserObject) o;
    return compare.count == count && compare.testString.equals(testString);
  }
}
