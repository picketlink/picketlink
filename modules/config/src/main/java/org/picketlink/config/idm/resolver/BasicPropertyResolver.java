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

import org.picketlink.common.reflection.Reflections;
import org.picketlink.common.reflection.Types;
import org.picketlink.config.idm.XMLConfigurationProvider;
import org.picketlink.idm.config.SecurityConfigurationException;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

/**
 * Base class for resolvers, which are able to map simple String to expected java type
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class BasicPropertyResolver<V> implements  PropertyResolver<V> {

    /**
     * {@inheritDoc}
     */
    @Override
    public V resolveProperty(Object propertyValueFromConfiguration, Class<V> propertyClass) {
        if (propertyValueFromConfiguration instanceof String) {
            String stringValue = (String)propertyValueFromConfiguration;

            // Handle null cases
            if ("null".equals(stringValue)) {
                return null;
            }

            return resolvePropertyFromString(stringValue, propertyClass);
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
     * String resolver simply return passed value
     */
    public static class StringResolver extends BasicPropertyResolver<String> {

        @Override
        protected String resolvePropertyFromString(String propertyValue, Class<String> propertyClass) {
            return propertyValue;
        }
    }


    /**
     * Class resolver will try to create class from passed String
     */
    @SuppressWarnings("rawtypes")
    public static class ClassResolver extends BasicPropertyResolver<Class> {

        @Override
        protected Class resolvePropertyFromString(String propertyValue, Class<Class> propertyClass) {
            try {
                // Property value represents className, TODO: classloader's list should be probably configurable
                return Reflections.classForName(propertyValue, XMLConfigurationProvider.IDM_CLASSLOADERS);
            } catch (ClassNotFoundException cnfe) {
                throw new SecurityConfigurationException(cnfe);
            }
        }
    }

    /**
     * Resolver for primitive java types. It delegate the work to JDK {@link PropertyEditor} API
     */
    public static class PropertyEditorDelegateResolver<V> extends BasicPropertyResolver<V> {

        @Override
        protected V resolvePropertyFromString(String propertyValue, Class<V> propertyClass) {
            PropertyEditor propertyEditor = PropertyEditorManager.findEditor(propertyClass);
            propertyEditor.setAsText(propertyValue);
            propertyClass = (Class<V>)Types.boxedClass(propertyClass);
            return propertyClass.cast(propertyEditor.getValue());
        }
    }
}
