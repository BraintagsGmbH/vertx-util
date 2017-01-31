/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util.tree;

/**
 * A leaf is the value keeping object of a tree
 * 
 * @author Michael Remme
 * @param T
 *          the value type of a leaf
 */
public class Leaf<T> extends Child<T> {
  private T value;

  /**
   * Create a new instance with the given parent node
   * 
   * @param parentNode
   */
  public Leaf(Node<T> parentNode) {
    super(parentNode);
  }

  /**
   * Create a new instance with the given parent node and value
   * 
   * @param value
   * @param parentNode
   */
  public Leaf(T value, Node<T> parentNode) {
    this(parentNode);
    this.value = value;
  }

  /**
   * @return the value
   */
  public T getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(T value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
