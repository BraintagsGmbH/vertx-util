/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2016 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.util.file;

import java.io.IOException;
import java.io.InputStream;

import io.vertx.core.buffer.Buffer;

/**
 * Some utility methods to read resources
 * 
 * @author Michael Remme
 * 
 */
public class ResourceUtil {

  private ResourceUtil() {
  }

  /**
   * Read the content of the resource, which is fitting with the given class
   * 
   * @param referenceClass
   * @param resource
   * @return
   */
  public static Buffer readToBuffer(Class referenceClass, String resource) {
    try {
      Buffer buffer = Buffer.buffer();
      try (InputStream in = referenceClass.getResourceAsStream(resource)) {
        if (in == null) {
          return null;
        }
        int read;
        byte[] data = new byte[4096];
        while ((read = in.read(data, 0, data.length)) != -1) {
          if (read == data.length) {
            buffer.appendBytes(data);
          } else {
            byte[] slice = new byte[read];
            System.arraycopy(data, 0, slice, 0, slice.length);
            buffer.appendBytes(slice);
          }
        }
      }
      return buffer;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

}
