/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.braintags.io.vertx.util.assertion.AssertTest;
import de.braintags.io.vertx.util.codec.AbstractPojoCodecTest;
import de.braintags.io.vertx.util.file.FileSystemUtilTest;
import de.braintags.io.vertx.util.lock.AsyncReadWriteLockTest;
import de.braintags.io.vertx.util.security.CRUDPermissionMapTest;
import de.braintags.io.vertx.util.security.crypt.impl.StandardEncoderTest;

/**
 * 
 * 
 * @author mremme
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ AsyncReadWriteLockTest.class, TestObjectUtil.class, ClassUtilTest.class, CounterObjectTest.class,
    ResultObjectTest.class, ErrorObjectTest.class, ExceptionUtilTest.class, AbstractCollectionAsyncTest.class,
    CollectionAsyncTest.class, FileSystemUtilTest.class, GeoLoationUtilTest.class, AbstractPojoCodecTest.class,
    AssertTest.class, CRUDPermissionMapTest.class, StandardEncoderTest.class, VertxAsyncTest.class })
public class TestAllUtil {

}
