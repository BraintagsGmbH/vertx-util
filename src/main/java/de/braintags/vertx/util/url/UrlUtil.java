package de.braintags.vertx.util.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

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

  public static URI appendQuery(final URI uri, final String paramName, final String paramValue) {
    try {
      return new URIBuilder(uri).addParameter(paramName, paramValue).build();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("unable to append query: " + paramName + "=" + paramValue, e);
    }
  }

  public static URI appendQuery(final URI uri, final NameValuePair... appendQueries) {
    try {
      return new URIBuilder(uri).addParameters(Arrays.asList(appendQueries)).build();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("unable to append query", e);
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

}
