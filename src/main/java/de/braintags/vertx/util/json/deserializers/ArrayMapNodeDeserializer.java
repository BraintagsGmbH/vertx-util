package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ArrayMapNodeDeserializer extends JsonNodeDeserializer {

  /**
   * Singleton instance of generic deserializer for {@link JsonNode}.
   * Only used for types other than JSON Object and Array.
   */
  private final static ArrayMapNodeDeserializer instance = new ArrayMapNodeDeserializer();

  protected ArrayMapNodeDeserializer() {
  }

  /**
   * Factory method for accessing deserializer for specific node type
   */
  public static JsonDeserializer<? extends JsonNode> getDeserializer(Class<?> nodeClass) {
    if (nodeClass == ObjectNode.class || nodeClass == ArrayNode.class) {
      return JsonNodeDeserializer.getDeserializer(nodeClass);
    }

    // For others, generic one works fine
    return instance;
  }

  @Override
  public JsonNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.getCurrentToken() == JsonToken.END_ARRAY) {
      // decode ArrayMapNode
      System.out.println("ok");
    }
    return super.deserialize(p, ctxt);
  }

}
