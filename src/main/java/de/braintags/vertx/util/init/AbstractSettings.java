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
package de.braintags.vertx.util.init;

/**
 * An abstract class dealing with settings
 * 
 * @author Michael Remme
 * 
 */
public class AbstractSettings {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(AbstractSettings.class);

  private String settingsName;

  /**
   * 
   * @param settingsName
   */
  public AbstractSettings(String settingsName) {
    this.settingsName = settingsName;
  }

}
