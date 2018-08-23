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

  private static final String JSON_DIFF_OVERRIDE = "@jsonDiffOverride";
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
      if (base instanceof ArrayMapNode || data instanceof ArrayMapNode) {
        ArrayMapNode b = base.isNull() ? new ArrayMapNode() : (ArrayMapNode) base;
        ArrayMapNode d = data.isNull() ? new ArrayMapNode() : (ArrayMapNode) data;
        return arrayMapDiff(b, d, nodeFactory, arrayMapsConverted);
      } else {
        throw new IllegalArgumentException("nodeType is null and base and/or data is not ArrayMapNode");
      }
    }
    switch (nodeType) {
      case ARRAY:
        return arrayDiff((ArrayNode) base, (ArrayNode) data, nodeFactory, arrayMapsConverted);
      case OBJECT:
        if (!arrayMapsConverted && (ArrayMapNode.isArrayMapNode(base) || ArrayMapNode.isArrayMapNode(data))) {
          ArrayMapNode arrayBase = (ArrayMapNode) ArrayMapNode.deepConvertNode(base);
          ArrayMapNode arrayData = (ArrayMapNode) ArrayMapNode.deepConvertNode(data);
          return ArrayMapNode.toRegularNode(arrayMapDiff(arrayBase, arrayData, nodeFactory, true), nodeFactory);
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

  private static ArrayMapNode arrayMapDiff(final ArrayMapNode base, final ArrayMapNode data,
      final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
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
        diffChildren.put(baseKey, nodeFactory.nullNode());
      }
    }
    return new ArrayMapNode(diffChildren);
  }

  private static ArrayNode arrayDiff(final ArrayNode baseNode, final ArrayNode dataNode,
      final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
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

  private static JsonNode internalApplyDiff(final JsonNode base, final JsonNode diff, final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    JsonNodeType nodeType = base.isNull() ? diff.getNodeType() : base.getNodeType();

    if (nodeType != diff.getNodeType()) {
      return diff.deepCopy();
    }

    if (nodeType == null) {
      if (diff instanceof ArrayMapNode) {
        ArrayMapNode b = base.isNull() ? new ArrayMapNode() : (ArrayMapNode) base;
        ArrayMapNode d = (ArrayMapNode) diff;
        applyArrayMapDiff(b, d, nodeFactory, true);
        return b;
      } else {
        throw new IllegalArgumentException("nodeType is null and base and/or diff is not ArrayMapNode");
      }
    }

    switch (nodeType) {
      case ARRAY:
        if (diff.isArray()) {
          ArrayNode baseNode = base.isNull() ? nodeFactory.arrayNode() : (ArrayNode) base;
          applyArrayDiff(baseNode, (ArrayNode) diff, nodeFactory, arrayMapsConverted);
          return baseNode;
        } else {
          return diff.deepCopy();
        }
      case OBJECT:
        if (!arrayMapsConverted && ArrayMapNode.isArrayMapNode(diff)) {
          ArrayMapNode arrayBase = base.isNull() ? new ArrayMapNode()
              : (ArrayMapNode) ArrayMapNode.deepConvertNode(base);
          ArrayMapNode arrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(diff);
          applyArrayMapDiff(arrayBase, arrayDiff, nodeFactory, true);
          return ArrayMapNode.toRegularNode(arrayBase, nodeFactory);
        } else if (diff.isObject()) {
          ObjectNode baseNode = base.isNull() ? nodeFactory.objectNode() : (ObjectNode) base;
          return applyObjectDiff(baseNode, (ObjectNode) diff, nodeFactory, arrayMapsConverted);
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

  private static JsonNode applyObjectDiff(final ObjectNode base, final ObjectNode diff,
      final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    if (diff.has(JSON_DIFF_OVERRIDE)) {
      ObjectNode res = diff.deepCopy();
      res.remove(JSON_DIFF_OVERRIDE);
      return res;
    }

    Iterator<Entry<String, JsonNode>> diffFields = diff.fields();
    while (diffFields.hasNext()) {
      Entry<String, JsonNode> diffField = diffFields.next();
      String fieldName = diffField.getKey();
      JsonNode baseNode = base.get(fieldName);
      JsonNode diffNode = diffField.getValue();
      if (baseNode == null) {
        baseNode = nodeFactory.nullNode();
      }
      base.set(fieldName, internalApplyDiff(baseNode, diffNode, nodeFactory, arrayMapsConverted));
    }
    return base;
  }

  private static void applyArrayMapDiff(final ArrayMapNode base, final ArrayMapNode diff,
      final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
    if (diff == null) {
      return;
    }
    Map<JsonNode, JsonNode> baseChildren = base.getChildren();
    for (Map.Entry<JsonNode, JsonNode> diffEntry : diff.getChildren().entrySet()) {
      if (diffEntry.getValue() == null || diffEntry.getValue().isNull()) {
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

  private static void applyArrayDiff(final ArrayNode baseNode, final ArrayNode diff, final JsonNodeFactory nodeFactory,
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
          JsonNode value = getArrayDiffValue(diff, i, obj);
          newValues[i] = value;
        }
      }
    }

    baseNode.removeAll();
    baseNode.addAll(Arrays.asList(newValues));
  }

  private static JsonNode getArrayDiffValue(final ArrayNode diff, final int index, final ObjectNode obj) {
    JsonNode value = obj.get(VALUE);
    if (value == null) {
      throw new IllegalStateException(
          "array node diff does not contain " + DIFF + " or " + VALUE + " property at entry " + index + ": "
              + diff);
    }
    return value;
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
        if (!arrayMapsConverted && ArrayMapNode.isArrayMapNode(diff)) {
          ArrayMapNode arrayBase = (ArrayMapNode) ArrayMapNode.deepConvertNode(base);
          ArrayMapNode arrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(diff);
          squashArrayMapDiff(arrayBase, arrayDiff, nodeFactory, true);
          return ArrayMapNode.toRegularNode(arrayBase, nodeFactory);
        } else if (diff.isObject()) {
          return squashObjectDiff((ObjectNode) base, (ObjectNode) diff, nodeFactory, arrayMapsConverted);
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

  private static ObjectNode squashObjectDiff(final ObjectNode base, final ObjectNode diff,
      final JsonNodeFactory nodeFactory,
      final boolean arrayMapsConverted) {
    if (diff.has(JSON_DIFF_OVERRIDE)) {
      return diff.deepCopy();
    }

    Iterator<Entry<String, JsonNode>> diffFields = diff.fields();
    while (diffFields.hasNext()) {
      Entry<String, JsonNode> diffField = diffFields.next();
      String fieldName = diffField.getKey();
      JsonNode baseNode = base.get(fieldName);
      JsonNode diffNode = diffField.getValue();
      if (baseNode == null) {
        baseNode = nodeFactory.nullNode();
      }
      base.set(fieldName, internalSquashDiff(baseNode, diffNode, nodeFactory, arrayMapsConverted));
    }
    return base;
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
                  internalApplyDiff(getArrayDiffValue(diff, i, (ObjectNode) baseNode).deepCopy(), valueDiff,
                      nodeFactory,
                      arrayMapsConverted));
              newValues[i] = newValue;
            }
          }
        } else {
          newValues[i] = obj;
        }
      }
    }

    base.removeAll();
    base.addAll(Arrays.asList(newValues));
  }

  /**
   * Retains the diff tree specified by retainedFields
   *
   * @param base
   *          is not modified
   * @param retainedFields
   *          is not modified
   * @param diff
   *          MAY BE MODIFIED!
   * @return
   *         the modified JsonNode
   */
  public static JsonNode retainDiffTree(final JsonNode base, final JsonNode retainedFields, final JsonNode diff) {
    return internalRetainDiffTree(base, retainedFields, diff, Json.mapper.getNodeFactory(), false);
  }

  private static JsonNode internalRetainDiffTree(final JsonNode base, final JsonNode retainedFields,
      final JsonNode diff, final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
    JsonNodeType nodeType = diff.getNodeType();
    if (nodeType == null) {
      if (diff instanceof ArrayMapNode) {
        ArrayMapNode arrayBase = (ArrayMapNode) ArrayMapNode.deepConvertNode(base);
        ArrayMapNode retainedArrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(retainedFields);
        ArrayMapNode arrayDiff = (ArrayMapNode) diff;
        retainDiffTreeArrayMapDiff(arrayBase, retainedArrayDiff, arrayDiff, nodeFactory, true);
        return arrayDiff;
      } else {
        throw new IllegalArgumentException("nodeType is null and base and/or diff is not ArrayMapNode");
      }
    }

    switch (nodeType) {
      case OBJECT:
        if (!arrayMapsConverted && (ArrayMapNode.isArrayMapNode(retainedFields) || ArrayMapNode.isArrayMapNode(diff))) {
          ArrayMapNode arrayBase = (ArrayMapNode) ArrayMapNode.deepConvertNode(base);
          ArrayMapNode retainedArrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(retainedFields);
          ArrayMapNode arrayDiff = (ArrayMapNode) ArrayMapNode.deepConvertNode(diff);
          retainDiffTreeArrayMapDiff(arrayBase, retainedArrayDiff, arrayDiff, nodeFactory, true);
          return ArrayMapNode.toRegularNode(arrayDiff, nodeFactory);
        } else if (retainedFields.isObject()) {
          retainDiffTreeObjectDiff(base.isNull() ? nodeFactory.objectNode() : (ObjectNode) base,
              (ObjectNode) retainedFields, (ObjectNode) diff, nodeFactory, arrayMapsConverted);
          return diff;
        } else {
          return diff;
        }
      case ARRAY:
        throw new UnsupportedOperationException("this operation is not supported for arrays");
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

  private static void retainDiffTreeObjectDiff(final ObjectNode base, final ObjectNode retainedFields,
      final ObjectNode diff, final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
    Iterator<Entry<String, JsonNode>> retainedFieldsIterator = retainedFields.fields();
    while (retainedFieldsIterator.hasNext()) {
      Entry<String, JsonNode> retainedField = retainedFieldsIterator.next();
      String fieldName = retainedField.getKey();
      JsonNode retainedFieldValue = retainedField.getValue();
      JsonNode baseFieldValue = base.get(fieldName);
      if (baseFieldValue == null) {
        baseFieldValue = nodeFactory.nullNode();
      }
      if (!diff.has(fieldName)) {
        if (baseFieldValue instanceof ArrayMapNode || retainedFieldValue instanceof ArrayMapNode) {
          diff.set(fieldName, internalRetainDiffTree(baseFieldValue, retainedFieldValue, new ArrayMapNode(),
              nodeFactory, arrayMapsConverted));
        } else if (baseFieldValue.isObject()) {
          diff.set(fieldName, internalRetainDiffTree(baseFieldValue, retainedFieldValue, nodeFactory.objectNode(),
              nodeFactory, arrayMapsConverted));
        } else if (baseFieldValue.isNull() || baseFieldValue.isValueNode()) {
          diff.set(fieldName, baseFieldValue.deepCopy());
        } else {
          throw new IllegalStateException("changes in the diff would by lost by merging: " + fieldName);
        }
      } else {
        JsonNode diffValue = diff.get(fieldName);
        diff.set(fieldName,
            internalRetainDiffTree(baseFieldValue, retainedFieldValue, diffValue, nodeFactory, arrayMapsConverted));
      }
    }
  }

  private static void retainDiffTreeArrayMapDiff(final ArrayMapNode base, final ArrayMapNode retainedFields,
      final ArrayMapNode diff, final JsonNodeFactory nodeFactory, final boolean arrayMapsConverted) {
    if (diff == null) {
      return;
    }
    Map<JsonNode, JsonNode> baseChildren = base.getChildren();
    Map<JsonNode, JsonNode> diffChildren = diff.getChildren();
    for (Map.Entry<JsonNode, JsonNode> retainedEntry : retainedFields.getChildren().entrySet()) {
      JsonNode key = retainedEntry.getKey();
      JsonNode baseFieldValue = baseChildren.get(key);
      if (baseFieldValue == null) {
        baseFieldValue = nodeFactory.nullNode();
      }
      if (!diffChildren.containsKey(key)) {
        if (baseFieldValue instanceof ArrayMapNode || retainedEntry.getValue() instanceof ArrayMapNode) {
          diffChildren.put(key, internalRetainDiffTree(baseFieldValue, retainedEntry.getValue(), new ArrayMapNode(),
              nodeFactory, arrayMapsConverted));
        } else if (baseFieldValue.isObject()) {
          diffChildren.put(key, internalRetainDiffTree(baseFieldValue, retainedEntry.getValue(),
              nodeFactory.objectNode(), nodeFactory, arrayMapsConverted));
        } else if (baseFieldValue.isNull() || baseFieldValue.isValueNode()) {
          diffChildren.put(key, baseFieldValue.deepCopy());
        } else {
          throw new IllegalStateException("changes in the diff would by lost by merging: " + key);
        }
      } else {
        JsonNode diffValue = diffChildren.get(key);
        diffChildren.put(key, internalRetainDiffTree(baseFieldValue, retainedEntry.getValue(), diffValue, nodeFactory,
            arrayMapsConverted));
      }
    }
  }

  public static JsonNode getEmptyDiff(final JsonNodeFactory nodeFactory) {
    return nodeFactory.objectNode();
  }

  public static JsonNode getEmptyDiff() {
    return getEmptyDiff(Json.mapper.getNodeFactory());
  }

}
