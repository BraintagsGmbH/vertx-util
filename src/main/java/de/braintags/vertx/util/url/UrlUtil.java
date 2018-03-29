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

  public static URI appendQuery(final String uri, final String... appendQuery) throws URISyntaxException {
    URI oldUri = new URI(uri);
    return appendQuery(oldUri, appendQuery);
  }

  public static URI appendQuery(final URI uri, final String paramName, final String paramValue) {
    return appendQuery(uri, paramName + "=" + paramValue);
  }

  public static URI appendQuery(final URI uri, final String... appendQueries) {
    String newQuery = uri.getQuery();
    for (String appendQuery : appendQueries) {
      if (newQuery == null) {
        newQuery = appendQuery;
      } else {
        newQuery += "&" + appendQuery;
      }
    }
    try {
      return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), newQuery,
          uri.getFragment());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static URI ensureFolder(final URI uri) {
    String path = uri.getPath();
    if (!path.isEmpty() && path.charAt(path.length() - 1) == '/') {
      return uri;
    } else {
      try {
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path + "/", uri.getQuery(),
            uri.getFragment());
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static URI appendPath(final URI uri, final String path) {
    String oldPath = uri.getPath();
    String newPath;
    if (oldPath != null && !oldPath.isEmpty()) {
      boolean oldEnd = oldPath.charAt(oldPath.length() - 1) == '/';
      boolean pathStart = path.charAt(0) == '/';
      if (oldEnd) {
        if (pathStart)
          newPath = oldPath + path.substring(1);
        else
          newPath = oldPath + path;
      } else {
        if (pathStart)
          newPath = oldPath + path;
        else
          newPath = oldPath + '/' + path;
      }
    } else
      newPath = path;
    try {
      return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), newPath, uri.getQuery(),
          uri.getFragment());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
