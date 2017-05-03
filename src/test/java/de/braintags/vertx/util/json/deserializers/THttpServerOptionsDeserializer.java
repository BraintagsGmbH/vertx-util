package de.braintags.vertx.util.json.deserializers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PfxOptions;

/**
 * Unit test for {@link HttpServerOptionsDeserializer}
 * 
 * @author sschmitt
 *
 */
public class THttpServerOptionsDeserializer {

  @Test
  public void testDeserialization_withCustomDeserializer() throws JsonProcessingException {
    ObjectMapper om = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(HttpServerOptions.class, new HttpServerOptionsDeserializer());
    om.registerModule(module);

    HttpServerOptions options = createTestOptions();
    JsonNode tree = om.valueToTree(options);
    HttpServerOptions convertedOptions = om.treeToValue(tree, HttpServerOptions.class);
    assertThat(convertedOptions, is(options));
  }

  @Test(expected = JsonMappingException.class)
  public void testDeserialization_withoutCustomDeserializer() throws JsonProcessingException {
    ObjectMapper om = new ObjectMapper();

    HttpServerOptions options = createTestOptions();
    JsonNode tree = om.valueToTree(options);
    om.treeToValue(tree, HttpServerOptions.class);
  }

  private HttpServerOptions createTestOptions() {
    HttpServerOptions options = new HttpServerOptions();
    options.setPort(1234);
    options.setSsl(true);

    PfxOptions pfxOptions = new PfxOptions();
    pfxOptions.setPassword("secret");
    pfxOptions.setPath("/etc/cert/keystore");

    options.setPfxKeyCertOptions(pfxOptions);
    return options;
  }
}
