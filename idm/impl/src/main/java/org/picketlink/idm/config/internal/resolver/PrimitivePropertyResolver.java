/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.config.internal.resolver;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.picketlink.identity.federation.core.config.idm.ObjectType;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.internal.XMLBasedIdentityManagerProvider;
import org.picketlink.idm.internal.util.reflection.Reflections;

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
