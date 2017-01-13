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

import java.io.ByteArrayInputStream;

import io.vertx.core.buffer.Buffer;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class BufferInputStream extends ByteArrayInputStream {

  /**
   * Constructor creating a bridge
   * 
   * @param buffer
   *          the buffer to be filled
   */
  public BufferInputStream(Buffer buffer) {
    super(buffer.getBytes());
  }

}
