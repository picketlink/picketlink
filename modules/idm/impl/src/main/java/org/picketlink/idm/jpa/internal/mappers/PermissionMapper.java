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

import java.util.List;

import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.jpa.annotations.entity.PermissionManaged;

/**
 * An EntityMapper that works with entities that store resource permission state.
 *
 * @author Shane Bryzak
 */
public class PermissionMapper extends AbstractModelMapper {

    @Override
    public boolean supports(Class<?> entityType) {
        return PropertyQueries
                .createQuery(entityType)
                .addCriteria(new AnnotatedPropertyCriteria(PermissionManaged.class))
                .getFirstResult() != null;
    }

    @Override
    protected List<EntityMapping> doCreateMapping(Class<?> entityType) throws SecurityConfigurationException {
        // TODO Auto-generated method stub
        return null;
    }

}
