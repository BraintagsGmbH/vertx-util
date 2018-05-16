package de.braintags.vertx.util.freezable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FreezableMapImpl<K, V> extends
    Freezable.Skeleton<FreezableMap<K, V>> implements FreezableMap<K, V> {

  private final Map<K, V> m;
  private Freezable<?> parent;

  protected FreezableMapImpl() {
    m = new LinkedHashMap<>();
    parent = null;
  }

  protected FreezableMapImpl(final Map<K, V> m, final Freezable<?> parent) {
    this.parent = parent;
    if (m == null)
      throw new NullPointerException();
    this.m = m;
  }

  @Override
  public int size() {
    return m.size();
  }

  @Override
  public boolean isEmpty() {
    return m.isEmpty();
  }

  @Override
  public boolean containsKey(final Object key) {
    return m.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object val) {
    return m.containsValue(val);
  }

  @Override
  public V get(final Object key) {
    return m.get(key);
  }

  @Override
  public V put(final K key, final V value) {
    checkFrozen();
    return m.put(key, value);
  }

  @Override
  public V remove(final Object key) {
    checkFrozen();
    return m.remove(key);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    checkFrozen();
    this.m.putAll(m);
  }

  @Override
  public void clear() {
    checkFrozen();
    this.m.clear();
  }

  private transient FreezableSet<K> keySet = null;
  private transient FreezableSet<Map.Entry<K, V>> entrySet = null;
  private transient FreezableCollection<V, ?> values = null;

  @Override
  public Set<K> keySet() {
    if (keySet == null)
      keySet = new FreezableSetImpl<>(m.keySet(), this);
    return keySet;
  }

  @Override
  public FreezableSet<Map.Entry<K, V>> entrySet() {
    if (entrySet == null)
      entrySet = new FreezableEntrySet<>(m.entrySet(), this);
    return entrySet;
  }

  @Override
  public FreezableCollection<V, ?> values() {
    if (values == null)
      values = new FreezableCollectionImpl<>(m.values(), this);
    return values;
  }

  @Override
  public boolean equals(final Object o) {
    return o == this || m.equals(o);
  }

  @Override
  public int hashCode() {
    return m.hashCode();
  }

  @Override
  public String toString() {
    return m.toString();
  }

  @Override
  public boolean isFrozen() {
    return super.isFrozen() || (parent != null && parent.isFrozen());
  }

  @Override
  public void freeze() {
    super.freeze();
    for (Map.Entry<K, V> e : m.entrySet()) {
      K key = e.getKey();
      if (key instanceof Freezable) {
        ((Freezable<?>) key).freeze();
      }
      V value = e.getValue();
      if (value instanceof Freezable) {
        ((Freezable<?>) value).freeze();
      }
    }

    if (parent != null) {
      parent.freeze();
    }
  }

  @Override
  public void copyFrom(final FreezableMap<K, V> source) {
    this.m.clear();
    this.m.putAll(source);
    this.parent = null;
  }

  static class FreezableEntrySet<K, V>
      implements FreezableSet<Map.Entry<K, V>>,
      Set<Map.Entry<K, V>> {
    private final Set<java.util.Map.Entry<K, V>> c;
    private final Freezable<?> parent;

    FreezableEntrySet(final Set<Map.Entry<K, V>> c, final Freezable<?> parent) {
      this.c = c;
      this.parent = parent;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      final FreezableEntrySet<K, V> ref = this;
      return new Iterator<Map.Entry<K, V>>() {
        private final Iterator<Map.Entry<K, V>> i = c.iterator();

        @Override
        public boolean hasNext() {
          return i.hasNext();
        }

        @Override
        public FreezableEntry<K, V> next() {
          return new FreezableEntry<>(i.next(), ref);
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] toArray() {
      Object[] a = c.toArray();
      for (int i = 0; i < a.length; i++)
        a[i] = new FreezableEntry<>((Map.Entry<K, V>) a[i], this);
      return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(final T[] a) {
      // We don't pass a to c.toArray, to avoid window of
      // vulnerability wherein an unscrupulous multithreaded client
      // could get his hands on raw (unwrapped) Entries from c.
      Object[] arr = c.toArray(a.length == 0 ? a : Arrays.copyOf(a, 0));

      for (int i = 0; i < arr.length; i++)
        arr[i] = new FreezableEntry<>((Map.Entry<K, V>) arr[i], this);

      if (arr.length > a.length)
        return (T[]) arr;

      System.arraycopy(arr, 0, a, 0, arr.length);
      if (a.length > arr.length)
        a[arr.length] = null;
      return a;
    }

    /**
     * This method is overridden to protect the backing set against an
     * object with a nefarious equals function that senses that the
     * equality-candidate is Map.Entry and calls its setValue method.
     */
    @Override
    public boolean contains(final Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      return c.contains(o);
    }

    /**
     * The next two methods are overridden to protect against an
     * unscrupulous List whose contains(Object o) method senses when o is a
     * Map.Entry, and calls o.setValue.
     */
    @Override
    public boolean containsAll(final Collection<?> coll) {
      for (Object e : coll) {
        if (!contains(e)) // Invokes safe contains() above
          return false;
      }
      return true;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this)
        return true;

      if (!(o instanceof Set))
        return false;
      Set<?> s = (Set<?>) o;
      if (s.size() != c.size())
        return false;
      return containsAll(s); // Invokes safe containsAll() above
    }

    @Override
    public int size() {
      return c.size();
    }

    @Override
    public boolean isEmpty() {
      return c.isEmpty();
    }

    @Override
    public boolean add(final java.util.Map.Entry<K, V> e) {
      checkFrozen();
      return c.add(e);
    }

    @Override
    public boolean remove(final Object o) {
      checkFrozen();
      return c.remove(o);
    }

    @Override
    public boolean addAll(final Collection<? extends java.util.Map.Entry<K, V>> c) {
      checkFrozen();
      return this.c.addAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      checkFrozen();
      return this.c.retainAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      checkFrozen();
      return this.c.removeAll(c);
    }

    @Override
    public void clear() {
      checkFrozen();
      this.c.clear();
    }

    @Override
    public void freeze() {
      parent.freeze();
    }

    @Override
    public boolean isFrozen() {
      return parent.isFrozen();
    }

    protected void checkFrozen() {
      if (isFrozen()) {
        throw new FrozenException("frozen");
      }
    }

    @Override
    public FreezableSet<java.util.Map.Entry<K, V>> copy() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void copyFrom(final FreezableSet<java.util.Map.Entry<K, V>> source) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FreezableSet<java.util.Map.Entry<K, V>> unfreeze() {
      throw new UnsupportedOperationException();
    }
  }

  private static class FreezableEntry<K, V> implements Map.Entry<K, V>,
      Freezable<FreezableEntry<K, V>> {
    private final Map.Entry<K, V> e;
    private final Freezable<?> parent;

    FreezableEntry(final Map.Entry<K, V> e, final Freezable<?> parent) {
      this.e = e;
      this.parent = parent;
    }

    @Override
    public K getKey() {
      return e.getKey();
    }

    @Override
    public V getValue() {
      return e.getValue();
    }

    @Override
    public V setValue(final V value) {
      checkFrozen();
      return e.setValue(value);
    }

    protected void checkFrozen() {
      if (isFrozen()) {
        throw new FrozenException("frozen");
      }
    }

    @Override
    public int hashCode() {
      return e.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o)
        return true;
      if (!(o instanceof Map.Entry))
        return false;
      @SuppressWarnings("rawtypes")
      Map.Entry t = (Map.Entry) o;
      return eq(e.getKey(), t.getKey()) && eq(e.getValue(), t.getValue());
    }

    @Override
    public String toString() {
      return e.toString();
    }

    @Override
    public boolean isFrozen() {
      return parent.isFrozen();
    }

    @Override
    public void freeze() {
      parent.freeze();
    }

    static boolean eq(final Object o1, final Object o2) {
      return o1 == null ? o2 == null : o1.equals(o2);
    }

    @Override
    public FreezableEntry<K, V> copy() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void copyFrom(final FreezableEntry<K, V> source) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FreezableEntry<K, V> unfreeze() {
      throw new UnsupportedOperationException();
    }

  }

}
