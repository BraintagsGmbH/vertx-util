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
import java.io.OutputStream;

import io.vertx.core.buffer.Buffer;

/**
 * An implementation of OutputStream using a {@link Buffer}
 * 
 * @author Michael Remme
 * 
 */
public class BufferOutputStream extends OutputStream {
  private Buffer buffer;

  /**
   * creates a new internal Buffer
   */
  public BufferOutputStream() {
    this(Buffer.buffer());
  }

  /**
   * instance with given buffer
   * 
   * @param buffer
   */
  public BufferOutputStream(Buffer buffer) {
    this.buffer = buffer;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int b) throws IOException {
    buffer.appendByte((byte) b);
  }

  /**
   * @return the buffer
   */
  public final Buffer getBuffer() {
    return buffer;
  }

}
