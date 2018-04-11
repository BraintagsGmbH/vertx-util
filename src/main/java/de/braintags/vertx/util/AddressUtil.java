package de.braintags.vertx.util;

public class AddressUtil {

  private AddressUtil() {
  }

  /**
   * Splits the house number from the street
   * 
   * @param street
   *          a combined street with house number
   * @return either an array with size 2 containing the street and the house number, in that order. Or null, if the
   *         house number could not be extracted.
   */
  public static String[] splitHouseNumber(final String street) {
    int number = -1;
    for (int i = 0; i < street.length(); i++) {
      if (Character.isDigit(street.charAt(i))) {
        number = i;
        break;
      }
    }
    if (number > 0) {
      return new String[] { street.substring(0, number).trim(), street.substring(number).trim() };
    }
    return null;
  }
}
