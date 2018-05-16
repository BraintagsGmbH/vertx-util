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
package de.braintags.vertx.util.json;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import de.braintags.vertx.util.json.VertxCodecs.InstantSerializer;
import de.braintags.vertx.util.json.VertxCodecs.JsonArraySerializer;
import de.braintags.vertx.util.json.VertxCodecs.JsonObjectSerializer;
import de.braintags.vertx.util.json.deserializers.ArrayMapModule;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonConfig {

  private static final List<WeakReference<ObjectMapper>> objectMappers;
  private static final List<Config> configs;

  static {
    objectMappers = new ArrayList<>();
    configs = new ArrayList<>();
    // add default configuration for all mappers
    addConfig(mapper -> {
      if (mapper != Json.mapper && mapper != Json.prettyMapper) {
        SimpleModule module = new SimpleModule();

        // custom types
        module.addSerializer(JsonObject.class, new JsonObjectSerializer());
        module.addSerializer(JsonArray.class, new JsonArraySerializer());
        // he have 2 extensions: RFC-7493
        module.addSerializer(Instant.class, new InstantSerializer());
        module.addSerializer(byte[].class, new ByteArraySerializer());

        mapper.registerModule(module);
      }

      mapper.registerModule(new ParameterNamesModule(Mode.DELEGATING));
      mapper.registerModule(new JodaModule());
      mapper.registerModule(new GuavaModule());
      mapper.registerModule(new ArrayMapModule());
    });

    configureObjectMapper(Json.mapper);
    configureObjectMapper(Json.prettyMapper);
  }

  public static void staticInit() {

  }

  public static void configureObjectMapper(final ObjectMapper mapper) {
    synchronized (configs) {
      for (WeakReference<ObjectMapper> objectMapper : objectMappers) {
        if (objectMapper.get() == mapper) {
          return;
        }
      }

      objectMappers.add(new WeakReference<>(mapper));
      for (Config config : new ArrayList<>(configs)) {
        config.configure(mapper);
      }
    }
  }

  public static void addConfig(final Config config) {
    synchronized (configs) {
      if (configs.contains(config)) {
        return;
      }

      ListIterator<WeakReference<ObjectMapper>> iter = new ArrayList<>(objectMappers).listIterator();
      while (iter.hasNext()) {
        WeakReference<ObjectMapper> objectMapperRef = iter.next();
        ObjectMapper objectMapper = objectMapperRef.get();
        if (objectMapper == null) {
          iter.remove();
        } else {
          config.configure(objectMapper);
        }
      }
      configs.add(config);
    }
  }

  @FunctionalInterface
  public interface Config {
    void configure(ObjectMapper mapper);
  }

}
