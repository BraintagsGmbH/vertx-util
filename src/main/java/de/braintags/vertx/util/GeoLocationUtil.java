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

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helper class dealing with longitude / latitude
 * 
 * @author Michael Remme
 * 
 */
public class GeoLocationUtil {
  /**
   * specifies the calculation to be in km
   */
  public static final char KM = 'K';

  /**
   * specifies the calculation to be in nautic miles
   */
  public static final char NM = 'N';

  /**
   * specifies the calculation to be in miles
   */
  public static final char MI = 'M';

  private GeoLocationUtil() {
  }

  /**
   * Calculate the distance between two points with the defined scale
   * 
   * @param lat1
   * @param lon1
   * @param lat2
   * @param lon2
   * @param unit
   *          the unit to be returned
   * @param scale
   *          the scale to be used
   * @return the distance in km, nm or miles
   */
  public static String distance(double lat1, double lon1, double lat2, double lon2, char unit, int scale) {
    double distance = distance(lat1, lon1, lat2, lon2, unit);
    return BigDecimal.valueOf(distance).setScale(scale, RoundingMode.HALF_UP).toString();
  }

  /**
   * Calculate the distance between two points
   * 
   * @param lat1
   * @param lon1
   * @param lat2
   * @param lon2
   * @param unit
   *          the unit to be returned
   * @return the distance in km, nm or miles
   */
  public static double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
    double theta = lon1 - lon2;
    double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
        + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    dist = Math.acos(dist);
    dist = rad2deg(dist);
    dist = dist * 60 * 1.1515;
    if (unit == KM) {
      dist = dist * 1.609344;
    } else if (unit == NM) {
      dist = dist * 0.8684;
    }
    return dist;
  }

  /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
  /* :: This function converts decimal degrees to radians : */
  /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
  private static double deg2rad(double deg) {
    return deg * Math.PI / 180.0;
  }

  /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
  /* :: This function converts radians to decimal degrees : */
  /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
  private static double rad2deg(double rad) {
    return rad * 180.0 / Math.PI;
  }

}
