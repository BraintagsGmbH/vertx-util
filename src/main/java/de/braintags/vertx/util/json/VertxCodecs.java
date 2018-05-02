package de.braintags.vertx.util.json;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Copied from {@link json} due to visibility
 *
 */
public class VertxCodecs {

  static class JsonObjectSerializer extends JsonSerializer<JsonObject> {
    @Override
    public void serialize(final JsonObject value, final JsonGenerator jgen, final SerializerProvider provider)
        throws IOException {
      jgen.writeObject(value.getMap());
    }
  }

  static class JsonArraySerializer extends JsonSerializer<JsonArray> {
    @Override
    public void serialize(final JsonArray value, final JsonGenerator jgen, final SerializerProvider provider)
        throws IOException {
      jgen.writeObject(value.getList());
    }
  }

  static class InstantSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(final Instant value, final JsonGenerator jgen, final SerializerProvider provider)
        throws IOException {
      jgen.writeString(ISO_INSTANT.format(value));
    }
  }

  static class ByteArraySerializer extends JsonSerializer<byte[]> {
    private final Base64.Encoder BASE64 = Base64.getEncoder();

    @Override
    public void serialize(final byte[] value, final JsonGenerator jgen, final SerializerProvider provider)
        throws IOException {
      jgen.writeString(BASE64.encodeToString(value));
    }
  }

}
