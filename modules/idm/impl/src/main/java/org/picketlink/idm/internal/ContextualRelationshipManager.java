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

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.event.RelationshipCreatedEvent;
import org.picketlink.idm.event.RelationshipDeletedEvent;
import org.picketlink.idm.event.RelationshipUpdatedEvent;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.IdentityContext;

import java.util.List;

import static org.picketlink.idm.IDMInternalMessages.MESSAGES;

/**
 * Default implementation for RelationshipManager.
 * <p/>
 * This class is not thread-safe!
 *
 * @author Shane Bryzak
 */
public class ContextualRelationshipManager extends AbstractAttributedTypeManager<Relationship> implements RelationshipManager {

    private final PartitionManager partitionManager;

    public ContextualRelationshipManager(DefaultPartitionManager partitionManager) {
        super(partitionManager.getConfiguration(), null);
        this.partitionManager = partitionManager;
    }

    @Override
    protected void doAdd(Relationship relationship) {
        IdentityContext identityContext = getIdentityContext();

        getStoreSelector().getStoreForRelationshipOperation(identityContext, relationship.getClass(), relationship, IdentityOperation.create)
                .add(identityContext, relationship);
    }

    @Override
    protected void fireAttributedTypeAddedEvent(Relationship relationship) {
        fireEvent(new RelationshipCreatedEvent(relationship, this.partitionManager));
    }

    @Override
    protected void fireAttributedTypeUpdatedEvent(Relationship relationship) {
        fireEvent(new RelationshipUpdatedEvent(relationship, this.partitionManager));
    }

    @Override
    protected void doUpdate(Relationship relationship) {
        IdentityContext identityContext = getIdentityContext();

        getStoreSelector().getStoreForRelationshipOperation(identityContext, relationship.getClass(), relationship, IdentityOperation.update)
                .update(identityContext, relationship);
    }

    @Override
    protected void fireAttributedTypeRemovedEvent(Relationship attributedType) {
        fireEvent(new RelationshipDeletedEvent(attributedType, this.partitionManager));
    }

    @Override
    protected void doRemove(Relationship relationship) {
        IdentityContext identityContext = getIdentityContext();

        getStoreSelector().getStoreForRelationshipOperation(identityContext, relationship.getClass(), relationship, IdentityOperation.delete)
                .remove(identityContext, relationship);
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipClass) {
        if (relationshipClass == null) {
            MESSAGES.nullArgument("Relationship Type");
        }

        return new DefaultRelationshipQuery<T>(getIdentityContext(), relationshipClass, this);
    }

    @Override
    public <C extends Relationship> C lookupById(Class<C> attributedType, String id) throws IdentityManagementException {
        List<C> result = createRelationshipQuery(attributedType).setParameter
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

        PrivilegeChainQuery privilegeChainQuery = getConfiguration().getPrivilegeChainQuery();

        return privilegeChainQuery.inheritsPrivileges(this, identity, assignee);
    }

    @Override
    protected void checkUniqueness(Relationship attributedType) throws IdentityManagementException {
        //no-op
    }

    @Override
    protected void checkIfExists(Relationship attributedType) throws IdentityManagementException {
        //no-op
    }

    public PartitionManager getPartitionManager() {
        return this.partitionManager;
    }
}
