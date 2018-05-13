package de.braintags.vertx.util.freezable;

import java.util.Collection;

public interface FreezableCollection<E, F extends FreezableCollection<E, F>> extends Collection<E>, Freezable<F> {

}
