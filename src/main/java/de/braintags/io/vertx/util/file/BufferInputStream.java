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

import java.io.IOException;
import java.io.InputStream;

import io.vertx.core.buffer.Buffer;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class BufferInputStream extends InputStream {
  private Buffer buffer;
  /**
   * The index of the next character to read from the input stream buffer.
   *
   */
  protected int pos;

  /**
   * The number of valid characters in the input stream buffer.
   *
   */
  protected int count;

  /**
   * Constructor creating a bridge
   * 
   * @param buffer
   *          the buffer to be filled
   */
  public BufferInputStream(Buffer buffer) {
    this.buffer = buffer;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    return (pos < count) ? (buffer.getByte(pos++) & 0xFF) : -1;
  }

}
