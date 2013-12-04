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
package org.picketlink.idm.jpa.internal.mappers;

import java.util.HashSet;
import java.util.Set;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.jpa.annotations.PermissionAssignee;
import org.picketlink.idm.jpa.annotations.PermissionOperation;
import org.picketlink.idm.jpa.annotations.PermissionResourceClass;
import org.picketlink.idm.jpa.annotations.PermissionResourceIdentifier;
import org.picketlink.idm.jpa.annotations.entity.PermissionManaged;

/**
 * Property mapping configuration for entity beans that store permission state
 *
 * @author Shane Bryzak
 */
public class PermissionEntityMapper {
    private Set<Class<?>> resourceClasses = new HashSet<Class<?>>();

    private Class<?> entityClass;

    private Property<?> assignee;
    private Property<String> resourceClass;
    private Property<String> resourceIdentifier;
    private Property<?> operation;

    public PermissionEntityMapper(Class<?> entityClass) {
        this.entityClass = entityClass;

        assignee = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(PermissionAssignee.class))
                .getSingleResult();

        resourceClass = PropertyQueries.<String>createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(PermissionResourceClass.class))
                .getSingleResult();

        resourceIdentifier = PropertyQueries.<String>createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(PermissionResourceIdentifier.class))
                .getSingleResult();

        operation = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(PermissionOperation.class))
                .getSingleResult();

        // Add the configured resource classes
        PermissionManaged annotation = entityClass.getAnnotation(PermissionManaged.class);
        if (annotation.resourceClasses().length == 0) {
            resourceClasses.add(Object.class);
        } else {
            for (Class<?> resourceClass : annotation.resourceClasses()) {
                resourceClasses.add(resourceClass);
            }
        }
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Set<Class<?>> getResourceClasses() {
        return resourceClasses;
    }

    public Property<?> getAssignee() {
        return assignee;
    }

    public Property<String> getResourceClass() {
        return resourceClass;
    }

    public Property<String> getResourceIdentifier() {
        return resourceIdentifier;
    }

    public Property<?> getOperation() {
        return operation;
    }
}
