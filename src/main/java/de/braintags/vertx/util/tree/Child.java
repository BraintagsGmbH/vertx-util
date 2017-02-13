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
 * 
 * 
 * @author Michael Remme
 * @param T
 *          the value type of containig nodes
 * 
 */
public class Child<T> {
  private Node<T> parentNode;
  private int currentIndex;

  /**
   * 
   */
  public Child(Node<T> parentNode) {
    this.parentNode = parentNode;
  }

  /**
   * This value is set, when a visitor is processed and marks the index of the current child of the parent node
   * 
   * @return the currentIndex
   */
  public int getCurrentIndex() {
    return currentIndex;
  }

  /**
   * @param currentIndex
   *          the currentIndex to set
   */
  public void setCurrentIndex(int currentIndex) {
    this.currentIndex = currentIndex;
  }

  /**
   * @return the parentNode
   */
  public Node<T> getParentNode() {
    return parentNode;
  }

  /**
   * Checks wether the current index is the last child element of the parent node
   * 
   * @return
   */
  public boolean isLastChild() {
    return currentIndex == getParentNode().getCompleteSize() - 1;
  }

}
