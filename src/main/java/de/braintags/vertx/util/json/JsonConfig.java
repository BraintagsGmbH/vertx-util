package de.braintags.vertx.util.json;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.Json;

public class JsonConfig {

  private static final List<WeakReference<ObjectMapper>> objectMappers = new ArrayList<>();
  private static final List<Config> configs = new ArrayList<>();

  static {
    configureObjectMapper(Json.mapper);
    configureObjectMapper(Json.prettyMapper);
  }

  public static synchronized void configureObjectMapper(ObjectMapper mapper) {
    for (WeakReference<ObjectMapper> objectMapper : objectMappers) {
      if (objectMapper.get() == mapper) {
        return;
      }
    }

    objectMappers.add(new WeakReference<ObjectMapper>(mapper));
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
