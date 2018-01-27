package de.braintags.vertx.util.url;

import java.net.URI;
import java.net.URISyntaxException;

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

  public static URI appendQuery(final String uri, final String appendQuery) throws URISyntaxException {
    URI oldUri = new URI(uri);
    return appendQuery(oldUri, appendQuery);
  }

  public static URI appendQuery(final URI uri, final String paramName, final String paramValue)
      throws URISyntaxException {
    return appendQuery(uri, paramName + "=" + paramValue);
  }

  public static URI appendQuery(final URI uri, final String appendQuery) throws URISyntaxException {
    String newQuery = uri.getQuery();
    if (newQuery == null) {
      newQuery = appendQuery;
    } else {
      newQuery += "&" + appendQuery;
    }

    return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, uri.getFragment());
  }
}
