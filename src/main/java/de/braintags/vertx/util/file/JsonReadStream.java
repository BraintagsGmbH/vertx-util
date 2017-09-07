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

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import de.braintags.vertx.util.ExceptionUtil;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;

/**
 * A {@link ReadStream} which takes a List of Objects and streams them as Json. Can be used as source for {@link Pump}
 * 
 * @author Michael Remme
 * @param <T>
 *          the type of the instances to be streamed
 */
public class JsonReadStream<T> implements ReadStream<Buffer> {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(JsonReadStream.class);

  private final AtomicBoolean paused = new AtomicBoolean(true);
  private final AtomicBoolean ended = new AtomicBoolean(false);
  private Handler<Throwable> exceptionHandler = new DefaultExceptionHandler();
  private Handler<Buffer> contentHandler;
  private Handler<Void> endHandler;
  private final Iterator<T> instances;
  private boolean pretty = false;
  private boolean array = true;
  private final AtomicBoolean firstElementWritten = new AtomicBoolean(false);

  /**
   * the same as this( instances, false, true )
   * 
   * @param instances
   */
  public JsonReadStream(final Iterable<T> instances) {
    this(instances, false, true);
  }

  /**
   * 
   * @param instances
   *          the instances to be streamed
   * @param pretty
   *          wether output shall be formatted json
   * @param array
   *          wether output shall be array. if false, then each entity is written into one line as json
   */
  public JsonReadStream(final Iterable<T> instances, final boolean pretty, final boolean array) {
    this.instances = instances.iterator();
    this.pretty = pretty;
    this.array = array;
  }

  @Override
  public ReadStream<Buffer> exceptionHandler(final Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  @Override
  public ReadStream<Buffer> handler(@Nullable final Handler<Buffer> handler) {
    this.contentHandler = handler;
    resume();
    return this;
  }

  private void next() {
    if (!paused.get()) {
      if (instances.hasNext()) {
        contentHandler.handle(createContent(instances.next()));
        next();
      } else {
        // mark as ended if the handler was registered too late
        ended.set(true);
        if (endHandler != null) {
          endHandler.handle(null);
        }
      }
    }
  }

  private Buffer createContent(final T instance) {
    try {
      boolean fst = firstElementWritten.getAndSet(true);
      Buffer b = Buffer.buffer();
      if (fst) {
        if (array) {
          b.appendString(",");
        }
        b.appendString("\n");
      } else { // we are writing the first element
        if (array) {
          b.appendString("[ ");
        }
      }

      b.appendString(pretty ? Json.encodePrettily(instance) : Json.encode(instance));
      if (array && !instances.hasNext()) {
        b.appendString(" ]");
      }
      return b;
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  private RuntimeException handleException(final Exception e) {
    if (exceptionHandler != null) {
      exceptionHandler.handle(e);
    }
    throw ExceptionUtil.createRuntimeException(e);
  }

  @Override
  public ReadStream<Buffer> pause() {
    paused.compareAndSet(false, true);
    return this;
  }

  @Override
  public ReadStream<Buffer> resume() {
    if (paused.compareAndSet(true, false)) {
      next();
    }
    return this;
  }

  @Override
  public ReadStream<Buffer> endHandler(@Nullable final Handler<Void> endHandler) {
    this.endHandler = endHandler;
    // registration was late but we're already ended, notify
    if (ended.compareAndSet(true, false) && endHandler != null) {
      // only notify once
      endHandler.handle(null);
    }
    return this;
  }

  class DefaultExceptionHandler implements Handler<Throwable> {

    @Override
    public void handle(final Throwable t) {
      LOGGER.error("", t);
      throw ExceptionUtil.createRuntimeException(t);
    }

  }

}
