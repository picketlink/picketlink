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

import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.AttributedType;

import java.util.ArrayList;
import java.util.List;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author pedroigor
 */
public abstract class AbstractIdentityManagedMapper extends AbstractModelMapper {

    @Override
    protected List<EntityMapping> doCreateMapping(Class<?> entityType) {
        ArrayList<EntityMapping> mappings = new ArrayList<EntityMapping>();

        IdentityManaged identityManaged = entityType.getAnnotation(IdentityManaged.class);

        if (identityManaged == null) {
            throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, IdentityManaged.class);
        }

        Class<? extends AttributedType>[] supportedTypes = null;

        if (identityManaged != null) {
            supportedTypes = identityManaged.value();
        } else {
            supportedTypes = new Class[]{AttributedType.class};
        }

        for (Class<?> supportedType: supportedTypes) {
            mappings.add(configure(supportedType, entityType));
        }

        return mappings;
    }

    protected abstract EntityMapping configure(Class<?> supportedType, Class<?> entityType);
}
