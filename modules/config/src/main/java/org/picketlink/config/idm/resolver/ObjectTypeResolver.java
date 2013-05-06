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

import java.util.Map;
 
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.config.idm.ObjectType;
import org.picketlink.config.idm.XMLBasedIdentityManagerProvider;

/**
 * Base resolver for passed {@link ObjectType} values. This resolver will try to create it's object based on java beans of given type
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
class ObjectTypeResolver<V> extends BasePropertyResolver<V> {

    @SuppressWarnings("unchecked")
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
