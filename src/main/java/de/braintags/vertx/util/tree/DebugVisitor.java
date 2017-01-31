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

import io.vertx.core.buffer.Buffer;

/**
 * Creates a String based output of the tree
 * 
 * @author Michael Remme
 * 
 */
public class DebugVisitor implements ITreeVisitor<String, String> {
  private Buffer buffer = Buffer.buffer();
  int index = 0;

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.util.tree.ITreeVisitor#startNode(de.braintags.vertx.util.tree.Node)
   */
  @Override
  public void startNode(Node<String> node) {
    for (int i = 0; i <= index; i++) {
      buffer.appendString("-");
    }
    buffer.appendString(" " + node.getName() + "\n");
    index++;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.util.tree.ITreeVisitor#finishNode(de.braintags.vertx.util.tree.Node)
   */
  @Override
  public void finishNode(Node<String> node) {
    index--;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.util.tree.ITreeVisitor#startLeaf(java.lang.Object)
   */
  @Override
  public void startLeaf(String leaf) {
    for (int i = 0; i <= index; i++) {
      buffer.appendString(" ");
    }
    buffer.appendString(leaf + "\n");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.util.tree.ITreeVisitor#getResult()
   */
  @Override
  public String getResult() {
    return buffer.toString();
  }

}
