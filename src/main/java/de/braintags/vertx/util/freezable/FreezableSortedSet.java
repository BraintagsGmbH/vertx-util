package de.braintags.vertx.util.freezable;

import java.util.SortedSet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FreezableSortedSetImpl.class)
public interface FreezableSortedSet<E> extends SortedSet<E>, FreezableSet<E> {

  @Override
  FreezableSortedSet<E> copy();

}
