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

package org.picketlink.permission.internal;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;

import org.picketlink.internal.util.Strings;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.permission.annotations.PermissionsHandledBy;
import org.picketlink.permission.spi.PermissionHandler;

/**
 * An Identifier strategy for entity-based permission checks
 *
 * @author Shane Bryzak
 */
public class EntityPermissionHandler extends BaseAbstractPermissionHandler implements PermissionHandler 
{
    private Map<Class<?>, String> identifierNames = new ConcurrentHashMap<Class<?>, String>();
    private Map<Class<?>, Property<Serializable>> identifierProperties = 
            new ConcurrentHashMap<Class<?>, Property<Serializable>>();

    @Inject Instance<EntityManager> entityManager;

    public boolean canHandle(Class<?> resourceClass) 
    {
        return resourceClass.isAnnotationPresent(Entity.class);
    }

    public String getGeneratedIdentifier(Object resource) 
    {
        return String.format("%s:%s", getIdentifierName(resource.getClass()),
                getNaturalIdentifier(resource));
    }
    
    public Serializable getNaturalIdentifier(Object resource)
    {
        Class<?> resourceClass = resource.getClass();
        
        if (!identifierProperties.containsKey(resourceClass))
        {
            PropertyQuery<Serializable> pq = PropertyQueries.createQuery(resource.getClass());
            pq.addCriteria(new AnnotatedPropertyCriteria(Id.class));
            identifierProperties.put(resourceClass, pq.getSingleResult());
        }
        
        Property<Serializable> p = identifierProperties.get(resourceClass);
        
        return p.getValue(resource);
    }    

    private String getIdentifierName(Class<?> cls) 
    {
        if (!identifierNames.containsKey(cls)) {
            String name = null;

            if (cls.isAnnotationPresent(PermissionsHandledBy.class)) 
            {
                PermissionsHandledBy identifier = (PermissionsHandledBy) cls.getAnnotation(PermissionsHandledBy.class);
                if (!Strings.isEmpty(identifier.name())) 
                {
                    name = identifier.name();
                }
            }

            if (name == null) 
            {
                name = cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
            }

            identifierNames.put(cls, name);
            return name;
        }

        return identifierNames.get(cls);
    }

    @Override
    public boolean canLoadResource(String identifier) 
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object lookupResource(String identifier) 
    {
        // TODO Auto-generated method stub
        return null;
    }
}
