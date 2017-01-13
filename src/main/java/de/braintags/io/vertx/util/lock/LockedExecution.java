/*
 * #%L
 * Vert.x utilities from Braintags
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.util.lock;

/**
 * A LockedExecution represents a piece of asynchronous work that should be executed with a read or write lock. After
 * the work is done, the lock is released by calling the given handler.
 * 
 * @author "Martin Pluecker"
 */
@FunctionalInterface
public interface LockedExecution {

  /**
   * Do the work and call the finishHandler afterwards in order to release the lock.
   *
   * @param finishHandler
   *          must be called as soon as the work is finished in order to release the lock.
   */
  void perform(Runnable finishHandler);
}
