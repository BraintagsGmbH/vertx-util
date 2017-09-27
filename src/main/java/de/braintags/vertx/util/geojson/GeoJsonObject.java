/*
 * #%L
 * vertx-pojo-mapper-common
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.util.geojson;

/**
 * An abstract class for representations of GeoJSON geographic objects.
 * 
 * @author Michael Remme
 * 
 */
public abstract class GeoJsonObject {

  /**
   * Get the {@link GeoJsonType} which is covered by the implementation
   * 
   * @return
   */
  public abstract GeoJsonType getType();

  @Override
  public boolean equals(final Object o) {
    return o != null && getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}
