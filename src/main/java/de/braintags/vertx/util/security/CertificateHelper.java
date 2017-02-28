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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import de.braintags.vertx.util.exception.InitException;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

/**
 * Helper Class to deal with certificates
 * 
 * @author Michael Remme
 */
public class CertificateHelper {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(CertificateHelper.class);

  private static final String ALGORYTM = "SHA1WithRSA"; // "MD5WithRSA"; //
  private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

  /**
   * 
   */
  private CertificateHelper() {
  }

  /**
   * Creates a self certificate, stores it into a keystore and adapts the server options to use that certificate
   * 
   * @param options
   * @param hostName
   * @param password
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static void generateSelfSignedCertificate(HttpServerOptions options, String hostname,
      String keystorePassword) {
    generateSelfSignedCertificate(hostname, new File(".keystore"), keystorePassword);
    options.setKeyStoreOptions(new JksOptions().setPath(".keystore").setPassword(keystorePassword));
    options.setSsl(true);
  }

  public static void generateSelfSignedCertificate(String hostname, File keystore, String keystorePassword) {
    // TODO cleanup, remove file against vertx file etc
    try {
      LOGGER.info("keystore in " + keystore.getAbsolutePath());
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

      // Generate KeyPair
      KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
      kpGen.initialize(1024, new SecureRandom());
      KeyPair pair = kpGen.generateKeyPair();

      // Generate self-signed certificate
      X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
      builder.addRDN(BCStyle.CN, hostname);

      Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
      Date notAfter = new Date(System.currentTimeMillis() + 10 * 365 * 24 * 60 * 60 * 1000);
      BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

      X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(builder.build(), serial, notBefore, notAfter,
          builder.build(), pair.getPublic());
      ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(BC)
          .build(pair.getPrivate());

      X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certGen.build(sigGen));
      cert.checkValidity(new Date());
      cert.verify(cert.getPublicKey());

      // Save to keystore
      KeyStore store = KeyStore.getInstance("JKS");
      if (keystore.exists()) {
        store.load(null, keystorePassword.toCharArray());
        store.setKeyEntry(hostname, pair.getPrivate(), keystorePassword.toCharArray(),
            new java.security.cert.Certificate[] { cert });
        FileOutputStream fos = new FileOutputStream(keystore);

        store.store(fos, keystorePassword.toCharArray());
        fos.close();
      }
    } catch (Exception t) {
      throw new InitException("Failed to generate self-signed certificate!", t);
    }

  }

}
