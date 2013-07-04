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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreSelector;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * Default implementation of the IdentityManager interface.
 *
 * This class is not thread-safe.
 *
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class ContextualIdentityManager implements IdentityManager, IdentityContext {

    private final Partition partition;
    private final StoreSelector storeSelector;
    private final EventBridge eventBridge;
    private final IdGenerator idGenerator;

    // We only create the parameters Map if required
    private Map<String,Object> parameters = null;

    public ContextualIdentityManager(Partition partition, StoreSelector storeSelector, EventBridge eventBridge,
            IdGenerator idGenerator) {
        this.partition = partition;
        this.storeSelector = storeSelector;
        this.eventBridge = eventBridge;
        this.idGenerator = idGenerator;
    }

    @Override
    public void add(IdentityType identityType) throws IdentityManagementException {
        this.storeSelector.getStoreForIdentityOperation(
                this,
                IdentityStore.class,
                identityType.getClass(),
                IdentityOperation.create).add(this, identityType);
    }

    @Override
    public Object getParameter(String paramName) {
        return parameters != null ? parameters.get(paramName) : null;
    }

    @Override
    public boolean isParameterSet(String paramName) {
        return parameters != null ? parameters.containsKey(paramName) : false;
    }

    @Override
    public void setParameter(String paramName, Object value) {
        if (parameters == null) {
            parameters = new HashMap<String,Object>();
        }
        parameters.put(paramName, value);
    }

    @Override
    public EventBridge getEventBridge() {
        return eventBridge;
    }

    @Override
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    @Override
    public Partition getPartition() {
        return partition;
    }

    @Override
    public void update(IdentityType identityType) throws IdentityManagementException {
        //TODO: Implement update
    }

    @Override
    public void remove(IdentityType value) throws IdentityManagementException {
        //TODO: Implement remove
    }

    @Override
    public Agent getAgent(String loginName) throws IdentityManagementException {
        return null;  //TODO: Implement getAgent
    }

    @Override
    public User getUser(String loginName) {
        return null;  //TODO: Implement getUser
    }

    @Override
    public Role getRole(String name) {
        return null;  //TODO: Implement getRole
    }

    @Override
    public Group getGroup(String groupPath) {
        return null;  //TODO: Implement getGroup
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        return null;  //TODO: Implement getGroup
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        return null;  //TODO: Implement lookupIdentityById
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        return null;  //TODO: Implement createIdentityQuery
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        //TODO: Implement validateCredentials
    }

    @Override
    public void updateCredential(Agent agent, Object credential) {
        //TODO: Implement updateCredential
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        //TODO: Implement updateCredential
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCurrentCredential
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCredentials
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) {
        //TODO: Implement loadAttribute
    }

}