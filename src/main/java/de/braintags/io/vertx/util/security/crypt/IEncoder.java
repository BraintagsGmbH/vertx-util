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
package de.braintags.io.vertx.util.security.crypt;

import java.util.Properties;

/**
 * Utility class to encode text like passwords and to check validity of values
 * 
 * @author Michael Remme
 * 
 */
public interface IEncoder {

  /**
   * Encode the raw text. Generally, a good encoding algorithm applies a SHA-1 or
   * greater hash combined with an 8-byte or greater randomly generated salt.
   */
  String encode(CharSequence rawText);

  /**
   * Verify the encoded test obtained from storage matches the submitted raw
   * text after it too is encoded. Returns true if the texts match, false if
   * they do not. The stored text itself is never decoded.
   *
   * @param rawText
   *          the raw text to encode and match
   * @param encodedText
   *          the encoded text from storage to compare with
   * @return true if the raw text, after encoding, matches the encoded text from
   *         storage
   */
  boolean matches(CharSequence rawText, String encodedText);

  /**
   * This method can be used to init an encoder. The names of properties and there meaning should be found in the
   * documentation of the appropriate implementation.
   * 
   * @param properties
   */
  void init(Properties properties);

}
