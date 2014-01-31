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

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.StoreSelector;

import java.io.Serializable;
import java.util.List;

import static org.picketlink.idm.IDMInternalMessages.MESSAGES;

/**
 * Default implementation for RelationshipManager.
 * <p/>
 * This class is not thread-safe!
 *
 * @author Shane Bryzak
 */
public class ContextualRelationshipManager extends AbstractIdentityContext implements RelationshipManager {

    private StoreSelector storeSelector;
    private PrivilegeChainQuery privilegeChainQuery;

    public ContextualRelationshipManager(EventBridge eventBridge, IdGenerator idGenerator, StoreSelector storeSelector,
            PrivilegeChainQuery privilegeChainQuery) {
        super(null, eventBridge, idGenerator);
        this.storeSelector = storeSelector;
        this.privilegeChainQuery = privilegeChainQuery;
    }


    @Override
    public void add(Relationship relationship) {
        if (relationship == null) {
            MESSAGES.nullArgument("Relationship");
        }

        try {
            storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(), relationship, IdentityOperation.create).add(this, relationship);

            addAttributes(relationship);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeAddFailed(relationship, e);
        }
    }

    @Override
    public void update(Relationship relationship) {
        if (relationship == null) {
            MESSAGES.nullArgument("Relationship");
        }

        try {
            storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(), relationship, IdentityOperation.update).update(this, relationship);

            removeAttributes(relationship);
            addAttributes(relationship);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeUpdateFailed(relationship, e);
        }
    }

    @Override
    public void remove(Relationship relationship) {
        if (relationship == null) {
            MESSAGES.nullArgument("Relationship");
        }

        try {
            List<? extends Relationship> result = createRelationshipQuery(relationship.getClass()).setParameter
                    (Relationship.ID, relationship.getId())
                    .getResultList();

            if (!result.isEmpty()) {
                removeAllAttributes(relationship);
            }

            storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(), relationship, IdentityOperation.delete).remove(this, relationship);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeRemoveFailed(relationship, e);
        }
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipClass) {
        if (relationshipClass == null) {
            MESSAGES.nullArgument("Relationship Type");
        }

        return new DefaultRelationshipQuery<T>(this, relationshipClass, storeSelector);
    }

    private void removeAllAttributes(final Relationship identityType) {
        AttributeStore<?> attributeStore = storeSelector.getStoreForAttributeOperation(this);

        if (attributeStore != null) {
            Relationship storedType = lookupById(identityType.getClass(), identityType.getId());

            if (storedType != null) {
                for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                    attributeStore.removeAttribute(this, identityType, attribute.getName());
                }
            }
        }
    }

    private void addAttributes(final Relationship identityType) {
        AttributeStore<?> attributeStore = storeSelector.getStoreForAttributeOperation(this);

        if (attributeStore != null) {
            for (Attribute<? extends Serializable> attribute : identityType.getAttributes()) {
                attributeStore.setAttribute(this, identityType, attribute);
            }
        }
    }

    private void removeAttributes(final Relationship identityType) {
        AttributeStore<?> attributeStore = storeSelector.getStoreForAttributeOperation(this);

        if (attributeStore != null) {
            Relationship storedType = lookupById(identityType.getClass(), identityType.getId());

            if (storedType != null) {
                for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                    if (identityType.getAttribute(attribute.getName()) == null) {
                        attributeStore.removeAttribute(this, identityType, attribute.getName());
                    }
                }
            }
        }
    }

    private Relationship lookupById(final Class<? extends Relationship> relationshipType, final String id) {
        List<? extends Relationship> result = createRelationshipQuery(relationshipType).setParameter
                (Relationship.ID, id)
                .getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        }

        return null;
    }

    @Override
    public boolean inheritsPrivileges(IdentityType identity, IdentityType assignee) {
        if (identity.equals(assignee)) {
            return true;
        }

        return privilegeChainQuery.inheritsPrivileges(this, identity, assignee);
    }
}
