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

import java.util.ArrayList;
import java.util.List;

import de.braintags.io.vertx.util.exception.NoSuchFileException;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

/**
 * Utilities with {@link FileSystem}
 * 
 * @author Michael Remme
 * 
 */
public class FileSystemUtil {

  private FileSystemUtil() {
    // hidden
  }

  /**
   * Get a list of filenames, which are fitting the criteria defined by the given filter. The same than
   * getChildren(vertx, directory, true, filter)
   * 
   * @param vertx
   *          the instance if vertx
   * @param directory
   *          the path to the directory to be used
   * @param filter
   *          the filter to be applied on found files
   * @return a list with fitting file paths relative to the directory
   * @throws NoSuchFileException
   */
  public static List<String> getChildren(Vertx vertx, String directory, Filter filter) throws NoSuchFileException {
    return getChildren(vertx, directory, true, filter);
  }

  /**
   * Get a list of filenames, which are fitting the criteria defined by the given filter
   * 
   * @param vertx
   *          the instance if vertx
   * @param directory
   *          the path to the directory to be used
   * @param recursive
   *          traverse deep?
   * @param filter
   *          the filter to be applied on found files
   * @return a list with fitting file paths relative to the directory
   * @throws NoSuchFileException
   */
  public static List<String> getChildren(Vertx vertx, String directory, boolean recursive, Filter filter)
      throws NoSuchFileException {
    List<String> returnList = new ArrayList<String>();
    if (isDirectory(vertx, directory)) {
      List<String> children = vertx.fileSystem().readDirBlocking(directory);
      for (String child : children) {
        int index = child.lastIndexOf('/');
        if (index > 0) {
          child = child.substring(index + 1);
        }
        String childPath = directory + "/" + child;
        if (isDirectory(vertx, childPath)) {
          if (recursive) {
            returnList.addAll(getChildren(vertx, childPath, recursive, filter));
          }
        } else if (filter == null || filter.accept(child)) {
          returnList.add(childPath);
        }
      }
    }
    return returnList;
  }

  /**
   * Is the given path a directory?
   * 
   * @param vertx
   * @param path
   * @return true, if a directory
   * @throws NoSuchFileException
   *           if the file does not exist
   */
  public static boolean isDirectory(Vertx vertx, String path) throws NoSuchFileException {
    if (vertx.fileSystem().existsBlocking(path)) {
      return vertx.fileSystem().lpropsBlocking(path).isDirectory();
    } else {
      throw new NoSuchFileException(path);
    }
  }

  /**
   * Filter decides, wether a file with a given name shall be used
   * 
   * @author Michael Remme
   *
   */
  @FunctionalInterface
  public interface Filter {

    public boolean accept(String filename);
  }

}
