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
 * An ITreeVisitor is used to traverse through a complete tree and to collect or generate some data like reqired
 * 
 * @author Michael Remme
 * 
 * @param F
 *          the type where inside the result is collected
 * @param T
 *          the type of a leaf of the underlaying tree
 */
public interface ITreeVisitor<F, T> {

  /**
   * A node is started
   * 
   * @param node
   */
  void startNode(Node<T> node);

  /**
   * A node is finished
   * 
   * @param node
   */
  void finishNode(Node<T> node);

  /**
   * A leaf ( a child from a node ) is called
   * 
   * @param leaf
   */
  void startLeaf(T leaf);

  /**
   * Get the result of the visit
   * 
   * @return
   */
  F getResult();
}
