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
import org.picketlink.idm.jpa.annotations.AttributeClass;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.model.Attribute;

import java.util.ArrayList;
import java.util.List;

import static org.picketlink.idm.IDMInternalMessages.MESSAGES;

/**
 * @author pedroigor
 */
public class AttributeTypeMapper extends AbstractModelMapper {

    @Override
    public boolean supports(Class<?> entityType) {
        return  getAnnotatedProperty(AttributeClass.class, entityType) != null;
    }

    @Override
    protected List<EntityMapping> doCreateMapping(final Class<?> entityType) {
        List<EntityMapping> mappings = new ArrayList<EntityMapping>();

        EntityMapping entityMapping = new EntityMapping(Attribute.class);

        entityMapping.addNotNullMappedProperty(getAnnotatedProperty(AttributeClass.class, entityType));

        Property nameProperty = getAnnotatedProperty(AttributeName.class, entityType);

        if (nameProperty == null) {
            throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, AttributeName.class);
        }

        entityMapping.addProperty(getNamedProperty("name", Attribute.class), nameProperty);

        Property valueProperty = getAnnotatedProperty(AttributeValue.class, entityType);

        if (valueProperty == null) {
            throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, AttributeValue.class);
        }

        entityMapping.addProperty(getNamedProperty("value", Attribute.class), valueProperty);

        entityMapping.addOwnerProperty(entityType);

        mappings.add(entityMapping);

        return mappings;
    }

}