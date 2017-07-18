package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ValueNode;

public class ArrayMapNode extends ValueNode {

  private Map<JsonNode, JsonNode> children;
  private final JsonNodeFactory nodeFactory;

  public ArrayMapNode(JsonNodeFactory nodeFactory) {
    this.nodeFactory = nodeFactory;
  }

  /**
   * Method that can be called to serialize this node and
   * all of its descendants using specified JSON generator.
   */
  @Override
  public void serialize(JsonGenerator g, SerializerProvider provider)
      throws IOException {
    g.writeEndArray();
    g.writeRaw(ArrayMapSerializer.ARRAY_MAP);
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
