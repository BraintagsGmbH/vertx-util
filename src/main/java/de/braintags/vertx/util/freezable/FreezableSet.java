package de.braintags.vertx.util.freezable;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FreezableSetImpl.class)
public interface FreezableSet<E> extends Set<E>, FreezableCollection<E, FreezableSet<E>> {

}
