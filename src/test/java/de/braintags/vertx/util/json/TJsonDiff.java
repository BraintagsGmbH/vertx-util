/*-
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
package de.braintags.vertx.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.braintags.vertx.util.json.deserializers.ArrayMap;
import de.braintags.vertx.util.json.deserializers.ArrayMapSerializer;
import de.braintags.vertx.util.json.deserializers.ComplexKey;
import io.vertx.core.json.Json;

public class TJsonDiff {

  private static final ComplexKey ARRAY_MAP_KEY_1 = ComplexKey.create("Key1", "foo", "bar");
  private static final ComplexKey ARRAY_MAP_KEY_REMOVED = ComplexKey.create("Key_Removed", "Hello", "World");

  @BeforeClass
  public static void setup() {
    JsonConfig.staticInit();
  }

  /**
   * Test method for {@link de.braintags.vertx.util.json.JsonDiff#getDiff(ObjectNode, ObjectNode, JsonNodeFactory)} and
   * {@link de.braintags.vertx.util.json.JsonDiff#applyDiff(ObjectNode, ObjectNode)}..
   */
  @Test
  public void testSimpleField() {
    JsonNodeFactory nc = new JsonNodeFactory(false);
    ObjectNode base = nc.objectNode();
    ObjectNode data = nc.objectNode();

    base.set("equalField", nc.numberNode(10));
    data.set("equalField", nc.numberNode(10));

    base.set("removedField", nc.numberNode(42));
    data.set("newField", nc.numberNode(43));

    base.set("diffField", nc.numberNode(10));
    NumericNode diffNumber = nc.numberNode(11);
    data.set("diffField", diffNumber);

    // do diff
    ObjectNode baseBackup = base.deepCopy();
    ObjectNode dataBackup = data.deepCopy();
    JsonNode diff = JsonDiff.getDiff(base, data, nc);

    // check base not modified
    assertEquals(baseBackup, base);
    assertEquals(dataBackup, data);

    assertTrue(diff.has("diffField"));
    assertEquals(diff.get("diffField"), diffNumber);
    assertFalse(diff.has("equalField"));

    // removed fields are set to null
    assertEquals(diff.get("removedField"), nc.nullNode());
    assertEquals(diff.get("newField"), nc.numberNode(43));

    // apply diff and compare
    ObjectNode undiff = baseBackup.deepCopy();
    undiff = (ObjectNode) JsonDiff.applyDiff(undiff, diff);

    assertEquals(undiff.get("removedField"), nc.nullNode());
    undiff.remove("removedField");
    assertEquals(data, undiff);
  }

  /**
   * Test method for {@link de.braintags.vertx.util.json.JsonDiff#getDiff(ObjectNode, ObjectNode, JsonNodeFactory)} and
   * {@link de.braintags.util.json.JsonDiff#applyDiff(ObjectNode, ObjectNode).
   */
  @Test
  public void testDeepDiff() {
    JsonNodeFactory nc = new JsonNodeFactory(false);
    ObjectNode base = nc.objectNode();
    ObjectNode data = nc.objectNode();

    ObjectNode baseInner = nc.objectNode();
    ObjectNode dataInner = nc.objectNode();

    baseInner.set("equalField", nc.numberNode(10));
    dataInner.set("equalField", nc.numberNode(10));

    baseInner.set("diffField", nc.numberNode(10));
    NumericNode diffNumber = nc.numberNode(11);
    dataInner.set("diffField", diffNumber);

    base.set("inner", baseInner);
    data.set("inner", dataInner);

    // do diff
    ObjectNode baseBackup = base.deepCopy();
    ObjectNode dataBackup = data.deepCopy();
    JsonNode diff = JsonDiff.getDiff(base, data, nc);

    // check base not modified
    assertEquals(baseBackup, base);
    assertEquals(dataBackup, data);

    // check the diff
    assertTrue(diff.has("inner"));
    JsonNode diffInner = diff.get("inner");
    assertTrue(diffInner.isObject());

    ObjectNode diffInnerObject = (ObjectNode) diffInner;

    assertTrue(diffInnerObject.has("diffField"));
    assertEquals(diffInnerObject.get("diffField"), diffNumber);
    assertFalse(diffInnerObject.has("equalField"));

    // apply diff and compare
    ObjectNode undiff = baseBackup.deepCopy();
    undiff = (ObjectNode) JsonDiff.applyDiff(undiff, diff);
    assertEquals(data, undiff);
  }

  /**
   * Test method for {@link de.braintags.vertx.util.json.JsonDiff#getDiff(ObjectNode, ObjectNode, JsonNodeFactory)}. and
   * {@link de.braintags.util.json.JsonDiff#applyDiff(ObjectNode, ObjectNode).
   */
  @Test
  public void testArrayDiff() {
    JsonNodeFactory nc = new JsonNodeFactory(false);

    // create test data
    ObjectNode base = nc.objectNode();
    ObjectNode data = nc.objectNode();

    ArrayNode baseArray = nc.arrayNode();
    ArrayNode dataArray = nc.arrayNode();

    ObjectNode baseInner = nc.objectNode();
    ObjectNode dataInner = nc.objectNode();

    baseInner.set("equalField", nc.numberNode(10));
    dataInner.set("equalField", nc.numberNode(10));

    baseInner.set("diffField", nc.numberNode(10));
    NumericNode diffNumber = nc.numberNode(11);
    dataInner.set("diffField", diffNumber);

    baseArray.add(baseInner);
    baseArray.add(0);
    baseArray.add(1);
    baseArray.add(2);
    baseArray.add(3);
    baseArray.add(4);

    dataArray.add(dataInner);
    dataArray.add(4);
    dataArray.add(1);
    dataArray.add(0);
    dataArray.add(5);

    base.set("array", baseArray);
    data.set("array", dataArray);

    // do diff
    ObjectNode baseBackup = base.deepCopy();
    ObjectNode dataBackup = data.deepCopy();
    JsonNode diff = JsonDiff.getDiff(base, data, nc);

    // check base not modified
    assertEquals(baseBackup, base);
    assertEquals(dataBackup, data);

    // check the diff
    assertTrue(diff.has("array"));
    JsonNode diffArray = diff.get("array");
    assertTrue(diffArray.isArray());
    assertEquals(5, diffArray.size());

    assertTrue(diffArray.get(0).isObject());
    JsonNode objectDiff = diffArray.get(0);
    assertTrue(objectDiff.has(JsonDiff.DIFF));
    JsonNode innerDiff = objectDiff.get(JsonDiff.DIFF);
    assertTrue(innerDiff.has("diffField"));
    assertEquals(nc.numberNode(11), innerDiff.get("diffField"));

    assertEquals(nc.numberNode(5), diffArray.get(1));
    assertEquals(nc.numberNode(2), diffArray.get(2)); // not changed
    assertEquals(nc.numberNode(1), diffArray.get(3));
    JsonNode numberDiff = diffArray.get(4);
    assertTrue(numberDiff.has(JsonDiff.DIFF));
    assertEquals(nc.numberNode(5), numberDiff.get(JsonDiff.DIFF));

    // apply diff and compare
    ObjectNode undiff = baseBackup.deepCopy();
    undiff = (ObjectNode) JsonDiff.applyDiff(undiff, diff);
    assertEquals(data, undiff);
  }

  /**
   * Test method for {@link de.braintags.vertx.util.json.JsonDiff#getDiff(ObjectNode, ObjectNode, JsonNodeFactory)}. and
   * {@link de.braintags.util.json.JsonDiff#applyDiff(ObjectNode, ObjectNode).
   * 
   * @throws JsonProcessingException
   */
  @Test
  public void testPojoDiff() throws JsonProcessingException {
    ObjectMapper objectMapper = Json.mapper;
    Triple<ObjectNode, ObjectNode, SimplePojo> pojos = getDiffPojos(objectMapper);

    JsonNode diff = JsonDiff.getDiff(pojos.getLeft().deepCopy(), pojos.getMiddle(), objectMapper.getNodeFactory());

    ObjectNode pojoJson = (ObjectNode) JsonDiff.applyDiff(pojos.getLeft().deepCopy(), diff);
    Object decodedPojo = objectMapper.treeToValue(pojoJson, SimplePojo.class);

    assertEquals(pojos.getRight(), decodedPojo);
  }

  private Triple<ObjectNode, ObjectNode, SimplePojo> getDiffPojos(final ObjectMapper objectMapper) {
    SimplePojo pojo = new SimplePojo();

    SimplePojo recursive = new SimplePojo();
    recursive.setInteger(42);
    pojo.setRecursive(recursive);
    pojo.setRemoved(new SimplePojo());
    pojo.setInteger(10);
    pojo.setString("Hello");
    pojo.setArray(new ArrayList<>(Arrays.asList(new SimplePojo(41), new SimplePojo(42), new SimplePojo(43))));
    ArrayMap<ComplexKey, SimplePojo> arrayMap = new ArrayMap<>();

    SimplePojo pojo1 = new SimplePojo();
    SimplePojo pojoRemoved = new SimplePojo();

    pojo1.string = "POJO 1";
    pojo1.integer = 10;
    pojoRemoved.string = "POJO 2";
    arrayMap.put(ARRAY_MAP_KEY_1, pojo1);
    arrayMap.put(ARRAY_MAP_KEY_REMOVED, pojoRemoved);

    pojo.setArrayMap(arrayMap);
    ObjectNode pojoJson = objectMapper.valueToTree(pojo);

    pojo.setRemoved(null);
    recursive.setInteger(2);
    recursive.setString("World");
    pojo.setInteger(5);
    pojo.getArray().remove(0);
    pojo.getArray().get(0).setInteger(100);
    pojo.getArray().add(1, new SimplePojo(102));
    pojo.getArray().add(1, new SimplePojo(103));
    pojo.getArray().get(3).setString("Hello");
    pojo1.string = "POJO 1 Modified";
    arrayMap.remove(ARRAY_MAP_KEY_REMOVED);
    ObjectNode modifiedPojoJson = objectMapper.valueToTree(pojo);
    
    return Triple.of(pojoJson, modifiedPojoJson, pojo);
  }

  /**
   * Test method for {@link de.braintags.vertx.util.json.JsonDiff#squashDiff(JsonNode, JsonNode)}.
   * 
   * @throws JsonProcessingException
   */
  @Test
  public void testSquashDiff() throws JsonProcessingException {
    ObjectMapper objectMapper = Json.mapper;
    Triple<ObjectNode, ObjectNode, SimplePojo> pojos = getDiffPojos(objectMapper);

    JsonNode diff = JsonDiff.getDiff(pojos.getLeft().deepCopy(), pojos.getMiddle(), objectMapper.getNodeFactory());

    SimplePojo pojo = pojos.getRight();

    pojo.setRemoved(null);
    pojo.getRecursive().setInteger(5);
    pojo.getRecursive().setString("Foo");
    pojo.setInteger(10);
    pojo.getArray().remove(0);
    pojo.getArray().add(1, new SimplePojo(102));

    SimplePojo arrayMapPojo = pojo.getArrayMap().get(ARRAY_MAP_KEY_1);
    arrayMapPojo.string = "POJO Second Modification";
    arrayMapPojo.integer = 20;

    ObjectNode secondModification = objectMapper.valueToTree(pojo);

    JsonNode secondDiff = JsonDiff.getDiff(pojos.getMiddle(), secondModification, objectMapper.getNodeFactory());

    JsonNode squashedDiff = JsonDiff.squashDiff(diff, secondDiff);

    ObjectNode pojoJson = (ObjectNode) JsonDiff.applyDiff(pojos.getLeft().deepCopy(), squashedDiff);

    Object decodedPojo = objectMapper.treeToValue(pojoJson, SimplePojo.class);
    assertEquals(pojo, decodedPojo);
  }

  /**
   * Test method for {@link de.braintags.vertx.util.json.JsonDiff#retainDiffTree(JsonNode, JsonNode, JsonNode)}.
   * 
   * @throws JsonProcessingException
   */
  @Test
  public void testRetainDiffTree() throws JsonProcessingException {
    ObjectMapper objectMapper = Json.mapper;
    Triple<ObjectNode, ObjectNode, SimplePojo> pojos = getDiffPojos(objectMapper);

    ObjectNode diff = (ObjectNode) JsonDiff.getDiff(pojos.getLeft().deepCopy(), pojos.getMiddle(),
        objectMapper.getNodeFactory());

    diff.remove("array");
    ObjectNode secondDiff = diff.deepCopy();
    secondDiff.remove("removed");
    ObjectNode recursive = (ObjectNode) secondDiff.get("recursive");
    recursive.remove("integer");
    secondDiff.remove("integer");
    ObjectNode arrayMap = (ObjectNode) secondDiff.get("arrayMap");
    ArrayNode arrayMapInternal = (ArrayNode) arrayMap.get(ArrayMapSerializer.ARRAY_MAP);
    ObjectNode entry = (ObjectNode) arrayMapInternal.get(0);
    ObjectNode value = (ObjectNode) entry.get(ArrayMapSerializer.VALUE);
    assertNotNull(value.remove("string"));

    secondDiff = (ObjectNode) JsonDiff.retainDiffTree(pojos.getMiddle(), diff, secondDiff);

    assertEquals(diff, secondDiff);
  }

  private static class SimplePojo {

    private SimplePojo recursive;
    private SimplePojo removed;
    private int        integer;
    private String     string;

    private List<SimplePojo> array;

    private ArrayMap<ComplexKey, SimplePojo> arrayMap;

    public SimplePojo() {
    }

    public SimplePojo(final int integer) {
      this.integer = integer;
    }

    public SimplePojo getRecursive() {
      return recursive;
    }

    public void setRecursive(final SimplePojo recursive) {
      this.recursive = recursive;
    }

    public SimplePojo getRemoved() {
      return removed;
    }

    public void setRemoved(final SimplePojo removed) {
      this.removed = removed;
    }

    public int getInteger() {
      return integer;
    }

    public void setInteger(final int integer) {
      this.integer = integer;
    }

    public String getString() {
      return string;
    }

    public void setString(final String string) {
      this.string = string;
    }

    public List<SimplePojo> getArray() {
      return array;
    }

    public void setArray(final List<SimplePojo> array) {
      this.array = array;
    }

    public ArrayMap<ComplexKey, SimplePojo> getArrayMap() {
      return arrayMap;
    }

    public void setArrayMap(final ArrayMap<ComplexKey, SimplePojo> arrayMap) {
      this.arrayMap = arrayMap;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((array == null) ? 0 : array.hashCode());
      result = prime * result + ((arrayMap == null) ? 0 : arrayMap.hashCode());
      result = prime * result + integer;
      result = prime * result + ((recursive == null) ? 0 : recursive.hashCode());
      result = prime * result + ((removed == null) ? 0 : removed.hashCode());
      result = prime * result + ((string == null) ? 0 : string.hashCode());
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
      SimplePojo other = (SimplePojo) obj;
      if (array == null) {
        if (other.array != null)
          return false;
      } else if (!array.equals(other.array))
        return false;
      if (arrayMap == null) {
        if (other.arrayMap != null)
          return false;
      } else if (!arrayMap.equals(other.arrayMap))
        return false;
      if (integer != other.integer)
        return false;
      if (recursive == null) {
        if (other.recursive != null)
          return false;
      } else if (!recursive.equals(other.recursive))
        return false;
      if (removed == null) {
        if (other.removed != null)
          return false;
      } else if (!removed.equals(other.removed))
        return false;
      if (string == null) {
        if (other.string != null)
          return false;
      } else if (!string.equals(other.string))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "SimplePojo [recursive=" + recursive + ", removed=" + removed + ", integer=" + integer + ", string="
          + string + ", array=" + array + ", arrayMap=" + arrayMap + "]";
    }

  }
}
