package de.braintags.vertx.util.url;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TUrlUtil {

  @Test
  public void testConcat_onePath() {
    String result = UrlUtil.concatPaths("root");
    assertThat(result, is("root"));
  }

  @Test
  public void testConcat_onePath_makeAbsolute() {
    String result = UrlUtil.concatPaths(true, "root");
    assertThat(result, is("/root"));
  }

  @Test
  public void testConcat_onePath_alreadyAbsolute() {
    String result = UrlUtil.concatPaths(true, "/root");
    assertThat(result, is("/root"));
  }

  @Test
  public void testConcat_multiplePaths_noSlash() {
    String result = UrlUtil.concatPaths("root", "sub");
    assertThat(result, is("root/sub"));
  }

  @Test
  public void testConcat_multiplePaths_endingSlash() {
    String result = UrlUtil.concatPaths("root/", "sub");
    assertThat(result, is("root/sub"));
  }

  @Test
  public void testConcat_multiplePaths_startingSlash() {
    String result = UrlUtil.concatPaths("root", "/sub");
    assertThat(result, is("root/sub"));
  }

  @Test
  public void testConcat_multiplePaths_startingAndEndingSlash() {
    String result = UrlUtil.concatPaths("root/", "/sub");
    assertThat(result, is("root/sub"));
  }

  @Test
  public void testConcat_multiplePaths_noSlash_blankPath() {
    String result = UrlUtil.concatPaths("root", "", "sub");
    assertThat(result, is("root/sub"));
  }

  @Test
  public void testConcat_multiplePaths_endingSlash_blankPath() {
    String result = UrlUtil.concatPaths("root/", "", "sub");
    assertThat(result, is("root/sub"));
  }

  @Test
  public void testConcat_multiplePaths_startingSlash_blankPath() {
    String result = UrlUtil.concatPaths("root", "", "/sub");
    assertThat(result, is("root/sub"));
  }

  @Test
  public void testConcat_multiplePaths_startingAndEndingSlash_blankPath() {
    String result = UrlUtil.concatPaths("root/", "", "/sub");
    assertThat(result, is("root/sub"));
  }

  @Test
  public void testConcat_multiplePaths_nullPath() {
    String result = UrlUtil.concatPaths("root", null, "sub");
    assertThat(result, is("root/sub"));
  }


}
