/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.util.file;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.braintags.io.vertx.BtVertxTestBase;
import de.braintags.io.vertx.util.exception.NoSuchFileException;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class FileSystemUtilTest extends BtVertxTestBase {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for
   * {@link de.braintags.io.vertx.util.file.FileSystemUtil#getChildren(io.vertx.core.Vertx, java.lang.String, boolean, de.braintags.io.vertx.util.file.FileSystemUtil.Filter)}
   * .
   */
  @Test
  public void testGetAllChildren() throws Exception {
    List<String> children = FileSystemUtil.getChildren(vertx, "testresources", null);
    Assert.assertEquals(6, children.size());
  }

  @Test
  public void testGetChildrenFirstLevel() throws Exception {
    List<String> children = FileSystemUtil.getChildren(vertx, "testresources/testDir", false, null);
    Assert.assertEquals(3, children.size());
  }

  /**
   * directory with only directory must return zer0
   * 
   * @throws Exception
   */
  @Test
  public void testGetChildrenOnlyDirs() throws Exception {
    List<String> children = FileSystemUtil.getChildren(vertx, "testresources", false, null);
    Assert.assertEquals(0, children.size());
  }

  @Test
  public void testGetChildrenFiltered() throws Exception {
    List<String> children = FileSystemUtil.getChildren(vertx, "testresources", true, child -> child.startsWith("f1"));
    Assert.assertEquals(4, children.size());
  }

  /**
   * Test method for
   * {@link de.braintags.io.vertx.util.file.FileSystemUtil#isDirectory(io.vertx.core.Vertx, java.lang.String)}.
   * 
   * @throws Exception
   */
  @Test
  public void testIsDirectory() throws Exception {
    Assert.assertTrue(FileSystemUtil.isDirectory(vertx, "testresources"));
    Assert.assertFalse(FileSystemUtil.isDirectory(vertx, "testresources/testDir/f1.txt"));
  }

  @Test
  public void testThrowsException() {
    try {
      FileSystemUtil.isDirectory(vertx, "testresources/doesNotExist.txt");
      Assert.fail("This test should throw an exception");
    } catch (NoSuchFileException e) {
      // expected result;
    }
  }

}
