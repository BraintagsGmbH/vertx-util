package de.braintags.vertx.util.json;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Utils for Json conversion
 * 
 * @author sschmitt
 * 
 */
public class JsonConverter {

  /**
   * Converts a {@link JsonNode} into an object that can be used by a vertx {@link JsonObject}<br>
   * <ul>
   * <li>If the node is a value node, returns the appropriate value notation</li>
   * <li>If it's an array node, returns a {@link JsonArray} with the contained values</li>
   * <li>If it's an object node, transforms the node into a {@link JsonObject}</li>
   * </ul>
   * 
   * @param jsonNode
   *          the jackson node to transform
   * @return a value that can be used as a value for a vertx {@link JsonObject}
   * @throws IOException
   */
  public static Object convertJsonNodeToVertx(JsonNode jsonNode) throws IOException {
    if (jsonNode.isValueNode()) {
      if (jsonNode.isNumber()) {
        return jsonNode.numberValue();
      } else if (jsonNode.isBoolean()) {
        return jsonNode.booleanValue();
      } else if (jsonNode.isBinary()) {
        return jsonNode.binaryValue();
      } else {
        return jsonNode.textValue();
      }
    } else if (jsonNode.isArray()) {
      JsonArray jsonArray = new JsonArray();
      ArrayNode arrayNode = (ArrayNode) jsonNode;
      for (JsonNode subNode : arrayNode) {
        jsonArray.add(convertJsonNodeToVertx(subNode));
      }
      return jsonArray;
    } else {
      JsonObject result = JsonObject.mapFrom(jsonNode);
      return result;
    }
  }

}
