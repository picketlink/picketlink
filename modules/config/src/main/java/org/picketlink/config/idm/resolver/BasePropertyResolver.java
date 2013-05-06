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

import org.picketlink.config.idm.ObjectType;
import org.picketlink.idm.SecurityConfigurationException;

/**
 * Base implementation of property resolver
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class BasePropertyResolver<V> implements PropertyResolver<V> {

    /**
     * {@inheritDoc}
     */
    @Override
    public V resolveProperty(Object propertyValueFromConfiguration, Class<V> propertyClass) {
        if (propertyValueFromConfiguration instanceof String) {
            String stringValue = (String)propertyValueFromConfiguration;
            return resolvePropertyFromString(stringValue, propertyClass);
        } else if (propertyValueFromConfiguration instanceof ObjectType) {
            ObjectType objectType = (ObjectType)propertyValueFromConfiguration;
            return resolvePropertyFromObjectType(objectType);
        } else {
            throw new SecurityConfigurationException("Unknown type of propertyValue: " + propertyValueFromConfiguration);
        }
    }

    /**
     * Should be overriden for resolvers, which are able to map simple String to expected java type
     *
     * @param stringPropertyValue property value from XML configuration
     * @param propertyClass type of property to return
     * @return value of configuration type resolved from stringPropertyValue parameter
     */
    protected abstract V resolvePropertyFromString(String stringPropertyValue, Class<V> propertyClass);

    /**
     * Should be overriden for resolvers, which are able to map compound {@link ObjectType} to expected java type
     *
     * @param objectType property value from XML configuration
     * @return real value of configuration type resolved from objectType parameter
     */
    protected abstract V resolvePropertyFromObjectType(ObjectType objectType);
}
