package de.braintags.io.vertx.util.asyncLock;

public class AsyncLock {

  final boolean readLock;
  final LockedExecution execution;
  long                  lockStamp;

  public AsyncLock(boolean readLock, LockedExecution execution) {
    this.readLock = readLock;
    this.execution = execution;
  }
}  
