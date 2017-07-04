package de.braintags.vertx.util.json.deserializers;

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Unit test for {@link MapAsArraySerializer}
 * 
 *
 */
public class TMapAsArraySerializer {

  @Test
  public void testDeserialization_withCustomDeserializer() throws JsonProcessingException {
    ObjectMapper om = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.setSerializerModifier(new MapAsArraySerializerModifier());
    om.registerModule(module);

    Map<String, Integer> test = new LinkedHashMap<>();
    test.put("Answer", 42);

    JsonNode tree = om.valueToTree(test);

    assertTrue(tree.isArray());
  }

}
