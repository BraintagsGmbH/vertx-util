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
 * A tree implementation to store values in hirarchies
 * 
 * @author Michael Remme
 * 
 */
public class Tree<T> {
  /**
   * The name of the root node
   */
  public static final String ROOT_NODE_NAME = "root";
  private Node<T> rootNode = new Node<>(this, null, ROOT_NODE_NAME);

  /**
   * Adds a new value into the given path entries. For each path entry new child nodes are created if not existing
   * 
   * @param pathEntries
   *          the path entries in the hirarchical order
   * @param value
   *          the value to be added at the deepest node
   * @return the node, where the value was added
   */
  public Node<T> add(String[] pathElements, T value) {
    Node<T> cn = rootNode;
    if (pathElements != null) {
      for (String pe : pathElements) {
        cn = cn.getOrCreateChildNode(pe);
      }
    }
    cn.addValue(value);
    return cn;
  }

  /**
   * Get the node, which is fitting into the given path elements
   * 
   * @param pathEntries
   * @return a fitting Node or null, if not existing
   */
  public Node<T> getNode(String[] pathElements) {
    Node<T> cn = rootNode;
    if (pathElements != null) {
      for (String pe : pathElements) {
        cn = cn.getChildNode(pe);
        if (cn == null) {
          return null;
        }
      }
    }
    return cn;
  }

  /**
   * Create an instance of Node
   * 
   * @return a new instance of node
   */
  public Node<T> createNode(String nodeName, Node<T> parent) {
    return new Node<>(this, parent, nodeName);
  }

}
