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
package de.braintags.vertx.util.lock;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An AsyncLock represents the lock handle for an execution.
 * 
 * @author "Martin Pluecker"
 */
public class AsyncLock {

  final boolean         readLock;
  final LockedExecution execution;
  final AtomicLong      lockStamp;

  AsyncLock(boolean readLock, LockedExecution execution) {
    this.readLock = readLock;
    this.execution = execution;
    lockStamp = new AtomicLong();
  }
}
