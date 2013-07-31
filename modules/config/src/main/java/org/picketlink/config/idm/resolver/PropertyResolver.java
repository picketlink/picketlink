/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.config.idm.resolver;

/**
 * Interface for resolve property value from configuration (usually XML configuration) to real property value, which will be used
 * in IDM configuration.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface PropertyResolver<V> {

    /**
     * Resolve property value from configuration (usually XML configuration) to real property value, which will be used
     * in IDM configuration
     *
     * @param configurationValue object from XML configuration. Type of this object is usually {@link String} (for simple text value of XML property)
     * @param propertyClass type of property to return
     * @return real value of property, which will be used in IDM configuration
     */
    V resolveProperty(Object configurationValue, Class<V> propertyClass);

}
