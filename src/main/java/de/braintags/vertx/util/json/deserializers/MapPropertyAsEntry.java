package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;

import de.braintags.vertx.util.freezable.ArrayMapSerializer;

public class MapPropertyAsEntry extends PropertyWriter {
  private static final long serialVersionUID = 1L;

  protected final TypeSerializer _typeSerializer;

  protected final BeanProperty _property;

  protected Object _key;

  protected JsonSerializer<Object> _keySerializer, _valueSerializer;

  public MapPropertyAsEntry(TypeSerializer typeSer, BeanProperty prop) {
    super((prop == null) ? PropertyMetadata.STD_REQUIRED_OR_OPTIONAL : prop.getMetadata());
    _typeSerializer = typeSer;
    _property = prop;
  }

  /**
   * Initialization method that needs to be called before passing
   * property to filter.
   */
  public void reset(Object key, JsonSerializer<Object> keySer, JsonSerializer<Object> valueSer) {
    _key = key;
    _keySerializer = keySer;
    _valueSerializer = valueSer;
  }

  @Override
  public String getName() {
    if (_key instanceof String) {
      return (String) _key;
    }
    return String.valueOf(_key);
  }

  @Override
  public PropertyName getFullName() {
    return new PropertyName(getName());
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> acls) {
    return (_property == null) ? null : _property.getAnnotation(acls);
  }

  @Override
  public <A extends Annotation> A getContextAnnotation(Class<A> acls) {
    return (_property == null) ? null : _property.getContextAnnotation(acls);
  }

  @Override
  public void serializeAsField(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {

    gen.writeStartObject();
    gen.writeFieldName(ArrayMapSerializer.KEY);
    _keySerializer.serialize(_key, gen, provider);

    gen.writeFieldName(ArrayMapSerializer.VALUE);
    if (_typeSerializer == null) {
      _valueSerializer.serialize(value, gen, provider);
    } else {
      _valueSerializer.serializeWithType(value, gen, provider, _typeSerializer);
    }
    gen.writeEndObject();
  }

  @Override
  public void serializeAsOmittedField(Object value, JsonGenerator gen, SerializerProvider provider) throws Exception {
    if (!gen.canOmitFields()) {
      gen.writeOmittedField(getName());
    }
  }

  @Override
  public void serializeAsElement(Object value, JsonGenerator gen, SerializerProvider provider) throws Exception {
    if (_typeSerializer == null) {
      _valueSerializer.serialize(value, gen, provider);
    } else {
      _valueSerializer.serializeWithType(value, gen, provider, _typeSerializer);
    }
  }

  @Override
  public void serializeAsPlaceholder(Object value, JsonGenerator gen, SerializerProvider provider) throws Exception {
    gen.writeNull();
  }

  /*
   * /**********************************************************
   * /* Rest of BeanProperty, nop
   * /**********************************************************
   */

  @Override
  public void depositSchemaProperty(JsonObjectFormatVisitor objectVisitor, SerializerProvider provider)
      throws JsonMappingException {
    if (_property != null) {
      _property.depositSchemaProperty(objectVisitor, provider);
    }
  }

  @Override
  @Deprecated
  public void depositSchemaProperty(ObjectNode propertiesNode, SerializerProvider provider)
      throws JsonMappingException {
    // nothing to do here
  }

  @Override
  public JavaType getType() {
    return (_property == null) ? TypeFactory.unknownType() : _property.getType();
  }

  @Override
  public PropertyName getWrapperName() {
    return (_property == null) ? null : _property.getWrapperName();
  }

  @Override
  public AnnotatedMember getMember() {
    return (_property == null) ? null : _property.getMember();
  }
}
