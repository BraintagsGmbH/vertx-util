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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import io.vertx.core.json.Json;

public class JsonConfig {

  private static final List<WeakReference<ObjectMapper>> objectMappers = new ArrayList<>();
  private static final List<Config> configs = new ArrayList<>();

  static {
    // add default configuration for all mappers
    addConfig(mapper -> {
      mapper.registerModule(new ParameterNamesModule(Mode.DELEGATING));
      mapper.registerModule(new JodaModule());
      mapper.registerModule(new GuavaModule());
    });
    configureObjectMapper(Json.mapper);
    configureObjectMapper(Json.prettyMapper);
  }

  public static synchronized void configureObjectMapper(ObjectMapper mapper) {
    for (WeakReference<ObjectMapper> objectMapper : objectMappers) {
      if (objectMapper.get() == mapper) {
        return;
      }
    }

    objectMappers.add(new WeakReference<>(mapper));
    for (Config config : configs) {
      config.configure(mapper);
    }
  }

  public static synchronized void addConfig(Config config) {
    if (configs.contains(config)) {
      return;
    }

    ListIterator<WeakReference<ObjectMapper>> iter = objectMappers.listIterator();
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

  @FunctionalInterface
  public interface Config {
    void configure(ObjectMapper mapper);
  }
}
