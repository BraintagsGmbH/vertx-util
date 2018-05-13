package de.braintags.vertx.util.json.deserializers;

import java.util.Collections;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.MapType;

import de.braintags.vertx.util.json.ArrayMapSerializer;


/**
 * This module enables the usage of {@link ArrayMap} encoding.
 * 
 * @author mpluecker
 *
 */
public class ArrayMapModule extends Module {

  @Override
  public String getModuleName() {
    return "ArrayMapModule";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }

  @Override
  public void setupModule(final SetupContext context) {
    context.addDeserializers(new Deserializers.Base() {
      @Override
      public JsonDeserializer<?> findMapDeserializer(final MapType type, final DeserializationConfig config,
          final BeanDescription beanDesc, final KeyDeserializer keyDeserializer,
          final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer)
          throws JsonMappingException {

        if (beanDesc.getClassInfo().getRawType().equals(ArrayMap.class)) {
          return new ArrayMapDeserializer(type, beanDesc, new ValueInstantiator.Base(ArrayMap.class), null,
              (JsonDeserializer<Object>) elementDeserializer, elementTypeDeserializer);
        } else {
          return super.findMapDeserializer(type, config, beanDesc, keyDeserializer, elementTypeDeserializer,
              elementDeserializer);
        }
      }

    });

    context.addSerializers(new Serializers.Base() {

      @Override
      public JsonSerializer<?> findMapSerializer(final SerializationConfig config, final MapType type,
          final BeanDescription beanDesc, final JsonSerializer<Object> keySerializer,
          final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        if (beanDesc.getBeanClass().equals(ArrayMap.class)) {
          JavaType valueType = type.getContentType();
          boolean valueTypeStatic = valueType != null && valueType.isFinal();
          return new ArrayMapSerializer(Collections.emptySet(), type.getKeyType(), valueType, valueTypeStatic,
              elementTypeSerializer, keySerializer, elementValueSerializer);
        } else {
          return super.findMapSerializer(config, type, beanDesc, keySerializer, elementTypeSerializer,
              elementValueSerializer);
        }
      }
    });

  }

}
