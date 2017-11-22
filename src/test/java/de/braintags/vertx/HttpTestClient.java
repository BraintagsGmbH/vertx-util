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
  private String hostName;
  private int port;

  public HttpTestClient(Vertx vertx, String hostName, int port) {
    this.hostName = hostName;
    this.port = port;
    client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(port));
  }

  public void close() {
    client.close();
    client = null;
  }

  public final void testRequest(TestContext context, HttpMethod method, String path, int expectedStatusCode,
      String expectedStatusMessage) throws Exception {
    testRequest(context, method, path, null, expectedStatusCode, expectedStatusMessage, null);
  }

  public final void testRequest(TestContext context, HttpMethod method, String path, int expectedStatusCode,
      String expectedStatusMessage, String responseBody) throws Exception {
    testRequest(context, method, path, null, expectedStatusCode, expectedStatusMessage, responseBody);
  }

  public final void testRequest(TestContext context, HttpMethod method, String path, int expectedStatusCode,
      String expectedStatusMessage, Buffer responseBody) throws Exception {
    testRequestBuffer(context, method, path, null, null, expectedStatusCode, expectedStatusMessage, responseBody);
  }

  public final void testRequestWithContentType(TestContext context, HttpMethod method, String path, String contentType,
      int expectedStatusCode, String expectedStatusMessage) throws Exception {
    testRequest(context, method, path, req -> req.putHeader("content-type", contentType), expectedStatusCode,
        expectedStatusMessage, null);
  }

  public final void testRequestWithAccepts(TestContext context, HttpMethod method, String path, String accepts,
      int expectedStatusCode, String expectedStatusMessage) throws Exception {
    testRequest(context, method, path, req -> req.putHeader("accept", accepts), expectedStatusCode,
        expectedStatusMessage, null);
  }

  public final void testRequestWithCookies(TestContext context, HttpMethod method, String path, String cookieHeader,
      int expectedStatusCode, String expectedStatusMessage) throws Exception {
    testRequest(context, method, path, req -> req.putHeader("cookie", cookieHeader), expectedStatusCode,
        expectedStatusMessage, null);
  }

  public final void testRequest(TestContext context, HttpMethod method, String path,
      Consumer<HttpClientRequest> requestAction, int expectedStatusCode, String expectedStatusMessage,
      String responseBody) throws Exception {
    testRequest(context, method, path, requestAction, null, expectedStatusCode, expectedStatusMessage, responseBody);
  }

  public final void testRequest(TestContext context, HttpMethod method, String path,
      Consumer<HttpClientRequest> requestAction, Consumer<ResponseCopy> responseAction, int expectedStatusCode,
      String expectedStatusMessage, String responseBody) throws Exception {
    testRequestBuffer(context, method, path, requestAction, responseAction, expectedStatusCode, expectedStatusMessage,
        responseBody != null ? Buffer.buffer(responseBody) : null);
  }

  public final void testRequestBuffer(TestContext context, HttpMethod method, String path,
      Consumer<HttpClientRequest> requestAction, Consumer<ResponseCopy> responseAction, int expectedStatusCode,
      String expectedStatusMessage, Buffer responseBodyBuffer) throws Exception {
    testRequestBuffer(context, client, method, port, path, requestAction, responseAction, expectedStatusCode,
        expectedStatusMessage, responseBodyBuffer);
  }

  public final void testRequestBuffer(TestContext context, HttpClient client, HttpMethod method, int port, String path,
      Consumer<HttpClientRequest> requestAction, Consumer<ResponseCopy> responseAction, int expectedStatusCode,
      String expectedStatusMessage, Buffer responseBodyBuffer) throws Exception {
    LOGGER.info("calling URL " + path);
    Async async = context.async();
    ResultObject<ResponseCopy> resultObject = new ResultObject<>(null);

    Handler<Throwable> exceptionHandler = new Handler<Throwable>() {

      @Override
      public void handle(Throwable ex) {
        LOGGER.error("", ex);
        async.complete();
      }
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
