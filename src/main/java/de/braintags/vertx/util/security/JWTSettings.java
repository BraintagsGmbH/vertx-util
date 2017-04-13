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

import java.security.PublicKey;
import java.util.Map;

public class JWTSettings {
  private KeyStoreSettings keyStoreSettings;
  private Map<String, PublicKey> publicKeys;

  private static final IllegalArgumentException NOTALLOWED_CONFIG = new IllegalArgumentException(
      "Either a keyStoreSettings config or a public key is allowed");

  public KeyStoreSettings getKeystoreSettings() {
    return keyStoreSettings;
  }

  public JWTSettings setKeystoreSettings(KeyStoreSettings keyStoreSettings) {
    if (publicKeys != null && keyStoreSettings != null) {
      throw NOTALLOWED_CONFIG;
    }
    this.keyStoreSettings = keyStoreSettings;
    return this;
  }

  public Map<String, PublicKey> getPublicKeys() {
    return publicKeys;
  }

  public JWTSettings setPublicKeys(Map<String, PublicKey> publicKeys) {
    if (keyStoreSettings != null && publicKeys != null) {
      throw NOTALLOWED_CONFIG;
    }
    this.publicKeys = publicKeys;
    return this;
  }

}
