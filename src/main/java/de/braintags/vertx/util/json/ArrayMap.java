package de.braintags.vertx.util.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class ArrayMap<K, V> extends LinkedHashMap<K, V> {

  public ArrayMap() {
  }

  public ArrayMap(Map<? extends K, ? extends V> source) {
    super(source);
  }
}
