package de.braintags.vertx.util.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
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

import de.braintags.vertx.util.json.deserializers.ArrayMap;
import de.braintags.vertx.util.json.deserializers.ArrayMapDeserializer;
import de.braintags.vertx.util.json.deserializers.ArrayMapSerializer;

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
  public void setupModule(SetupContext context) {
    context.addDeserializers(new Deserializers.Base() {
      @Override
      public JsonDeserializer<?> findMapDeserializer(MapType type, DeserializationConfig config,
          BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer,
          JsonDeserializer<?> elementDeserializer) throws JsonMappingException {

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
      public JsonSerializer<?> findMapSerializer(SerializationConfig config, MapType type, BeanDescription beanDesc,
          JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer,
          JsonSerializer<Object> elementValueSerializer) {
        if (beanDesc.getBeanClass().equals(ArrayMap.class)) {
          return new ArrayMapSerializer();
        } else {
          return super.findMapSerializer(config, type, beanDesc, keySerializer, elementTypeSerializer,
              elementValueSerializer);
        }
      }
    });

  }

}
