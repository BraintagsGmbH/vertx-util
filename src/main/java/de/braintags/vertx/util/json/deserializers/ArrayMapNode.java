package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public class ArrayMapNode extends ValueNode {

  private Map<JsonNode, JsonNode> children;
  private final JsonNodeFactory nodeFactory;

  public static JsonNode deepConvertNode(JsonNodeFactory factory, JsonNode node) {
    if (node.isObject()) {
      if (node.size() == 1) {
        Entry<String, JsonNode> arrayMapField = node.fields().next();
        if (arrayMapField.getKey().equals(ArrayMapSerializer.ARRAY_MAP) && arrayMapField.getValue().isArray()) {
          return convertToArrayNode(factory, node, arrayMapField);
        }
      }
      return convertObjNode(factory, (ObjectNode) node);
    } else if (node.isArray()) {
      return convertArrayNode(factory, (ArrayNode) node);
    } else {
      return node;
    }
  }

  public static boolean isArrayMapNode(JsonNode node) {
    if (node.isObject() && node.size() == 1) {
      Entry<String, JsonNode> arrayMapField = node.fields().next();
      return arrayMapField.getKey().equals(ArrayMapSerializer.ARRAY_MAP) && arrayMapField.getValue().isArray();
    }
    return false;
  }

  private static JsonNode convertArrayNode(JsonNodeFactory factory, ArrayNode node) {
    ArrayNode modified = null;
    for (int i = node.size() - 1; i >= 0; i--) {
      JsonNode oldNode = node.get(i);
      JsonNode newNode = deepConvertNode(factory, oldNode);
      if (oldNode != newNode) {
        if (modified == null) {
          modified = node.arrayNode().addAll(node);
        }
        modified.set(i, newNode);
      }
    }
    return modified != null ? modified : node;
  }

  private static JsonNode convertObjNode(JsonNodeFactory factory, ObjectNode node) {
    ObjectNode modified = null;
    Iterator<Entry<String, JsonNode>> fielditer = node.fields();
    while (fielditer.hasNext()) {
      Entry<String, JsonNode> field = fielditer.next();
      JsonNode newNode = deepConvertNode(factory, field.getValue());
      if (field.getValue() != newNode) {
        if (modified == null) {
          modified = node.objectNode();
          modified.setAll(node);
        }
        modified.set(field.getKey(), newNode);
      }
    }

    return modified != null ? modified : node;
  }

  private static ArrayMapNode convertToArrayNode(JsonNodeFactory factory, JsonNode node,
      Entry<String, JsonNode> arrayMapField) {
    Map<JsonNode, JsonNode> children = new LinkedHashMap<>();
    ArrayNode entries = (ArrayNode) arrayMapField.getValue();
    for (JsonNode e : entries) {
      ObjectNode entry = (ObjectNode) e;
      JsonNode key = null;
      JsonNode value = null;
      if (entry.size() != 2) {
        throw new IllegalArgumentException(
            "malformed array map entry, expected 2 field but got " + entry.size() + " in " + entry);
      }
      Iterator<Entry<String, JsonNode>> fielditer = entry.fields();
      while (fielditer.hasNext()) {
        Entry<String, JsonNode> field = fielditer.next();
        switch (field.getKey()) {
          case ArrayMapSerializer.KEY:
            key = deepConvertNode(factory, field.getValue());
            break;
          case ArrayMapSerializer.VALUE:
            key = deepConvertNode(factory, field.getValue());
            break;
          default:
            throw new IllegalArgumentException(
                "malformed array map entry, unknown fieldname: " + field.getKey() + " in " + node);
        }
      }
      children.put(key, value);
    }
    return new ArrayMapNode(factory, children);
  }

  public ArrayMapNode(JsonNodeFactory nodeFactory) {
    this.nodeFactory = nodeFactory;
  }

  public ArrayMapNode(JsonNodeFactory nodeFactory, Map<JsonNode, JsonNode> children) {
    this.nodeFactory = nodeFactory;
    this.children = children;
  }

  /**
   * Method that can be called to serialize this node and
   * all of its descendants using specified JSON generator.
   */
  @Override
  public void serialize(JsonGenerator g, SerializerProvider provider)
      throws IOException {
    g.writeStartObject();
    innserSerialize(g, provider);
    g.writeEndObject();
  }

  private void innserSerialize(JsonGenerator g, SerializerProvider provider) throws IOException {
    g.writeFieldName(ArrayMapSerializer.ARRAY_MAP);
    g.writeStartArray();
    for (Map.Entry<JsonNode, JsonNode> en : children.entrySet()) {
      g.writeStartObject();
      g.writeFieldName(ArrayMapSerializer.KEY);
      en.getKey().serialize(g, provider);
      g.writeFieldName(ArrayMapSerializer.VALUE);
      en.getValue().serialize(g, provider);
      g.writeEndObject();
    }
    g.writeEndArray();
  }

  @Override
  public void serializeWithType(JsonGenerator g, SerializerProvider provider,
      TypeSerializer typeSer)
      throws IOException {
    @SuppressWarnings("deprecation")
    boolean trimEmptyArray = (provider != null) &&
        !provider.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    typeSer.writeTypePrefixForObject(this, g);
    innserSerialize(g, provider);
    typeSer.writeTypeSuffixForObject(this, g);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArrayMapNode deepCopy() {
    ArrayMapNode ret = new ArrayMapNode(nodeFactory);

    for (Map.Entry<JsonNode, JsonNode> entry : children.entrySet())
      ret.children.put(entry.getKey().deepCopy(), entry.getValue().deepCopy());

    return ret;
  }

  @Override
  public JsonToken asToken() {
    return JsonToken.VALUE_EMBEDDED_OBJECT;
  }

  @Override
  public int hashCode() {
    return children.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (o == null)
      return false;
    if (o instanceof ArrayMapNode) {
      return _childrenEqual((ArrayMapNode) o);
    }
    return false;
  }

  /**
   * @since 2.3
   */
  protected boolean _childrenEqual(ArrayMapNode other) {
    return children.equals(other.children);
  }

  @Override
  public boolean equals(Comparator<JsonNode> comparator, JsonNode o) {
    if (!(o instanceof ArrayMapNode)) {
      return false;
    }

    ArrayMapNode other = (ArrayMapNode) o;
    Map<JsonNode, JsonNode> m1 = children;
    Map<JsonNode, JsonNode> m2 = other.children;

    final int len = m1.size();
    if (m2.size() != len) {
      return false;
    }

    for (Map.Entry<JsonNode, JsonNode> entry : m1.entrySet()) {
      JsonNode v2 = m2.get(entry.getKey());
      if ((v2 == null) || !entry.getValue().equals(comparator, v2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public JsonNodeType getNodeType() {
    return JsonNodeType.POJO;
  }

  @Override
  public String asText() {
    return "";
  }


}
