package de.braintags.vertx.util.freezable;

public interface Immutable<F extends Freezable<?>> extends Freezable<F> {

  @Override
  default F copy() {
    return (F) this;
  }

  @Override
  default boolean isFrozen() {
    return true;
  }

  @Override
  default void copyFrom(final F source) {
    throw new UnsupportedOperationException("object is immutable");
  }
  
  @Override
  default F unfreeze() {
    throw new UnsupportedOperationException("object is immutable");
  }

  public static class Skeleton<F extends Freezable<?>> extends Freezable.Skeleton<F> implements Immutable<F> {

    @Override
    public final F copy() {
      return (F) this;
    }

    @Override
    public final boolean isFrozen() {
      return true;
    }

    @Override
    public final void copyFrom(final F source) {
      throw new UnsupportedOperationException("object is immutable");
    }

    @Override
    public final F unfreeze() {
      throw new UnsupportedOperationException("object is immutable");
    }
  }

}
