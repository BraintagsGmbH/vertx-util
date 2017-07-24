package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Serializer implementation for serializing {link {@link ArrayMap}} types.
 * <p>
 */
public class ArrayMapSerializer
    extends ContainerSerializer<Map<?, ?>>
    implements ContextualSerializer {

  public static final String VALUE = "value";
  public static final String KEY = "key";
  public static final String ARRAY_MAP = "@arrayMap";

  private static final long serialVersionUID = 1L;

  protected final static JavaType UNSPECIFIED_TYPE = TypeFactory.unknownType();


  /**
   * Map-valued property being serialized with this instance
   */
  protected final BeanProperty _property;

  /**
   * Set of entries to omit during serialization, if any
   */
  protected final Set<String> _ignoredEntries;

  /**
   * Whether static types should be used for serialization of values
   * or not (if not, dynamic runtime type is used)
   */
  protected final boolean _valueTypeIsStatic;

  /**
   * Declared type of keys
   */
  protected final JavaType _keyType;

  /**
   * Declared type of contained values
   */
  protected final JavaType _valueType;

  /**
   * Key serializer to use, if it can be statically determined
   */
  protected JsonSerializer<Object> _keySerializer;

  /**
   * Value serializer to use, if it can be statically determined
   */
  protected JsonSerializer<Object> _valueSerializer;

  /**
   * Type identifier serializer used for values, if any.
   */
  protected final TypeSerializer _valueTypeSerializer;

  /**
   * If value type can not be statically determined, mapping from
   * runtime value types to serializers are stored in this object.
   */
  protected PropertySerializerMap _dynamicValueSerializers;

  /**
   * Id of the property filter to use, if any; null if none.
   *
   * @since 2.3
   */
  protected final Object _filterId;

  /**
   * Flag set if output is forced to be sorted by keys (usually due
   * to annotation).
   * 
   * @since 2.4
   */
  protected final boolean _sortKeys;

  /**
   * Value that indicates suppression mechanism to use for <b>values contained</b>;
   * either one of values of {@link com.fasterxml.jackson.annotation.JsonInclude.Include},
   * or actual object to compare against ("default value").
   * Note that inclusion value for Map instance itself is handled by caller (POJO
   * property that refers to the Map value).
   * 
   * @since 2.5
   */
  protected final Object _suppressableValue;

  /*
   * /**********************************************************
   * /* Life-cycle
   * /**********************************************************
   */

  public ArrayMapSerializer() {
    this(Collections.emptySet(), null, null, false, null, null, null);
  }

  /**
   * @since 2.5
   */
  @SuppressWarnings("unchecked")
  protected ArrayMapSerializer(Set<String> ignoredEntries,
      JavaType keyType, JavaType valueType, boolean valueTypeIsStatic,
      TypeSerializer vts,
      JsonSerializer<?> keySerializer, JsonSerializer<?> valueSerializer) {
    super(ArrayMap.class, false);
    _ignoredEntries = ((ignoredEntries == null) || ignoredEntries.isEmpty())
        ? null : ignoredEntries;
    _keyType = keyType;
    _valueType = valueType;
    _valueTypeIsStatic = valueTypeIsStatic;
    _valueTypeSerializer = vts;
    _keySerializer = (JsonSerializer<Object>) keySerializer;
    _valueSerializer = (JsonSerializer<Object>) valueSerializer;
    _dynamicValueSerializers = PropertySerializerMap.emptyForProperties();
    _property = null;
    _filterId = null;
    _sortKeys = false;
    _suppressableValue = null;
  }

  /**
   * @since 2.5
   */
  protected void _ensureOverride() {
    if (getClass() != ArrayMapSerializer.class) {
      throw new IllegalStateException("Missing override in class " + getClass().getName());
    }
  }

  @SuppressWarnings("unchecked")
  protected ArrayMapSerializer(ArrayMapSerializer src, BeanProperty property,
      JsonSerializer<?> keySerializer, JsonSerializer<?> valueSerializer,
      Set<String> ignoredEntries) {
    super(Map.class, false);
    _ignoredEntries = ((ignoredEntries == null) || ignoredEntries.isEmpty())
        ? null : ignoredEntries;
    _keyType = src._keyType;
    _valueType = src._valueType;
    _valueTypeIsStatic = src._valueTypeIsStatic;
    _valueTypeSerializer = src._valueTypeSerializer;
    _keySerializer = (JsonSerializer<Object>) keySerializer;
    _valueSerializer = (JsonSerializer<Object>) valueSerializer;
    _dynamicValueSerializers = src._dynamicValueSerializers;
    _property = property;
    _filterId = src._filterId;
    _sortKeys = src._sortKeys;
    _suppressableValue = src._suppressableValue;
  }

  /**
   * @since 2.5
   */
  protected ArrayMapSerializer(ArrayMapSerializer src, TypeSerializer vts,
      Object suppressableValue) {
    super(Map.class, false);
    _ignoredEntries = src._ignoredEntries;
    _keyType = src._keyType;
    _valueType = src._valueType;
    _valueTypeIsStatic = src._valueTypeIsStatic;
    _valueTypeSerializer = vts;
    _keySerializer = src._keySerializer;
    _valueSerializer = src._valueSerializer;
    _dynamicValueSerializers = src._dynamicValueSerializers;
    _property = src._property;
    _filterId = src._filterId;
    _sortKeys = src._sortKeys;
    // 05-Jun-2015, tatu: For referential, this is same as NON_EMPTY; for others, NON_NULL, so:
    if (suppressableValue == JsonInclude.Include.NON_ABSENT) {
      suppressableValue = _valueType.isReferenceType() ? JsonInclude.Include.NON_EMPTY : JsonInclude.Include.NON_NULL;
    }
    _suppressableValue = suppressableValue;
  }

  protected ArrayMapSerializer(ArrayMapSerializer src, Object filterId, boolean sortKeys) {
    super(Map.class, false);
    _ignoredEntries = src._ignoredEntries;
    _keyType = src._keyType;
    _valueType = src._valueType;
    _valueTypeIsStatic = src._valueTypeIsStatic;
    _valueTypeSerializer = src._valueTypeSerializer;
    _keySerializer = src._keySerializer;
    _valueSerializer = src._valueSerializer;
    _dynamicValueSerializers = src._dynamicValueSerializers;
    _property = src._property;
    _filterId = filterId;
    _sortKeys = sortKeys;
    _suppressableValue = src._suppressableValue;
  }

  public ArrayMapSerializer(ArrayMapSerializer src, JavaType keyType, JavaType valueType,
      boolean valueTypeIsStatic, JsonSerializer<?> keySer, JsonSerializer<?> valueSer, Set<String> ignoredEntries) {
    super(ArrayMap.class, false);
    _ignoredEntries = ((ignoredEntries == null) || ignoredEntries.isEmpty())
        ? null : ignoredEntries;
    _keyType = keyType;
    _valueType = valueType;
    _valueTypeIsStatic = valueTypeIsStatic;
    _valueTypeSerializer = src._valueTypeSerializer;
    _keySerializer = (JsonSerializer<Object>) keySer;
    _valueSerializer = (JsonSerializer<Object>) valueSer;
    _dynamicValueSerializers = src._dynamicValueSerializers;
    _property = src._property;
    _filterId = src._filterId;
    _sortKeys = src._sortKeys;
    _suppressableValue = src._suppressableValue;
  }

  @Override
  public ArrayMapSerializer _withValueTypeSerializer(TypeSerializer vts) {
    if (_valueTypeSerializer == vts) {
      return this;
    }
    _ensureOverride();
    return new ArrayMapSerializer(this, vts, null);
  }

  /**
   * @since 2.4
   */
  public ArrayMapSerializer withResolved(BeanProperty property,
      JsonSerializer<?> keySerializer, JsonSerializer<?> valueSerializer,
      Set<String> ignored, boolean sortKeys) {
    _ensureOverride();
    ArrayMapSerializer ser = new ArrayMapSerializer(this, property, keySerializer, valueSerializer, ignored);
    if (sortKeys != ser._sortKeys) {
      ser = new ArrayMapSerializer(ser, _filterId, sortKeys);
    }
    return ser;
  }

  private ArrayMapSerializer withResolved(JavaType keyType, JavaType valueType, boolean staticValueType,
      JsonSerializer<?> keySer, JsonSerializer<?> valueSer, Set<String> ignored) {
    ArrayMapSerializer result = new ArrayMapSerializer(this, keyType, valueType, staticValueType, keySer, valueSer,
        ignored);
    return result;
  }

  @Override
  public ArrayMapSerializer withFilterId(Object filterId) {
    if (_filterId == filterId) {
      return this;
    }
    _ensureOverride();
    return new ArrayMapSerializer(this, filterId, _sortKeys);
  }

  /**
   * Mutant factory for constructing an instance with different inclusion strategy
   * for content (Map values).
   * 
   * @since 2.5
   */
  public ArrayMapSerializer withContentInclusion(Object suppressableValue) {
    if (suppressableValue == _suppressableValue) {
      return this;
    }
    _ensureOverride();
    return new ArrayMapSerializer(this, _valueTypeSerializer, suppressableValue);
  }

  /*
   * /**********************************************************
   * /* Post-processing (contextualization)
   * /**********************************************************
   */

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider provider,
      BeanProperty property)
      throws JsonMappingException {
    JsonSerializer<?> ser = null;
    JsonSerializer<?> keySer = null;
    final AnnotationIntrospector intr = provider.getAnnotationIntrospector();
    final AnnotatedMember propertyAcc = (property == null) ? null : property.getMember();
    Object suppressableValue = _suppressableValue;

    // First: if we have a property, may have property-annotation overrides
    if ((propertyAcc != null) && (intr != null)) {
      Object serDef = intr.findKeySerializer(propertyAcc);
      if (serDef != null) {
        keySer = provider.serializerInstance(propertyAcc, serDef);
      }
      serDef = intr.findContentSerializer(propertyAcc);
      if (serDef != null) {
        ser = provider.serializerInstance(propertyAcc, serDef);
      }
    }

    JavaType keyType = _keyType;
    JavaType valueType = _valueType;
    boolean staticValueType = _valueTypeIsStatic;

    JavaType type = property.getType();
    MapType mapType = type instanceof MapType ? (MapType) type : null;
    if (keyType == null && valueType == null) {
      if (mapType == null) {
        keyType = valueType = UNSPECIFIED_TYPE;
      } else {
        keyType = mapType.getKeyType();
        valueType = mapType.getContentType();
      }
      // If value type is final, it's same as forcing static value typing:
      if (!staticValueType) {
        staticValueType = (valueType != null && valueType.isFinal());
      } else {
        // also: Object.class can not be handled as static, ever
        if (valueType.getRawClass() == Object.class) {
          staticValueType = false;
        }
      }
    }

    JsonInclude.Value inclV = findIncludeOverrides(provider, property, Map.class);
    JsonInclude.Include incl = inclV.getContentInclusion();
    if ((incl != null) && (incl != JsonInclude.Include.USE_DEFAULTS)) {
      suppressableValue = incl;
    }
    if (ser == null) {
      ser = _valueSerializer;
    }
    // [databind#124]: May have a content converter
    ser = findConvertingContentSerializer(provider, property, ser);
    if (ser == null) {
      // 30-Sep-2012, tatu: One more thing -- if explicit content type is annotated,
      // we can consider it a static case as well.
      // 20-Aug-2013, tatu: Need to avoid trying to access serializer for java.lang.Object tho
      if (staticValueType && !valueType.isJavaLangObject()) {
        ser = provider.findValueSerializer(valueType, property);
      }
    } else {
      ser = provider.handleSecondaryContextualization(ser, property);
    }
    if (keySer == null) {
      keySer = _keySerializer;
    }
    if (keySer == null) {
      if (keyType != null && !keyType.isJavaLangObject()) {
        keySer = provider.findValueSerializer(keyType, property);
      }
    } else {
      keySer = provider.handleSecondaryContextualization(keySer, property);
    }
    Set<String> ignored = _ignoredEntries;
    boolean sortKeys = false;
    if ((intr != null) && (propertyAcc != null)) {
      JsonIgnoreProperties.Value ignorals = intr.findPropertyIgnorals(propertyAcc);
      if (ignorals != null) {
        Set<String> newIgnored = ignorals.findIgnoredForSerialization();
        if ((newIgnored != null) && !newIgnored.isEmpty()) {
          ignored = (ignored == null) ? new HashSet<>() : new HashSet<>(ignored);
          for (String str : newIgnored) {
            ignored.add(str);
          }
        }
      }
      Boolean b = intr.findSerializationSortAlphabetically(propertyAcc);
      sortKeys = (b != null) && b.booleanValue();
    }
    JsonFormat.Value format = findFormatOverrides(provider, property, Map.class);
    if (format != null) {
      Boolean B = format.getFeature(JsonFormat.Feature.WRITE_SORTED_MAP_ENTRIES);
      if (B != null) {
        sortKeys = B.booleanValue();
      }
    }

    ArrayMapSerializer mser = withResolved(keyType, valueType, staticValueType, keySer, ser, ignored);

    if (suppressableValue != _suppressableValue) {
      mser = mser.withContentInclusion(suppressableValue);
    }

    // [databind#307]: allow filtering
    if (property != null) {
      AnnotatedMember m = property.getMember();
      if (m != null) {
        Object filterId = intr.findFilterId(m);
        if (filterId != null) {
          mser = mser.withFilterId(filterId);
        }
      }
    }
    return mser;
  }

  /*
   * /**********************************************************
   * /* Accessors
   * /**********************************************************
   */

  @Override
  public JavaType getContentType() {
    return _valueType;
  }

  @Override
  public JsonSerializer<?> getContentSerializer() {
    return _valueSerializer;
  }

  @Override
  public boolean isEmpty(SerializerProvider prov, Map<?, ?> value) {
    if (value == null || value.isEmpty()) {
      return true;
    }
    // 05-Nove-2015, tatu: Simple cases are cheap, but for recursive
    // emptiness checking we actually need to see if values are empty as well.
    Object supp = _suppressableValue;

    if ((supp == null) || (supp == JsonInclude.Include.ALWAYS)) {
      return false;
    }
    JsonSerializer<Object> valueSer = _valueSerializer;
    if (valueSer != null) {
      for (Object elemValue : value.values()) {
        if ((elemValue != null) && !valueSer.isEmpty(prov, elemValue)) {
          return false;
        }
      }
      return true;
    }
    // But if not statically known, try this:
    PropertySerializerMap serializers = _dynamicValueSerializers;
    for (Object elemValue : value.values()) {
      if (elemValue == null) {
        continue;
      }
      Class<?> cc = elemValue.getClass();
      // 05-Nov-2015, tatu: Let's not worry about generic types here, actually;
      // unlikely to make any difference, but does add significant overhead
      valueSer = serializers.serializerFor(cc);
      if (valueSer == null) {
        try {
          valueSer = _findAndAddDynamic(serializers, cc, prov);
        } catch (JsonMappingException e) { // Ugh... can not just throw as-is, so...
          // 05-Nov-2015, tatu: For now, probably best not to assume empty then
          return false;
        }
        serializers = _dynamicValueSerializers;
      }
      if (!valueSer.isEmpty(prov, elemValue)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean hasSingleElement(Map<?, ?> value) {
    return (value.size() == 1);
  }

  /*
   * /**********************************************************
   * /* Extended API
   * /**********************************************************
   */

  /**
   * Accessor for currently assigned key serializer. Note that
   * this may return null during construction of <code>MapAsArraySerializer2</code>:
   * depedencies are resolved during {@link #createContextual} method
   * (which can be overridden by custom implementations), but for some
   * dynamic types, it is possible that serializer is only resolved
   * during actual serialization.
   * 
   * @since 2.0
   */
  public JsonSerializer<?> getKeySerializer() {
    return _keySerializer;
  }

  /*
   * /**********************************************************
   * /* JsonSerializer implementation
   * /**********************************************************
   */

  @Override
  public void serialize(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeStartObject();
    gen.writeFieldName(ArrayMapSerializer.ARRAY_MAP);
    gen.writeStartArray();
    if (!value.isEmpty()) {
      Object suppressableValue = _suppressableValue;
      if (suppressableValue == JsonInclude.Include.ALWAYS) {
        suppressableValue = null;
      } else if (suppressableValue == null) {
        if (!provider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES)) {
          suppressableValue = JsonInclude.Include.NON_NULL;
        }
      }
      if (_sortKeys || provider.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)) {
        value = _orderEntries(value, gen, provider, suppressableValue);
      }
      PropertyFilter pf;
      if ((_filterId != null) && (pf = findPropertyFilter(provider, _filterId, value)) != null) {
        serializeFilteredFields(value, gen, provider, pf, suppressableValue);
      } else if (suppressableValue != null) {
        serializeOptionalFields(value, gen, provider, suppressableValue);
      } else if (_keySerializer != null && _valueSerializer != null) {
        serializeFieldsUsing(value, gen, provider, _keySerializer, _valueSerializer);
      } else {
        serializeFields(value, gen, provider);
      }
    }
    gen.writeEndArray();
    gen.writeEndObject();
  }

  @Override
  public void serializeWithType(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider,
      TypeSerializer typeSer)
      throws IOException {
    typeSer.writeTypePrefixForObject(value, gen);
    // [databind#631]: Assign current value, to be accessible by custom serializers
    gen.setCurrentValue(value);
    if (!value.isEmpty()) {
      Object suppressableValue = _suppressableValue;
      if (suppressableValue == JsonInclude.Include.ALWAYS) {
        suppressableValue = null;
      } else if (suppressableValue == null) {
        if (!provider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES)) {
          suppressableValue = JsonInclude.Include.NON_NULL;
        }
      }
      if (_sortKeys || provider.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)) {
        value = _orderEntries(value, gen, provider, suppressableValue);
      }
      PropertyFilter pf;
      if ((_filterId != null) && (pf = findPropertyFilter(provider, _filterId, value)) != null) {
        serializeFilteredFields(value, gen, provider, pf, suppressableValue);
      } else if (suppressableValue != null) {
        serializeOptionalFields(value, gen, provider, suppressableValue);
      } else if (_keySerializer != null && _valueSerializer != null) {
        serializeFieldsUsing(value, gen, provider, _keySerializer, _valueSerializer);
      } else {
        serializeFields(value, gen, provider);
      }
    }
    typeSer.writeTypeSuffixForObject(value, gen);
  }

  /*
   * /**********************************************************
   * /* Secondary serialization methods
   * /**********************************************************
   */

  /**
   * General-purpose serialization for contents, where we do not necessarily know
   * the value serialization, but
   * we do know that no value suppression is needed (which simplifies processing a bit)
   */
  public void serializeFields(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    // If value type needs polymorphic type handling, some more work needed:
    if (_valueTypeSerializer != null) {
      serializeTypedFields(value, gen, provider, null);
      return;
    }
    final JsonSerializer<Object> keySerializer = _keySerializer != null ? _keySerializer : new Dynamic(_property);
    final Set<String> ignored = _ignoredEntries;

    PropertySerializerMap serializers = _dynamicValueSerializers;

    for (Map.Entry<?, ?> entry : value.entrySet()) {
      Object valueElem = entry.getValue();
      // First, serialize key
      Object keyElem = entry.getKey();

      gen.writeStartObject();
      if (keyElem == null) {
        gen.writeFieldName(KEY);
        provider.findNullKeySerializer(_keyType, _property).serialize(null, gen, provider);
      } else {
        // One twist: is entry ignorable? If so, skip
        if ((ignored != null) && ignored.contains(keyElem))
          continue;
        gen.writeFieldName(KEY);
        keySerializer.serialize(keyElem, gen, provider);
      }

      // And then value
      if (valueElem == null) {
        provider.defaultSerializeNull(gen);
        continue;
      }
      JsonSerializer<Object> serializer = _valueSerializer;
      if (serializer == null) {
        Class<?> cc = valueElem.getClass();
        serializer = serializers.serializerFor(cc);
        if (serializer == null) {
          if (_valueType.hasGenericTypes()) {
            serializer = _findAndAddDynamic(serializers,
                provider.constructSpecializedType(_valueType, cc), provider);
          } else {
            serializer = _findAndAddDynamic(serializers, cc, provider);
          }
          serializers = _dynamicValueSerializers;
        }
      }
      try {
        gen.writeFieldName(VALUE);
        serializer.serialize(valueElem, gen, provider);
        gen.writeEndObject();
      } catch (Exception e) {
        // Add reference information
        String keyDesc = "" + keyElem;
        wrapAndThrow(provider, e, value, keyDesc);
      }
    }
  }

  /**
   * Serialization method called when exclusion filtering needs to be applied.
   */
  public void serializeOptionalFields(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider,
      Object suppressableValue)
      throws IOException {
    // If value type needs polymorphic type handling, some more work needed:
    if (_valueTypeSerializer != null) {
      serializeTypedFields(value, gen, provider, suppressableValue);
      return;
    }
    final Set<String> ignored = _ignoredEntries;
    PropertySerializerMap serializers = _dynamicValueSerializers;

    for (Map.Entry<?, ?> entry : value.entrySet()) {
      // First find key serializer
      final Object keyElem = entry.getKey();

      // Then value serializer
      final Object valueElem = entry.getValue();
      JsonSerializer<Object> valueSer;
      if (valueElem == null) {
        if (suppressableValue != null) { // all suppressions include null-suppression
          continue;
        }
        valueSer = provider.getDefaultNullValueSerializer();
      } else {
        valueSer = _valueSerializer;
        if (valueSer == null) {
          Class<?> cc = valueElem.getClass();
          valueSer = serializers.serializerFor(cc);
          if (valueSer == null) {
            if (_valueType.hasGenericTypes()) {
              valueSer = _findAndAddDynamic(serializers,
                  provider.constructSpecializedType(_valueType, cc), provider);
            } else {
              valueSer = _findAndAddDynamic(serializers, cc, provider);
            }
            serializers = _dynamicValueSerializers;
          }
        }
        // also may need to skip non-empty values:
        if ((suppressableValue == JsonInclude.Include.NON_EMPTY)
            && valueSer.isEmpty(provider, valueElem)) {
          continue;
        }
      }
      // and then serialize, if all went well
      try {
        gen.writeStartObject();
        if (keyElem == null) {
          gen.writeFieldName(KEY);
          provider.findNullKeySerializer(_keyType, _property).serialize(null, gen, provider);
        } else {
          if (ignored != null && ignored.contains(keyElem))
            continue;
          gen.writeFieldName(KEY);
          _keySerializer.serialize(keyElem, gen, provider);
        }

        gen.writeFieldName(VALUE);
        valueSer.serialize(valueElem, gen, provider);
        gen.writeEndObject();
      } catch (Exception e) {
        String keyDesc = "" + keyElem;
        wrapAndThrow(provider, e, value, keyDesc);
      }
    }
  }

  /**
   * Method called to serialize fields, when the value type is statically known,
   * so that value serializer is passed and does not need to be fetched from
   * provider.
   */
  public void serializeFieldsUsing(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider,
      JsonSerializer<Object> keySerializer, JsonSerializer<Object> ser)
      throws IOException {
    final Set<String> ignored = _ignoredEntries;
    final TypeSerializer typeSer = _valueTypeSerializer;

    for (Map.Entry<?, ?> entry : value.entrySet()) {
      Object keyElem = entry.getKey();
      if (ignored != null && ignored.contains(keyElem))
        continue;

      gen.writeStartObject();

      gen.writeFieldName(KEY);
      if (keyElem == null) {
        provider.findNullKeySerializer(_keyType, _property).serialize(null, gen, provider);
      } else {
        keySerializer.serialize(keyElem, gen, provider);
      }
      final Object valueElem = entry.getValue();
      gen.writeFieldName(VALUE);
      if (valueElem == null) {
        provider.defaultSerializeNull(gen);
      } else {
        try {
          if (typeSer == null) {
            ser.serialize(valueElem, gen, provider);
          } else {
            ser.serializeWithType(valueElem, gen, provider, typeSer);
          }
        } catch (Exception e) {
          String keyDesc = "" + keyElem;
          wrapAndThrow(provider, e, value, keyDesc);
        }
      }
      gen.writeEndObject();
    }
  }

  /**
   * Helper method used when we have a JSON Filter to use for potentially
   * filtering out Map entries.
   * 
   * @since 2.5
   */
  public void serializeFilteredFields(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider,
      PropertyFilter filter,
      Object suppressableValue) // since 2.5
      throws IOException {
    final Set<String> ignored = _ignoredEntries;

    PropertySerializerMap serializers = _dynamicValueSerializers;
    final MapPropertyAsEntry prop = new MapPropertyAsEntry(_valueTypeSerializer, _property);

    for (Map.Entry<?, ?> entry : value.entrySet()) {
      // First, serialize key; unless ignorable by key
      final Object keyElem = entry.getKey();
      if (ignored != null && ignored.contains(keyElem))
        continue;

      JsonSerializer<Object> keySerializer;
      if (keyElem == null) {
        keySerializer = provider.findNullKeySerializer(_keyType, _property);
      } else {
        keySerializer = _keySerializer;
      }
      // or by value; nulls often suppressed
      final Object valueElem = entry.getValue();

      JsonSerializer<Object> valueSer;
      // And then value
      if (valueElem == null) {
        if (suppressableValue != null) { // all suppressions include null-suppression
          continue;
        }
        valueSer = provider.getDefaultNullValueSerializer();
      } else {
        valueSer = _valueSerializer;
        if (valueSer == null) {
          Class<?> cc = valueElem.getClass();
          valueSer = serializers.serializerFor(cc);
          if (valueSer == null) {
            if (_valueType.hasGenericTypes()) {
              valueSer = _findAndAddDynamic(serializers,
                  provider.constructSpecializedType(_valueType, cc), provider);
            } else {
              valueSer = _findAndAddDynamic(serializers, cc, provider);
            }
            serializers = _dynamicValueSerializers;
          }
        }
        // also may need to skip non-empty values:
        if ((suppressableValue == JsonInclude.Include.NON_EMPTY)
            && valueSer.isEmpty(provider, valueElem)) {
          continue;
        }
      }
      // and with that, ask filter to handle it
      prop.reset(keyElem, keySerializer, valueSer);
      try {
        filter.serializeAsField(valueElem, gen, provider, prop);
      } catch (Exception e) {
        String keyDesc = "" + keyElem;
        wrapAndThrow(provider, e, value, keyDesc);
      }
    }
  }

  /**
   * @since 2.5
   */
  public void serializeTypedFields(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider,
      Object suppressableValue) // since 2.5
      throws IOException {
    final Set<String> ignored = _ignoredEntries;
    PropertySerializerMap serializers = _dynamicValueSerializers;

    for (Map.Entry<?, ?> entry : value.entrySet()) {
      Object keyElem = entry.getKey();
      JsonSerializer<Object> keySerializer;
      if (keyElem == null) {
        keySerializer = provider.findNullKeySerializer(_keyType, _property);
      } else {
        // One twist: is entry ignorable? If so, skip
        if (ignored != null && ignored.contains(keyElem))
          continue;
        keySerializer = _keySerializer;
      }
      final Object valueElem = entry.getValue();

      // And then value
      JsonSerializer<Object> valueSer;
      if (valueElem == null) {
        if (suppressableValue != null) { // all suppression include null suppression
          continue;
        }
        valueSer = provider.getDefaultNullValueSerializer();
      } else {
        valueSer = _valueSerializer;
        Class<?> cc = valueElem.getClass();
        valueSer = serializers.serializerFor(cc);
        if (valueSer == null) {
          if (_valueType.hasGenericTypes()) {
            valueSer = _findAndAddDynamic(serializers,
                provider.constructSpecializedType(_valueType, cc), provider);
          } else {
            valueSer = _findAndAddDynamic(serializers, cc, provider);
          }
          serializers = _dynamicValueSerializers;
        }
        // also may need to skip non-empty values:
        if ((suppressableValue == JsonInclude.Include.NON_EMPTY)
            && valueSer.isEmpty(provider, valueElem)) {
          continue;
        }
      }
      gen.writeStartObject();
      gen.writeFieldName(KEY);
      keySerializer.serialize(keyElem, gen, provider);
      try {
        gen.writeFieldName(VALUE);
        valueSer.serializeWithType(valueElem, gen, provider, _valueTypeSerializer);
        gen.writeEndObject();
      } catch (Exception e) {
        String keyDesc = "" + keyElem;
        wrapAndThrow(provider, e, value, keyDesc);
      }
    }
  }

  /*
   * /**********************************************************
   * /* Schema related functionality
   * /**********************************************************
   */

  @Override
  public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
    // (ryan) even though it's possible to statically determine the "value" type of the map,
    // there's no way to statically determine the keys, so the "Entries" can't be determined.
    return createSchemaNode("array", true);
  }

  @Override
  public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
      throws JsonMappingException {
    JsonMapFormatVisitor v2 = (visitor == null) ? null : visitor.expectMapFormat(typeHint);
    if (v2 != null) {

      JsonObjectFormatVisitor objectVisitor = visitor.expectObjectFormat(typeHint);
      if (objectVisitor == null) {
        return;
      }
      objectVisitor.property(KEY, _keySerializer, _keyType);

      JsonSerializer<?> valueSer = _valueSerializer;
      if (valueSer == null) {
        valueSer = _findAndAddDynamic(_dynamicValueSerializers,
            _valueType, visitor.getProvider());
      }
      objectVisitor.property(VALUE, valueSer, _valueType);
    }
  }

  /*
   * /**********************************************************
   * /* Internal helper methods
   * /**********************************************************
   */

  protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
      Class<?> type, SerializerProvider provider) throws JsonMappingException {
    PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSecondarySerializer(type, provider, _property);
    // did we get a new map of serializers? If so, start using it
    if (map != result.map) {
      _dynamicValueSerializers = result.map;
    }
    return result.serializer;
  }

  protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
      JavaType type, SerializerProvider provider) throws JsonMappingException {
    PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSecondarySerializer(type, provider, _property);
    if (map != result.map) {
      _dynamicValueSerializers = result.map;
    }
    return result.serializer;
  }

  protected ArrayMap<?, ?> _orderEntries(Map<?, ?> input, JsonGenerator gen,
      SerializerProvider provider, Object suppressableValue) throws IOException {
    // minor optimization: may already be sorted?
    if (input instanceof SortedMap<?, ?>) {
      return new ArrayMap<Object, Object>(input);
    }
    // [databind#1411]: TreeMap does not like null key... (although note that
    // check above should prevent this code from being called in that case)
    // [databind#153]: but, apparently, some custom Maps do manage hit this
    // problem.
    if (_hasNullKey(input)) {
      TreeMap<Object, Object> result = new TreeMap<>();
      for (Map.Entry<?, ?> entry : input.entrySet()) {
        Object key = entry.getKey();
        if (key == null) {
          _writeNullKeyedEntry(gen, provider, suppressableValue, entry.getValue());
          continue;
        }
        result.put(key, entry.getValue());
      }
      return new ArrayMap<>(result);
    }
    return new ArrayMap<>(new TreeMap<Object, Object>(input));
  }

  /**
   * @since 2.8.7
   */
  protected boolean _hasNullKey(Map<?, ?> input) {
    // 19-Feb-2017, tatu: As per [databind#1513] there are many cases where `null`
    // keys are not allowed, and even attempt to check for presence can cause
    // problems. Without resorting to external sorting (and internal API change),
    // or custom sortable Map implementation (more code) we can try black- or
    // white-listing (that is; either skip known problem cases; or only apply for
    // known good cases).
    // While my first instinct was to do black-listing (remove Hashtable and ConcurrentHashMap),
    // all in all it is probably better to just white list `HashMap` (and its sub-classes).

    return (input instanceof HashMap) && input.containsKey(null);
  }

  protected void _writeNullKeyedEntry(JsonGenerator gen, SerializerProvider provider,
      Object suppressableValue, Object value) throws IOException {
    JsonSerializer<Object> keySerializer = provider.findNullKeySerializer(_keyType, _property);
    JsonSerializer<Object> valueSer;
    if (value == null) {
      if (suppressableValue != null) { // all suppressions include null-suppression
        return;
      }
      valueSer = provider.getDefaultNullValueSerializer();
    } else {
      valueSer = _valueSerializer;
      if (valueSer == null) {
        Class<?> cc = value.getClass();
        valueSer = _dynamicValueSerializers.serializerFor(cc);
        if (valueSer == null) {
          if (_valueType.hasGenericTypes()) {
            valueSer = _findAndAddDynamic(_dynamicValueSerializers,
                provider.constructSpecializedType(_valueType, cc), provider);
          } else {
            valueSer = _findAndAddDynamic(_dynamicValueSerializers, cc, provider);
          }
        }
      }
      // also may need to skip non-empty values:
      if ((suppressableValue == JsonInclude.Include.NON_EMPTY)
          && valueSer.isEmpty(provider, value)) {
        return;
      }
    }
    // and then serialize, if all went well
    try {
      gen.writeStartObject();
      gen.writeFieldName(KEY);
      keySerializer.serialize(null, gen, provider);
      gen.writeFieldName(VALUE);
      valueSer.serialize(value, gen, provider);
      gen.writeEndObject();
    } catch (Exception e) {
      String keyDesc = "";
      wrapAndThrow(provider, e, value, keyDesc);
    }
  }

  /**
   * Key serializer used when key type is not known statically, and actual key
   * serializer needs to be dynamically located.
   */
  public static class Dynamic extends StdSerializer<Object> {
    // Important: MUST be transient, to allow serialization of key serializer itself
    protected transient PropertySerializerMap _dynamicSerializers;
    private BeanProperty property;

    public Dynamic(BeanProperty property) {
      super(String.class, false);
      _dynamicSerializers = PropertySerializerMap.emptyForProperties();
    }

    Object readResolve() {
      // Since it's transient, and since JDK serialization by-passes ctor, need this:
      _dynamicSerializers = PropertySerializerMap.emptyForProperties();
      return this;
    }

    @Override
    public void serialize(Object value, JsonGenerator g, SerializerProvider provider)
        throws IOException {
      Class<?> cls = value.getClass();
      PropertySerializerMap m = _dynamicSerializers;
      JsonSerializer<Object> ser = m.serializerFor(cls);
      if (ser == null) {
        ser = _findAndAddDynamic(m, cls, provider);
      }
      ser.serialize(value, g, provider);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
        throws JsonMappingException {
      visitStringFormat(visitor, typeHint);
    }

    protected JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
        Class<?> type, SerializerProvider provider) throws JsonMappingException {
      PropertySerializerMap.SerializerAndMapResult result =
          // null -> for now we won't keep ref or pass BeanProperty; could change
          map.findAndAddSecondarySerializer(type, provider, property);
      // did we get a new map of serializers? If so, start using it
      if (map != result.map) {
        _dynamicSerializers = result.map;
      }
      return result.serializer;
    }
  }
}
