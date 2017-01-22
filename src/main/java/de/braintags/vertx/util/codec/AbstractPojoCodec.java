/*-
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
package de.braintags.vertx.util.codec;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

/**
 * An abstract implementation of {@link MessageCodec} which transforms POJOs into json for transport
 * 
 * @author Michael Remme
 * 
 */
public abstract class AbstractPojoCodec<S, R> implements MessageCodec<S, R> {

  @Override
  public void encodeToWire(Buffer buffer, S s) {
    String jo = Json.encode(s);
    byte[] encoded = jo.getBytes(CharsetUtil.UTF_8);
    buffer.appendInt(encoded.length);
    Buffer buff = Buffer.buffer(encoded);
    buffer.appendBuffer(buff);
  }

  @Override
  public R decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    pos += 4;
    byte[] encoded = buffer.getBytes(pos, pos + length);
    String str = new String(encoded, CharsetUtil.UTF_8);
    return Json.decodeValue(str, getInstanceClass());
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }

  @Override
  public String name() {
    return getInstanceClass().getSimpleName();
  }

  protected abstract Class<R> getInstanceClass();

}
