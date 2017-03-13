package de.braintags.vertx.util.json.deserializers;

import java.io.IOException;
import java.sql.Time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import de.braintags.vertx.util.json.BtModule;

/**
 * Deserializer for {@link Time}. Needed because the ParameterNamesModule for jackson disables standard use of "valueOf"
 * methods for object creation. Add {@link BtModule} to use this deserializer for all {@link Time} values.
 * 
 * @author sschmitt
 * 
 */
public class TimeDeserializer extends StdDeserializer<Time> {

  private static final long serialVersionUID = 1L;

  public TimeDeserializer() {
    super(Time.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser,
   * com.fasterxml.jackson.databind.DeserializationContext)
   */
  @Override
  public Time deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return Time.valueOf(p.getValueAsString());
  }

}