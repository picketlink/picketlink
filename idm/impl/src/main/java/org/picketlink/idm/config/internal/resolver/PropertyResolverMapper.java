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
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.config.idm.ObjectType;


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
    private final PropertyResolver<Object> DEFAULT_PRIMITIVE_RESOLVER = new PrimitivePropertyResolver.PropertyEditorDelegateResolver();

    // Default resolver for all types, which are configured in XML as 'Object'
    private final PropertyResolver<Object> DEFAULT_OBJECT_RESOLVER = new ObjectTypeResolver();

    // Map from types to resolvers. Key is java type. Value is resolver
    private Map<Class<?>, PropertyResolver> propertyResolvers = new ConcurrentHashMap<Class<?>, PropertyResolver>();

    public static PropertyResolverMapper getInstance() {
        return INSTANCE;
    }

    private static PropertyResolverMapper initInstance() {
        PropertyResolverMapper instance = new PropertyResolverMapper();

        // Fill map with basic resolver types here. User can add his own resolvers
        instance.addPropertyResolver(String.class, new PrimitivePropertyResolver.StringResolver());
        instance.addPropertyResolver(Class.class, new PrimitivePropertyResolver.ClassResolver());
        instance.addPropertyResolver(Properties.class, new JavaPropertiesResolver());

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

    public <V> V resolveProperty(Object configurationType, Class<V> clazz) {
        // Find resolver in mapping first
        PropertyResolver<V> propertyResolver = propertyResolvers.get(clazz);

        if (propertyResolver == null) {
            if (configurationType instanceof String) {
                // Default resolver for simple types
                propertyResolver = (PropertyResolver<V>)DEFAULT_PRIMITIVE_RESOLVER;
            } else if (configurationType instanceof ObjectType) {
                // Default resolver for compound (Object) types
                propertyResolver = (PropertyResolver<V>)DEFAULT_OBJECT_RESOLVER;
            }
        }

        // Use resolver to find correct property
        return propertyResolver.resolveProperty(configurationType, clazz);
    }
}
