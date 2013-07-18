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

import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.model.AttributedType;

/**
 * @author pedroigor
 */
public abstract class AbstractAttributedTypeMapper extends AbstractModelMapper {

    @Override
    public EntityMapping createMapping(Class<? extends AttributedType> managedType, Class<?> entityType) {
        EntityMapping entityMapping = new EntityMapping(managedType, true);

        entityMapping.addProperty(getNamedProperty("id", managedType), getAnnotatedProperty(Identifier.class, entityType));

        try {
            entityMapping.addOwnerProperty(getAnnotatedProperty(OwnerReference.class, entityType));
        } catch (Exception e) {
            // ignore
        }

        return entityMapping;
    }

}
