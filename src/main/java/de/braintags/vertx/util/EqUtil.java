package de.braintags.vertx.util;

public class EqUtil {

  /**
   * Tests two null pointers or objects for equality.
   * 
   * @return
   *    true if both objects are <code>null</code> or equal. 
   */
	public static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

}
