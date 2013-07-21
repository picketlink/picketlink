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

import static org.picketlink.idm.IDMMessages.MESSAGES;

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
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreSelector;

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
            throw MESSAGES.attributedTypeAddFailed(identityType, e);
        }
    }

    @Override
    public void update(IdentityType identityType) throws IdentityManagementException {
        checkIfIdentityTypeExists(identityType);

        try {
            storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, IdentityType.class, IdentityOperation.update)
                    .update(this, identityType);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeUpdateFailed(identityType, e);
        }
    }

    @Override
    public void remove(IdentityType identityType) throws IdentityManagementException {
        checkIfIdentityTypeExists(identityType);

        try {
            storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, IdentityType.class, IdentityOperation.delete)
                    .remove(this, identityType);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeRemoveFailed(identityType, e);
        }
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