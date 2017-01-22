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

/**
 * <br>
 * <br>
 * Copyright: Copyright (c) 16.12.2016 <br>
 * Company: Braintags GmbH <br>
 *
 * @author jkerkenhoff
 *
 */

public class AbstractIdentifier {

  private String identifier;
  private int    hashCode;

  /**
   * @param identifier
   */
  public AbstractIdentifier(String identifier) {
    super();
    this.identifier = identifier;
    this.hashCode = identifier.hashCode();
  }

  /**
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return hashCode;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractIdentifier other = (AbstractIdentifier) obj;
    return identifier.equals(other.identifier);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return identifier;
  }

}
