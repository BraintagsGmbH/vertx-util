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
package de.braintags.vertx.util.security.crypt.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;

import de.braintags.vertx.util.security.crypt.IEncoder;
import io.vertx.core.VertxException;

/**
 * Implementation of IEncoder which is using SHA-512 as crypt per default
 * 
 * @author Michael Remme
 * 
 */
public class StandardEncoder implements IEncoder {
  /**
   * Used as property name by the method {@link #init(Properties)} to read the salt from the properties
   */
  public static final String SALT_PROPERTY = "salt";

  /**
   * Used as property name by the method {@link #init(Properties)} to read the algorithm from the properties
   */
  public static final String ALGORITHM_PROPERTY = "algorithm";

  private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
  private static final String DEFAULT_ALG = "SHA-512";
  private String salt;
  private String algorithm;

  /**
   * Default constructor with empty salt and SHA-512 algorithm
   * 
   * @param salt
   */
  public StandardEncoder() {
    this(null);
  }

  /**
   * Constructor with SHA-512 algorithm
   * 
   * @param salt
   */
  public StandardEncoder(String salt) {
    this(DEFAULT_ALG, salt);
  }

  /**
   * Contructor with given salt and algorithm
   * 
   * @param algorithm
   * @param salt
   */
  public StandardEncoder(String algorithm, CharSequence salt) {
    this.algorithm = algorithm == null ? DEFAULT_ALG : algorithm;
    this.salt = salt == null ? "" : salt.toString();
  }

  /**
   * The salt to be used to crypt values
   * 
   * @return the salt
   */
  public String getSalt() {
    return salt;
  }

  /**
   * The salt to be used to crypt values
   * 
   * @param salt
   *          the salt to set
   */
  public void setSalt(String salt) {
    this.salt = salt;
  }

  /**
   * the algorithm to be used
   * 
   * @return the algorithm
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * the algorithm to be used
   * 
   * @param algorithm
   *          the algorithm to set
   */
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.util.security.crypt.IEncoder#encode(java.lang.CharSequence)
   */
  @Override
  public String encode(CharSequence rawText) {
    return computeHash(rawText);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.util.security.crypt.IEncoder#matches(java.lang.CharSequence, java.lang.String)
   */
  @Override
  public boolean matches(CharSequence rawText, String encodedText) {
    String hashedText = computeHash(rawText);
    return encodedText != null && encodedText.equals(hashedText);
  }

  private String computeHash(CharSequence rawText) {
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      String concat = (salt == null ? "" : salt) + rawText;
      byte[] bHash = md.digest(concat.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(bHash);
    } catch (NoSuchAlgorithmException e) {
      throw new VertxException(e);
    }
  }

  /**
   * Generate a salt
   * 
   * @return the generated salt
   */
  public static String generateSalt() {
    final Random r = new SecureRandom();
    byte[] salt = new byte[32];
    r.nextBytes(salt);
    return bytesToHex(salt);
  }

  private static String bytesToHex(byte[] bytes) {
    char[] chars = new char[bytes.length * 2];
    for (int i = 0; i < bytes.length; i++) {
      int x = 0xFF & bytes[i];
      chars[i * 2] = HEX_CHARS[x >>> 4];
      chars[1 + i * 2] = HEX_CHARS[0x0F & x];
    }
    return new String(chars);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.util.security.crypt.IEncoder#init(java.util.Properties)
   */
  @Override
  public void init(Properties properties) {
    String tmp = properties.getProperty(SALT_PROPERTY);
    if (tmp != null && tmp.hashCode() != 0) {
      setSalt(tmp);
    }
    tmp = properties.getProperty(ALGORITHM_PROPERTY);
    if (tmp != null && tmp.hashCode() != 0) {
      setAlgorithm(tmp);
    }
  }

}
