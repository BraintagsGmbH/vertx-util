package de.braintags.vertx.util.json.deserializers;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.braintags.vertx.util.freezable.FreezableMapImpl;

/**
 * Just a marker class that is used in annotations.
 * Use <code>@JsonSerialize(as = ArrayMap.class)</code> and <code>@JsonDeserialize(as = ArrayMap.class)</code>
 * 
 * @author mpluecker
 *
 * @param <K>
 * @param <V>
 */
@JsonAutoDetect(creatorVisibility = Visibility.ANY)
@JsonDeserialize(as = ArrayMap.class)
public final class ArrayMap<K, V> extends FreezableMapImpl<K, V> {

  @JsonCreator
  public ArrayMap() {
  }

  public ArrayMap(final Map<K, V> source) {
    super(source, null);
  }

  @Override
  public V put(final K key, final V value) {
    if (value == null) {
      return remove(key);
    }
    return super.put(key, value);
  }
}
