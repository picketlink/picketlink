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
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.StoreSelector;

/**
 * Default implementation for RelationshipManager.
 *
 * This class is not thread-safe!
 *
 * @author Shane Bryzak
 *
 */
public class ContextualRelationshipManager extends AbstractIdentityContext implements RelationshipManager {

    private StoreSelector storeSelector;

    public ContextualRelationshipManager(EventBridge eventBridge, IdGenerator idGenerator, StoreSelector storeSelector) {
        super(null, eventBridge, idGenerator);
        this.storeSelector = storeSelector;
    }


    @Override
    public void add(Relationship relationship) {
        storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(), relationship, IdentityOperation.create).add(this, relationship);
    }

    @Override
    public void update(Relationship relationship) {
        storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(), relationship, IdentityOperation.update).update(this, relationship);
    }

    @Override
    public void remove(Relationship relationship) {
        storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(), relationship, IdentityOperation.delete).remove(this, relationship);
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipClass) {
        return new DefaultRelationshipQuery<T>(this, relationshipClass, storeSelector);
    }
}
