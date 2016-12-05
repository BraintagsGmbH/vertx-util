package de.braintags.io.vertx.util.asyncLock;

@FunctionalInterface
public interface LockedExecution {

  /**
   * Do the work and call result handler afterwards.
   *
   * @param finishHandler must be called as soon as the work is finished.
   */
  void perform(Runnable finishHandler);
}
