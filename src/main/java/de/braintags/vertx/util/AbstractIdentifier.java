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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Abstract class for simple identifiers
 * 
 * @author sschmitt
 *
 */
public abstract class AbstractIdentifier implements Comparable<AbstractIdentifier> {

  private String identifier;
  private int hashCode;

  /**
   * Create a new instance with an identifier. The identifier will be the only value used for serialization and
   * equality-check.
   * 
   * @param identifier
   *          the unique identifier
   */
  @JsonCreator
  protected AbstractIdentifier(final String identifier) {
    super();
    this.identifier = identifier;
    this.hashCode = identifier.hashCode();
  }

  /**
   * Get the unique identifier
   * 
   * @return the identifier
   */
  @JsonValue
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(AbstractIdentifier o) {
    return this.identifier.compareTo(o.identifier);
  }

}
