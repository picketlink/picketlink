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
package org.picketlink.idm.internal;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.IdentityContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static org.picketlink.idm.IDMLog.IDENTITY_STORE_LOGGER;

/**
 * @author pedroigor
 */
public abstract class AbstractAttributeStore<C extends IdentityStoreConfiguration> extends AbstractIdentityStore<C> implements AttributeStore<C> {

    @Override
    public <V extends Serializable> Attribute<V> getAttribute(IdentityContext context, AttributedType attributedType, String attributeName) {
        loadAttributes(context, attributedType);
        return attributedType.getAttribute(attributeName);
    }

    @Override
    public void loadAttributes(IdentityContext identityContext, AttributedType attributedType) {
        Collection<Attribute<? extends Serializable>> attributes = getAttributes(identityContext, attributedType);

        for (Attribute attribute : attributes) {
            attributedType.setAttribute(attribute);

            if (isTraceEnabled()) {
                IDENTITY_STORE_LOGGER.tracef("Ad-hoc attribute [%s] loaded into type [%s] with identifier [%s] and value [%s]", attribute.getName(), attributedType.getClass(), attributedType.getId(), attribute.getValue());
            }
        }

        loadManagedAttributes(attributedType);
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType attributedType, Attribute<? extends Serializable> attribute) {
        doSetAttribute(context, attributedType, attribute);
        attributedType.setAttribute(attribute);

        if (isTraceEnabled()) {
            IDENTITY_STORE_LOGGER.tracef("Ad-hoc attribute [%s] stored for type [%s] with identifier [%s] and value [%s]", attribute.getName(), attributedType.getClass(), attributedType.getId(), attribute.getValue());
        }
    }

    protected abstract void doSetAttribute(IdentityContext context, AttributedType type, Attribute<? extends Serializable> attribute);

    protected abstract Collection<Attribute<? extends Serializable>> getAttributes(IdentityContext context, AttributedType attributedType);

    private void loadManagedAttributes(AttributedType attributedType) {
        for (Attribute attribute : attributedType.getAttributes()) {
            List<Property<Object>> properties = PropertyQueries.createQuery(attributedType.getClass())
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                    .getResultList();

            for (Property property : properties) {
                AttributeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(AttributeProperty.class);

                if (property.getName().equals(attribute.getName()) && attributeProperty.managed()) {
                    Object value = null;

                    if (attribute != null) {
                        value = attribute.getValue();
                    }

                    property.setValue(attributedType, value);

                    if (isTraceEnabled()) {
                        IDENTITY_STORE_LOGGER.tracef("Managed attribute [%s] loaded into type [%s] with identifier [%s] and value [%s]", attribute.getName(), attributedType.getClass(), attributedType.getId(), attribute.getValue());
                    }
                }
            }
        }
    }

    private boolean isTraceEnabled() {
        return IDENTITY_STORE_LOGGER.isTraceEnabled();
    }
}
