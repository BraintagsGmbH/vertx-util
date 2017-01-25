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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class Node<T> {
  private Tree<T> tree;
  private Node<T> parentNode;
  private String name;
  private List<T> values = new ArrayList<>();
  private List<Node<T>> childNodes = new ArrayList<>();

  /**
   * Creates a new node
   * 
   * @param parentNode
   *          the parent node, which may be null for a root node
   * @param name
   *          the name of the node
   */
  Node(Tree<T> tree, Node<T> parentNode, String name) {
    this.tree = tree;
    this.parentNode = parentNode;
    this.name = name;
  }

  /**
   * Add a new entry as value of the node
   * 
   * @param value
   */
  public void addValue(T value) {
    if (!values.contains(value)) {
      values.add(value);
    }
  }

  /**
   * Get the first node with the fitting name
   * 
   * @param nodeName
   * @return a fitting node with the given name or NULL, if none found
   */
  public Node<T> getChildNode(String nodeName) {
    return childNodes.stream().filter(n -> n.getName().equals(nodeName)).findFirst().orElse(null);
  }

  /**
   * Get the first node with the fitting name. If none is found, it will be created
   * 
   * @param nodeName
   * @return a found or created child node
   */
  public Node<T> getOrCreateChildNode(String nodeName) {
    return childNodes.stream().filter(n -> n.getName().equals(nodeName)).findFirst()
        .orElseGet(() -> addChildNode(nodeName));
  }

  private Node<T> addChildNode(String newNodeName) {
    Node<T> n = tree.createNode(newNodeName, this);
    childNodes.add(n);
    return n;
  }

  /**
   * @return the values
   */
  public Collection<T> getValues() {
    return values;
  }

  /**
   * The pure name of the current node
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the parentNode
   */
  public Node<T> getParentNode() {
    return parentNode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("/" + name);
    Node<T> parent = getParentNode();
    while (parent != null) {
      sb.insert(0, "/" + parent.name);
      parent = parent.getParentNode();
    }
    return sb.toString();
  }

}
