package de.braintags.vertx.util.url;

import org.apache.commons.lang3.StringUtils;

public class UrlUtil {

  private UrlUtil() {
  }

  public static String concatPaths(final String... paths) {
    return concatPaths(false, paths);
  }

  public static String concatPaths(final boolean absolute, final String... paths) {
    StringBuilder result = new StringBuilder();
    boolean endsWithSlash = absolute;

    if (absolute) {
      result.append("/");
    }

    for (int i = 0; i < paths.length; i++) {
      String path = paths[i];
      if (StringUtils.isEmpty(path))
        continue;
      if (i != 0 || absolute) {
        boolean startsWithSlash = path.charAt(0) == '/';
        if (startsWithSlash && endsWithSlash) {
          if (path.length() < 2)
            continue;
          path = path.substring(1);
        } else if (!startsWithSlash && !endsWithSlash)
          result.append('/');
        result.append(path);
      } else
        result.append(path);

      endsWithSlash = path.charAt(path.length() - 1) == '/';
    }
    return result.toString();
  }

}
