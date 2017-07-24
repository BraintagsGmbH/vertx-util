package de.braintags.vertx.util.json.deserializers;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ComplexKey {

  private Map<String, String> keyProperties;
  private final String identifier;

  public static ComplexKey create(String identifier, String... properties) {
    ComplexKey result = new ComplexKey(identifier);
    for (int i = 0; i < properties.length - 1; i += 2) {
      result.getKeyProperties().put(properties[i], properties[i + 1]);
    }
    return result;
  }

  @JsonCreator
  public ComplexKey(final String identifier) {
    this.identifier = identifier;
    setKeyProperties(new LinkedHashMap<>());
  }

  public Map<String, String> getKeyProperties() {
    return keyProperties;
  }

  public void setKeyProperties(Map<String, String> keyProperties) {
    this.keyProperties = keyProperties;
  }

  public String getIdentifier() {
    return identifier;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
    result = prime * result + ((keyProperties == null) ? 0 : keyProperties.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ComplexKey other = (ComplexKey) obj;
    if (getIdentifier() == null) {
      if (other.getIdentifier() != null)
        return false;
    } else if (!getIdentifier().equals(other.getIdentifier()))
      return false;
    if (keyProperties == null) {
      if (other.keyProperties != null)
        return false;
    } else if (!keyProperties.equals(other.keyProperties))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ComplexKey [identifier=" + identifier + ", keyProperties=" + keyProperties + "]";
  }

}