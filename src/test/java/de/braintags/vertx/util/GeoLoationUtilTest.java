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
package de.braintags.vertx.util;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class GeoLoationUtilTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.braintags.vertx.util.GeoLocationUtil#distance(double, double, double, double, char)}.
   */
  @Test
  public void testDistance() {
    System.out
        .println(GeoLocationUtil.distance(32.9697, -96.80322, 29.46786, -98.53506, GeoLocationUtil.MI) + " Miles\n");
    System.out.println(
        GeoLocationUtil.distance(32.9697, -96.80322, 29.46786, -98.53506, GeoLocationUtil.KM) + " Kilometers\n");
    System.out.println(
        GeoLocationUtil.distance(32.9697, -96.80322, 29.46786, -98.53506, GeoLocationUtil.NM) + " Nautical Miles\n");

  }

}
