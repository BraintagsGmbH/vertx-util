package de.braintags.vertx.util.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.MapType;

public class BtSerializerFactory extends BeanSerializerFactory {

  public BtSerializerFactory(SerializerFactoryConfig config) {
    super(config);
  }

  /**
   * Method used by module registration functionality, to attach additional
   * serializer providers into this serializer factory. This is typically
   * handled by constructing a new instance with additional serializers,
   * to ensure thread-safe access.
   */
  @Override
  public BtSerializerFactory withConfig(SerializerFactoryConfig config) {
    if (_factoryConfig == config) {
      return this;
    }
    /*
     * 22-Nov-2010, tatu: Handling of subtypes is tricky if we do immutable-with-copy-ctor;
     * and we pretty much have to here either choose between losing subtype instance
     * when registering additional serializers, or losing serializers.
     * Instead, let's actually just throw an error if this method is called when subtype
     * has not properly overridden this method; this to indicate problem as soon as possible.
     */
    if (getClass() != BtSerializerFactory.class) {
      throw new IllegalStateException("Subtype of BtSerializerFactory (" + getClass().getName()
          + ") has not properly overridden method 'withAdditionalSerializers': can not instantiate subtype with "
          + "additional serializer definitions");
    }
    return new BtSerializerFactory(config);
  }

  /**
   * Helper method that handles configuration details when constructing serializers for
   * {@link java.util.Map} types.
   */
  @Override
  protected JsonSerializer<?> buildMapSerializer(SerializerProvider prov,
      MapType type, BeanDescription beanDesc,
      boolean staticTyping, JsonSerializer<Object> keySerializer,
      TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
      throws JsonMappingException {
    if (!type.getRawClass().equals(ArrayMap.class)) {
      return super.buildMapSerializer(prov, type, beanDesc, staticTyping, keySerializer, elementTypeSerializer,
          elementValueSerializer);
    }

    final SerializationConfig config = prov.getConfig();
    JsonSerializer<?> ser = null;

    // Order of lookups:
    // 1. Custom serializers
    // 2. Annotations (@JsonValue, @JsonDeserialize)
    // 3. Defaults

    for (Serializers serializers : customSerializers()) { // (1) Custom
      ser = serializers.findMapSerializer(config, type, beanDesc,
          keySerializer, elementTypeSerializer, elementValueSerializer);
      if (ser != null) {
        break;
      }
    }
    if (ser == null) {
      ser = findSerializerByAnnotations(prov, type, beanDesc); // (2) Annotations
      if (ser == null) {
      }
    }
    // [databind#120]: Allow post-processing
    if (_factoryConfig.hasSerializerModifiers()) {
      for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
        ser = mod.modifyMapSerializer(config, type, beanDesc, ser);
      }
    }
    return ser;
  }

}
