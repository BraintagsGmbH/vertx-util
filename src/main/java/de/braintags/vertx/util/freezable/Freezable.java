package de.braintags.vertx.util.freezable;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Freezable<F extends Freezable<?>> {

  @JsonIgnore
  public boolean isFrozen();

  public void freeze();

  @JsonIgnore
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public default F copy() {
    try {
      F copy = (F) instantiateProtected(this.getClass());
      ((Freezable) copy).copyFrom(this);
      return copy;
    } catch (InstantiationException e) {
      if (e.getCause() instanceof NoSuchMethodException) {
        throw new IllegalStateException(
            "No no args contructor found and deepCopy not overrided for class: " + this.getClass());
      }
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public void copyFrom(final F source);

  @JsonIgnore
  @SuppressWarnings("unchecked")
  public default F unfreeze() {
    synchronized (this) {
      if (isFrozen()) {
        return copy();
      } else {
        return (F) this;
      }
    }
  }


  public abstract static class Skeleton<F extends Freezable<?>> implements Freezable<F> {

    @JsonIgnore
    private boolean frozen;

    @Override
    @JsonIgnore
    public boolean isFrozen() {
      return frozen;
    }

    @Override
    public synchronized void freeze() {
      frozen = true;
    }

    protected final void checkFrozen() {
      if (isFrozen()) {
        throw new FrozenException("frozen");
      }
    }

    @JsonIgnore
    @Override
    public void copyFrom(final F source) {
      checkFrozen();
    }

  }

  public abstract static class Wrapper<F extends Freezable<?>> implements Freezable<F> {

    private final Freezable<?> wrapped;

    public Wrapper(final Freezable<?> wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    @JsonIgnore
    public boolean isFrozen() {
      return wrapped.isFrozen();
    }

    @Override
    public synchronized void freeze() {
      wrapped.freeze();
    }

    protected final void checkFrozen() {
      if (isFrozen()) {
        throw new FrozenException("frozen");
      }
    }

    @JsonIgnore
    @Override
    public void copyFrom(final F source) {
      checkFrozen();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((wrapped == null) ? 0 : wrapped.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Wrapper other = (Wrapper) obj;
      if (wrapped == null) {
        if (other.wrapped != null)
          return false;
      } else if (!wrapped.equals(other.wrapped))
        return false;
      return true;
    }

  }

  public static <F> F instantiateProtected(final Class<F> cls) throws InstantiationException {
    F copy;
    try {
      copy = cls.newInstance();
    } catch (IllegalAccessException e) {
      try {
        copy = AccessController.doPrivileged((PrivilegedExceptionAction<F>) () -> {
          Constructor<F> constructor = cls.getDeclaredConstructor();
          if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
          }
          return constructor.newInstance();
        });
      } catch (PrivilegedActionException e1) {
        throw new RuntimeException(e1);
      }
    }
    return copy;
  }
}
