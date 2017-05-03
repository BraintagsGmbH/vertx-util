package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;

/**
 * Custom deserializer for {@link HttpServerOptions}, as default deserialization via jackson is not possible due to the
 * "keyCertOptions" being an interface without any jackson type annotation
 * 
 * @author sschmitt
 *
 */
public class HttpServerOptionsDeserializer extends StdDeserializer<HttpServerOptions> {

  private static final long serialVersionUID = 1L;

  protected HttpServerOptionsDeserializer() {
    super(HttpServerOptions.class);
  }

  @Override
  public HttpServerOptions deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    String stringValue = p.readValueAsTree().toString();
    if (stringValue != null) {
      HttpServerOptions options = new HttpServerOptions(new JsonObject(stringValue));
      return options;
    } else
      return null;
  }

}