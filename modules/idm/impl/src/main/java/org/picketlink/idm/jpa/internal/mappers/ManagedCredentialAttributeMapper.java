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

import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;

/**
 * @author pedroigor
 */
public class ManagedCredentialAttributeMapper extends AttributeTypeMapper {

    @Override
    public boolean supports(Class<?> entityType) {

        return  entityType.isAnnotationPresent(ManagedCredential.class)
                && getAnnotatedProperty(AttributeName.class, entityType) != null
                && getAnnotatedProperty(AttributeValue.class, entityType) != null;
    }

    @Override
    public EntityMapping configure(Class<?> managedType, Class<?> entityType) {
        EntityMapping entityMapping = super.configure(managedType, entityType);

        entityMapping.addProperty(getNamedProperty("effectiveDate", getSupportedAttributeType(managedType)), getAnnotatedProperty(EffectiveDate.class, entityType));
        entityMapping.addProperty(getNamedProperty("expiryDate", getSupportedAttributeType(managedType)), getAnnotatedProperty(ExpiryDate.class, entityType));

        return entityMapping;
    }

    @Override
    protected Class<?> getSupportedAttributeType(Class<?> managedType) {
        return CredentialStorage.class.isAssignableFrom(managedType) ? managedType : CredentialStorage.class;
    }
}