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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.ext.auth.jwt.JWT;

/**
 * Interface for general JWT handling
 * 
 * @author sschmitt
 * 
 */
public interface JWTHandler {
  /**
   * Create a new {@link JWT} instance from the provided {@link JWTSettings}.
   * If no {@link KeyStoreSettings} settings are set, it tries to create a JWT using a public key.
   *
   * @param vertx
   *          current vertx instance, only needed for a {@link KeyStore} based {@link JWT}
   * @param config
   *          config to create a {@link JWT} from
   * @return a new {@link JWT} instance
   */
  public static JWT createJWT(Vertx vertx, JWTSettings config) {
    final KeyStoreSettings keyStore = config.getKeystoreSettings();

    try {
      if (keyStore != null) {
        char[] keyStorePassword = keyStore.getPassword().toCharArray();
        KeyStore ks = KeyStore.getInstance(keyStore.getType());

        // synchronize on the class to avoid the case where multiple file accesses will overlap
        synchronized (JWTHandler.class) {
          final Buffer keystore = vertx.fileSystem().readFileBlocking(keyStore.getPath());

          try (InputStream in = new ByteArrayInputStream(keystore.getBytes())) {
            ks.load(in, keyStorePassword);
          }
        }

        return new JWT(ks, keyStorePassword);
      } else {
        // in the case of not having a key store we will try to load a public key in pem format
        // this is how keycloak works as an example.
        // FIXME: wait for JWT to support multiple keys
        byte[] keyBytes = config.getPublicKeys().get("RS256").getEncoded();
        String key = Base64.getMimeEncoder().encodeToString(keyBytes);
        return new JWT(key, false);
      }

    } catch (KeyStoreException | IOException | FileSystemException | CertificateException
        | NoSuchAlgorithmException e) {
      throw new RuntimeException(e); // NOSONAR Should not happen
    }
  }
}
