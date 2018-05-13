package de.braintags.vertx.util.freezable;

import java.util.Comparator;
import java.util.SortedMap;

public class FreezableSortedMapImpl<K, V>
    extends FreezableMapImpl<K, V> implements FreezableSortedMap<K, V> {

  private final SortedMap<K, V> sm;

  FreezableSortedMapImpl(final SortedMap<K, V> m, final Freezable<?> parent) {
    super(m, parent);
    sm = m;
  }

  @Override
  public Comparator<? super K> comparator() {
    return sm.comparator();
  }

  @Override
  public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
    return new FreezableSortedMapImpl<>(sm.subMap(fromKey, toKey), this);
  }

  @Override
  public SortedMap<K, V> headMap(final K toKey) {
    return new FreezableSortedMapImpl<>(sm.headMap(toKey), this);
  }

  @Override
  public SortedMap<K, V> tailMap(final K fromKey) {
    return new FreezableSortedMapImpl<>(sm.tailMap(fromKey), this);
  }

  @Override
  public K firstKey() {
    return sm.firstKey();
  }

  @Override
  public K lastKey() {
    return sm.lastKey();
  }

}
