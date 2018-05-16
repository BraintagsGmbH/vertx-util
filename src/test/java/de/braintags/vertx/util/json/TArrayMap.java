package de.braintags.vertx.util.json;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.braintags.vertx.util.json.deserializers.ArrayMap;
import de.braintags.vertx.util.json.deserializers.ArrayMapModule;
import de.braintags.vertx.util.json.deserializers.ComplexKey;
import io.vertx.core.json.Json;

/**
 * Unit test for {@link MapAsArraySerializer}
 * 
 *
 */
public class TArrayMap {

  @BeforeClass
  public static void prepare() throws IOException {
    JsonConfig.addConfig(mapper -> mapper.registerModule(new ArrayMapModule()));
  }

  @Test
  public void testSerializationRoundtrip() throws IOException {
    NestedMapObject testData = buildNestedMapObject();

    JsonNode valueTree = Json.mapper.valueToTree(testData);
    JsonNode mapNode = valueTree.get("map");
    Assert.assertNotNull(mapNode);
    Assert.assertTrue(mapNode.isObject());
    JsonNode mapArrayNode = mapNode.get(ArrayMapSerializer.ARRAY_MAP);
    Assert.assertNotNull(mapArrayNode);
    Assert.assertTrue(mapArrayNode.isArray());

    NestedMapObject decoded = Json.mapper.treeToValue(valueTree, NestedMapObject.class);

    assertTrue(decoded.equals(testData));
  }

  @Test
  public void testArrayNodeConvert() throws IOException {
    NestedMapObject testData = buildNestedMapObject();

    JsonNode valueTree = Json.mapper.valueToTree(testData);
    JsonNode mapNode = valueTree.get("map");
    Assert.assertNotNull(mapNode);
    Assert.assertTrue(mapNode.isObject());

    JsonNode convertedValueTree = ArrayMapNode.deepConvertNode(valueTree);
    Assert.assertTrue(convertedValueTree != valueTree);

    JsonNode convertedMapNode = convertedValueTree.get("map");
    Assert.assertNotNull(convertedMapNode);
    Assert.assertTrue(convertedMapNode instanceof ArrayMapNode);
  }

  @Test
  public void testArrayMapDiff() throws IOException {
    NestedMapObject testDataBase = buildNestedMapObject();
    NestedMapObject testData = buildNestedMapObject();

    modifyMap(testData, "");

    JsonNode valueTreeBase = Json.mapper.valueToTree(testDataBase);
    JsonNode valueTreeData = Json.mapper.valueToTree(testData);

    JsonNode diff = JsonDiff.getDiff(valueTreeBase, valueTreeData);

    JsonNode diffApplied = JsonDiff.applyDiff(valueTreeBase, diff);
    Assert.assertEquals(valueTreeData, diffApplied);
    NestedMapObject decodedTextData = Json.mapper.treeToValue(diffApplied, NestedMapObject.class);
    Assert.assertEquals(testData, decodedTextData);
  }

  @Test
  public void testArrayMapSquashDiff() throws IOException {
    NestedMapObject testDataBase = buildNestedMapObject();
    NestedMapObject testData = buildNestedMapObject();

    modifyMap(testData, "1");
    JsonNode valueTreeBase = Json.mapper.valueToTree(testDataBase);
    JsonNode valueTreeData = Json.mapper.valueToTree(testData);
    modifyMap(testData, "2");
    JsonNode secondModification = Json.mapper.valueToTree(testData);

    JsonNode diff = JsonDiff.getDiff(valueTreeBase, valueTreeData);
    JsonNode secondDiff = JsonDiff.getDiff(valueTreeData, secondModification);

    diff = JsonDiff.squashDiff(diff, secondDiff);

    JsonNode diffApplied = JsonDiff.applyDiff(valueTreeBase, diff);
    Assert.assertEquals(secondModification, diffApplied);
    NestedMapObject decodedTextData = Json.mapper.treeToValue(diffApplied, NestedMapObject.class);
    Assert.assertEquals(testData, decodedTextData);
  }

  private void modifyMap(final NestedMapObject testData, final String suffix) {
    Iterator<Entry<MapContainer, MapContainer>> entryIterator = testData.map.entrySet().iterator();
    Entry<MapContainer, MapContainer> firstEntry = entryIterator.next();
    firstEntry.setValue(buildMapContainer("freshValue" + suffix));
    // Entry<MapContainer, MapContainer> secondEntry = entryIterator.next();
    // entryIterator.remove();
    // testData.map.put(buildMapContainer("NewKey2" + suffix), buildMapContainer("NewValue2" + suffix));
  }

  private NestedMapObject buildNestedMapObject() {
    NestedMapObject test = new NestedMapObject();
    test.map.put(buildMapContainer("Key1"), buildMapContainer("Value1"));
    test.map.put(buildMapContainer("Key2"), buildMapContainer("Value2"));
    // test.map.put(buildMapContainer("KeyNull"), null);
    return test;
  }

  private MapContainer buildMapContainer(final String prefix) {
    MapContainer test = new MapContainer();
    test.map.put(ComplexKey.create(prefix + "12345", "Hello", "World"), 10);
    return test;
  }

  public static class NestedMapObject {
    @JsonSerialize(as = ArrayMap.class)
    @JsonDeserialize(as = ArrayMap.class)
    public Map<MapContainer, MapContainer> map;

    public NestedMapObject() {
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
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      NestedMapObject other = (NestedMapObject) obj;
      if (map == null) {
        if (other.map != null)
          return false;
      } else if (!map.equals(other.map))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "MapContainer [map=" + map + "]";
    }

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
    public boolean equals(final Object obj) {
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

    @Override
    public String toString() {
      return "MapContainer [map=" + map + "]";
    }

  }

}
