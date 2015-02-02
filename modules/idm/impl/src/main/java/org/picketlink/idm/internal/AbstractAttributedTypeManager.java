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
import org.picketlink.idm.AttributedTypeManager;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.StoreSelector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.picketlink.idm.IDMInternalMessages.MESSAGES;

/**
 * @author pedroigor
 */
public abstract class AbstractAttributedTypeManager<T extends AttributedType> implements AttributedTypeManager<T> {

    private final IdentityContext identityContext;
    private final PartitionManagerConfiguration configuration;

    AbstractAttributedTypeManager(PartitionManagerConfiguration configuration, Partition partition) {
        this.configuration = configuration;
        this.identityContext = createIdentityContext(partition, configuration.getEventBridge(), configuration.getIdGenerator());
    }

    public AbstractAttributedTypeManager(PartitionManagerConfiguration configuration) {
        this(configuration, null);
    }

    public AbstractAttributedTypeManager(DefaultPartitionManager partitionManager) {
        this(partitionManager.getConfiguration(), null);
    }

    @Override
    public void add(T attributedType) throws IdentityManagementException {
        if (attributedType == null) {
            throw MESSAGES.nullArgument("AttributedType");
        }

        checkUniqueness(attributedType);

        try {
            doAdd(attributedType);

            addAttributes(identityContext, attributedType);

            fireAttributedTypeAddedEvent(attributedType);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeAddFailed(attributedType, e);
        }
    }

    protected abstract void fireAttributedTypeAddedEvent(T attributedType);
    protected abstract void doAdd(T attributedType);

    @Override
    public void update(T attributedType) throws IdentityManagementException {
        if (attributedType == null) {
            throw MESSAGES.nullArgument("AttributedType");
        }

        checkIfExists(attributedType);

        try {
            doUpdate(attributedType);

            T storedType = lookupById((Class<T>) attributedType.getClass(), attributedType.getId());

            removeAttributes(identityContext, attributedType, storedType);
            addAttributes(identityContext, attributedType);

            fireAttributedTypeUpdatedEvent(attributedType);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeUpdateFailed(attributedType, e);
        }
    }

    protected abstract void fireAttributedTypeUpdatedEvent(T attributedType);
    protected abstract void doUpdate(T attributedType);

    @Override
    public void remove(T attributedType) throws IdentityManagementException {
        if (attributedType == null) {
            throw MESSAGES.nullArgument("AttributedType");
        }

        checkIfExists(attributedType);

        try {
            T storedType = lookupById((Class<T>) attributedType.getClass(), attributedType.getId());

            removeAllAttributes(storedType);

            doRemove(attributedType);

            fireAttributedTypeRemovedEvent(attributedType);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeRemoveFailed(attributedType, e);
        }
    }

    protected abstract void fireAttributedTypeRemovedEvent(T attributedType);
    protected abstract void doRemove(T attributedType);

    protected abstract void checkUniqueness(T attributedType) throws IdentityManagementException;
    protected abstract void checkIfExists(T attributedType) throws IdentityManagementException;

    protected void fireEvent(Object event) {
        getEventBridge().raiseEvent(event);
    }

    protected EventBridge getEventBridge() {
        return getIdentityContext().getEventBridge();
    }

    protected IdentityContext createIdentityContext(Partition partition, EventBridge eventBridge, IdGenerator idGenerator) {
        return new DefaultIdentityContext(partition, eventBridge, idGenerator);
    }

    protected IdGenerator getIdGenerator() {
        return getIdentityContext().getIdGenerator();
    }

    protected void addAttributes(IdentityContext identityContext, AttributedType identityType) {
        AttributeStore<?> attributeStore = getStoreSelector().getStoreForAttributeOperation(identityContext);

        if (attributeStore != null) {
            for (Attribute<? extends Serializable> attribute : getAttributes(identityContext, identityType).values()) {
                attributeStore.setAttribute(identityContext, identityType, attribute);
            }
        }
    }

    protected void removeAttributes(IdentityContext identityContext, AttributedType identityType, AttributedType storedType) {
        AttributeStore<?> attributeStore = getStoreSelector().getStoreForAttributeOperation(identityContext);

        if (attributeStore != null) {
            if (storedType != null) {
                Map<String, Attribute> attributes = getAttributes(identityContext, identityType);

                for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                    if (attributes.get(attribute.getName()) == null) {
                        attributeStore.removeAttribute(identityContext, identityType, attribute.getName());
                    }
                }
            }
        }
    }

    protected void removeAllAttributes(AttributedType storedType) {
        AttributeStore<?> attributeStore = getStoreSelector().getStoreForAttributeOperation(identityContext);

        if (attributeStore != null) {
            for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                attributeStore.removeAttribute(identityContext, storedType, attribute.getName());
            }
        }
    }

    private Map<String, Attribute> getAttributes(IdentityContext identityContext, AttributedType identityType) {
        Map<String, Attribute> attributes = new HashMap<String, Attribute>();

        for (Attribute attribute : identityType.getAttributes()) {
            attributes.put(attribute.getName(), attribute);
        }

        List<Property<Object>> properties = PropertyQueries.createQuery(identityType.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                .getResultList();

        for (Property property : properties) {
            AttributeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(AttributeProperty.class);

            if (attributeProperty.managed()) {
                String attributeName = property.getName();
                Object attributeValue = property.getValue(identityType);

                if (attributeValue != null) {
                    attributes.put(attributeName, new Attribute(attributeName, (Serializable) attributeValue));
                } else {
                    attributes.remove(attributeName);
                }
            }
        }

        return attributes;
    }

    public  StoreSelector getStoreSelector() {
        return this.configuration.getStoreSelector();
    }

    public  IdentityContext getIdentityContext() {
        return this.identityContext;
    }

    protected PartitionManagerConfiguration getConfiguration() {
        return this.configuration;
    }
}
