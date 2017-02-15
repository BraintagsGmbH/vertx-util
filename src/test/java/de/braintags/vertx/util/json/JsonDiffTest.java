package de.braintags.vertx.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.braintags.vertx.util.json.JsonDiff;

public class JsonDiffTest {

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
    assertTrue(diffArray.isObject());
    assertTrue(diffArray.has("size"));
    assertEquals(diffArray.get("size").intValue(), 5);

    assertTrue(diffArray.has("0"));
    JsonNode objectDiff = diffArray.get("0");
    assertTrue(objectDiff.has("diffField"));
    assertEquals(nc.numberNode(11), objectDiff.get("diffField"));

    assertEquals(nc.numberNode(5), diffArray.get("#1"));
    assertFalse(diffArray.has("#2")); // not changed
    assertEquals(nc.numberNode(1), diffArray.get("#3"));
    assertFalse(diffArray.has("#4")); // removed
    assertFalse(diffArray.has("#5")); // removed

    assertEquals(diffArray.get("4"), nc.numberNode(5));

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
    SimplePojo pojo = new SimplePojo();

    SimplePojo recursive = new SimplePojo();
    recursive.setInteger(42);
    pojo.setRecursive(recursive);
    pojo.setRemoved(new SimplePojo());
    pojo.setInteger(10);
    pojo.setString("Hello");
    pojo.setArray(new ArrayList<>(Arrays.asList(new SimplePojo(41), new SimplePojo(42), new SimplePojo(43))));

    ObjectMapper objectMapper = new ObjectMapper();
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

    ObjectNode modifiedPojoJson = objectMapper.valueToTree(pojo);

    JsonNode diff = JsonDiff.getDiff(pojoJson, modifiedPojoJson, objectMapper.getNodeFactory());

    pojoJson = (ObjectNode) JsonDiff.applyDiff(pojoJson, diff);
    Object decodedPojo = objectMapper.treeToValue(pojoJson, SimplePojo.class);

    assertEquals(pojo, decodedPojo);
  }

  private static class SimplePojo {

    private SimplePojo recursive;
    private SimplePojo removed;
    private int        integer;
    private String     string;

    private List<SimplePojo> array;
    
    public SimplePojo() {
    }

    public SimplePojo(int integer) {
      this.integer = integer;
    }

    public SimplePojo getRecursive() {
      return recursive;
    }

    public void setRecursive(SimplePojo recursive) {
      this.recursive = recursive;
    }

    public SimplePojo getRemoved() {
      return removed;
    }

    public void setRemoved(SimplePojo removed) {
      this.removed = removed;
    }

    public int getInteger() {
      return integer;
    }

    public void setInteger(int integer) {
      this.integer = integer;
    }

    public String getString() {
      return string;
    }

    public void setString(String string) {
      this.string = string;
    }

    public List<SimplePojo> getArray() {
      return array;
    }

    public void setArray(List<SimplePojo> array) {
      this.array = array;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((array == null) ? 0 : array.hashCode());
      result = prime * result + integer;
      result = prime * result + ((recursive == null) ? 0 : recursive.hashCode());
      result = prime * result + ((removed == null) ? 0 : removed.hashCode());
      result = prime * result + ((string == null) ? 0 : string.hashCode());
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
      SimplePojo other = (SimplePojo) obj;
      if (array == null) {
        if (other.array != null)
          return false;
      } else if (!array.equals(other.array))
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
    
  }
}
