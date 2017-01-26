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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class TreeTest {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(TreeTest.class);

  static final String firstValue = "firstValue";
  static final String secondValue = "secondValue";

  @Test
  public void testPathTree() {
    PathTree<String> tree = new PathTree<>();
    String path1 = "/L1-1/L2-1/";
    String path2 = "L1-1/L2-2/";

    tree.add(path1, firstValue);
    tree.add(path2, secondValue);

    Node<String> node = tree.getNode(path1);
    assertNotNull(node);

    node = tree.getNode("");
    assertEquals(Tree.ROOT_NODE_NAME, node.getName());

    node = tree.getNode("gibbetnich");
    assertNull(node);

    node = tree.getNode((String) null);
    assertNotNull(node);
    assertEquals(Tree.ROOT_NODE_NAME, node.getName());

    node = tree.getNode(path1);
    assertTrue("value not contained", node.getValues().contains(firstValue));
    assertFalse("value is contained", node.getValues().contains(secondValue));

  }

  @Test
  public void testTreeToJson() {
    Tree<String> tree = new Tree<>();
    String[] cats1 = { "L1-1", "L2-1", "L3-1" };
    String[] cats2 = { "L1-1", "L2-2", "L3-2" };
    tree.add(cats1, firstValue);
    tree.add(cats2, secondValue);
    JsonObject json = tree.toJson();
    LOGGER.info(json.encodePrettily());

    // Tree<String> tree2 = Json.decodeValue(j, Tree.class);
    // Node<String> node = tree2.getNode(cats1);
    // assertNotNull(node);
    // LOGGER.info(node.toString());
    // assertTrue("to String must display a path", node.toString().contains("/"));
  }

  @Test
  public void testNodeToString() {
    Tree<String> tree = new Tree<>();
    String[] cats1 = { "L1-1", "L2-1", "L3-1" };
    String[] cats2 = { "L1-1", "L2-2", "L3-2" };
    tree.add(cats1, firstValue);
    tree.add(cats2, secondValue);

    Node<String> node = tree.getNode(cats1);
    assertNotNull(node);
    LOGGER.info(node.toString());
    assertTrue("to String must display a path", node.toString().contains("/"));
  }

  @Test
  public void testTree() {
    Tree<String> tree = new Tree<>();
    String[] cats1 = { "L1-1", "L2-1", "L3-1" };
    String[] cats2 = { "L1-1", "L2-2", "L3-2" };
    tree.add(cats1, firstValue);
    tree.add(cats2, secondValue);

    assertEquals(1, tree.getRootNode().getChildNodes().size());

    Node<String> node = tree.getNode(cats1);
    assertNotNull(node);

    node = tree.getNode(new String[] { "" });
    assertNull(node);

    node = tree.getNode(new String[] { "gibbetnich" });
    assertNull(node);

    node = tree.getNode(null);
    assertNotNull(node);
    assertEquals(Tree.ROOT_NODE_NAME, node.getName());

    node = tree.getNode(cats1);
    assertTrue("value not contained", node.getValues().contains(firstValue));
    assertFalse("value is contained", node.getValues().contains(secondValue));
  }

}
