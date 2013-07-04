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
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreSelector;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * Default implementation of the IdentityManager interface.
 *
 * This lightweight class is intended to be created any time a batch of partition-specific identity management
 * operations are to be performed.  In a web environment, it is recommended that instances are scoped to the
 * web request lifecycle.
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
    public void add(IdentityType identityType) throws IdentityManagementException {
        storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, identityType.getClass(), IdentityOperation.create)
            .add(this, identityType);
    }

    @Override
    public void update(IdentityType value) throws IdentityManagementException {
        storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, IdentityType.class, IdentityOperation.update)
            .update(this, value);
    }

    @Override
    public void remove(IdentityType value) throws IdentityManagementException {
        storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, IdentityType.class, IdentityOperation.delete)
            .remove(this, value);
    }

    @Override
    public Agent getAgent(String loginName) throws IdentityManagementException {
        List<Agent> agents = createIdentityQuery(Agent.class).setParameter(Agent.LOGIN_NAME, loginName).getResultList();
        if (agents.isEmpty()) {
            return null;
        } else if (agents.size() == 1) {
            return agents.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Agent objects found with same login name");
        }
    }

    @Override
    public User getUser(String loginName) {
        List<User> users = createIdentityQuery(User.class).setParameter(User.LOGIN_NAME, loginName).getResultList();
        if (users.isEmpty()) {
            return null;
        } else if (users.size() == 1) {
            return users.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple User objects found with same login name");
        }
    }

    @Override
    public Role getRole(String name) {
        List<Role> roles = createIdentityQuery(Role.class).setParameter(Role.NAME, name).getResultList();
        if (roles.isEmpty()) {
            return null;
        } else if (roles.size() == 1) {
            return roles.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Role objects found with same name");
        }
    }

    @Override
    public Group getGroup(String groupPath) {
        List<Group> groups = createIdentityQuery(Group.class).setParameter(Group.PATH, groupPath).getResultList();
        if (groups.isEmpty()) {
            return null;
        } else if (groups.size() == 1) {
            return groups.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Group objects found with same path");
        }
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        List<Group> groups = createIdentityQuery(Group.class)
                .setParameter(Group.NAME, groupName)
                .setParameter(Group.PARENT, parent)
                .getResultList();
        if (groups.isEmpty()) {
            return null;
        } else if (groups.size() == 1) {
            return groups.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Group objects found with same name and parent");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        return (T) storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, identityType, IdentityOperation.read)
                .getIdentity(identityType, id);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        return new DefaultIdentityQuery(this, identityType, storeSelector.getStoreForIdentityOperation(
                this, IdentityStore.class, identityType, IdentityOperation.read));
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        storeSelector.getStoreForCredentialOperation(this, credentials.getClass()).validateCredentials(this, credentials);
    }

    @Override
    public void updateCredential(Account account, Object credential) {
        updateCredential(account, credential, null, null);
    }

    @Override
    public void updateCredential(Account account, Object credential, Date effectiveDate, Date expiryDate) {
        storeSelector.getStoreForCredentialOperation(this, credential.getClass())
            .updateCredential(this, account, credential, effectiveDate, expiryDate);
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Account account, Class<T> storageClass) {
        return (T) ((CredentialStore<?>) storeSelector.getStoreForCredentialOperation(this, storageClass))
            .retrieveCurrentCredential(this, account, storageClass);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Account account, Class<T> storageClass) {
        return (List<T>) ((CredentialStore<?>) storeSelector.getStoreForCredentialOperation(this, storageClass))
                .retrieveCredentials(this, account, storageClass);
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) {
        //TODO: Implement loadAttribute
    }

}