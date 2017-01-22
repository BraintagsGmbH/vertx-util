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
package de.braintags.vertx.util.exception;

/**
 * Exception is thrown in case of problems during dynamic class access
 * 
 * @author Michael Remme
 * 
 */
public class ClassAccessException extends RuntimeException {

  /**
   * @param message
   */
  public ClassAccessException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public ClassAccessException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ClassAccessException(String message, Throwable cause) {
    super(message, cause);
  }

}
