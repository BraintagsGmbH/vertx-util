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

import java.util.Collection;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

/**
 * Types are resolved the usual way except that the behavior of the defaultImpl is changed. Whenever the data do not
 * specify a type, the type specified in the type declaration respectively the type provided to the ObjectMapper is
 * used.
 * 
 * @author mpluecker
 *
 */
public class DefaultingTypeResolver extends StdTypeResolverBuilder {

  @Override
  public TypeDeserializer buildTypeDeserializer(final DeserializationConfig config, final JavaType baseType,
      final Collection<NamedType> subtypes) {
    TypeIdResolver idRes = idResolver(config, baseType, subtypes, false, true);
    return new DefaultingTypeDeserializer(baseType, idRes, _typeProperty, _typeIdVisible);
  }

}
