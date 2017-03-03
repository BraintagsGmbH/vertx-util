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
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

  public static JsonNode getDiff(JsonNode base, JsonNode data) {
    return getDiff(base, data, Json.mapper.getNodeFactory());
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
  public static JsonNode getDiff(JsonNode base, JsonNode data, JsonNodeFactory nodeFactory) {
    if (base.getNodeType() != data.getNodeType()) {
      return data.deepCopy();
    }
    switch (base.getNodeType()) {
    case ARRAY:
      return arrayDiff((ArrayNode) base, (ArrayNode) data, nodeFactory);
    case OBJECT:
      return objectDiff((ObjectNode) base, (ObjectNode) data, nodeFactory);
    case MISSING:
      throw new IllegalArgumentException("node type may not be " + JsonNodeType.MISSING);
    default:
      return valueDiff(base, data);
    }
  }

  private static JsonNode valueDiff(JsonNode base, JsonNode data) {
    assert (base.isValueNode());
    if (base.equals(data)) {
      return null;
    } else {
      return data.deepCopy();
    }
  }

  private static ObjectNode objectDiff(ObjectNode base, ObjectNode data, JsonNodeFactory nodeFactory) {
    ObjectNode diff = nodeFactory.objectNode();
    Iterator<Entry<String, JsonNode>> dataFields = data.fields();
    while (dataFields.hasNext()) {
      Entry<String, JsonNode> dataField = dataFields.next();
      JsonNode dataNode = dataField.getValue();
      String fieldName = dataField.getKey();
      JsonNode baseNode = base.get(fieldName);
      if (baseNode != null) {
        JsonNode subDiff = getDiff(baseNode, dataNode, nodeFactory);
        if (subDiff != null) {
          if (!subDiff.isObject() || subDiff.size() > 0) {
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

  private static ObjectNode arrayDiff(ArrayNode baseNode, ArrayNode dataNode, JsonNodeFactory nodeFactory) {
    ObjectNode elements = nodeFactory.objectNode();
    boolean[] usedIndices = new boolean[baseNode.size()];
    for (int index = 0; index < dataNode.size(); index++) {
      JsonNode element = dataNode.get(index);
      int oldIndex = indexOf(baseNode, element, usedIndices);
      if (oldIndex >= 0) {
        if (index != oldIndex) {
          elements.set("#" + Integer.toString(index), nodeFactory.numberNode(oldIndex));
        }
        usedIndices[oldIndex] = true;
      } else {
        if (baseNode.size() > index) {
          JsonNode diffEncoding = getDiff(baseNode.get(index), element, nodeFactory);
          assert diffEncoding != null;
          elements.set(Integer.toString(index), diffEncoding);
        } else {
          elements.set(Integer.toString(index), element.deepCopy());
        }
      }
    }
    elements.set("size", nodeFactory.numberNode(dataNode.size()));

    return elements;
  }

  private static int indexOf(ArrayNode baseNode, JsonNode element, boolean[] ignoredIndices) {
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
  public static JsonNode applyDiff(JsonNode base, JsonNode diff) {
    if (base.isNull()) {
      return diff.deepCopy();
    }
    switch (base.getNodeType()) {
    case ARRAY:
      if (diff.isObject()) {
        applyArrayDiff((ArrayNode) base, (ObjectNode) diff);
        return base;
      } else {
        return diff.deepCopy();
      }
    case OBJECT:
      if (diff.isObject()) {
        applyObjectDiff((ObjectNode) base, (ObjectNode) diff);
        return base;
      } else {
        return diff.deepCopy();
      }
    case MISSING:
      throw new IllegalArgumentException("node type may not be " + JsonNodeType.MISSING);
    default:
      return diff.deepCopy();
    }
  }

  private static void applyObjectDiff(ObjectNode base, ObjectNode diff) {
    Iterator<Entry<String, JsonNode>> diffFields = diff.fields();
    while (diffFields.hasNext()) {
      Entry<String, JsonNode> diffField = diffFields.next();
      String fieldName = diffField.getKey();
      JsonNode baseNode = base.get(fieldName);
      JsonNode diffNode = diffField.getValue();
      if (baseNode != null) {
        base.set(fieldName, applyDiff(baseNode, diffNode));
      } else {
        base.set(fieldName, diffNode.deepCopy());
      }
    }
  }

  private static void applyArrayDiff(ArrayNode baseNode, ObjectNode diffNode) {
    JsonNode[] newValues = new JsonNode[diffNode.get("size").intValue()];
    for (int i = Math.min(newValues.length, baseNode.size()) - 1; i >= 0; i--) {
      newValues[i] = baseNode.get(i);
    }

    Iterator<Entry<String, JsonNode>> iterator = diffNode.fields();
    while (iterator.hasNext()) {
      Entry<String, JsonNode> entry = iterator.next();
      String fieldName = entry.getKey();
      JsonNode value = entry.getValue();
      if (fieldName.charAt(0) == '#') {
        newValues[Integer.parseInt(fieldName.substring(1))] = baseNode.get(value.intValue());
      } else if (!fieldName.equals("size")) {
        int index = Integer.parseInt(fieldName);
        if (value.isObject() && index < baseNode.size()) {
          JsonNode arrayValue = baseNode.get(index).deepCopy();
          if (arrayValue.isObject()) {
            applyDiff(arrayValue, value);
            newValues[index] = arrayValue;
          } else {
            newValues[index] = value.deepCopy();
          }
        } else {
          newValues[index] = value.deepCopy();
        }
      }
    }

    baseNode.removeAll();
    baseNode.addAll(Arrays.asList(newValues).stream().filter(entry -> entry != null).collect(Collectors.toList()));
  }
}
