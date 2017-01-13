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
package de.braintags.io.vertx.util.file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A filter, which defines only directories to be used
 * 
 * @author Michael Remme
 * 
 */
public class DirectoryFilter implements DirectoryStream.Filter<Path> {
  public static final DirectoryFilter ACCEPT_ALL_DIRECTORIES = new DirectoryFilter();

  private DirectoryFilter() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.nio.file.DirectoryStream.Filter#accept(java.lang.Object)
   */
  @Override
  public boolean accept(Path path) throws IOException {
    return Files.isDirectory(path);
  }

}
