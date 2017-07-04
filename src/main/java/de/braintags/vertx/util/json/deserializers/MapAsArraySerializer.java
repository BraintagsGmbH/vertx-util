package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class MapAsArraySerializer extends JsonSerializer<Map<?, ?>> {

  @Override
  public void serialize(Map<?, ?> value, JsonGenerator generator,
      SerializerProvider serializers) throws IOException,
      JsonProcessingException {
    serializers.findTypedValueSerializer(serializers.get, true, property)
    generator.writeStartArray();
    for (Entry<?, ?> entry : value.entrySet()) {
      generator.writeStartObject();
      generator.writeObjectField("key", entry.getKey());
      generator.writeObjectField("value", entry.getValue());
      generator.writeEndObject();
    }
    generator.writeEndArray();
  }
}