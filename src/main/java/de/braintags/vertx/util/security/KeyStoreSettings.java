/*-
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
package de.braintags.vertx.util.security;

/**
 * Container class for KeyStoreSettings settings.
 *
 * @author Jan Kerkenhoff
 */
public class KeyStoreSettings {
  /**
   * Default type of the keystore if no other is set
   */
  public static final String DEFAULT_TYPE = "jceks";

  private String type;
  private String path;
  private String password;

  public KeyStoreSettings() {
    this.type = DEFAULT_TYPE;
  }

  public String getType() {
    return type;
  }

  public KeyStoreSettings setType(String type) {
    this.type = type;
    return this;
  }

  public String getPath() {
    return path;
  }

  public KeyStoreSettings setPath(String path) {
    this.path = path;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public KeyStoreSettings setPassword(String password) {
    this.password = password;
    return this;
  }

}
