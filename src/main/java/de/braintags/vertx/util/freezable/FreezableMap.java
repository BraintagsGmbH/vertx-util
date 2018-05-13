package de.braintags.vertx.util.freezable;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FreezableMapImpl.class)
public interface FreezableMap<K, V> extends Map<K, V>, Freezable<FreezableMap<K, V>> {

  @Override
  FreezableSet<Map.Entry<K, V>> entrySet();

  @Override
  FreezableCollection<V, ?> values();
  
}
