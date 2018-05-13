package de.braintags.vertx.util.freezable;

import java.util.SortedMap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FreezableSortedMapImpl.class)
public interface FreezableSortedMap<K, V> extends SortedMap<K, V>, FreezableMap<K, V> {

}
