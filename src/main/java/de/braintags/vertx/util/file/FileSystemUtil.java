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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.braintags.vertx.util.exception.NoSuchFileException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * Utilities with {@link FileSystem}
 * 
 * @author Michael Remme
 * 
 */
public class FileSystemUtil {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(FileSystemUtil.class);

  private FileSystemUtil() {
    // hidden
  }

  /**
   * This method writes the content of the Buffer into a file inside the given directory. If a file with the same name
   * already exists, then a unique filename is created and returned
   * 
   * @param vertx
   * @param directory
   *          the directory, where the file shall be stored
   * @param fileName
   *          the name of the file
   * @param data
   *          the data to be stored
   * @param handler
   *          the handler will be informed about the new filename
   */
  public static final void storeFile(Vertx vertx, String directory, String fileName, Buffer data,
      Handler<AsyncResult<String>> handler) {
    try {
      FileSystem fs = vertx.fileSystem();
      checkDirectory(vertx, directory);
      String newFileName = createUniqueName(fs, directory, fileName);
      String destination = directory + (directory.endsWith("/") ? "" : "/") + newFileName;
      fs.writeFile(destination, data, wr -> {
        if (wr.failed()) {
          handler.handle(Future.failedFuture(wr.cause()));
        } else {
          LOGGER.info("stored file into " + destination);
          handler.handle(Future.succeededFuture(newFileName));
        }
      });
    } catch (IOException e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  /**
   * Creates a unique file name based on a directory.
   * 
   * @param fs
   * @param upDir
   * @param fileInName
   * @return
   */
  public static String createUniqueName(FileSystem fs, String upDir, String fileInName) {
    final String fileName = cleanFileName(fileInName);
    String newFileName = fileName;
    int counter = 0;
    String path = createPath(upDir, fileName);
    while (fs.existsBlocking(path)) {
      LOGGER.info("file exists already: " + path);
      if (fileName.indexOf('.') >= 0) {
        newFileName = fileName.replaceFirst("\\.", "_" + counter++ + ".");
      } else {
        newFileName = fileName + "_" + counter++;
      }
      path = createPath(upDir, newFileName);
    }
    return newFileName;
  }

  private static String createPath(String upDir, String fileName) {
    return upDir + (upDir.endsWith("/") ? "" : "/") + fileName;
  }

  /**
   * Create a filename, where illegal characters are replaced against '_'
   * 
   * @param fileName
   * @return
   */
  public static String cleanFileName(String fName) {
    return fName.replaceAll("[\\\\/:*?\"<>|]", "_");
  }

  /**
   * Checks existence of the given path as directory. If it does not exist, it is created
   * 
   * @param vertx
   * @param directory
   * @throws IOException
   */
  public static final void checkDirectory(Vertx vertx, Path directory) throws IOException {
    checkDirectory(vertx, directory.toAbsolutePath().toString());
  }

  /**
   * Checks existence of the given path as directory. If it does not exist, it is created
   * 
   * @param vertx
   * @param directory
   * @throws IOException
   */
  public static final void checkDirectory(Vertx vertx, String directory) throws IOException {
    try {
      FileSystem fs = vertx.fileSystem();
      if (!fs.existsBlocking(directory)) {
        fs.mkdirsBlocking(directory);
      } else if (!isDirectory(vertx, directory)) {
        throw new IOException("File exists and is no directory: " + directory);
      }
    } catch (NoSuchFileException e) {
      // can not occur here
    }
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
    List<String> returnList = new ArrayList<>();
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

    /**
     * Decides wether a file shall be used or not
     * 
     * @param filename
     *          the file to examine
     * @return true if file shall be used
     */
    public boolean accept(String filename);
  }

}
