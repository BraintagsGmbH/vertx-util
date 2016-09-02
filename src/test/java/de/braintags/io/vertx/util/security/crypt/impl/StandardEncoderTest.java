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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class StandardEncoderTest {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(StandardEncoderTest.class);

  private StandardEncoder encoder = new StandardEncoder("secret");

  @Test
  public void matches() {
    String result = encoder.encode("password");
    assertNotEquals(result, "password");
    assertTrue(encoder.matches("password", result));
  }

  @Test
  public void matchesLengthChecked() {
    String result = encoder.encode("password");
    assertFalse(encoder.matches("password", result.substring(0, result.length() - 2)));
  }

  @Test
  public void notMatches() {
    String result = encoder.encode("password");
    assertFalse(encoder.matches("bogus", result));
  }

  @Test
  public void generateSalt() {
    String salt = StandardEncoder.generateSalt();
    LOGGER.info(salt);
    encoder = new StandardEncoder(salt);
    String result = encoder.encode("password");
    assertNotEquals(result, "password");
    assertTrue(encoder.matches("password", result));
  }

}
