package de.braintags.vertx.util.json;

import java.sql.Time;

import com.fasterxml.jackson.databind.module.SimpleModule;

import de.braintags.vertx.util.json.deserializers.TimeDeserializer;

/**
 * Module for all custom serializers and deserializers of the util project
 * 
 * @author sschmitt
 * 
 */
public class BtModule extends SimpleModule {

  private static final long serialVersionUID = -7095661124862487777L;

  public BtModule() {
    super();
    addDeserializer(Time.class, new TimeDeserializer());
  }

}
