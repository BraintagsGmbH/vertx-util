/**
 * 
 */
package de.braintags.vertx.util;

/**
 * Helper class to calculate cross sum
 * 
 * @author mremme
 *
 */
public class CrossSum {
  private CrossSum() {
  }

  /**
   * Creates the cross sum of the given long
   * 
   * @param source
   * @return
   */
  public static long getCrossSum(Long source) {
    if (source < 0) {
      throw new IllegalArgumentException("need positive value");
    }
    long summe = 0;
    while (0 != source) {
      summe = summe + (source % 10);
      source = source / 10;
    }
    return summe;
  }


}
