package de.braintags.vertx.util.freezable;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class FreezableSortedSetImpl<E> extends FreezableCollectionImpl<E, FreezableSet<E>>
    implements FreezableSortedSet<E> {

  private final SortedSet<E> ss;

  protected FreezableSortedSetImpl() {
    this(new TreeSet<>(), null);
  }

  public FreezableSortedSetImpl(final SortedSet<E> s, final Freezable<?> parent) {
    super(s, parent);
    ss = s;
  }

  @Override
  public Comparator<? super E> comparator() {
    return ss.comparator();
  }

  @Override
  public SortedSet<E> subSet(final E fromElement, final E toElement) {
    return new FreezableSortedSetImpl<>(ss.subSet(fromElement, toElement), this);
  }

  @Override
  public SortedSet<E> headSet(final E toElement) {
    return new FreezableSortedSetImpl<>(ss.headSet(toElement), this);
  }

  @Override
  public SortedSet<E> tailSet(final E fromElement) {
    return new FreezableSortedSetImpl<>(ss.tailSet(fromElement), this);
  }

  @Override
  public E first() {
    return ss.first();
  }

  @Override
  public E last() {
    return ss.last();
  }

  @Override
  public FreezableSortedSet<E> copy() {
    return (FreezableSortedSet<E>) super.copy();
  }
}
