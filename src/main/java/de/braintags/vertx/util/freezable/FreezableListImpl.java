package de.braintags.vertx.util.freezable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class FreezableListImpl<E> extends FreezableCollectionImpl<E, FreezableList<E>> implements FreezableList<E> {

  final List<E> list;

  protected FreezableListImpl() {
    this(new ArrayList<>(), null);
  }

  FreezableListImpl(final List<E> list, final Freezable<?> parent) {
    super(list, parent);
    this.list = list;
  }

  @Override
  public boolean equals(final Object o) {
    return o == this || list.equals(o);
  }

  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public E get(final int index) {
    return list.get(index);
  }

  @Override
  public E set(final int index, final E element) {
    checkFrozen();
    return list.set(index, element);
  }

  @Override
  public void add(final int index, final E element) {
    checkFrozen();
    list.add(index, element);
  }

  @Override
  public E remove(final int index) {
    checkFrozen();
    return list.remove(index);
  }

  @Override
  public int indexOf(final Object o) {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(final Object o) {
    return list.lastIndexOf(o);
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends E> c) {
    checkFrozen();
    return list.addAll(index, c);
  }

  @Override
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }

  @Override
  public ListIterator<E> listIterator(final int index) {
    return new ListIterator<E>() {

      private final ListIterator<E> i = list.listIterator(index);

      @Override
      public boolean hasNext() {
        return i.hasNext();
      }

      @Override
      public E next() {
        return i.next();
      }

      @Override
      public boolean hasPrevious() {
        return i.hasPrevious();
      }

      @Override
      public E previous() {
        return i.previous();
      }

      @Override
      public int nextIndex() {
        return i.nextIndex();
      }

      @Override
      public int previousIndex() {
        return i.previousIndex();
      }

      @Override
      public void remove() {
        checkFrozen();
        i.remove();
      }

      @Override
      public void set(final E e) {
        checkFrozen();
        i.set(e);
      }

      @Override
      public void add(final E e) {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public List<E> subList(final int fromIndex, final int toIndex) {
    return new FreezableListImpl<>(list.subList(fromIndex, toIndex), this);
  }

}
