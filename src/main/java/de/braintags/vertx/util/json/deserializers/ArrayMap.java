package de.braintags.vertx.util.json.deserializers;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;

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
public final class ArrayMap<K, V> extends LinkedHashMap<K, V> {

  @JsonCreator
  public ArrayMap() {
  }

  public ArrayMap(final Map<? extends K, ? extends V> source) {
    super(source);
  }

}
