package de.braintags.vertx.util.freezable;

import java.util.LinkedHashSet;
import java.util.Set;

public class FreezableSetImpl<E> extends FreezableCollectionImpl<E, FreezableSet<E>> implements FreezableSet<E> {

  protected FreezableSetImpl() {
    this(new LinkedHashSet<>(), null);
  }

  FreezableSetImpl(final Set<E> s, final Freezable<?> parent) {
    super(s, parent);
  }

  @Override
  public boolean equals(final Object o) {
    return o == this || c.equals(o);
  }

  @Override
  public int hashCode() {
    return c.hashCode();
  }

}
