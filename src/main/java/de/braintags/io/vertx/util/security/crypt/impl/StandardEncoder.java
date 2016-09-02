/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2016 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.util.security.crypt.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import de.braintags.io.vertx.util.assertion.Assert;
import de.braintags.io.vertx.util.security.crypt.IEncoder;
import io.vertx.core.VertxException;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class StandardEncoder implements IEncoder {
  private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
  private static final String DEFAULT_ALG = "SHA-512";
  private String salt;
  private String algorithm;

  public StandardEncoder(String salt) {
    this(DEFAULT_ALG, salt);
  }

  public StandardEncoder(String algorithm, CharSequence salt) {
    this.algorithm = algorithm == null ? DEFAULT_ALG : algorithm;
    Assert.notNull("salt", salt);
    this.salt = salt.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.util.security.crypt.IEncoder#encode(java.lang.CharSequence)
   */
  @Override
  public String encode(CharSequence rawText) {
    return computeHash(rawText);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.util.security.crypt.IEncoder#matches(java.lang.CharSequence, java.lang.String)
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

}
