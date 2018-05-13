package de.braintags.vertx.util.freezable;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FreezableListImpl.class)
public interface FreezableList<E> extends List<E>, FreezableCollection<E, FreezableList<E>> {

}
