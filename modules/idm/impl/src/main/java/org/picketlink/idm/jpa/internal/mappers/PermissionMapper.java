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

import org.picketlink.common.properties.Property;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.jpa.annotations.PermissionOperation;
import org.picketlink.idm.jpa.annotations.PermissionResourceClass;
import org.picketlink.idm.jpa.annotations.PermissionResourceIdentifier;
import org.picketlink.idm.jpa.annotations.entity.PermissionManaged;
import org.picketlink.idm.permission.Permission;

import java.util.ArrayList;
import java.util.List;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Property mapping configuration for entity beans that store permission state
 *
 * @author Shane Bryzak
 */
public class PermissionMapper extends AbstractModelMapper {

    @Override
    protected List<EntityMapping> doCreateMapping(Class<?> entityType) throws SecurityConfigurationException {
        ArrayList<EntityMapping> mappings = new ArrayList<EntityMapping>();
        PermissionManaged identityManaged = entityType.getAnnotation(PermissionManaged.class);
        Class<?>[] resourceClasses = identityManaged.resourceClasses();

        if (resourceClasses.length > 0) {
            for (Class<?> resourceClass : resourceClasses) {
                mappings.add(createEntityMapping(entityType, resourceClass));
            }
        } else {
            mappings.add(createEntityMapping(entityType, Object.class));
        }

        return mappings;
    }

    private EntityMapping createEntityMapping(Class<?> entityType, Class<?> resourceClass) {
        EntityMapping mapping = new EntityMapping(resourceClass);

        mapping.addOwnerProperty(entityType);

        Property resourceClassProperty = getAnnotatedProperty(PermissionResourceClass.class, entityType);

        if (resourceClassProperty == null) {
            throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, PermissionResourceClass.class);
        }

        mapping.addNotNullMappedProperty(resourceClassProperty);

        Property resourceIdentifierProperty = getAnnotatedProperty(PermissionResourceIdentifier.class, entityType);

        if (resourceIdentifierProperty == null) {
            throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, PermissionResourceIdentifier.class);
        }

        mapping.addProperty(getNamedProperty("resourceIdentifier", Permission.class), resourceIdentifierProperty);

        Property operationProperty = getAnnotatedProperty(PermissionOperation.class, entityType);

        if (operationProperty == null) {
            throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, PermissionOperation.class);
        }

        mapping.addProperty(getNamedProperty("operation", Permission.class), operationProperty);

        return mapping;
    }

    @Override
    public boolean supports(Class<?> entityType) {
        return entityType.isAnnotationPresent(PermissionManaged.class);
    }
}
