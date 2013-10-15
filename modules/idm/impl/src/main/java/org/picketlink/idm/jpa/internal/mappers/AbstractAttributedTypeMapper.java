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
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.model.AttributedType;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author pedroigor
 */
public abstract class AbstractAttributedTypeMapper extends AbstractIdentityManagedMapper {

    @Override
    public EntityMapping configure(Class<?> managedType, Class<?> entityType) {
        EntityMapping entityMapping = new EntityMapping(managedType);

        Property idProperty = getAnnotatedProperty(Identifier.class, entityType);

        if (idProperty == null) {
            throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, Identifier.class);
        }

        entityMapping.addProperty(getNamedProperty("id", AttributedType.class), idProperty);

        for (Property mappedProperty : getAnnotatedProperties(AttributeValue.class, entityType)) {
            AttributeValue attributeValue = mappedProperty.getAnnotatedElement().getAnnotation(AttributeValue.class);
            String propertyName = mappedProperty.getName();

            if (!isNullOrEmpty(attributeValue.name())) {
                propertyName = attributeValue.name();
            }

            Property property = getNamedProperty(propertyName, managedType);

            if (property != null) {
                entityMapping.addProperty(property, mappedProperty);
            }
        }

        return entityMapping;
    }

}
