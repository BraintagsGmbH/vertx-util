/*
 * #%L
 * vertx-util
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util.file;

import org.junit.Before;
import org.junit.Test;

import de.braintags.vertx.BtVertxTestBase;
import de.braintags.vertx.util.codec.UserObject;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class InputOutputStreamTest extends BtVertxTestBase {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testWriteRead(TestContext context) throws Exception {
    UserObject uo = new UserObject();
    BufferOutputStream bo = new BufferOutputStream();
    Json.mapper.writeValue(bo, uo);
    BufferInputStream bi = new BufferInputStream(bo.getBuffer());
    UserObject bo2 = Json.mapper.readValue(bi, UserObject.class);
    context.assertEquals(uo.count, bo2.count);
    context.assertEquals(uo.testString, bo2.testString);

  }

}
