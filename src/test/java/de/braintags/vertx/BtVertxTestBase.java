package de.braintags.vertx;
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

import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * BAsic Class which is activating an instance of Vertx
 * 
 * @author Michael Remme
 * 
 */
@RunWith(VertxUnitRunner.class)
public class BtVertxTestBase {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(BtVertxTestBase.class);

  protected static Vertx vertx;

  @Rule
  public Timeout rule = Timeout.seconds(Integer.parseInt(System.getProperty("testTimeout", "20")));

  @Rule
  public TestName name = new TestName();

  @Before
  public final void initBeforeTest(TestContext context) {
    LOGGER.info("Starting test: " + this.getClass().getSimpleName() + "#" + name.getMethodName());
    initTest(context);
  }

  public void initTest(TestContext context) {

  }

  @After
  public final void afterTest(TestContext context) {
    LOGGER.info("Stopping test: " + this.getClass().getSimpleName() + "#" + name.getMethodName());
    stopTest(context);
  }

  protected void stopTest(TestContext context) {

  }

  @BeforeClass
  public static void startup(TestContext context) throws Exception {
    LOGGER.debug("starting class");
    vertx = Vertx.vertx(getVertxOptions());
  }

  @AfterClass
  public static void shutdown(TestContext context) throws Exception {
    LOGGER.debug("performing shutdown");

    if (vertx != null) {
      Async async = context.async();
      LOGGER.debug("going to close vertx");
      Set<String> deploymentIds = vertx.deploymentIDs();
      if (!deploymentIds.isEmpty()) {
        deploymentIds.forEach(di -> {
          LOGGER.info("undeploying " + di);
          vertx.undeploy(di);
        });
        async.complete();
      } else {
        vertx.close(ar -> {
          vertx = null;
          LOGGER.debug("close called");
          async.complete();
        });
      }
      async.awaitSuccess();
    }
  }

  protected void undeployVerticle(TestContext context, AbstractVerticle verticle) {
    LOGGER.debug("undeploying verticle " + verticle.deploymentID());
    Async async = context.async();
    vertx.undeploy(verticle.deploymentID(), result -> {
      if (result.failed()) {
        LOGGER.error(result.cause());
        context.fail(result.cause());
        async.complete();
      } else {
        LOGGER.debug("succeeded undeploying verticle " + verticle.deploymentID());
        async.complete();
      }
    });
    async.awaitSuccess();
    LOGGER.debug("finished undeploying verticle " + verticle.deploymentID());
  }

  /**
   * Creates the VertxOptions by checking System variables BlockedThreadCheckInterval and WarningExceptionTime
   */
  public static VertxOptions getVertxOptions() {
    VertxOptions options = new VertxOptions();
    String blockedThreadCheckInterval = System.getProperty("BlockedThreadCheckInterval");
    if (blockedThreadCheckInterval != null) {
      LOGGER.info("setting setBlockedThreadCheckInterval to " + blockedThreadCheckInterval);
      options.setBlockedThreadCheckInterval(Long.parseLong(blockedThreadCheckInterval));
    }
    String warningExceptionTime = System.getProperty("WarningExceptionTime");
    if (warningExceptionTime != null) {
      LOGGER.info("setting setWarningExceptionTime to " + warningExceptionTime);
      options.setWarningExceptionTime(Long.parseLong(warningExceptionTime));
    }
    return options;
  }

  /**
   * Examines the given Throwable and creates an AssertionError from it or rethrows, if already one
   * 
   * @param e
   *          the Throwable to examine
   */
  public void createAssertionError(Throwable e) {
    if (e instanceof AssertionError) {
      throw (AssertionError) e;
    } else {
      throw new AssertionError(e);
    }
  }
}
