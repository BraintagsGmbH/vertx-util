package de.braintags.vertx.util.freezable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public final class Freezables {

  private Freezables() {
    // noop
  }

  public static <T> FreezableList<T> emptyList() {
    return emptyList(false);
  }

  public static <T> FreezableList<T> emptyList(final boolean frozen) {
    FreezableListImpl<T> freezableList = new FreezableListImpl<>(Collections.<T> emptyList(), null);
    if (frozen)
      freezableList.freeze();
    return freezableList;
  }

  public static <T> FreezableList<T> freezableList(final List<T> list) {
    return freezableList(false, list);
  }

  public static <T> FreezableList<T> freezableList(final boolean frozen, final List<T> list) {
    FreezableListImpl<T> freezableList = new FreezableListImpl<>(list, null);
    if (frozen)
      freezableList.freeze();
    return freezableList;
  }

  public static <T> FreezableList<T> freezableList(final int initialCapacity) {
    return new FreezableListImpl<>(new ArrayList<>(initialCapacity), null);
  }

  public static <T> FreezableList<T> freezableList() {
    return new FreezableListImpl<>(new ArrayList<>(), null);
  }

  @SafeVarargs
  public static <T> FreezableList<T> freezableList(final T... vs) {
    return freezableList(false, vs);
  }

  @SafeVarargs
  public static <T> FreezableList<T> freezableList(final boolean frozen, final T... vs) {
    FreezableListImpl<T> freezableList;
    if (vs.length == 0) {
      freezableList = new FreezableListImpl<>(new ArrayList<>(), null);
    } else {
      freezableList = new FreezableListImpl<>(new ArrayList<>(Arrays.asList(vs)), null);
    }
    if (frozen)
      freezableList.freeze();
    return freezableList;
  }

  public static <T> FreezableSet<T> emptySet() {
    return emptySet(false);
  }

  public static <T> FreezableSet<T> emptySet(final boolean frozen) {
    FreezableSetImpl<T> freezableSet = new FreezableSetImpl<>(Collections.<T> emptySet(), null);
    if (frozen)
      freezableSet.freeze();
    return freezableSet;
  }

  public static <T> FreezableList<T> singletonList(final T value) {
    return singletonList(false, value);
  }

  public static <T> FreezableList<T> singletonList(final boolean frozen, final T value) {
    return freezableList(frozen, Collections.singletonList(value));
  }

  public static <T> FreezableSet<T> singletonSet(final T value) {
    return singletonSet(false, value);
  }

  public static <T> FreezableSet<T> singletonSet(final boolean frozen, final T value) {
    return freezableSet(frozen, Collections.singleton(value));
  }

  public static <T> FreezableSet<T> freezableSet() {
    return freezableSet(new LinkedHashSet<>());
  }

  public static <T> FreezableSet<T> freezableSet(final boolean frozen) {
    if (frozen) {
      return emptySet(true);
    }
    return freezableSet(false, new LinkedHashSet<>());
  }

  @SafeVarargs
  public static <T> FreezableSet<T> freezableSet(final T... vs) {
    return freezableSet(false, vs);
  }

  @SafeVarargs
  public static <T> FreezableSet<T> freezableSet(final boolean frozen, final T... vs) {
    if (vs.length == 0) {
      return freezableSet(frozen, new LinkedHashSet<>());
    } else {
      return freezableSet(frozen, new LinkedHashSet<>(Arrays.asList(vs)));
    }
  }

  public static <T> FreezableSet<T> freezableSet(final Set<T> s) {
    return freezableSet(false, s);
  }

  public static <T> FreezableSet<T> freezableSet(final boolean frozen, final Set<T> s) {
    if (s instanceof SortedSet) {
      return freezableSortedSet(frozen, (SortedSet<T>) s);
    }
    FreezableSetImpl<T> freezableSet = new FreezableSetImpl<>(s, null);
    if (frozen)
      freezableSet.freeze();
    return freezableSet;
  }

  @SafeVarargs
  public static <T> FreezableSortedSet<T> freezableSortedSet(final T... vs) {
    return freezableSortedSet(false, vs);
  }

  @SafeVarargs
  public static <T> FreezableSortedSet<T> freezableSortedSet(final boolean frozen, final T... vs) {
    if (vs.length == 0) {
      return freezableSortedSet(frozen, new TreeSet<>());
    } else {
      return freezableSortedSet(frozen, new TreeSet<>(Arrays.asList(vs)));
    }
  }

  public static <T> FreezableSortedSet<T> freezableSortedSet(final SortedSet<T> s) {
    return freezableSortedSet(false, s);
  }

  public static <T> FreezableSortedSet<T> freezableSortedSet(final boolean frozen, final SortedSet<T> s) {
    FreezableSortedSetImpl<T> freezableSortedSet = new FreezableSortedSetImpl<>(s, null);
    if (frozen)
      freezableSortedSet.freeze();
    return freezableSortedSet;
  }

  public static <K, V> FreezableMap<K, V> emptyMap() {
    return emptyMap(false);
  }

  public static <K, V> FreezableMap<K, V> emptyMap(final boolean frozen) {
    FreezableMapImpl<K, V> freezableMap = new FreezableMapImpl<>(Collections.<K, V> emptyMap(), null);
    if (frozen)
      freezableMap.freeze();
    return freezableMap;
  }

  public static <K, V> FreezableMap<K, V> freezableMap() {
    return freezableMap(false);
  }

  public static <K, V> FreezableMap<K, V> freezableMap(final boolean frozen) {
    FreezableMapImpl<K, V> freezableMap = new FreezableMapImpl<>(new LinkedHashMap<>(), null);
    if (frozen)
      freezableMap.freeze();
    return freezableMap;
  }

  public static <K, V> FreezableMap<K, V> freezableMap(final Map<K, V> m) {
    return freezableMap(false, m);
  }

  public static <K, V> FreezableMap<K, V> freezableMap(final boolean frozen, final Map<K, V> m) {
    if (m instanceof SortedMap) {
      return freezableSortedMap(frozen, (SortedMap<K, V>) m);
    }
    FreezableMapImpl<K, V> freezableMap = new FreezableMapImpl<>(m, null);
    if (frozen)
      freezableMap.freeze();
    return freezableMap;
  }

  public static <K, V> FreezableSortedMap<K, V> freezableSortedMap(final SortedMap<K, V> m) {
    return freezableSortedMap(false, m);
  }

  public static <K, V> FreezableSortedMap<K, V> freezableSortedMap(final boolean frozen, final SortedMap<K, V> m) {
    FreezableSortedMapImpl<K, V> freezableSortedMap = new FreezableSortedMapImpl<>(m, null);
    if (frozen)
      freezableSortedMap.freeze();
    return freezableSortedMap;
  }

  public static <K, V> FreezableMap<K, V> singletonMap(final K key, final V value) {
    return singletonMap(false, key, value);
  }

  public static <K, V> FreezableMap<K, V> singletonMap(final boolean frozen, final K key, final V value) {
    return freezableMap(frozen, Collections.singletonMap(key, value));
  }

  public static void checkFreezableObjects(final Collection<?> c) {
    for (Object o : c) {
      if (!(o instanceof Freezable || o instanceof Number || o instanceof String || o instanceof Boolean
          || o instanceof Class || o instanceof Enum)) {
        throw new FrozenException("object is not freezable " + o);
      }
    }
  }

}
