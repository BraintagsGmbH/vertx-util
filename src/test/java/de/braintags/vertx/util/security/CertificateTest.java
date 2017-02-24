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
import org.junit.Test;

/**
 * @author Michael Remme
 */
public class CertificateTest {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(CertificateTest.class);

  @Test
  public void certificateTest() {
    File file = new File("src/test/resources/de/braintags/vertx/util/security/certificate.jks");
    CertificateHelper.generateSelfSignedCertificate("localhost", file, "bouncyCastle");
  }

}
