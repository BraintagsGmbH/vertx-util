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

import de.braintags.vertx.util.exception.DuplicateObjectException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 
 * 
 * @author Michael Remme
 * @param T
 *          the value type of containig leafs
 */
public class Node<T> extends Child<T> {
  private Tree<T> tree;
  private String name;
  private List<Leaf<T>> values = new ArrayList<>();
  private List<Node<T>> childNodes = new ArrayList<>();

  /**
   * Creates a new node
   * 
   * @param parentNode
   *          the parent node, which may be null for a root node
   * @param name
   *          the name of the node
   */
  protected Node(Tree<T> tree, Node<T> parentNode, String name) {
    super(parentNode);
    this.tree = tree;
    this.name = name;
  }

  /**
   * Let the visitor traverse the tree and collect or generate some data
   * 
   * @param visitor
   */
  public void visit(ITreeVisitor<?, T> visitor) {
    visitor.startNode(this);
    int counter = 0;
    for (Node<T> child : childNodes) {
      child.setCurrentIndex(counter++);
      child.visit(visitor);
    }
    for (Leaf<T> leaf : values) {
      leaf.setCurrentIndex(counter++);
      visitor.startLeaf(leaf);
    }
    visitor.finishNode(this);
  }

  /**
   * Add a new entry as value of the node
   * 
   * @param value
   */
  public void addValue(T value) {
    Leaf<T> leaf = createLeaf(value);
    if (!values.contains(leaf)) {
      values.add(leaf);
    } else {
      throw new DuplicateObjectException("value already inside node " + toString() + ": " + value);
    }
  }

  /**
   * Creates an instance of {@link Leaf} with the given value
   * 
   * @param value
   * @return
   */
  public Leaf<T> createLeaf(T value) {
    return new Leaf<>(value, this);
  }

  /**
   * Get the child nodes of the current node
   * 
   * @return
   */
  protected List<Node<T>> getChildNodes() {
    return childNodes;
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
    List<T> list = new ArrayList<>();
    values.stream().forEach(leaf -> list.add(leaf.getValue()));
    return list;
  }

  /**
   * @return the values
   */
  public Collection<Leaf<T>> getLeafs() {
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

  /**
   * Export the given node into Json
   * 
   * @return
   */
  public JsonObject toJson() {
    JsonObject jo = new JsonObject();
    jo.put("name", getName());
    jo.put("path", toString());
    JsonArray arr = new JsonArray();
    for (Node<T> child : childNodes) {
      arr.add(child.toJson());
    }
    for (Leaf<T> t : values) {
      arr.add(t.getValue());
    }
    jo.put("children", arr);
    return jo;
  }

  /**
   * Get the number of alle children ( nodes and leafs )
   * 
   * @return
   */
  public int getCompleteSize() {
    return childNodes.size() + values.size();
  }
}
