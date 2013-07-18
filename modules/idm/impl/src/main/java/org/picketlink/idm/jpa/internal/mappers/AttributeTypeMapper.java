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

import org.picketlink.idm.jpa.annotations.AttributeClass;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;
import org.picketlink.idm.jpa.annotations.entity.MappedAttribute;
import org.picketlink.idm.model.Attribute;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * @author pedroigor
 */
public class AttributeTypeMapper extends AbstractIdentityManagedMapper {

    @Override
    public boolean supports(Class<?> entityType) {
        MappedAttribute mappedAttribute = entityType.getAnnotation(MappedAttribute.class);

        return  !entityType.isAnnotationPresent(ManagedCredential.class)
                && mappedAttribute != null && isNullOrEmpty(mappedAttribute.value())
                && getAnnotatedProperty(AttributeName.class, entityType) != null
                && getAnnotatedProperty(AttributeValue.class, entityType) != null;
    }

    @Override
    public EntityMapping configure(Class<?> managedType, Class<?> entityType) {
        EntityMapping entityMapping = new EntityMapping(getSupportedAttributeType(managedType), true);

        entityMapping.addProperty(getNamedProperty("name", Attribute.class), getAnnotatedProperty(AttributeName.class, entityType));
        entityMapping.addProperty(getNamedProperty("value", Attribute.class), getAnnotatedProperty(AttributeValue.class, entityType));
        entityMapping.addProperty(getSupportedAttributeType(managedType).getClass().getName(), getAnnotatedProperty(AttributeClass.class, entityType));

        entityMapping.addOwnerProperty(entityType);

        return entityMapping;
    }

    protected Class<?> getSupportedAttributeType(Class<?> managedType) {
        return Attribute.class;
    }

}