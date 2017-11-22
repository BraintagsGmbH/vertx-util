/*
 * #%L
 * Vert.x utilities from Braintags
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.vertx.core.json.Json;

public class TDefaultingTypeResolver {

  @Test
  public void testTypeResolving() throws JsonProcessingException {
    Dog dog = new Dog("Foo");
    ObjectNode encodedDog = Json.mapper.valueToTree(dog);
    Dog decodedDog = Json.mapper.treeToValue(encodedDog, Dog.class);
    Animal decodedAnimalAsDog = Json.mapper.treeToValue(encodedDog, Animal.class);
    assertEquals(dog, decodedDog);
    assertEquals(dog, decodedAnimalAsDog);

    JsonNode classAnnotation = encodedDog.remove("@class");
    assertNotNull(classAnnotation);

    Animal decodedAnimal = Json.mapper.treeToValue(encodedDog, Animal.class);
    Dog decodedDefaultedDog = Json.mapper.treeToValue(encodedDog, Dog.class);
    assertNotEquals(dog, decodedAnimal);
    assertEquals(dog, decodedDefaultedDog);
  }

  @JsonTypeInfo(include = As.PROPERTY, property = "@class", use = Id.CLASS)
  @JsonTypeResolver(DefaultingTypeResolver.class)
  public static class Animal {
    private final String name;

    @JsonCreator
    public Animal(@JsonProperty("name") final String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Animal other = (Animal) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }

  }

  public static class Dog extends Animal {

    @JsonCreator
    public Dog(@JsonProperty("name") final String name) {
      super(name);
    }

  }

}
