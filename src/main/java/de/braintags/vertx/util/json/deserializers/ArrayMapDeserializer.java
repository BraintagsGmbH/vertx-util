package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import de.braintags.vertx.util.json.ArrayMapSerializer;

/**
 * Custom serializer that can take a special {@link ArrayMapNode} structure and
 * construct a {@link java.util.Map} instance, with typed contents.
 * <p>
 * Note: for untyped content (one indicated by passing Object.class
 * as the type), {@link UntypedObjectDeserializer} is used instead.
 * It can also construct {@link java.util.Map}s, but not with specific
 * POJO types, only other containers and primitives/wrappers.
 */
public class ArrayMapDeserializer extends MapDeserializer implements ContextualDeserializer, ResolvableDeserializer {
  private static final long serialVersionUID = 1L;

  /**
   * Key deserializer to use; either passed via constructor
   * (when indicated by annotations), or resolved when
   * {@link #resolve} is called;
   */
  protected JsonDeserializer<?> _objKeyDeserializer;

  private BeanDescription _beanDesc;

  /*
   * /**********************************************************
   * /* Life-cycle
   * /**********************************************************
   */
  public ArrayMapDeserializer(final JavaType mapType, final BeanDescription beanDesc,
      final ValueInstantiator valueInstantiator, final JsonDeserializer<?> keyDeser,
      final JsonDeserializer<Object> valueDeser, final TypeDeserializer valueTypeDeser) {
    super(mapType, valueInstantiator, null, valueDeser, valueTypeDeser);
    this._beanDesc = beanDesc;
    this._objKeyDeserializer = keyDeser;
  }

  /**
   * Copy-constructor that can be used by sub-classes to allow
   * copy-on-write styling copying of settings of an existing instance.
   */
  protected ArrayMapDeserializer(final ArrayMapDeserializer src) {
    super(src);
  }

  protected ArrayMapDeserializer(final ArrayMapDeserializer src, final BeanDescription beanDesc,
      final ValueInstantiator valueInstantiator, final JsonDeserializer<?> keyDeser,
      final JsonDeserializer<Object> valueDeser, final TypeDeserializer valueTypeDeser, final Set<String> ignorable) {
    this(src._mapType, src._beanDesc, valueInstantiator, keyDeser, valueDeser, valueTypeDeser);
    _propertyBasedCreator = src._propertyBasedCreator;
    _delegateDeserializer = src._delegateDeserializer;
    if (ignorable != null && !ignorable.isEmpty()) {
      throw new UnsupportedOperationException("ignored entries are note supported for array maps");
    }
  }

  @Override
  @Deprecated
  protected MapDeserializer withResolved(final KeyDeserializer keyDeser, final TypeDeserializer valueTypeDeser,
      final JsonDeserializer<?> valueDeser, final Set<String> ignorable) {
    // do not use this! You need a JsonDeserializer as keyDeser
    throw new UnsupportedOperationException();
  }

  /**
   * Fluent factory method used to create a copy with slightly
   * different settings. When sub-classing, MUST be overridden.
   */
  @SuppressWarnings("unchecked")
  protected ArrayMapDeserializer withResolved(final ValueInstantiator valueInstatiator,
      final JsonDeserializer<?> keyDeser, final TypeDeserializer valueTypeDeser, final JsonDeserializer<?> valueDeser,
      final Set<String> ignorable) {

    if ((_objKeyDeserializer == keyDeser) && (_valueDeserializer == valueDeser)
        && (_valueTypeDeserializer == valueTypeDeser) && (_ignorableProperties == ignorable)) {
      return this;
    }
    return new ArrayMapDeserializer(this, _beanDesc, valueInstatiator, keyDeser, (JsonDeserializer<Object>) valueDeser,
        valueTypeDeser, ignorable);
  }

  /*
   * /**********************************************************
   * /* Validation, post-processing (ResolvableDeserializer)
   * /**********************************************************
   */

  @Override
  public void resolve(final DeserializationContext ctxt) throws JsonMappingException {
    super.resolve(ctxt);
    _standardStringKey = false;
  }

  /**
   * Method called to finalize setup of this deserializer,
   * when it is known for which property deserializer is needed for.
   */
  @Override
  public JsonDeserializer<?> createContextual(final DeserializationContext ctxt, final BeanProperty property)
      throws JsonMappingException {
    JsonDeserializer<?> kd = _objKeyDeserializer;
    if (kd == null) {
      kd = ctxt.findNonContextualValueDeserializer(_mapType.getKeyType());
    } else {
      if (kd instanceof ContextualDeserializer) {
        kd = ((ContextualDeserializer) kd).createContextual(ctxt, property);
      }
    }

    JsonDeserializer<?> vd = _valueDeserializer;
    // [databind#125]: May have a content converter
    if (property != null) {
      vd = findConvertingContentDeserializer(ctxt, property, vd);
    }
    final JavaType vt = _mapType.getContentType();
    if (vd == null) {
      vd = ctxt.findContextualValueDeserializer(vt, property);
    } else { // if directly assigned, probably not yet contextual, so:
      vd = ctxt.handleSecondaryContextualization(vd, property, vt);
    }
    TypeDeserializer vtd = _valueTypeDeserializer;
    if (vtd != null) {
      vtd = vtd.forProperty(property);
    }
    Set<String> ignored = _ignorableProperties;
    AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
    if (intr != null && property != null) {
      AnnotatedMember member = property.getMember();
      if (member != null) {
        JsonIgnoreProperties.Value ignorals = intr.findPropertyIgnorals(member);
        if (ignorals != null) {
          Set<String> ignoresToAdd = ignorals.findIgnoredForDeserialization();
          if (!ignoresToAdd.isEmpty()) {
            ignored = (ignored == null) ? new HashSet<>() : new HashSet<>(ignored);
            for (String str : ignoresToAdd) {
              ignored.add(str);
            }
          }
        }
      }
    }
    ValueInstantiator valueInstantiator = ctxt.getFactory().findValueInstantiator(ctxt, _beanDesc);
    return withResolved(valueInstantiator, kd, vtd, vd, ignored);
  }

  /*
   * /**********************************************************
   * /* JsonDeserializer API
   * /**********************************************************
   */

  @Override
  @SuppressWarnings("unchecked")
  public Map<Object, Object> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
    if (_propertyBasedCreator != null) {
      return _deserializeUsingCreator(p, ctxt);
    }
    if (_delegateDeserializer != null) {
      return (Map<Object, Object>) _valueInstantiator.createUsingDelegate(ctxt,
          _delegateDeserializer.deserialize(p, ctxt));
    }
    if (!_hasDefaultCreator) {
      return (Map<Object, Object>) ctxt.handleMissingInstantiator(getMapClass(), p, "no default constructor found");
    }
    // Ok: must point to START_OBJECT, FIELD_NAME or END_OBJECT
    final Map<Object, Object> result = (Map<Object, Object>) _valueInstantiator.createUsingDefault(ctxt);
    if (!p.isExpectedStartObjectToken()) {
      return (Map<Object, Object>) ctxt.handleUnexpectedToken(getMapClass(), p);
    }
    _readAsArrayAndBind(p, ctxt, result);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<Object, Object> deserialize(final JsonParser p, final DeserializationContext ctxt,
      final Map<Object, Object> result) throws IOException {
    // [databind#631]: Assign current value, to be accessible by custom serializers
    p.setCurrentValue(result);

    // Ok: must point to START_OBJECT or FIELD_NAME
    if (!p.isExpectedStartObjectToken()) {
      return (Map<Object, Object>) ctxt.handleUnexpectedToken(getMapClass(), p);
    }
    _readAsArrayAndBind(p, ctxt, result);
    return result;
  }

  @Override
  public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt,
      final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
    if (!jp.isExpectedStartObjectToken()) {
      return ctxt.handleUnexpectedToken(getMapClass(), jp);
    }
    return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
  }

  /*
   * /**********************************************************
   * /* Internal methods
   * /**********************************************************
   */

  protected final void _readAsArrayAndBind(final JsonParser p, final DeserializationContext ctxt,
      final Map<Object, Object> result) throws IOException {
    final JsonDeserializer<?> keyDes = _objKeyDeserializer;
    final JsonDeserializer<Object> valueDes = _valueDeserializer;
    final TypeDeserializer typeDeser = _valueTypeDeserializer;

    MapReferringAccumulator referringAccumulator = null;
    boolean useObjectId = valueDes.getObjectIdReader() != null;
    if (useObjectId) {
      referringAccumulator = new MapReferringAccumulator(_mapType.getContentType().getRawClass(), result);
    }

    String headerField = p.nextFieldName();
    if (!ArrayMapSerializer.ARRAY_MAP.equals(headerField)) {
      throw new JsonMappingException(p, "missing fieldname entry " + ArrayMapSerializer.ARRAY_MAP,
          p.getCurrentLocation());
    }
    if (p.nextToken() != JsonToken.START_ARRAY) {
      ctxt.handleUnexpectedToken(getMapClass(), p);
      return;
    }
    // currently at START_ARRAY
    while (p.nextToken() != JsonToken.END_ARRAY) {
      if (p.isExpectedStartObjectToken()) {
        Object key = null;
        Object value = null;
        while (true) {
          String fieldName = p.nextFieldName();
          if (fieldName == null) {
            if (p.getCurrentToken() != JsonToken.END_OBJECT) {
              ctxt.handleUnexpectedToken(getMapClass(), p);
            }
            break;
          }
          switch (fieldName) {
            case ArrayMapSerializer.KEY:
              p.nextToken();
              key = deserialize(p, ctxt, keyDes, null);
              break;
            case ArrayMapSerializer.VALUE:
              // And then the value...
              try {
                p.nextToken();
                value = deserialize(p, ctxt, valueDes, typeDeser);
                if (useObjectId) {
                  referringAccumulator.put(key, value);
                } else {
                  result.put(key, value);
                }
              } catch (UnresolvedForwardReference reference) {
                handleUnresolvedReference(p, referringAccumulator, key, reference);
              } catch (Exception e) {
                wrapAndThrow(e, result, key != null ? key.toString() : "null");
              }
              break;
            default:
              throw new JsonMappingException(p, "unknown map entry field: " + fieldName, p.getCurrentLocation());
          }
        }
      } else {
        ctxt.handleUnexpectedToken(getMapClass(), p);
        break;
      }
    }
    if (p.nextToken() != JsonToken.END_OBJECT) {
      throw new JsonMappingException(p, "expected " + JsonToken.END_OBJECT, p.getCurrentLocation());
    }
  }

  private Object deserialize(final JsonParser p, final DeserializationContext ctxt, final JsonDeserializer<?> valueDes,
      final TypeDeserializer typeDeser) throws JsonMappingException, IOException, JsonProcessingException {
    // Note: must handle null explicitly here; value deserializers won't
    Object value;
    if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
      value = valueDes.getNullValue(ctxt);
    } else if (typeDeser == null) {
      value = valueDes.deserialize(p, ctxt);
    } else {
      value = valueDes.deserializeWithType(p, ctxt, typeDeser);
    }
    return value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Object, Object> _deserializeUsingCreator(final JsonParser p, final DeserializationContext ctxt)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  private void handleUnresolvedReference(final JsonParser jp, final MapReferringAccumulator accumulator,
      final Object key, final UnresolvedForwardReference reference) throws JsonMappingException {
    if (accumulator == null) {
      throw JsonMappingException.from(jp, "Unresolved forward reference but no identity info.", reference);
    }
    Referring referring = accumulator.handleUnresolvedReference(reference, key);
    reference.getRoid().appendReferring(referring);
  }

  private final static class MapReferringAccumulator {
    private final Class<?> _valueType;
    private final Map<Object, Object> _result;
    /**
     * A list of {@link MapReferring} to maintain ordering.
     */
    private final List<MapReferring> _accumulator = new ArrayList<>();

    public MapReferringAccumulator(final Class<?> valueType, final Map<Object, Object> result) {
      _valueType = valueType;
      _result = result;
    }

    public void put(final Object key, final Object value) {
      if (_accumulator.isEmpty()) {
        _result.put(key, value);
      } else {
        MapReferring ref = _accumulator.get(_accumulator.size() - 1);
        ref.next.put(key, value);
      }
    }

    public Referring handleUnresolvedReference(final UnresolvedForwardReference reference, final Object key) {
      MapReferring id = new MapReferring(this, reference, _valueType, key);
      _accumulator.add(id);
      return id;
    }

    public void resolveForwardReference(final Object id, final Object value) throws IOException {
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

      throw new IllegalArgumentException(
          "Trying to resolve a forward reference with id [" + id + "] that wasn't previously seen as unresolved.");
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

    MapReferring(final MapReferringAccumulator parent, final UnresolvedForwardReference ref, final Class<?> valueType,
        final Object key) {
      super(ref, valueType);
      _parent = parent;
      this.key = key;
    }

    @Override
    public void handleResolvedForwardReference(final Object id, final Object value) throws IOException {
      _parent.resolveForwardReference(id, value);
    }
  }
}
