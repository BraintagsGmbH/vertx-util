package de.braintags.vertx.util;

import java.util.Objects;

public class EqUtil {

  /**
   * @deprecated use Objects.equals() instead
   */
  @Deprecated
  public static boolean eq(Object o1, Object o2) {
    return Objects.equals(o1, o2);
  }

}
