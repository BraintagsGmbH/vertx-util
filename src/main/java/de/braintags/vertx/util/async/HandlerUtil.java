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
package de.braintags.vertx.util.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * Utility class for vertx {@link Handler Handlers}
 * 
 * @author sschmitt
 * 
 */
public class HandlerUtil {

  private HandlerUtil() {
  }

  /**
   * Forwards the result of a handler with an object return type to a void handler, omitting the result and simply
   * returning the success or failure.
   * Useful if you have a handler:
   * <p>
   * <code>Handler&lt;Asyncresult&lt;Void&gt;&gt; voidHandler</code>
   * </p>
   * and call a method:
   * <p>
   * <code>doSomething(Handler&lt;Asyncresult&lt;Object&gt;&gt; objectHandler)</code>
   * </p>
   * and simply want to forward the result of the method to your handler, you can use:
   * <p>
   * <code>doSomething(HandlerUtil.voidCompleter(voidHandler))</code>
   * </p>
   * 
   * @param handler
   *          your void handler
   * @return a handler for any object that forwards the success or failure to your void handler
   */
  public static <T> Handler<AsyncResult<T>> voidCompleter(Handler<AsyncResult<Void>> handler) {
    return result -> {
      if (result.failed())
        handler.handle(Future.failedFuture(result.cause()));
      else
        handler.handle(Future.succeededFuture());
    };
  }

}
