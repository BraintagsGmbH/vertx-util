package de.braintags.io.vertx.util.codec;

public class UserObject {
  public String testString = "testString";
  public int count = 34;

  @Override
  public boolean equals(Object o) {
    UserObject compare = (UserObject) o;
    return compare.count == count && compare.testString.equals(testString);
  }
}