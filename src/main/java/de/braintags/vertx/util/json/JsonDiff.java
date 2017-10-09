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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.braintags.vertx.util.EqUtil;
import io.vertx.core.json.Json;

/**
 * Creates a diff for two JsonNodes. For Object and value nodes, the diff will be generated in the natural way,
 * i.e. only changed fields in data will be contained in the diff. For an array an ObjectNode is created:
 * { "size" : 2, "#0" : 4, "1" : { SOME DIFF }}
 * The field "size" determines the size for the data array. For each index of the data array there is a field in the
 * object.
 * if the index is prefixed with a "#", the field value is the index of the element in the base array.
 * Otherwise there is a diff between the base element and the data element at the corresponding index.
 *
 * @author mpluecker
 *
 */
public class JsonDiff {

  static final String VALUE = "value";
  static final String DIFF = "diff";
  static final String INDEX = "index";

  /**
   * Creates a diff from base to data.
   *
   * @param base
   *          is not modified
   * @param data
   *          is not modified
   */
  public static JsonNode getDiff(final JsonNode base, final JsonNode data) {
    return getDiff(base, data, Json.mapper.getNodeFactory(), false);
  }

  /**
   * Creates a diff from base to data.
   *
   * @param base
   *          is not modified
   * @param data
   *          is not modified
   * @param nodeFactory
   *          a JsonNodeFactory
   */
  public static JsonNode getDiff(final JsonNode base, final JsonNode data, final JsonNodeFactory nodeFactory) {
    return getDiff(base, data, nodeFactory, false);
  }

  private static JsonNode getDiff(final JsonNode base, final JsonNode data, final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    JsonNodeType nodeType = base.getNodeType();
    if (nodeType != data.getNodeType()) {
      return data.deepCopy();
    }

    if (nodeType == null) {
      if (base instanceof ArrayMapNode && data instanceof ArrayMapNode) {
        return arrayMapDiff((ArrayMapNode) base, (ArrayMapNode) data, nodeFactory, arrayMapsConverted);
      } else {
        throw new IllegalArgumentException("nodeType is null and base and/or data is not ArrayMapNode");
      }
    }
    switch (nodeType) {
      case ARRAY:
        return arrayDiff((ArrayNode) base, (ArrayNode) data, nodeFactory, arrayMapsConverted);
      case OBJECT:
        if (!arrayMapsConverted && ArrayMapNode.isArrayMapNode(base)) {
          ArrayMapNode arrayBase = (ArrayMapNode) ArrayMapNode.deepConvertNode(base);
          ArrayMapNode arrayData = (ArrayMapNode) ArrayMapNode.deepConvertNode(data);
          return arrayMapDiff(arrayBase, arrayData, nodeFactory, true);
        } else {
          return objectDiff((ObjectNode) base, (ObjectNode) data, nodeFactory, arrayMapsConverted);
        }
      case BINARY:
      case BOOLEAN:
      case NULL:
      case NUMBER:
      case STRING:
        return valueDiff(base, data);
      case POJO:
      case MISSING:
      default:
        throw new IllegalArgumentException("node type may not be " + nodeType);
    }
  }

  private static JsonNode valueDiff(final JsonNode base, final JsonNode data) {
    assert (base.isValueNode());
    if (base.equals(data)) {
      return null;
    } else {
      return data.deepCopy();
    }
  }

  private static ObjectNode objectDiff(final ObjectNode base, final ObjectNode data, final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    ObjectNode diff = nodeFactory.objectNode();
    Iterator<Entry<String, JsonNode>> dataFields = data.fields();
    while (dataFields.hasNext()) {
      Entry<String, JsonNode> dataField = dataFields.next();
      JsonNode dataNode = dataField.getValue();
      String fieldName = dataField.getKey();
      JsonNode baseNode = base.get(fieldName);
      if (baseNode != null) {
        JsonNode subDiff = getDiff(baseNode, dataNode, nodeFactory, arrayMapsConverted);
        if (subDiff != null) {
          if ((!subDiff.isObject() && !(subDiff instanceof ArrayMapNode)) || subDiff.size() > 0) {
            diff.set(fieldName, subDiff);
          }
        }
      } else {
        diff.set(fieldName, dataField.getValue().deepCopy());
      }
    }

    Iterator<String> baseFieldNames = base.fieldNames();
    while (baseFieldNames.hasNext()) {
      String fieldName = baseFieldNames.next();
      if (!data.has(fieldName)) {
        diff.set(fieldName, null);
      }
    }
    return diff;
  }

  private static ArrayMapNode arrayMapDiff(final ArrayMapNode base, final ArrayMapNode data, final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    Map<JsonNode, JsonNode> diffChildren = new LinkedHashMap<>();

    Map<JsonNode, JsonNode> baseChildren = base.getChildren();
    for (Entry<JsonNode, JsonNode> dataEntry : data.getChildren().entrySet()) {
      JsonNode baseValue = baseChildren.get(dataEntry.getKey());
      if (baseValue == null) {
        diffChildren.put(dataEntry.getKey(), dataEntry.getValue());
      } else {
        JsonNode subDiff = getDiff(baseValue, dataEntry.getValue(), nodeFactory, arrayMapsConverted);
        if (subDiff != null) {
          if ((!subDiff.isObject() && !(subDiff instanceof ArrayMapNode)) || subDiff.size() > 0) {
            diffChildren.put(dataEntry.getKey(), subDiff);
          }
        }
      }
    }

    for (JsonNode baseKey : baseChildren.keySet()) {
      if (!data.getChildren().containsKey(baseKey)) {
        diffChildren.put(baseKey, null);
      }
    }
    return new ArrayMapNode(diffChildren);
  }

  private static ArrayNode arrayDiff(final ArrayNode baseNode, final ArrayNode dataNode,
      final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    ArrayNode elements = nodeFactory.arrayNode(dataNode.size());
    boolean[] usedIndices = new boolean[baseNode.size()];
    for (int index = 0; index < dataNode.size(); index++) {
      JsonNode element = dataNode.get(index);
      int oldIndex = indexOf(baseNode, element, usedIndices);
      if (oldIndex >= 0) {
        elements.add(nodeFactory.numberNode(oldIndex));
        usedIndices[oldIndex] = true;
      } else {
        ObjectNode valueNode = nodeFactory.objectNode();
        if (baseNode.size() > index) {
          JsonNode diffEncoding = getDiff(baseNode.get(index), element, nodeFactory, arrayMapsConverted);
          assert diffEncoding != null;
          valueNode.set(DIFF, diffEncoding);
          valueNode.set(INDEX, nodeFactory.numberNode(index));
        } else {
          valueNode.set(VALUE, element.deepCopy());
        }
        elements.add(valueNode);
      }
    }
    return elements;
  }

  private static int indexOf(final ArrayNode baseNode, final JsonNode element, final boolean[] ignoredIndices) {
    if (baseNode == null) {
      return -1;
    }
    for (int i = baseNode.size() - 1; i >= 0; i--) {
      if (!ignoredIndices[i] && EqUtil.eq(baseNode.get(i), element)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Applies the diff to the base node. If the base is not a value node, the base node will be modified and returned.
   *
   * @param base
   *          MAY BE MODIFIED!
   * @param diff
   *          is not modified
   * @return
   *         the modified JsonNode
   */
  public static JsonNode applyDiff(final JsonNode base, final JsonNode diff) {
    return internalApplyDiff(base, diff, Json.mapper.getNodeFactory(), false);
  }

  private static JsonNode internalApplyDiff(final JsonNode base, final JsonNode diff, final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
    if (base.isNull()) {
      return diff.deepCopy();
    }
    JsonNodeType nodeType = base.getNodeType();
    if (nodeType == null) {
      if (base instanceof ArrayMapNode && diff instanceof ArrayMapNode) {
        ArrayMapNode arrayBase = (ArrayMapNode) base;
        ArrayMapNode arrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(diff);
        applyArrayMapDiff(arrayBase, arrayDiff, nodeFactory, true);
        return ArrayMapNode.toRegularNode(arrayBase, nodeFactory);
      } else {
        throw new IllegalArgumentException("nodeType is null and base and/or diff is not ArrayMapNode");
      }
    }

    if (nodeType != diff.getNodeType()) {
      return diff.deepCopy();
    }

    switch (nodeType) {
      case ARRAY:
        if (diff.isArray()) {
          applyArrayDiff((ArrayNode) base, (ArrayNode) diff, nodeFactory, arrayMapsConverted);
          return base;
        } else {
          return diff.deepCopy();
        }
      case OBJECT:
        if (!arrayMapsConverted && ArrayMapNode.isArrayMapNode(base)) {
          ArrayMapNode arrayBase = (ArrayMapNode) ArrayMapNode.deepConvertNode(base);
          ArrayMapNode arrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(diff);
          applyArrayMapDiff(arrayBase, arrayDiff, nodeFactory, true);
          return ArrayMapNode.toRegularNode(arrayBase, nodeFactory);
        } else if (diff.isObject()) {
          applyObjectDiff((ObjectNode) base, (ObjectNode) diff, nodeFactory, arrayMapsConverted);
          return base;
        } else {
          return diff.deepCopy();
        }
      case BINARY:
      case BOOLEAN:
      case NULL:
      case NUMBER:
      case STRING:
        return diff.deepCopy();

      case POJO:
      case MISSING:
      default:
        throw new IllegalArgumentException("node type may not be " + nodeType);
    }
  }

  private static void applyObjectDiff(final ObjectNode base, final ObjectNode diff, final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    Iterator<Entry<String, JsonNode>> diffFields = diff.fields();
    while (diffFields.hasNext()) {
      Entry<String, JsonNode> diffField = diffFields.next();
      String fieldName = diffField.getKey();
      JsonNode baseNode = base.get(fieldName);
      JsonNode diffNode = diffField.getValue();
      if (baseNode != null) {
        base.set(fieldName, internalApplyDiff(baseNode, diffNode,nodeFactory, arrayMapsConverted));
      } else {
        base.set(fieldName, diffNode.deepCopy());
      }
    }
  }

  private static void applyArrayMapDiff(final ArrayMapNode base, final ArrayMapNode diff,
      final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    if (diff == null) {
      return;
    }
    Map<JsonNode, JsonNode> baseChildren = base.getChildren();
    for (Map.Entry<JsonNode, JsonNode> diffEntry : diff.getChildren().entrySet()) {
      if (diffEntry.getValue() == null) {
        baseChildren.remove(diffEntry.getKey());
      } else {
        JsonNode baseValue = baseChildren.get(diffEntry.getKey());
        if (baseValue != null) {
          baseChildren.put(diffEntry.getKey(),
              internalApplyDiff(baseValue, diffEntry.getValue(), nodeFactory, arrayMapsConverted));
        } else {
          baseChildren.put(diffEntry.getKey(), diffEntry.getValue());
        }
      }
    }
  }

  private static void applyArrayDiff(final ArrayNode baseNode, final ArrayNode diff,
      final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    JsonNode[] newValues = new JsonNode[diff.size()];
    for (int i = 0; i < diff.size(); i++) {
      JsonNode diffNode = diff.get(i);
      if (diffNode.isNumber()) {
        newValues[i] = baseNode.get(diffNode.asInt());
      } else {
        ObjectNode obj = (ObjectNode) diffNode;
        JsonNode valueDiff = obj.get(DIFF);
        if (valueDiff != null) {
          newValues[i] = internalApplyDiff(baseNode.get(obj.get(INDEX).intValue()).deepCopy(), valueDiff, nodeFactory,
              arrayMapsConverted);
        } else {
          newValues[i] = obj.get(VALUE);
        }
      }
    }

    baseNode.removeAll();
    baseNode.addAll(Arrays.asList(newValues));
  }

  /**
   * Squashes two diffs and saves the squashed diff in the base node.
   *
   * @param base
   *          MAY BE MODIFIED!
   * @param diff
   *          is not modified
   * @return
   *         the modified JsonNode
   */
  public static JsonNode squashDiff(final JsonNode base, final JsonNode diff) {
    return internalSquashDiff(base, diff, Json.mapper.getNodeFactory(), false);
  }

  private static JsonNode internalSquashDiff(final JsonNode base, final JsonNode diff,
      final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
    if (base.isNull()) {
      return diff.deepCopy();
    }
    JsonNodeType nodeType = base.getNodeType();
    if (nodeType == null) {
      if (base instanceof ArrayMapNode && diff instanceof ArrayMapNode) {
        ArrayMapNode arrayBase = (ArrayMapNode) base;
        ArrayMapNode arrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(diff);
        squashArrayMapDiff(arrayBase, arrayDiff, nodeFactory, true);
        return arrayBase;
      } else {
        throw new IllegalArgumentException("nodeType is null and base and/or diff is not ArrayMapNode");
      }
    }

    switch (nodeType) {
      case OBJECT:
        if (!arrayMapsConverted && ArrayMapNode.isArrayMapNode(base)) {
          ArrayMapNode arrayBase = (ArrayMapNode) ArrayMapNode.deepConvertNode(base);
          ArrayMapNode arrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(diff);
          squashArrayMapDiff(arrayBase, arrayDiff, nodeFactory, true);
          return ArrayMapNode.toRegularNode(arrayBase, nodeFactory);
        } else if (diff.isObject()) {
          squashObjectDiff((ObjectNode) base, (ObjectNode) diff, nodeFactory, arrayMapsConverted);
          return base;
        } else {
          return diff.deepCopy();
        }
      case ARRAY:
        squashArrayDiff((ArrayNode) base, (ArrayNode) diff, nodeFactory, arrayMapsConverted);
        return base;
      case BINARY:
      case BOOLEAN:
      case NULL:
      case NUMBER:
      case STRING:
        return diff.deepCopy();
      case POJO:
      case MISSING:
      default:
        throw new IllegalArgumentException("node type may not be " + nodeType);
    }
  }

  private static void squashObjectDiff(final ObjectNode base, final ObjectNode diff, final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    Iterator<Entry<String, JsonNode>> diffFields = diff.fields();
    while (diffFields.hasNext()) {
      Entry<String, JsonNode> diffField = diffFields.next();
      String fieldName = diffField.getKey();
      JsonNode baseNode = base.get(fieldName);
      JsonNode diffNode = diffField.getValue();
      if (baseNode != null) {
        base.set(fieldName, internalSquashDiff(baseNode, diffNode, nodeFactory, arrayMapsConverted));
      } else {
        base.set(fieldName, diffNode.deepCopy());
      }
    }
  }

  private static void squashArrayMapDiff(final ArrayMapNode base, final ArrayMapNode diff,
      final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
    if (diff == null) {
      return;
    }
    Map<JsonNode, JsonNode> baseChildren = base.getChildren();
    for (Map.Entry<JsonNode, JsonNode> diffEntry : diff.getChildren().entrySet()) {
      if (diffEntry.getValue() == null) {
        baseChildren.remove(diffEntry.getKey());
      } else {
        JsonNode baseValue = baseChildren.get(diffEntry.getKey());
        if (baseValue != null) {
          baseChildren.put(diffEntry.getKey(),
              internalSquashDiff(baseValue, diffEntry.getValue(), nodeFactory, arrayMapsConverted));
        } else {
          baseChildren.put(diffEntry.getKey(), diffEntry.getValue());
        }
      }
    }
  }

  private static void squashArrayDiff(final ArrayNode base, final ArrayNode diff, final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    JsonNode[] newValues = new JsonNode[diff.size()];
    for (int i = 0; i < diff.size(); i++) {
      JsonNode diffNode = diff.get(i);
      if (diffNode.isNumber()) {
        newValues[i] = base.get(diffNode.asInt());
      } else {
        ObjectNode obj = (ObjectNode) diffNode;

        JsonNode valueDiff = obj.get(DIFF);
        if (valueDiff != null) {
          JsonNode baseNode = base.get(obj.get(INDEX).asInt());
          if (baseNode.isNumber()) {
            ObjectNode newDiff = obj.deepCopy();
            newDiff.set(INDEX, baseNode.deepCopy());
            newValues[i] = newDiff;
          } else {
            JsonNode baseValue = baseNode.get(DIFF);
            if (baseValue != null) {
              ObjectNode newValue = nodeFactory.objectNode();
              newValue.set(DIFF, internalSquashDiff(baseValue, valueDiff, nodeFactory, arrayMapsConverted));
              newValue.set(INDEX, baseNode.get(INDEX));
              newValues[i] = newValue;
            } else {
              ObjectNode newValue = nodeFactory.objectNode();
              newValue.set(VALUE,
                  internalApplyDiff(baseNode.get(VALUE).deepCopy(), valueDiff, nodeFactory, arrayMapsConverted));
              newValues[i] = newValue;
            }
          }
        } else {
          newValues[i] = obj.get(VALUE);
        }
      }
    }

    base.removeAll();
    base.addAll(Arrays.asList(newValues));
  }

}
 