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
 * An extension of {@link Tree}, where the path elements are specified in a path like form "/category/subCategory"
 * 
 * @author Michael Remme
 * 
 */
public class PathTree<T> extends Tree<T> {

  /**
   * Add a value at the given path
   * 
   * @param path
   *          a path declaration like "cat/subcat/"
   * @param value
   *          the value to be added
   * @return the node, which was added or found
   */
  public Node<T> add(String path, T value) {
    return add(transform(path), value);
  }

  /**
   * get a node which is fittong the given path
   * 
   * @param path
   *          a path declaration like "cat/subcat/"
   * @return a fitting Node or null
   */
  public Node<T> getNode(String path) {
    return getNode(transform(path));
  }

  private String[] transform(String path) {
    if (path == null || path.trim().isEmpty() || path.trim().equals("/")) {
      return new String[] {};
    } else {
      String tmpPath = path.startsWith("/") ? path.substring(1) : path;
      return tmpPath.split("/");
    }
  }

}
