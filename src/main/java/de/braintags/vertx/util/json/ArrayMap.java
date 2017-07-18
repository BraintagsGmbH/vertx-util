package de.braintags.vertx.util.json;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ArrayMap<K, V> extends LinkedHashMap<K, V> {

  @JsonCreator
  public ArrayMap() {
  }

  public ArrayMap(Map<? extends K, ? extends V> source) {
    super(source);
  }
}
