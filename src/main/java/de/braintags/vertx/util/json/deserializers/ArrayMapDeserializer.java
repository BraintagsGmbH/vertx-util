package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Basic serializer that can take JSON "Object" structure and
 * construct a {@link java.util.Map} instance, with typed contents.
 * <p>
 * Note: for untyped content (one indicated by passing Object.class
 * as the type), {@link UntypedObjectDeserializer} is used instead.
 * It can also construct {@link java.util.Map}s, but not with specific
 * POJO types, only other containers and primitives/wrappers.
 */
@JacksonStdImpl
public class ArrayMapDeserializer
    extends MapDeserializer
    implements ContextualDeserializer, ResolvableDeserializer {
  private static final long serialVersionUID = 1L;

  /*
   * /**********************************************************
   * /* Life-cycle
   * /**********************************************************
   */

  public ArrayMapDeserializer(JavaType mapType, ValueInstantiator valueInstantiator,
      KeyDeserializer keyDeser, JsonDeserializer<Object> valueDeser,
      TypeDeserializer valueTypeDeser) {
    super(mapType, valueInstantiator, keyDeser, valueDeser, valueTypeDeser);
  }

  /**
   * Copy-constructor that can be used by sub-classes to allow
   * copy-on-write styling copying of settings of an existing instance.
   */
  protected ArrayMapDeserializer(ArrayMapDeserializer src) {
    super(src);
  }

  protected ArrayMapDeserializer(ArrayMapDeserializer src,
      KeyDeserializer keyDeser, JsonDeserializer<Object> valueDeser,
      TypeDeserializer valueTypeDeser,
      Set<String> ignorable) {
    super(src, keyDeser, valueDeser, valueTypeDeser, ignorable);
  }

  /**
   * Fluent factory method used to create a copy with slightly
   * different settings. When sub-classing, MUST be overridden.
   */
  @Override
  @SuppressWarnings("unchecked")
  protected ArrayMapDeserializer withResolved(KeyDeserializer keyDeser,
      TypeDeserializer valueTypeDeser, JsonDeserializer<?> valueDeser,
      Set<String> ignorable) {

    if ((_keyDeserializer == keyDeser) && (_valueDeserializer == valueDeser)
        && (_valueTypeDeserializer == valueTypeDeser) && (_ignorableProperties == ignorable)) {
      return this;
    }
    return new ArrayMapDeserializer(this,
        keyDeser, (JsonDeserializer<Object>) valueDeser, valueTypeDeser, ignorable);
  }

  /*
   * /**********************************************************
   * /* Validation, post-processing (ResolvableDeserializer)
   * /**********************************************************
   */

  @Override
  public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    super.resolve(ctxt);
    _standardStringKey = false;
  }

  /*
   * /**********************************************************
   * /* ContainerDeserializerBase API
   * /**********************************************************
   */

  @Override
  public JavaType getContentType() {
    return _mapType.getContentType();
  }

  @Override
  public JsonDeserializer<Object> getContentDeserializer() {
    return _valueDeserializer;
  }

  /*
   * /**********************************************************
   * /* JsonDeserializer API
   * /**********************************************************
   */

  /**
   * Turns out that these are expensive enough to create so that caching
   * does make sense.
   * <p>
   * IMPORTANT: but, note, that instances CAN NOT BE CACHED if there is
   * a value type deserializer; this caused an issue with 2.4.4 of
   * JAXB Annotations (failing a test).
   * It is also possible that some other settings could make deserializers
   * un-cacheable; but on the other hand, caching can make a big positive
   * difference with performance... so it's a hard choice.
   * 
   * @since 2.4.4
   */
  @Override
  public boolean isCachable() {
    /*
     * As per [databind#735], existence of value or key deserializer (only passed
     * if annotated to use non-standard one) should also prevent caching.
     */
    return (_valueDeserializer == null)
        && (_keyDeserializer == null)
        && (_valueTypeDeserializer == null)
        && (_ignorableProperties == null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Object, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (_propertyBasedCreator != null) {
      return _deserializeUsingCreator(p, ctxt);
    }
    if (_delegateDeserializer != null) {
      return (Map<Object, Object>) _valueInstantiator.createUsingDelegate(ctxt,
          _delegateDeserializer.deserialize(p, ctxt));
    }
    if (!_hasDefaultCreator) {
      return (Map<Object, Object>) ctxt.handleMissingInstantiator(getMapClass(), p,
          "no default constructor found");
    }
    // Ok: must point to START_OBJECT, FIELD_NAME or END_OBJECT
    final Map<Object, Object> result = (Map<Object, Object>) _valueInstantiator.createUsingDefault(ctxt);
    JsonToken t = p.getCurrentToken();
    if (t != JsonToken.START_ARRAY) {
      return (Map<Object, Object>) ctxt.handleUnexpectedToken(getMapClass(), p);
    }
    _readAsArrayAndBind(p, ctxt, result);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<Object, Object> deserialize(JsonParser p, DeserializationContext ctxt,
      Map<Object, Object> result)
      throws IOException {
    // [databind#631]: Assign current value, to be accessible by custom serializers
    p.setCurrentValue(result);

    // Ok: must point to START_OBJECT or FIELD_NAME
    JsonToken t = p.getCurrentToken();
    if (t != JsonToken.START_ARRAY) {
      return (Map<Object, Object>) ctxt.handleUnexpectedToken(getMapClass(), p);
    }
    _readAsArrayAndBind(p, ctxt, result);
    return result;
  }

  @Override
  public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
      TypeDeserializer typeDeserializer)
      throws IOException, JsonProcessingException {
    JsonToken t = jp.getCurrentToken();
    if (t != JsonToken.START_ARRAY) {
      return ctxt.handleUnexpectedToken(getMapClass(), jp);
    }
    return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
  }

  /*
   * /**********************************************************
   * /* Internal methods
   * /**********************************************************
   */

  protected final void _readAsArrayAndBind(JsonParser p, DeserializationContext ctxt,
      Map<Object, Object> result) throws IOException {
    final KeyDeserializer keyDes = _keyDeserializer;
    final JsonDeserializer<Object> valueDes = _valueDeserializer;
    final TypeDeserializer typeDeser = _valueTypeDeserializer;

    MapReferringAccumulator referringAccumulator = null;
    boolean useObjectId = valueDes.getObjectIdReader() != null;
    if (useObjectId) {
      referringAccumulator = new MapReferringAccumulator(_mapType.getContentType().getRawClass(), result);
    }

    String keyStr;
    if (p.isExpectedStartArrayToken()) {
      keyStr = p.nextFieldName();
    } else {
      JsonToken t = p.getCurrentToken();
      if (t == JsonToken.END_OBJECT) {
        return;
      }
      if (t != JsonToken.FIELD_NAME) {
        ctxt.reportWrongTokenException(p, JsonToken.FIELD_NAME, null);
      }
      keyStr = p.getCurrentName();
    }

    for (; keyStr != null; keyStr = p.nextFieldName()) {
      Object key = keyDes.deserializeKey(keyStr, ctxt);
      // And then the value...
      JsonToken t = p.nextToken();
      if (_ignorableProperties != null && _ignorableProperties.contains(keyStr)) {
        p.skipChildren();
        continue;
      }
      try {
        // Note: must handle null explicitly here; value deserializers won't
        Object value;
        if (t == JsonToken.VALUE_NULL) {
          value = valueDes.getNullValue(ctxt);
        } else if (typeDeser == null) {
          value = valueDes.deserialize(p, ctxt);
        } else {
          value = valueDes.deserializeWithType(p, ctxt, typeDeser);
        }
        if (useObjectId) {
          referringAccumulator.put(key, value);
        } else {
          result.put(key, value);
        }
      } catch (UnresolvedForwardReference reference) {
        handleUnresolvedReference(p, referringAccumulator, key, reference);
      } catch (Exception e) {
        wrapAndThrow(e, result, keyStr);
      }
    }
  }

   @Override
  @SuppressWarnings("unchecked")
  public Map<Object, Object> _deserializeUsingCreator(JsonParser p, DeserializationContext ctxt) throws IOException {
    throw new UnsupportedOperationException();
  }


  private final static class MapReferringAccumulator {
    private final Class<?> _valueType;
    private final Map<Object, Object> _result;
    /**
     * A list of {@link MapReferring} to maintain ordering.
     */
    private final List<MapReferring> _accumulator = new ArrayList<>();

    public MapReferringAccumulator(Class<?> valueType, Map<Object, Object> result) {
      _valueType = valueType;
      _result = result;
    }

    public void put(Object key, Object value) {
      if (_accumulator.isEmpty()) {
        _result.put(key, value);
      } else {
        MapReferring ref = _accumulator.get(_accumulator.size() - 1);
        ref.next.put(key, value);
      }
    }

    public Referring handleUnresolvedReference(UnresolvedForwardReference reference, Object key) {
      MapReferring id = new MapReferring(this, reference, _valueType, key);
      _accumulator.add(id);
      return id;
    }

    public void resolveForwardReference(Object id, Object value) throws IOException {
      Iterator<MapReferring> iterator = _accumulator.iterator();
      // Resolve ordering after resolution of an id. This means either:
      // 1- adding to the result map in case of the first unresolved id.
      // 2- merge the content of the resolved id with its previous unresolved id.
      Map<Object, Object> previous = _result;
      while (iterator.hasNext()) {
        MapReferring ref = iterator.next();
        if (ref.hasId(id)) {
          iterator.remove();
          previous.put(ref.key, value);
          previous.putAll(ref.next);
          return;
        }
        previous = ref.next;
      }

      throw new IllegalArgumentException("Trying to resolve a forward reference with id [" + id
          + "] that wasn't previously seen as unresolved.");
    }
  }

  /**
   * Helper class to maintain processing order of value.
   * The resolved object associated with {@link #key} comes before the values in
   * {@link #next}.
   */
  static class MapReferring extends Referring {
    private final MapReferringAccumulator _parent;

    public final Map<Object, Object> next = new LinkedHashMap<>();
    public final Object key;

    MapReferring(MapReferringAccumulator parent, UnresolvedForwardReference ref,
        Class<?> valueType, Object key) {
      super(ref, valueType);
      _parent = parent;
      this.key = key;
    }

    @Override
    public void handleResolvedForwardReference(Object id, Object value) throws IOException {
      _parent.resolveForwardReference(id, value);
    }
  }
}
