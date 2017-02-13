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
package de.braintags.vertx.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.braintags.vertx.util.file.DirectoryFilter;
import de.braintags.vertx.util.file.ResourceUtil;
import io.vertx.core.buffer.Buffer;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class TestResourceUtil {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ObjectUtil#isEqual(java.lang.Object, java.lang.Object)}.
   */
  @Test
  public void testReadResource() {
    Buffer buffer = ResourceUtil.readToBuffer(getClass(), "testresource.txt");
    assertNotNull("resource not loaded, is null", buffer);
    assertTrue("resource not loaded", buffer.toString().contains("testcontent"));
  }

  /**
   * Test method for {@link de.braintags.vertx.util.ObjectUtil#isEqual(java.lang.Object, java.lang.Object)}.
   */
  @Test
  public void testNotExist() {
    Buffer buffer = ResourceUtil.readToBuffer(getClass(), "testresourceXXX.txt");
    assertNull("must be null", buffer);
  }

  /**
   * Read the sites, which are defined inside the theme directory
   * 
   * @return
   * @throws IOException
   */
  @Test
  public void testDirectoryFilter() {
    Path path = FileSystems.getDefault().getPath("src/test/");
    List<Path> files = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, DirectoryFilter.ACCEPT_ALL_DIRECTORIES)) {
      stream.forEach(cp -> files.add(cp));
    } catch (IOException x) {
      throw new DirectoryIteratorException(x);
    }
    assertFalse("files not read", files.isEmpty());
    Path rp = files.stream().filter(f -> f.getFileName().getFileName().toString().equals("resources")).findFirst()
        .get();
    assertNotNull("resource directory not found", rp);

  }

}
