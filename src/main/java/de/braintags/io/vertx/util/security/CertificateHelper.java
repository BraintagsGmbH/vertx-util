package de.braintags.io.vertx.util.security;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Date;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

/**
 * Helper Class to deal with certificates
 * 
 * @author Michael Remme
 * 
 */
public class CertificateHelper {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(CertificateHelper.class);

  /**
   * 
   */
  private CertificateHelper() {
  }

  @SuppressWarnings("restriction")
  public static void createSelfCertificate(HttpServerOptions options, String hostName, String password)
      throws GeneralSecurityException, IOException {
    LOGGER.info("creating self certificate");
    KeyStore store = KeyStore.getInstance("JKS");
    store.load(null, null);

    sun.security.tools.keytool.CertAndKeyGen keypair = new sun.security.tools.keytool.CertAndKeyGen("RSA",
        "SHA1WithRSA", null);
    sun.security.x509.X500Name x500Name = new sun.security.x509.X500Name(hostName, "IT", "unknown", "unknown",
        "unknown", "unknown");
    keypair.generate(1024);
    PrivateKey privKey = keypair.getPrivateKey();
    java.security.cert.X509Certificate[] chain = new java.security.cert.X509Certificate[1];
    chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long) 365 * 24 * 60 * 60);
    store.setKeyEntry("selfsigned", privKey, password.toCharArray(), chain);
    FileOutputStream fo = new FileOutputStream(".keystore");
    store.store(fo, password.toCharArray());
    options.setKeyStoreOptions(new JksOptions().setPath(".keystore").setPassword(password));
    options.setSsl(true);
  }

}
