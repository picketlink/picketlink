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
import java.util.concurrent.ConcurrentHashMap;


/**
 * Mapper for resolvers. It's main purpose is to find correct resolver for type from XML configuration and map it to requested
 * Java type
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PropertyResolverMapper {

    // PropertyResolverMapper is actually singleton
    private static final PropertyResolverMapper INSTANCE = initInstance();

    // Default resolver for all types, which are configured in XML as simple String
    private final PropertyResolver<Object> DEFAULT_PRIMITIVE_RESOLVER = new BasicPropertyResolver.PropertyEditorDelegateResolver<Object>();

    // Map from types to resolvers. Key is java type. Value is resolver
    @SuppressWarnings("rawtypes")
    private Map<Class<?>, PropertyResolver> propertyResolvers = new ConcurrentHashMap<Class<?>, PropertyResolver>();

    public static PropertyResolverMapper getInstance() {
        return INSTANCE;
    }

    private static PropertyResolverMapper initInstance() {
        PropertyResolverMapper instance = new PropertyResolverMapper();

        // Fill map with basic resolver types here. User can add his own resolvers
        instance.addPropertyResolver(String.class, new BasicPropertyResolver.StringResolver());
        instance.addPropertyResolver(Class.class, new BasicPropertyResolver.ClassResolver());

        return instance;
    }

    private PropertyResolverMapper() {};

    /**
     * Adding resolvers for custom java types
     *
     * @param clazz expected class of type, which will be used as return type of resolver
     * @param propertyResolver resolver to be added
     * @param <V>
     */
    public <V> void addPropertyResolver(Class<V> clazz, PropertyResolver<V> propertyResolver) {
        propertyResolvers.put(clazz, propertyResolver);
    }

    @SuppressWarnings("unchecked")
    public <V> V resolveProperty(Object configurationValue, Class<V> clazz) {
        // Find resolver in mapping first
        PropertyResolver<V> propertyResolver = propertyResolvers.get(clazz);

        if (propertyResolver == null) {
            // Default resolver for simple types
            propertyResolver = (PropertyResolver<V>)DEFAULT_PRIMITIVE_RESOLVER;
        }

        // Use resolver to find correct property
        return propertyResolver.resolveProperty(configurationValue, clazz);
    }
}
