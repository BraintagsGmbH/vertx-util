package de.braintags.vertx.util.json.deserializers;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
    JsonConfig.addConfig(mapper -> mapper.registerModule(new ArrayMapModule()));

    MapContainer test = new MapContainer();
    test.map.put(ComplexKey.create("12345", "Hello", "World"), 10);
    test.map.put(ComplexKey.create("67890", "foo", "bar"), 20);

    JsonNode valueTree = Json.mapper.valueToTree(test);
    JsonNode mapNode = valueTree.get("map");
    Assert.assertNotNull(mapNode);
    Assert.assertTrue(mapNode.isObject());
    JsonNode mapArrayNode = mapNode.get(ArrayMapSerializer.ARRAY_MAP);
    Assert.assertNotNull(mapArrayNode);
    Assert.assertTrue(mapArrayNode.isArray());

    MapContainer decoded = Json.mapper.treeToValue(valueTree, MapContainer.class);

    assertTrue(decoded.equals(test));
  }

  public static class MapContainer {
    @JsonSerialize(as = ArrayMap.class)
    @JsonDeserialize(as = ArrayMap.class)
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

}
