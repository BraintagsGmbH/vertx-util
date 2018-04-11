package de.braintags.vertx.util.security;

import java.util.List;

import io.vertx.core.json.JsonObject;

public class JWTOptions {

  private static final IllegalArgumentException INCOMPATIBLE_CONFIG = new IllegalArgumentException(
      "Can only set expiresIn to seconds or minutes");

  private JsonObject header;
  private String algorithm = "HS256";
  private boolean noTimestamp = false;
  private int expiresInMinutes;
  private int expiresInSeconds;
  private List<String> audience;
  private String issuer;
  private String subject;

  public JsonObject getHeader() {
    return header;
  }

  public JWTOptions setHeader(final JsonObject header) {
    this.header = header;
    return this;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public JWTOptions setAlgorithm(final String algorithm) {
    this.algorithm = algorithm;
    return this;
  }

  public boolean isNoTimestamp() {
    return noTimestamp;
  }

  public JWTOptions setNoTimestamp(final boolean noTimestamp) {
    this.noTimestamp = noTimestamp;
    return this;
  }

  public long getExpiresInMinutes() {
    return expiresInMinutes;
  }

  public JWTOptions setExpiresInMinutes(final int expiresInMinutes) {
    if (expiresInSeconds != 0) {
      throw INCOMPATIBLE_CONFIG;
    }
    this.expiresInMinutes = expiresInMinutes;
    return this;
  }

  public long getExpiresInSeconds() {
    return expiresInSeconds;
  }

  public JWTOptions setExpiresInSeconds(final int expiresInSeconds) {
    if (expiresInMinutes != 0) {
      throw INCOMPATIBLE_CONFIG;
    }
    this.expiresInSeconds = expiresInSeconds;
    return this;
  }

  public List<String> getAudience() {
    return audience;
  }

  public JWTOptions setAudience(final List<String> audience) {
    this.audience = audience;
    return this;
  }

  public String getIssuer() {
    return issuer;
  }

  public JWTOptions setIssuer(final String issuer) {
    this.issuer = issuer;
    return this;
  }

  public String getSubject() {
    return subject;
  }

  public JWTOptions setSubject(final String subject) {
    this.subject = subject;
    return this;
  }

  public io.vertx.ext.jwt.JWTOptions asNativeOptions() {
    io.vertx.ext.jwt.JWTOptions options = new io.vertx.ext.jwt.JWTOptions();
    options.setAlgorithm(algorithm);
    options.setNoTimestamp(noTimestamp);
    if (header != null) {
      options.setHeader(header);
    }
    if (audience != null) {
      options.setAudience(audience);
    }
    if (issuer != null) {
      options.setIssuer(issuer);
    }
    if (subject != null) {
      options.setSubject(subject);
    }
    if (expiresInMinutes != 0) {
      options.setExpiresInMinutes(expiresInMinutes);
    }
    if (expiresInSeconds != 0) {
      options.setExpiresInSeconds(expiresInSeconds);
    }

    return options;
  }
}
