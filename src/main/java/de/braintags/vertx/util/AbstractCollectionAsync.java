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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * The abstract implementation defines an unmodifyable list, cause all methods, which are changing the content are
 * throwing an {@link UnsupportedOperationException}
 *
 * @author Michael Remme
 * @param <E>
 *          the underlaying class to be used
 */
public abstract class AbstractCollectionAsync<E> implements CollectionAsync<E> {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(AbstractCollectionAsync.class);

  /**
   *
   */
  public AbstractCollectionAsync() {
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public void contains(Object o, Handler<AsyncResult<Boolean>> handler) {
    toArray(result -> {
      if (result.failed()) {
        handler.handle(Future.failedFuture(result.cause()));
      } else {
        handler.handle(Future.succeededFuture(contains(result.result(), o)));
      }
    });
  }

  /*
   * (non-Javadoc)
   *
   * @see de.braintags.vertx.util.util.CollectionAsync#containsAll(de.braintags.vertx.util.util.CollectionAsync,
   * io.vertx.core.Handler)
   */
  @Override
  public void containsAll(CollectionAsync<?> c, Handler<AsyncResult<Boolean>> handler) {
    if (c.isEmpty() || isEmpty()) {
      handler.handle(Future.succeededFuture(false));
      return;
    }

    toArray(sourceResult -> {
      if (sourceResult.failed()) {
        handler.handle(Future.failedFuture(sourceResult.cause()));
      } else {
        c.toArray(checkResult -> {
          if (checkResult.failed()) {
            handler.handle(Future.failedFuture(checkResult.cause()));
          } else {
            Object[] sourceArray = sourceResult.result();
            Object[] checkArray = checkResult.result();
            handler.handle(Future.succeededFuture(containsAll(sourceArray, checkArray)));
          }
        });
      }
    });
  }

  private boolean containsAll(Object[] objects, Object[] searchObjects) {
    for (Object searchObject : searchObjects) {
      if (!contains(objects, searchObject)) {
        return false;
      }
    }
    return true;
  }

  private boolean contains(Object[] objects, Object searchObject) {
    for (Object sourceObject : objects) {
      if (sourceObject.equals(searchObject)) {
        return true;
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.braintags.vertx.util.util.CollectionAsync#toArray(io.vertx.core.Handler)
   */
  @Override
  public void toArray(Handler<AsyncResult<Object[]>> handler) {
    Object[] or = new Object[size()];

    if (isEmpty()) {
      handler.handle(Future.succeededFuture(or));
    } else {
      List<Future> fl = createFutureList(or);
      CompositeFuture.all(fl).setHandler(result -> {
        if (result.failed()) {
          LOGGER.error("", result.cause());
          handler.handle(Future.failedFuture(result.cause()));
        } else {
          LOGGER.trace("futurelist finished");
          handler.handle(Future.succeededFuture(or));
        }
      });
    }
  }

  @SuppressWarnings("rawtypes")
  private List<Future> createFutureList(Object[] or) {
    List<Future> futures = new ArrayList<>();
    IteratorAsync<E> it = iterator();
    int counter = 0;
    while (it.hasNext()) {
      Future future = Future.future();
      futures.add(future);
      AtomicInteger ai = new AtomicInteger(counter++);
      LOGGER.trace("preparing future for position " + counter);
      it.next(nr -> {
        if (nr.failed()) {
          future.fail(nr.cause());
        } else {
          LOGGER.trace("executed position " + ai.get());
          or[ai.get()] = nr.result();
          future.complete();
        }
      });
    }

    LOGGER.trace("created futurelist with " + futures.size() + " entries ");
    return futures;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.braintags.vertx.util.util.CollectionAsync#add(java.lang.Object)
   */
  @Override
  public boolean add(E e) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.braintags.vertx.util.util.CollectionAsync#remove(java.lang.Object)
   */
  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.braintags.vertx.util.util.CollectionAsync#addAll(de.braintags.vertx.util.util.CollectionAsync)
   */
  @Override
  public boolean addAll(CollectionAsync<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.braintags.vertx.util.util.CollectionAsync#removeAll(java.util.Collection)
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.braintags.vertx.util.util.CollectionAsync#clear()
   */
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

}
