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
import org.picketlink.idm.jpa.annotations.entity.MappedAttribute;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author pedroigor
 */
public class NamedMappedAttribute extends AbstractIdentityManagedMapper {

    @Override
    public boolean supports(Class<?> entityType) {
        MappedAttribute mappedAttribute = entityType.getAnnotation(MappedAttribute.class);
        return mappedAttribute != null && !isNullOrEmpty(mappedAttribute.value());
    }

    @Override
    public EntityMapping configure(Class<?> supportedType, Class<?> entityType) {
        EntityMapping entityMapping = new EntityMapping(supportedType);

        MappedAttribute mappedAttribute = entityType.getAnnotation(MappedAttribute.class);
        Property namedProperty = getNamedProperty(mappedAttribute.value(), supportedType);

        if (namedProperty == null) {
            throw MESSAGES.configJpaStoreMappedPropertyNotFound(entityType, mappedAttribute.value(),
                    supportedType);
        }

        entityMapping.addOwnerProperty(entityType);

        return entityMapping;
    }

}