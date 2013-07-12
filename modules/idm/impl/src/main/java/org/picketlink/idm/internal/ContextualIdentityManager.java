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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreSelector;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Default implementation of the IdentityManager interface.
 * <p/>
 * This lightweight class is intended to be created any time a batch of partition-specific identity management
 * operations are to be performed.  In a web environment, it is recommended that instances are scoped to the
 * web request lifecycle.
 * <p/>
 * This class is not thread-safe.
 *
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class ContextualIdentityManager extends AbstractIdentityContext implements IdentityManager {

    public static final String IDENTITY_MANAGER_CTX_PARAMETER = "IDENTITY_MANAGER_CTX_PARAMETER";
    private final StoreSelector storeSelector;

    public ContextualIdentityManager(Partition partition, EventBridge eventBridge, IdGenerator idGenerator,
                                     StoreSelector storeSelector) {
        super(partition, eventBridge, idGenerator);
        this.storeSelector = storeSelector;
        setParameter(IDENTITY_MANAGER_CTX_PARAMETER, this);
    }

    @Override
    public void add(IdentityType identityType) throws IdentityManagementException {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        checkUniqueness(identityType);

        try {
            storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, identityType.getClass(), IdentityOperation.create)
                    .add(this, identityType);
        } catch (Exception e) {
            throw MESSAGES.identityTypeAddFailed(identityType, e);
        }
    }

    @Override
    public void update(IdentityType identityType) throws IdentityManagementException {
        checkIfIdentityTypeExists(identityType);

        try {
            storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, IdentityType.class, IdentityOperation.update)
                    .update(this, identityType);
        } catch (Exception e) {
            throw MESSAGES.identityTypeUpdateFailed(identityType, e);
        }
    }

    @Override
    public void remove(IdentityType identityType) throws IdentityManagementException {
        checkIfIdentityTypeExists(identityType);

        try {
            storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, IdentityType.class, IdentityOperation.delete)
                    .remove(this, identityType);
        } catch (Exception e) {
            throw MESSAGES.identityTypeRemoveFailed(identityType, e);
        }
    }

    @Override
    public Agent getAgent(String loginName) throws IdentityManagementException {
        if (isNullOrEmpty(loginName)) {
            return null;
        }

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
        if (isNullOrEmpty(loginName)) {
            return null;
        }

        List<User> agents = createIdentityQuery(User.class).setParameter(User.LOGIN_NAME, loginName).getResultList();

        if (agents.isEmpty()) {
            return null;
        } else if (agents.size() == 1) {
            return agents.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Agent objects found with same login name");
        }
    }

    @Override
    public Role getRole(String name) {
        if (isNullOrEmpty(name)) {
            return null;
        }

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
        if (isNullOrEmpty(groupPath)) {
            return null;
        }

        if (!groupPath.startsWith("/")) {
            groupPath = "/" + groupPath;
        }

        String[] paths = groupPath.split("/");

        if (paths.length > 0) {
            String name = paths[paths.length - 1];

            IdentityQuery<Group> query = createIdentityQuery(Group.class);

            query.setParameter(Group.NAME, name);

            List<Group> result = query.getResultList();

            for (Group group : result) {
                if (group.getPath().equals(groupPath)) {
                    return group;
                }
            }
        }

        return null;
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        if (groupName == null || parent == null) {
            return null;
        }

        return getGroup(new Group(groupName, parent).getPath());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        IdentityQuery<T> query = createIdentityQuery(identityType);

        query.setParameter(IdentityType.ID, id);

        List<T> result = query.getResultList();

        T identity = null;

        if (!result.isEmpty()) {
            if (result.size() > 1) {
                throw MESSAGES.identityTypeAmbiguosFoundWithId(id);
            } else {
                identity = result.get(0);
            }
        }

        return identity;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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

    private void checkUniqueness(IdentityType identityType) {
        PropertyQuery<Serializable> propertyQuery = PropertyQueries.createQuery(identityType.getClass());

        propertyQuery.addCriteria(new AnnotatedPropertyCriteria(Unique.class));

        IdentityQuery<? extends IdentityType> identityQuery = createIdentityQuery(identityType.getClass());

        for (Property<Serializable> property : propertyQuery.getResultList()) {
            identityQuery.setParameter(AttributedType.QUERY_ATTRIBUTE.byName(property.getName()), property.getValue(identityType));
        }

        if (!identityQuery.getResultList().isEmpty()) {
            throw MESSAGES.identityTypeAlreadyExists(identityType.getClass(), identityType.toString(), getPartition());
        }
    }

    private void checkIfIdentityTypeExists(IdentityType identityType) throws IdentityManagementException {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (lookupIdentityById(identityType.getClass(), identityType.getId()) == null) {
            throw MESSAGES.attributedTypeNotFoundWithId(identityType.getClass(), identityType.getId(),
                    getPartition());
        }
    }
}