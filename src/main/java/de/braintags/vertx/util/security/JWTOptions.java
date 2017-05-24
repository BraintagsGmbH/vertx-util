package de.braintags.vertx.util.security;

import io.vertx.core.json.JsonObject;

public class JWTOptions {

  private static final IllegalArgumentException INCOMPATIBLE_CONFIG = new IllegalArgumentException(
      "Can only set expiresIn to seconds or minutes");

  private JsonObject header;
  private String     algorithm   = "HS256";
  private boolean    noTimestamp = false;
  private long       expiresInMinutes;
  private long       expiresInSeconds;
  private JsonObject audience;
  private JsonObject issuer;
  private JsonObject subject;

  public JsonObject getHeader() {
    return header;
  }

  public JWTOptions setHeader(JsonObject header) {
    this.header = header;
    return this;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public JWTOptions setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
    return this;
  }

  public boolean isNoTimestamp() {
    return noTimestamp;
  }

  public JWTOptions setNoTimestamp(boolean noTimestamp) {
    this.noTimestamp = noTimestamp;
    return this;
  }

  public long getExpiresInMinutes() {
    return expiresInMinutes;
  }

  public JWTOptions setExpiresInMinutes(long expiresInMinutes) {
    if (expiresInSeconds != 0) {
      throw INCOMPATIBLE_CONFIG;
    }
    this.expiresInMinutes = expiresInMinutes;
    return this;
  }

  public long getExpiresInSeconds() {
    return expiresInSeconds;
  }

  public JWTOptions setExpiresInSeconds(long expiresInSeconds) {
    if (expiresInMinutes != 0) {
      throw INCOMPATIBLE_CONFIG;
    }
    this.expiresInSeconds = expiresInSeconds;
    return this;
  }

  public JsonObject getAudience() {
    return audience;
  }

  public JWTOptions setAudience(JsonObject audience) {
    this.audience = audience;
    return this;
  }

  public JsonObject getIssuer() {
    return issuer;
  }

  public JWTOptions setIssuer(JsonObject issuer) {
    this.issuer = issuer;
    return this;
  }

  public JsonObject getSubject() {
    return subject;
  }

  public JWTOptions setSubject(JsonObject subject) {
    this.subject = subject;
    return this;
  }

  public JsonObject asJsonObject() {
    JsonObject json = new JsonObject();
    json.put("algorithm", algorithm);
    json.put("noTimestamp", noTimestamp);
    if (header != null) {
      json.put("header", header);
    }
    if (audience != null) {
      json.put("audience", audience);
    }
    if (issuer != null) {
      json.put("issuer", issuer);
    }
    if (subject != null) {
      json.put("subject", subject);
    }
    if (expiresInMinutes != 0) {
      json.put("expiresInMinutes", expiresInMinutes);
    }
    if (expiresInSeconds != 0) {
      json.put("expiresInSeconds", expiresInSeconds);
    }

    return json;
  }
}
