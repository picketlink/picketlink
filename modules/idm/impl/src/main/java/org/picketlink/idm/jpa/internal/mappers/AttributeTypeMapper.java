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

import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.model.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pedroigor
 */
public class AttributeTypeMapper extends AbstractModelMapper {

    @Override
    public boolean supports(Class<?> entityType) {
        return  getAnnotatedProperty(AttributeName.class, entityType) != null
                && getAnnotatedProperty(AttributeValue.class, entityType) != null;
    }

    @Override
    protected List<EntityMapping> doCreateMapping(final Class<?> entityType) {
        List<EntityMapping> mappings = new ArrayList<EntityMapping>();

        EntityMapping entityMapping = new EntityMapping(Attribute.class);

        entityMapping.addProperty(getNamedProperty("name", Attribute.class), getAnnotatedProperty(AttributeName.class, entityType));
        entityMapping.addProperty(getNamedProperty("value", Attribute.class), getAnnotatedProperty(AttributeValue.class, entityType));

        entityMapping.addOwnerProperty(entityType);

        mappings.add(entityMapping);

        return mappings;
    }

}