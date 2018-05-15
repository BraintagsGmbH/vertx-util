/*
 * #%L
 * Vert.x utilities from Braintags
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import com.google.common.base.Joiner;

public class HttpContentType {

  public static final String MAIN_TYPE_APPLICATION = "application";
  public static final String MAIN_TYPE_TEXT = "text";
  public static final String MAIN_TYPE_IMAGE = "image";

  public static final String SUB_TYPE_JSON = "json";
  public static final String SUB_TYPE_JAVASCRIPT = "javascript";
  public static final String SUB_TYPE_HTML = "html";
  public static final String SUB_TYPE_CSS = "css";

  public static final String SUB_TYPE_PNG = "png";
  public static final String SUB_TYPE_SVG = "svg+xml";
  public static final String SUB_TYPE_JPEG = "jpeg";

  private static final String PARAM_CHARSET = "charset";

  private static final String CHARSET_UTF8 = "utf-8";

  public static HttpContentType TEXT_HTML = new HttpContentType(MAIN_TYPE_TEXT, SUB_TYPE_HTML);
  public static HttpContentType TEXT_CSS = new HttpContentType(MAIN_TYPE_TEXT, SUB_TYPE_CSS);

  public static HttpContentType APPLICATION_JAVASCRIPT = new HttpContentType(MAIN_TYPE_APPLICATION, SUB_TYPE_JAVASCRIPT);

  public static HttpContentType JSON = new HttpContentType(MAIN_TYPE_APPLICATION, SUB_TYPE_JSON);
  public static HttpContentType JSON_UTF8 = new HttpContentType(MAIN_TYPE_APPLICATION, SUB_TYPE_JSON,
      Collections.singletonMap(PARAM_CHARSET, CHARSET_UTF8));

  public static HttpContentType IMAGE_PNG = new HttpContentType(MAIN_TYPE_IMAGE, SUB_TYPE_PNG);
  public static HttpContentType IMAGE_SVG = new HttpContentType(MAIN_TYPE_IMAGE, SUB_TYPE_SVG);
  public static HttpContentType IMAGE_JPEG = new HttpContentType(MAIN_TYPE_IMAGE, SUB_TYPE_JPEG);

  private final String value;
  private final String mainType;
  private final String subType;
  private final Map<String, String> parameters;
  private final String parameterlessValue;

  public static HttpContentType parse(final String string) {
    try {
      MimeType mimeType = new MimeType(string);
      return new HttpContentType(mimeType.getPrimaryType(), mimeType.getSubType(), toMap(mimeType.getParameters()));
    } catch (MimeTypeParseException e) {
      throw new IllegalArgumentException("unable to parse_ " + string, e);
    }
  }

  private static Map<String, String> toMap(final MimeTypeParameterList parameters) {
    LinkedHashMap<String, String> result = new LinkedHashMap<>();
    Enumeration names = parameters.getNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement().toString();
      result.put(name, parameters.get(name));
    }
    return result;
  }

  public HttpContentType(final String mainType, final String subType) {
    this(mainType, subType, Collections.emptyMap());
  }

  public HttpContentType(final String mainType, final String subType, final Map<String, String> parameters) {
    this.mainType = mainType;
    this.subType = subType;
    this.parameters = parameters;
    parameterlessValue = mainType + "/" + subType;
    this.value = parameterlessValue + (parameters.isEmpty() ? ""
            : ";" + Joiner.on(';').join(parameters.entrySet().stream()
                .map(param -> param.getKey() + "=" + param.getValue()).collect(toList())));
  }

  @Override
  public String toString() {
    return value;
  }

  public String getParameterlessValue() {
    return parameterlessValue;
  }

  public String getValue() {
    return value;
  }

  public String getMainType() {
    return mainType;
  }

  public String getSubType() {
    return subType;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mainType == null) ? 0 : mainType.hashCode());
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
    result = prime * result + ((subType == null) ? 0 : subType.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    HttpContentType other = (HttpContentType) obj;
    if (mainType == null) {
      if (other.mainType != null)
        return false;
    } else if (!mainType.equals(other.mainType))
      return false;
    if (parameters == null) {
      if (other.parameters != null)
        return false;
    } else if (!parameters.equals(other.parameters))
      return false;
    if (subType == null) {
      if (other.subType != null)
        return false;
    } else if (!subType.equals(other.subType))
      return false;
    return true;
  }
  
}
