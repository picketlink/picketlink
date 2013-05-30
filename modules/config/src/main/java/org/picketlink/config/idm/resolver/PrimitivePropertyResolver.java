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
import org.picketlink.config.idm.ObjectType;
import org.picketlink.config.idm.XMLBasedIdentityManagerProvider;
import org.picketlink.idm.config.SecurityConfigurationException;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

/**
 * Base class for resolvers, which are able to map simple String to expected java type
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class PrimitivePropertyResolver<V> extends BasePropertyResolver<V> {


    @Override
    protected V resolvePropertyFromObjectType(ObjectType objectType) {
        throw new SecurityConfigurationException("Not implemented");
    }

    /**
     * String resolver simply return passed value
     */
    public static class StringResolver extends PrimitivePropertyResolver<String> {

        @Override
        protected String resolvePropertyFromString(String propertyValue, Class<String> propertyClass) {
            return propertyValue;
        }
    }


    /**
     * Class resolver will try to create class from passed String
     */
    @SuppressWarnings("rawtypes")
    public static class ClassResolver extends PrimitivePropertyResolver<Class> {
        @Override
        protected Class resolvePropertyFromString(String propertyValue, Class<Class> propertyClass) {
            try {
                // Property value represents className, TODO: classloader's list should be probably configurable
                return Reflections.classForName(propertyValue, XMLBasedIdentityManagerProvider.IDM_CLASSLOADER);
            } catch (ClassNotFoundException cnfe) {
                throw new SecurityConfigurationException(cnfe);
            }
        }
    }

    /**
     * Resolver for primitive java types. It delegate the work to JDK {@link PropertyEditor} API
     */
    public static class PropertyEditorDelegateResolver extends PrimitivePropertyResolver<Object> {

        @Override
        protected Object resolvePropertyFromString(String propertyValue, Class<Object> propertyClass) {
            PropertyEditor propertyEditor = PropertyEditorManager.findEditor(propertyClass);
            propertyEditor.setAsText(propertyValue);
            return propertyClass.cast(propertyEditor.getValue());
        }
    }
}
