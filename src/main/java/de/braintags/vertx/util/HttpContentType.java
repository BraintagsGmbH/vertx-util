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
package de.braintags.vertx.util;

/**
 * enumeration for various HTTP content types
 * 
 * @author Michael Remme
 * 
 */
public enum HttpContentType {

  TEXT_HTML("text/html"),
  JSON("application/json"),
  JSON_UTF8("application/json; charset=utf-8");

  String value;

  HttpContentType(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
