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
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.CredentialProperty;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;

import java.util.ArrayList;
import java.util.List;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author pedroigor
 */
public class ManagedCredentialAttributeMapper extends AbstractModelMapper {

    @Override
    public boolean supports(Class<?> entityType) {
        return getManagedCredential(entityType) != null;
    }

    @Override
    public List<EntityMapping> doCreateMapping(Class<?> entityType) throws SecurityConfigurationException {
        List<EntityMapping> mappings = new ArrayList<EntityMapping>();

        Class<? extends CredentialStorage>[] storageTypes = getManagedCredential(entityType).value();

        if (storageTypes.length == 0) {
            storageTypes = new Class[] {CredentialStorage.class};
        }

        for (Class<? extends CredentialStorage> storageType : storageTypes) {
            EntityMapping entityMapping = new EntityMapping(storageType);

            Property typeProperty = getAnnotatedProperty(CredentialClass.class, entityType);

            if (typeProperty == null) {
                throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, CredentialClass.class);
            }

            entityMapping.addTypeProperty(typeProperty);

            Property effectiveDate = getAnnotatedProperty(EffectiveDate.class, entityType);

            if (effectiveDate == null) {
                throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, EffectiveDate.class);
            }

            entityMapping.addProperty(getNamedProperty("effectiveDate", storageType), effectiveDate);

            Property expirationDate = getAnnotatedProperty(ExpiryDate.class, entityType);

            if (expirationDate == null) {
                throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, ExpiryDate.class);
            }

            entityMapping.addProperty(getNamedProperty("expiryDate", storageType), expirationDate);

            entityMapping.addOwnerProperty(entityType);

            List<Property<Object>> properties = PropertyQueries
                    .createQuery(entityType)
                    .addCriteria(new AnnotatedPropertyCriteria(CredentialProperty.class))
                    .getResultList();

            for (Property<Object> property : properties) {
                CredentialProperty credentialProperty = property.getAnnotatedElement().getAnnotation(CredentialProperty.class);
                String propertyName = credentialProperty.name();

                if (StringUtil.isNullOrEmpty(propertyName)) {
                    propertyName = property.getName();
                }

                entityMapping.addProperty(propertyName, property);
            }

            mappings.add(entityMapping);
        }

        return mappings;
    }

    private ManagedCredential getManagedCredential(Class<?> entityType) {
        return entityType.getAnnotation(ManagedCredential.class);
    }

}