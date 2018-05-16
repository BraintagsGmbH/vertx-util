package de.braintags.vertx.util.freezable;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

public class FreezableRandomAccessList<E> extends FreezableListImpl<E> implements RandomAccess {

  protected FreezableRandomAccessList() {
    super(new ArrayList<>(), null);
  }

  FreezableRandomAccessList(final List<E> list, final Freezable<?> parent) {
    super(list, parent);
  }

}
