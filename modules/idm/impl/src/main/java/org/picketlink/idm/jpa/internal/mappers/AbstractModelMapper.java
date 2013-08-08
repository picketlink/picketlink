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

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.SecurityConfigurationException;

/**
 * @author pedroigor
 */
public abstract class AbstractModelMapper implements ModelMapper {

    @Override
    public List<EntityMapping> createMapping(Class<?> entityType) {
        if (!supports(entityType)) {
            return Collections.emptyList();
        }

        if (Modifier.isAbstract(entityType.getModifiers())) {
            throw new IdentityManagementException("Mapped entity [" + entityType + "] is marked as abstract.");
        }

        try {
            return doCreateMapping(entityType);
        } catch (Exception e) {
            throw new IdentityManagementException("Could not map entity [" + entityType + "].", e);
        }
    }

    protected abstract List<EntityMapping> doCreateMapping(Class<?> entityType) throws SecurityConfigurationException;

    protected Property getAnnotatedProperty(Class<? extends Annotation> annotationType, Class<?> type) {
        return PropertyQueries.<String>createQuery(type)
                .addCriteria(new AnnotatedPropertyCriteria(annotationType))
                .getFirstResult();
    }

    protected List<Property<String>> getAnnotatedProperties(Class<? extends Annotation> annotationType, Class<?> type) {
        return PropertyQueries.<String>createQuery(type)
                .addCriteria(new AnnotatedPropertyCriteria(annotationType))
                .getResultList();
    }

    protected Property getNamedProperty(String propertyName, Class<?> type) {
        return PropertyQueries.<String>createQuery(type)
                .addCriteria(new NamedPropertyCriteria(propertyName))
                .getFirstResult();
    }

}
