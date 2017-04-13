package de.braintags.vertx.util.json;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Unit test for {@link JsonConverter}
 * 
 * @author sschmitt
 * 
 */
public class TJsonConverter {

  @Before
  public void prepareTest() {
    JsonConfig.configureObjectMapper(Json.mapper);
  }

  private ObjectNode createTestObjectNode() {
    ObjectNode object = Json.mapper.createObjectNode();
    object.put("text", "value");
    object.put("int", 1);
    object.put("long", 1l);
    object.put("double", 1.0d);
    return object;
  }

  private void assertJsonObject(final JsonObject jsonObject) {
    assertThat(jsonObject.getString("text"), is("value"));
    assertThat(jsonObject.getInteger("int"), is(1));
    assertThat(jsonObject.getLong("long"), is(1l));
    assertThat(jsonObject.getDouble("double"), is(1.0d));
  }

  private ArrayNode createTestArrayNode() {
    ArrayNode array = Json.mapper.createArrayNode();
    array.add("value");
    array.add(1);
    array.add(1l);
    array.add(1.0d);
    return array;
  }


  private void assertJsonArray(final JsonArray jsonArray, final ArrayNode expected) throws IOException {
    assertThat(jsonArray.size(), is(expected.size()));
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonNode expectedValue = expected.get(i);
      Object jsonValue = jsonArray.getValue(i);
      assertThat(jsonValue, is(JsonConverter.convertValueNode(expectedValue)));
    }
  }

  private void assertValueNode(final JsonNode valueNode, final Object expected, final Class<?> expectedClass)
      throws IOException {
    Object vertxNode = JsonConverter.convertJsonNodeToVertx(valueNode);
    assertThat(vertxNode, instanceOf(expectedClass));
    assertThat(vertxNode, is(expected));

    Object value = JsonConverter.convertValueNode(valueNode);
    assertThat(value, instanceOf(expectedClass));
    assertThat(value, is(expected));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueConversion_invalidNode_objectNode() throws IOException {
    JsonConverter.convertValueNode(Json.mapper.createObjectNode());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueConversion_invalidNode_arrayNode() throws IOException {
    JsonConverter.convertValueNode(Json.mapper.createArrayNode());
  }

  @Test
  public void testJsonConversion_objectNode_simple() throws IOException {
    ObjectNode object = createTestObjectNode();
    Object vertxNode = JsonConverter.convertJsonNodeToVertx(object);
    assertThat(vertxNode, instanceOf(JsonObject.class));
    JsonObject vertxObject = (JsonObject) vertxNode;
    assertJsonObject(vertxObject);
  }

  @Test
  public void testJsonConversion_objectNode_complex() throws IOException {
    ObjectNode object = createTestObjectNode();
    ObjectNode subObject = createTestObjectNode();
    ObjectNode subSubObject = createTestObjectNode();
    object.set("sub_object", subObject);
    subObject.set("sub_sub_object", subSubObject);

    ArrayNode subArray = createTestArrayNode();
    ArrayNode subSubArray = createTestArrayNode();
    object.set("sub_array", subArray);
    subObject.set("sub_sub_array", subSubArray);
    Object vertxNode = JsonConverter.convertJsonNodeToVertx(object);
    assertThat(vertxNode, instanceOf(JsonObject.class));

    JsonObject vertxObject = (JsonObject) vertxNode;
    assertJsonObject(vertxObject);

    JsonArray jsonArray = vertxObject.getJsonArray("sub_array");
    assertJsonArray(jsonArray, subArray);
    JsonObject subVertx = vertxObject.getJsonObject("sub_object");
    assertJsonObject(subVertx);

    JsonObject subSubVertxObject = subVertx.getJsonObject("sub_sub_object");
    assertJsonObject(subSubVertxObject);
    JsonArray subSubVertxArray = subVertx.getJsonArray("sub_sub_array");
    assertJsonArray(subSubVertxArray, subSubArray);
  }

  @Test
  public void testJsonConversion_arrayNode_simple() throws IOException {
    ArrayNode array = createTestArrayNode();
    Object vertxNode = JsonConverter.convertJsonNodeToVertx(array);
    assertThat(vertxNode, instanceOf(JsonArray.class));
    JsonArray vertxArray = (JsonArray) vertxNode;
    assertJsonArray(vertxArray, array);
  }

  @Test
  public void testJsonConversion_arrayNode_complex() throws IOException {
    ArrayNode array = Json.mapper.createArrayNode();
    ObjectNode objectNode = createTestObjectNode();
    ArrayNode subArrayNode = createTestArrayNode();
    array.add(objectNode);
    array.add(subArrayNode);
    Object vertxNode = JsonConverter.convertJsonNodeToVertx(array);
    assertThat(vertxNode, instanceOf(JsonArray.class));
    JsonArray vertxArray = (JsonArray) vertxNode;
    JsonObject subObject = vertxArray.getJsonObject(0);
    assertJsonObject(subObject);
    JsonArray subArray = vertxArray.getJsonArray(1);
    assertJsonArray(subArray, subArrayNode);
  }

  @Test
  public void testJsonConversion_intNode() throws IOException {
    IntNode intNode = new IntNode(1);
    assertValueNode(intNode, 1, Integer.class);
  }

  @Test
  public void testJsonConversion_longNode() throws IOException {
    LongNode longNode = new LongNode(1l);
    assertValueNode(longNode, 1l, Long.class);
  }

  @Test
  public void testJsonConversion_doubleNode() throws IOException {
    DoubleNode doubleNode = new DoubleNode(1.5d);
    assertValueNode(doubleNode, 1.5d, Double.class);
  }

  @Test
  public void testJsonConversion_binaryNode() throws IOException {
    byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);
    BinaryNode binaryNode = new BinaryNode(bytes);
    assertValueNode(binaryNode, bytes, byte[].class);
  }

  @Test
  public void testJsonConversion_booleanNode() throws IOException {
    BooleanNode booleanNode = BooleanNode.TRUE;
    assertValueNode(booleanNode, true, Boolean.class);
  }

  @Test
  public void testJsonConversion_textNode() throws IOException {
    TextNode textNode = new TextNode("test");
    assertValueNode(textNode, "test", String.class);
  }

}
