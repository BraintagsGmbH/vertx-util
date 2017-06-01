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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/**
 * Types are deserialized the usual way except that the behavior of the defaultImpl is changed. Whenever the data do not
 * specify a type, the base type is used.
 * 
 * @author mpluecker
 *
 */
public class DefaultingTypeDeserializer extends AsPropertyTypeDeserializer {

  public DefaultingTypeDeserializer(
      final JavaType bt, final TypeIdResolver idRes,
      final String typePropertyName, final boolean typeIdVisible) {
    super(bt, idRes, typePropertyName, typeIdVisible, null);
  }

  public DefaultingTypeDeserializer(
      final AsPropertyTypeDeserializer src, final BeanProperty property) {
    super(src, property);
  }

  @Override
  public TypeDeserializer forProperty(
      final BeanProperty prop) {
    return (prop == _property) ? this : new DefaultingTypeDeserializer(this, prop);
  }

  @Override
  protected Object _deserializeTypedUsingDefaultImpl(JsonParser p, final DeserializationContext ctxt,
      final TokenBuffer tb)
      throws IOException {

    JavaType targetType = _baseType;
    
    Class<?> raw = targetType.getRawClass();
    if (ClassUtil.isBogusClass(raw)) {
        return NullifyingDeserializer.instance;
    }

    synchronized (this) {
      if (_defaultImplDeserializer == null) {
        _defaultImplDeserializer = ctxt.findContextualValueDeserializer(targetType, _property);
      }
    }

    if (tb != null) {
      tb.writeEndObject();
      p = tb.asParser(p);
      // must move to point to the first token:
      p.nextToken();
    }

    return _defaultImplDeserializer.deserialize(p, ctxt);
  }
}