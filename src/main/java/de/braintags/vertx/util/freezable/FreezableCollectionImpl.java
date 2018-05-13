package de.braintags.vertx.util.freezable;

import java.util.Collection;
import java.util.Iterator;

public class FreezableCollectionImpl<E extends Object, F extends FreezableCollection<E, F>>
    extends Freezable.Skeleton<F> implements Collection<E>, FreezableCollection<E, F> {

  final Collection<E> c;
  private Freezable<?> parent;

  protected FreezableCollectionImpl(final Collection<E> c, final Freezable<?> parent) {
    this.parent = parent;
    if (c == null)
      throw new NullPointerException();
    this.c = c;
  }

  protected Freezable<?> getParent() {
    return parent;
  }

  protected void setParent(final Freezable<?> parent) {
    this.parent = parent;
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
  public boolean contains(final Object o) {
    return c.contains(o);
  }

  @Override
  public Object[] toArray() {
    return c.toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return c.toArray(a);
  }

  @Override
  public String toString() {
    return c.toString();
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {

      private final Iterator<? extends E> i = c.iterator();

      @Override
      public boolean hasNext() {
        return i.hasNext();
      }

      @Override
      public E next() {
        return i.next();
      }

      @Override
      public void remove() {
        checkFrozen();
        i.remove();
      }
    };
  }

  @Override
  public boolean add(final E e) {
    checkFrozen();
    return c.add(e);
  }

  @Override
  public boolean remove(final Object o) {
    checkFrozen();
    return c.remove(o);
  }

  @Override
  public boolean containsAll(final Collection<?> coll) {
    return c.containsAll(coll);
  }

  @Override
  public boolean addAll(final Collection<? extends E> coll) {
    checkFrozen();
    return c.addAll(coll);
  }

  @Override
  public boolean removeAll(final Collection<?> coll) {
    checkFrozen();
    return c.removeAll(coll);
  }

  @Override
  public boolean retainAll(final Collection<?> coll) {
    checkFrozen();
    return c.retainAll(coll);
  }

  @Override
  public void clear() {
    checkFrozen();
    c.clear();
  }

  @Override
  public boolean isFrozen() {
    return super.isFrozen() || (parent != null && parent.isFrozen());
  }

  @Override
  public final void freeze() {
    if (!isFrozen()) {
      super.freeze();
      for (Object o : this) {
        if (o instanceof Freezable) {
          ((Freezable<?>) o).freeze();
        }
      }
      if (parent != null) {
        parent.freeze();
      }
    }
  }

  @Override
  public void copyFrom(final F source) {
    checkFrozen();
    this.c.clear();
    this.c.addAll(source);
    this.setParent(null);
  }

}
