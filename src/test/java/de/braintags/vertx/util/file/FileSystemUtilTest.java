/*
 * #%L
 * Vert.x utilities from Braintags
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.braintags.vertx.BtVertxTestBase;
import de.braintags.vertx.util.exception.NoSuchFileException;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.test.core.TestUtils;

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

  @Test
  public void testStoreFile(TestContext context) throws Exception {
    String directory = "tmpDir";
    String fileName = "somefile.dat";
    Buffer fileData = TestUtils.randomBuffer(50);
    if (vertx.fileSystem().existsBlocking(directory)) {
      vertx.fileSystem().deleteBlocking(directory);
      assertFalse("Directory was not deleted", vertx.fileSystem().existsBlocking(directory));
    }

    Async async1 = context.async();
    FileSystemUtil.storeFile(vertx, directory, fileName, fileData, result -> {
      if (result.failed()) {
        context.fail(result.cause());
        async1.complete();
      } else {
        context.assertEquals(fileName, result.result(), "filename should not be changed here");
        async1.complete();
      }
    });
    async1.await();

    Async async = context.async();
    FileSystemUtil.storeFile(vertx, directory, fileName, fileData, result -> {
      if (result.failed()) {
        context.fail(result.cause());
        async.complete();
      } else {
        context.assertNotEquals(fileName, result.result(), "filename should be changed here");
        async.complete();
      }
    });
    async.await();

  }

  @Test
  public void testCheckDirectory(TestContext context) throws Exception {
    String directory = "tmpDir";
    if (vertx.fileSystem().existsBlocking(directory)) {
      vertx.fileSystem().deleteRecursiveBlocking(directory, true);
      assertFalse("Directory was not deleted", vertx.fileSystem().existsBlocking(directory));
    }
    FileSystemUtil.checkDirectory(vertx, directory);
    assertTrue("Directory was not created", vertx.fileSystem().existsBlocking(directory));
    assertTrue("Must return true for a directory", FileSystemUtil.isDirectory(vertx, directory));

  }

  /**
   * Test method for
   * {@link de.braintags.vertx.util.file.FileSystemUtil#getChildren(io.vertx.core.Vertx, java.lang.String, boolean, de.braintags.vertx.util.file.FileSystemUtil.Filter)}
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
   * {@link de.braintags.vertx.util.file.FileSystemUtil#isDirectory(io.vertx.core.Vertx, java.lang.String)}.
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
