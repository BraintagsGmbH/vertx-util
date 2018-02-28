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
package de.braintags.vertx;

import java.io.Closeable;
import java.util.List;
import java.util.function.Consumer;

import de.braintags.vertx.util.ResultObject;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * A helper class for testing HTTP requests.
 *
 * Copyright: Copyright (c) 02.02.2017 <br>
 * Company: Braintags GmbH <br>
 *
 * @author mpluecker
 *
 */
public class HttpTestClient implements Closeable {

  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(HttpTestClient.class);

  public HttpClient client;
  private final String hostName;
  private final int port;

  public HttpTestClient(final Vertx vertx, final String hostName, final int port) {
    this.hostName = hostName;
    this.port = port;
    client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(port));
  }

  @Override
  public void close() {
    client.close();
    client = null;
  }

  public final void testRequest(final TestContext context, final HttpMethod method, final String path,
      final int expectedStatusCode, final String expectedStatusMessage) throws Exception {
    testRequest(context, method, path, null, expectedStatusCode, expectedStatusMessage, null);
  }

  public final void testRequest(final TestContext context, final HttpMethod method, final String path,
      final int expectedStatusCode, final String expectedStatusMessage, final String responseBody) throws Exception {
    testRequest(context, method, path, null, expectedStatusCode, expectedStatusMessage, responseBody);
  }

  public final void testRequest(final TestContext context, final HttpMethod method, final String path,
      final int expectedStatusCode, final String expectedStatusMessage, final Buffer responseBody) throws Exception {
    testRequestBuffer(context, method, path, null, null, expectedStatusCode, expectedStatusMessage, responseBody);
  }

  public final void testRequestWithContentType(final TestContext context, final HttpMethod method, final String path,
      final String contentType, final int expectedStatusCode, final String expectedStatusMessage) throws Exception {
    testRequest(context, method, path, req -> req.putHeader("content-type", contentType), expectedStatusCode,
        expectedStatusMessage, null);
  }

  public final void testRequestWithAccepts(final TestContext context, final HttpMethod method, final String path,
      final String accepts, final int expectedStatusCode, final String expectedStatusMessage) throws Exception {
    testRequest(context, method, path, req -> req.putHeader("accept", accepts), expectedStatusCode,
        expectedStatusMessage, null);
  }

  public final void testRequestWithCookies(final TestContext context, final HttpMethod method, final String path,
      final String cookieHeader, final int expectedStatusCode, final String expectedStatusMessage) throws Exception {
    testRequest(context, method, path, req -> req.putHeader("cookie", cookieHeader), expectedStatusCode,
        expectedStatusMessage, null);
  }

  public final void testRequest(final TestContext context, final HttpMethod method, final String path,
      final Consumer<HttpClientRequest> requestAction, final int expectedStatusCode, final String expectedStatusMessage,
      final String responseBody) throws Exception {
    testRequest(context, method, path, requestAction, null, expectedStatusCode, expectedStatusMessage, responseBody);
  }

  public final void testRequest(final TestContext context, final HttpMethod method, final String path,
      final Consumer<HttpClientRequest> requestAction, final Consumer<ResponseCopy> responseAction,
      final int expectedStatusCode, final String expectedStatusMessage, final String responseBody) throws Exception {
    testRequestBuffer(context, method, path, requestAction, responseAction, expectedStatusCode, expectedStatusMessage,
        responseBody != null ? Buffer.buffer(responseBody) : null);
  }

  public final void testRequestBuffer(final TestContext context, final HttpMethod method, final String path,
      final Consumer<HttpClientRequest> requestAction, final Consumer<ResponseCopy> responseAction,
      final int expectedStatusCode, final String expectedStatusMessage, final Buffer responseBodyBuffer)
      throws Exception {
    testRequestBuffer(context, client, method, port, path, requestAction, responseAction, expectedStatusCode,
        expectedStatusMessage, responseBodyBuffer);
  }

  public final void testRequestBuffer(final TestContext context, final HttpClient client, final HttpMethod method,
      final int port, final String path, final Consumer<HttpClientRequest> requestAction,
      final Consumer<ResponseCopy> responseAction, final int expectedStatusCode, final String expectedStatusMessage,
      final Buffer responseBodyBuffer) throws Exception {
    LOGGER.info("calling URL " + path);
    Async async = context.async();
    ResultObject<ResponseCopy> resultObject = new ResultObject<>(null);

    Handler<Throwable> exceptionHandler = ex -> {
      LOGGER.error("", ex);
      async.complete();
    };

    HttpClientRequest req = client.request(method, port, hostName, path, resp -> {
      resp.exceptionHandler(exceptionHandler);

      ResponseCopy rc = new ResponseCopy();
      resp.bodyHandler(buff -> {
        LOGGER.debug("Executing body handler");
        rc.content = buff.toString();
        rc.code = resp.statusCode();
        rc.statusMessage = resp.statusMessage();
        rc.headers = MultiMap.caseInsensitiveMultiMap();
        rc.headers.addAll(resp.headers());
        rc.cookies = resp.cookies();
        resultObject.setResult(rc);
        async.complete();
      });
    });
    req.exceptionHandler(exceptionHandler);
    if (requestAction != null) {
      requestAction.accept(req);
    }
    req.end();
    async.await();

    LOGGER.debug("request executed");
    ResponseCopy rc = resultObject.getResult();
    if (responseAction != null) {
      responseAction.accept(rc);
    }
    context.assertNotNull(rc, "Responsecopy is null");
    context.assertEquals(expectedStatusCode, rc.code);
    if (expectedStatusMessage != null)
      context.assertEquals(expectedStatusMessage, rc.statusMessage);
    if (responseBodyBuffer == null) {
      // async.complete();
    } else {
      context.assertEquals(responseBodyBuffer.toString(), rc.content);
    }
  }

  public class ResponseCopy {
    public String content;
    public int code;
    public String statusMessage;
    public MultiMap headers;
    public List<String> cookies;
  }

}
