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

import java.util.Map;

import org.picketlink.identity.federation.core.config.idm.ObjectType;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.internal.XMLBasedIdentityManagerProvider;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;

/**
 * Base resolver for passed {@link ObjectType} values. This resolver will try to create it's object based on java beans of given type
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
class ObjectTypeResolver<V> extends BasePropertyResolver<V> {

    @Override
    protected V resolvePropertyFromObjectType(ObjectType objectType) {
        // TODO: this code is almost the same like XMLBasedIdentityManagerProvider.buildStoreConfiguration
        String className = objectType.getClassName();
        if (className == null) {
            throw new SecurityConfigurationException("ClassName must be always configured for all 'Object' types from configuration");
        }

        V object = (V) XMLBasedIdentityManagerProvider.instantiateComponent(className);
        Class<?> objectClass = object.getClass();

        Map<String, Object> props = objectType.getAllProperties();
        for (String propertyName : props.keySet()) {
            // It's not optimal as we need to query and iterate methods separately for each property.
            // But performance shouldn't be big deal during reading configuration as it usually needs to be read only once...
            PropertyQuery<Object> propertyQuery = PropertyQueries.createQuery(objectClass);
            propertyQuery.addCriteria(new NamedPropertyCriteria(propertyName));
            Property<Object> property = propertyQuery.getWritableSingleResult();

            // Obtain value from XML configuration
            Object propertyValueFromConfig = props.get(propertyName);

            // Create real instance of property from XML configuration
            Object propertyValue = PropertyResolverMapper.getInstance().resolveProperty(propertyValueFromConfig, property.getJavaClass());

            // Set property to current storeConfiguration
            property.setValue(object, propertyValue);
        }
        return object;
    }

    @Override
    protected V resolvePropertyFromString(String stringPropertyValue, Class<V> propertyClass) {
        throw new SecurityConfigurationException("Not implemented");
    }
}
