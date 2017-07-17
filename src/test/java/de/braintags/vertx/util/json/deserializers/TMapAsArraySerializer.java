package de.braintags.vertx.util.json.deserializers;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.braintags.vertx.util.AbstractIdentifier;
import de.braintags.vertx.util.json.ArrayMap;
import de.braintags.vertx.util.json.JsonConfig;
import io.vertx.core.json.Json;

/**
 * Unit test for {@link MapAsArraySerializer}
 * 
 *
 */
public class TMapAsArraySerializer {

  @Test
  public void testDeserialization_withCustomDeserializer() throws IOException {
    SimpleModule module = new SimpleModule();
    JsonSerializer se = new ArrayMapSerializer();
    module.addSerializer(ArrayMap.class, se);
    JsonConfig.addConfig(mapper -> mapper.registerModule(module));

    MapContainer test = new MapContainer();
    test.map.put(ComplexKey.create("12345", "Hello", "World"), 10);
    test.map.put(ComplexKey.create("67890", "foo", "bar"), 20);

    JsonNode valueTree = Json.mapper.valueToTree(test);
    JsonNode mapNode = valueTree.get("map");
    System.out.println(valueTree);
    Assert.assertNotNull(mapNode);
    Assert.assertTrue(mapNode.isArray());

    MapContainer decoded = Json.mapper.treeToValue(valueTree, MapContainer.class);

    assertTrue(decoded.equals(test));
  }

  public static class MapContainer {
    @JsonSerialize(as = ArrayMap.class)
    public Map<ComplexKey, Integer> map;

    public MapContainer() {
      map = new LinkedHashMap<>();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((map == null) ? 0 : map.hashCode());
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
      MapContainer other = (MapContainer) obj;
      if (map == null) {
        if (other.map != null)
          return false;
      } else if (!map.equals(other.map))
        return false;
      return true;
    }

  }

  public static class ComplexKey extends AbstractIdentifier {

    private Map<String, String> keyProperties;

    public static ComplexKey create(String identifier, String... properties) {
      ComplexKey result = new ComplexKey(identifier);
      for (int i = 0; i < properties.length - 1; i += 2) {
        result.getKeyProperties().put(properties[i], properties[i + 1]);
      }
      return result;
    }

    @JsonCreator
    protected ComplexKey(String identifier) {
      super(identifier);
      setKeyProperties(new LinkedHashMap<>());
    }

    public Map<String, String> getKeyProperties() {
      return keyProperties;
    }

    public void setKeyProperties(Map<String, String> keyProperties) {
      this.keyProperties = keyProperties;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((keyProperties == null) ? 0 : keyProperties.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (!super.equals(obj))
        return false;
      if (getClass() != obj.getClass())
        return false;
      ComplexKey other = (ComplexKey) obj;
      if (keyProperties == null) {
        if (other.keyProperties != null)
          return false;
      } else if (!keyProperties.equals(other.keyProperties))
        return false;
      return true;
    }

  }
}
