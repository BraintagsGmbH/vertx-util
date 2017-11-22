package de.braintags.vertx.util.json;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import de.braintags.vertx.util.json.deserializers.ArrayMap;
import de.braintags.vertx.util.json.deserializers.ArrayMapSerializer;

/**
 * Internal intermediate {@link JsonNode} for dealing with {@link ArrayMap} encoding.
 * 
 * @author mpluecker
 *
 */
class ArrayMapNode extends ValueNode {

  private final Map<JsonNode, JsonNode> children;

  public static boolean isArrayMapNode(final JsonNode node) {
    if (node.isObject() && node.size() == 1) {
      Entry<String, JsonNode> arrayMapField = node.fields().next();
      return arrayMapField.getKey().equals(ArrayMapSerializer.ARRAY_MAP) && arrayMapField.getValue().isArray();
    }
    return false;
  }

  public static JsonNode deepConvertNode(final JsonNode node) {
    if (node.isObject()) {
      if (node.size() == 1) {
        Entry<String, JsonNode> arrayMapField = node.fields().next();
        if (arrayMapField.getKey().equals(ArrayMapSerializer.ARRAY_MAP) && arrayMapField.getValue().isArray()) {
          return convertToArrayNode(node, arrayMapField);
        }
      }
      return convertObjNode((ObjectNode) node);
    } else if (node.isArray()) {
      return convertArrayNode((ArrayNode) node);
    } else {
      return node;
    }
  }

  private static JsonNode convertArrayNode(final ArrayNode node) {
    ArrayNode modified = null;
    for (int i = node.size() - 1; i >= 0; i--) {
      JsonNode oldNode = node.get(i);
      JsonNode newNode = deepConvertNode(oldNode);
      if (oldNode != newNode) {
        if (modified == null) {
          modified = node.arrayNode().addAll(node);
        }
        modified.set(i, newNode);
      }
    }
    return modified != null ? modified : node;
  }

  private static JsonNode convertObjNode(final ObjectNode node) {
    ObjectNode modified = null;
    Iterator<Entry<String, JsonNode>> fielditer = node.fields();
    while (fielditer.hasNext()) {
      Entry<String, JsonNode> field = fielditer.next();
      JsonNode newNode = deepConvertNode(field.getValue());
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

  private static ArrayMapNode convertToArrayNode(final JsonNode node, final Entry<String, JsonNode> arrayMapField) {
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
            key = deepConvertNode(field.getValue());
            break;
          case ArrayMapSerializer.VALUE:
            value = deepConvertNode(field.getValue());
            break;
          default:
            throw new IllegalArgumentException(
                "malformed array map entry, unknown fieldname: " + field.getKey() + " in " + node);
        }
      }
      children.put(key, value);
    }
    return new ArrayMapNode(children);
  }

  public static JsonNode toRegularNode(final JsonNode node, final JsonNodeCreator nodeFactory) {
    if (node.isObject()) {
      return toRegularNode((ObjectNode) node, nodeFactory);
    } else if (node.isArray()) {
      return toRegularNode((ArrayNode) node, nodeFactory);
    } else if (node instanceof ArrayMapNode) {
      return toRegularNode((ArrayMapNode) node, nodeFactory);
    } else {
      return node;
    }
  }

  public static ObjectNode toRegularNode(final ArrayMapNode node, final JsonNodeCreator nodeFactory) {
    ArrayNode childrenNode = nodeFactory.arrayNode();
    for (Map.Entry<JsonNode, JsonNode> en : node.children.entrySet()) {
      ObjectNode entryNode = nodeFactory.objectNode();
      entryNode.set(ArrayMapSerializer.KEY, toRegularNode(en.getKey(), nodeFactory));
      entryNode.set(ArrayMapSerializer.VALUE, toRegularNode(en.getValue(), nodeFactory));
      childrenNode.add(entryNode);
    }

    ObjectNode result = nodeFactory.objectNode();
    result.set(ArrayMapSerializer.ARRAY_MAP, childrenNode);
    return result;
  }

  private static JsonNode toRegularNode(final ArrayNode node, final JsonNodeCreator nodeFactory) {
    ArrayNode modified = null;
    for (int i = node.size() - 1; i >= 0; i--) {
      JsonNode oldNode = node.get(i);
      JsonNode newNode = toRegularNode(oldNode, nodeFactory);
      if (oldNode != newNode) {
        if (modified == null) {
          modified = node.arrayNode().addAll(node);
        }
        modified.set(i, newNode);
      }
    }
    return modified != null ? modified : node;
  }

  private static JsonNode toRegularNode(final ObjectNode node, final JsonNodeCreator nodeFactory) {
    ObjectNode modified = null;
    Iterator<Entry<String, JsonNode>> fielditer = node.fields();
    while (fielditer.hasNext()) {
      Entry<String, JsonNode> field = fielditer.next();
      JsonNode newNode = toRegularNode(field.getValue(), nodeFactory);
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

  public ArrayMapNode() {
    children = new LinkedHashMap<>();
  }

  public ArrayMapNode(final Map<JsonNode, JsonNode> children) {
    if (children.values().contains(null)) {
      throw new IllegalStateException();
    }
    this.children = children;
  }

  /**
   * Method that can be called to serialize this node and
   * all of its descendants using specified JSON generator.
   */
  @Override
  public void serialize(final JsonGenerator g, final SerializerProvider provider) throws IOException {
    g.writeStartObject();
    innerSerialize(g, provider);
    g.writeEndObject();
  }

  private void innerSerialize(final JsonGenerator g, final SerializerProvider provider) throws IOException {
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
  public void serializeWithType(final JsonGenerator g, final SerializerProvider provider, final TypeSerializer typeSer)
      throws IOException {
    typeSer.writeTypePrefixForObject(this, g);
    innerSerialize(g, provider);
    typeSer.writeTypeSuffixForObject(this, g);
  }

  public Map<JsonNode, JsonNode> getChildren() {
    return children;
  }

  @Override
  public int size() {
    return children.size();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArrayMapNode deepCopy() {
    ArrayMapNode ret = new ArrayMapNode();

    for (Map.Entry<JsonNode, JsonNode> entry : children.entrySet())
      ret.children.put(entry.getKey().deepCopy(), entry.getValue().deepCopy());

    return ret;
  }

  @Override
  public JsonToken asToken() {
    return JsonToken.START_OBJECT;
  }

  @Override
  public int hashCode() {
    return children.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
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
  protected boolean _childrenEqual(final ArrayMapNode other) {
    return children.equals(other.children);
  }

  @Override
  public boolean equals(final Comparator<JsonNode> comparator, final JsonNode o) {
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
  public String toString() {
    StringBuilder sb = new StringBuilder(32 + (size() << 4));
    sb.append("{\"");
    sb.append(ArrayMapSerializer.ARRAY_MAP);
    sb.append("\":[");
    int count = 0;
    for (Map.Entry<JsonNode, JsonNode> en : children.entrySet()) {
      if (count > 0) {
        sb.append(", ");
      }
      ++count;
      sb.append("{\"");
      sb.append(ArrayMapSerializer.KEY);
      sb.append("\":");
      if (en.getKey() != null) {
        sb.append(en.getKey().toString());
      } else {
        sb.append("null");
      }
      sb.append(",\"");
      sb.append(ArrayMapSerializer.VALUE);
      sb.append("\":");
      if (en.getValue() != null) {
        sb.append(en.getValue().toString());
      } else {
        sb.append("null");
      }
      sb.append("}");
    }
    sb.append("]}");
    return sb.toString();
  }

  @Override
  public JsonNodeType getNodeType() {
    return null;
  }

  @Override
  public String asText() {
    return "";
  }

}
