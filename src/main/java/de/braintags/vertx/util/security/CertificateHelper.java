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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.KeyFactory;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

/**
 * Helper Class to deal with certificates
 * 
 * @author Michael Remme
 */
public class CertificateHelper {
  private static final io.vertx.core.logging.Logger LOGGER   = io.vertx.core.logging.LoggerFactory
      .getLogger(CertificateHelper.class);

  private static final String                       ALGORYTM = "SHA1WithRSA";                     // "MD5WithRSA"; //

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
  @SuppressWarnings("restriction")
  public static void createSelfCertificate(HttpServerOptions options, String hostName, String password)
      throws GeneralSecurityException, IOException {

    LOGGER.info("creating self certificate");
    KeyStore store = KeyStore.getInstance("JKS");
    store.load(null, null);
    sun.security.tools.keytool.CertAndKeyGen keypair = new sun.security.tools.keytool.CertAndKeyGen("RSA", ALGORYTM,
        null);
    sun.security.x509.X500Name x500Name = new sun.security.x509.X500Name(hostName, "IT", "firm", "city", "country",
        "state");
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

  /**
   * generate a certificate with the boncycastle libary
   * 
   * @param dnsName
   * @param validityDays
   * @param caFile
   * @param caAlias
   * @param caPassword
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static void newCertificateHelper(String caName, int validityDays, String caFile, String caAlias,
      String caPassword) throws GeneralSecurityException, IOException {

    PublicKey pubKey;
    X509Certificate caCert = null;
    RSAPrivateCrtKeyParameters caPrivateKey;

    SecureRandom sr = new SecureRandom();

    // generate public key and private key
    LOGGER.info("creating self certificate");
    RSAKeyPairGenerator gen = new RSAKeyPairGenerator();
    gen.init(new RSAKeyGenerationParameters(BigInteger.valueOf(3), sr, 1024, 80));
    AsymmetricCipherKeyPair keypair = gen.generateKeyPair();
    RSAKeyParameters publicKey = (RSAKeyParameters) keypair.getPublic();
    RSAPrivateCrtKeyParameters privateKey = (RSAPrivateCrtKeyParameters) keypair.getPrivate();

    RSAPublicKeyStructure pkStruct = new RSAPublicKeyStructure(publicKey.getModulus(), publicKey.getExponent());
    pubKey = KeyFactory.getInstance("RSA")
        .generatePublic(new RSAPublicKeySpec(publicKey.getModulus(), publicKey.getExponent()));
    KeyFactory.getInstance("RSA").generatePrivate(
        new RSAPrivateCrtKeySpec(publicKey.getModulus(), publicKey.getExponent(), privateKey.getExponent(),
            privateKey.getP(), privateKey.getQ(), privateKey.getDP(), privateKey.getDQ(), privateKey.getQInv()));

    // creating new certificate structure
    Calendar expiry = Calendar.getInstance();
    expiry.add(Calendar.DAY_OF_YEAR, validityDays);

    X509Name x509Name = new X509Name("CN=" + caName);

    V3TBSCertificateGenerator certGen = new V3TBSCertificateGenerator();
    certGen.setSerialNumber(new DERInteger(BigInteger.valueOf(System.currentTimeMillis())));
    certGen.setIssuer(PrincipalUtil.getSubjectX509Principal(caCert));
    certGen.setSubject(x509Name);
    //DERObjectIdentifier sigOID = X509Util.getAlgorithmOID("SHA1WithRSAEncryption");
    AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(X509ObjectIdentifiers.id_SHA1);
    certGen.setSignature(sigAlgId);

    certGen.setSubjectPublicKeyInfo(new SubjectPublicKeyInfo(
        (ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(pubKey.getEncoded())).readObject()));
    certGen.setStartDate(new Time(new Date(System.currentTimeMillis())));
    certGen.setEndDate(new Time(expiry.getTime()));
    TBSCertificateStructure tbsCert = certGen.generateTBSCertificate();

    KeyStore caKs = KeyStore.getInstance("PKCS12");
    caKs.load(new FileInputStream(new File(caFile)), caPassword.toCharArray());

    // load the key entry from the keystore
    Key key = caKs.getKey(caAlias, caPassword.toCharArray());
    if (key == null) {
      throw new RuntimeException("Got null key from keystore!");
    }
    RSAPrivateCrtKey privKey = (RSAPrivateCrtKey) key;
    caPrivateKey = new RSAPrivateCrtKeyParameters(privKey.getModulus(), privKey.getPublicExponent(),
        privKey.getPrivateExponent(), privKey.getPrimeP(), privKey.getPrimeQ(), privKey.getPrimeExponentP(),
        privKey.getPrimeExponentQ(), privKey.getCrtCoefficient());
    // and get the certificate
    caCert = (X509Certificate) caKs.getCertificate(caAlias);
    if (caCert == null) {
      throw new RuntimeException("Got null cert from keystore!");
    }
    caCert.verify(caCert.getPublicKey());

  }

  public static X509Certificate generateV3Certificate(KeyPair pair) throws InvalidKeyException, NoSuchProviderException,
      SignatureException, CertificateEncodingException, IllegalStateException, NoSuchAlgorithmException {

    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

    certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
    certGen.setIssuerDN(new X500Principal("CN=Test Certificate"));
    certGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
    certGen.setNotAfter(new Date(System.currentTimeMillis() + 10000));
    certGen.setSubjectDN(new X500Principal("CN=Test Certificate"));
    certGen.setPublicKey(pair.getPublic());
    certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

    certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
    certGen.addExtension(X509Extensions.KeyUsage, true,
        new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
    certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));

    certGen.addExtension(X509Extensions.SubjectAlternativeName, false,
        new GeneralNames(new GeneralName(GeneralName.rfc822Name, "test@test.test")));

    return certGen.generate(pair.getPrivate(), "BC");
  }

  private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

  public static void generateSelfSignedCertificate(String hostname, File keystore, String keystorePassword) {
    try {
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

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
      KeyStore store = KeyStore.getInstance("BKS");
      if (keystore.exists()) {
        FileInputStream caFile = new FileInputStream(keystore);
        store.load(caFile, keystorePassword.toCharArray());
        caFile.close();
      } else {
        store.load(null);
        throw new RuntimeException("Got null cert from keystore!");
      }
      store.setKeyEntry(hostname, pair.getPrivate(), keystorePassword.toCharArray(),
          new java.security.cert.Certificate[] { cert });
      FileOutputStream fos = new FileOutputStream(keystore);
      store.store(fos, keystorePassword.toCharArray());
      fos.close();
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException("Failed to generate self-signed certificate!", t);
    }

  }

}
