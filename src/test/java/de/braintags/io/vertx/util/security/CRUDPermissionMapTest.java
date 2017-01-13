/*-
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
package de.braintags.io.vertx.util.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class CRUDPermissionMapTest {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(CRUDPermissionMapTest.class);

  @Test
  public void testToString() {
    CRUDPermissionMap pMap = new CRUDPermissionMap();
    pMap.addPermission(CRUDPermissionMap.DEFAULT_PERMISSION_KEY_NAME, 'C');
    pMap.addPermissions("admin", "CRUD");
    LOGGER.info(pMap.toString());
    assertTrue(pMap.hasPermission("admin", 'C'));
    assertTrue(pMap.hasPermission("user", 'C'));
    assertFalse(pMap.hasPermission("user", 'R'));
    String export = pMap.toString();
    CRUDPermissionMap pMap2 = new CRUDPermissionMap(export);
    assertTrue(pMap2.hasPermission("admin", 'C'));
    assertTrue(pMap.hasPermission("user", 'C'));
    assertFalse(pMap2.hasPermission("user", 'R'));

    String export2 = pMap2.toString();
    LOGGER.info("Export: " + export);
    LOGGER.info("copy:  " + export2);
    assertEquals(export, export2);
  }

  @Test
  public void testAddPermissions() {
    CRUDPermissionMap pMap = new CRUDPermissionMap();
    pMap.addPermissions(CRUDPermissionMap.DEFAULT_PERMISSION_KEY_NAME, "C");
    pMap.addPermissions("admin", "CRUD");
    LOGGER.info(pMap.toString());
    assertTrue(pMap.hasPermission("admin", 'C'));
    assertFalse(pMap.hasPermission("user", 'R'));
  }

  @Test
  public void test2() {
    CRUDPermissionMap pMap = new CRUDPermissionMap();
    pMap.addPermission(CRUDPermissionMap.DEFAULT_PERMISSION_KEY_NAME, 'C');
    pMap.addPermissions("admin", "CRUD");
    LOGGER.info(pMap.toString());
    assertTrue(pMap.hasPermission("admin", 'C'));
    assertFalse(pMap.hasPermission("user", 'R'));
  }

  @Test
  public void test1() {
    CRUDPermissionMap pMap = new CRUDPermissionMap();
    pMap.addPermission(CRUDPermissionMap.DEFAULT_PERMISSION_KEY_NAME, 'C');
    LOGGER.info(pMap.toString());
    assertTrue(pMap.hasPermission("admin", 'C'));
    assertFalse(pMap.hasPermission("admin", 'R'));
  }

  /**
   * Test method for
   * {@link de.braintags.io.vertx.util.security.PermissionMap#add(de.braintags.io.vertx.util.security.Permission)}.
   */
  @Test
  public void testEmpty() {
    CRUDPermissionMap pMap = new CRUDPermissionMap();
    LOGGER.info(pMap.toString());
    assertFalse("default permission must be false", pMap.hasPermission("admin", 'C'));
  }

  @Test
  public void testUnsupported() {
    CRUDPermissionMap pMap = new CRUDPermissionMap();
    LOGGER.info(pMap.toString());
    try {
      assertFalse("default permission must be false", pMap.hasPermission("admin", 'X'));
      fail("Expected UnsupportedOperationException here");
    } catch (UnsupportedOperationException e) {
      // expected result
    }
  }

}
