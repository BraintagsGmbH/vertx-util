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
package de.braintags.vertx.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.braintags.vertx.util.assertion.AssertTest;
import de.braintags.vertx.util.codec.AbstractPojoCodecTest;
import de.braintags.vertx.util.file.FileSystemUtilTest;
import de.braintags.vertx.util.file.InputOutputStreamTest;
import de.braintags.vertx.util.file.JsonReadStreamTest;
import de.braintags.vertx.util.json.TJsonConverter;
import de.braintags.vertx.util.json.deserializers.THttpServerOptionsDeserializer;
import de.braintags.vertx.util.lock.AsyncReadWriteLockTest;
import de.braintags.vertx.util.security.CRUDPermissionMapTest;
import de.braintags.vertx.util.security.crypt.impl.StandardEncoderTest;
import de.braintags.vertx.util.tree.TreeTest;

/**
 * 
 * 
 * @author mremme
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ AsyncReadWriteLockTest.class, TestObjectUtil.class, ClassUtilTest.class, CounterObjectTest.class,
    ResultObjectTest.class, ErrorObjectTest.class, ExceptionUtilTest.class, AbstractCollectionAsyncTest.class,
    CollectionAsyncTest.class, FileSystemUtilTest.class, InputOutputStreamTest.class, JsonReadStreamTest.class,
    GeoLoationUtilTest.class, AbstractPojoCodecTest.class, AssertTest.class, CRUDPermissionMapTest.class,
    StandardEncoderTest.class, VertxAsyncTest.class, TreeTest.class, TestResourceUtil.class, EqUtilTest.class,
    TJsonConverter.class, THttpServerOptionsDeserializer.class })
public class TestAllUtil {

}
