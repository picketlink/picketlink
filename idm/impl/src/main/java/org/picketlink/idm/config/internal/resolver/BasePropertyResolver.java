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

import org.picketlink.identity.federation.core.config.idm.ObjectType;
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
