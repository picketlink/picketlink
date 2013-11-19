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
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreSelector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.picketlink.idm.IDMInternalMessages.MESSAGES;
import static org.picketlink.idm.util.IDMUtil.configureDefaultPartition;

/**
 * <p>Default implementation of the IdentityManager interface.<p/> <p/> <p> This lightweight class is intended to be
 * created any time a batch of partition-specific identity management operations are to be performed.  In a web
 * environment, it is recommended that instances are scoped to the web request lifecycle. <p/> <p/> <p> This class is
 * not thread-safe. </p>
 *
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class ContextualIdentityManager extends AbstractIdentityContext implements IdentityManager {

    private final StoreSelector storeSelector;
    private final RelationshipManager relationshipManager;

    public ContextualIdentityManager(Partition partition, EventBridge eventBridge, IdGenerator idGenerator,
                                     StoreSelector storeSelector, RelationshipManager relationshipManager) {
        super(partition, eventBridge, idGenerator);
        this.storeSelector = storeSelector;
        setParameter(IDENTITY_MANAGER_CTX_PARAMETER, this);
        this.relationshipManager = relationshipManager;
    }

    @Override
    public void add(IdentityType identityType) throws IdentityManagementException {
        checkUniqueness(identityType);

        try {
            IdentityStore identityStore = storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, identityType.getClass(), IdentityOperation.create);

            identityStore.add(this, identityType);

            configureDefaultPartition(identityType, identityStore, getPartitionManager());

            addAttributes(identityType);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeAddFailed(identityType, e);
        }
    }

    @Override
    public void update(IdentityType identityType) throws IdentityManagementException {
        checkIfIdentityTypeExists(identityType);

        try {
            storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, identityType.getClass(), IdentityOperation.update)
                    .update(this, identityType);

            if (identityType.getPartition() == null) {
                throw MESSAGES.attributedUndefinedPartition(identityType);
            }

            removeAttributes(identityType);
            addAttributes(identityType);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeUpdateFailed(identityType, e);
        }
    }

    @Override
    public void remove(IdentityType identityType) throws IdentityManagementException {
        checkIfIdentityTypeExists(identityType);

        try {
            RelationshipQuery<Relationship> query = this.relationshipManager.createRelationshipQuery(Relationship.class);

            query.setParameter(Relationship.IDENTITY, identityType);

            for (Relationship relationship : query.getResultList()) {
                this.relationshipManager.remove(relationship);
            }

            removeAllAttributes(identityType);

            storeSelector.getStoreForIdentityOperation(this, IdentityStore.class, identityType.getClass(), IdentityOperation.delete)
                    .remove(this, identityType);
        } catch (Exception e) {
            throw MESSAGES.attributedTypeRemoveFailed(identityType, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType class");
        }

        if (identityType == null) {
            throw MESSAGES.nullArgument("Identifier");
        }

        IdentityQuery<T> query = createIdentityQuery(identityType);

        query.setParameter(IdentityType.ID, id);

        List<T> result = query.getResultList();

        T identity = null;

        if (!result.isEmpty()) {
            if (result.size() > 1) {
                throw MESSAGES.attributedTypeAmbiguosFoundWithId(id);
            } else {
                identity = result.get(0);
            }
        }

        return identity;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType class");
        }

        return new DefaultIdentityQuery(this, identityType, this.storeSelector);
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        if (credentials == null) {
            throw MESSAGES.nullArgument("Credentials");
        }

        try {
            storeSelector.getStoreForCredentialOperation(this, credentials.getClass()).validateCredentials(this, credentials);
        } catch (Exception e) {
            throw MESSAGES.credentialValidationFailed(credentials, e);
        }
    }

    @Override
    public void updateCredential(Account account, Object credential) {
        updateCredential(account, credential, null, null);
    }

    @Override
    public void updateCredential(Account account, Object credential, Date effectiveDate, Date expiryDate) {
        checkIfIdentityTypeExists(account);

        if (credential == null) {
            throw MESSAGES.nullArgument("Credential");
        }

        try {
            storeSelector.getStoreForCredentialOperation(this, credential.getClass()).updateCredential(this, account, credential, effectiveDate, expiryDate);
        } catch (Exception e) {
            throw MESSAGES.credentialUpdateFailed(account, credential, e);
        }
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Account account, Class<T> storageClass) {
        checkIfIdentityTypeExists(account);

        if (storageClass == null) {
            throw MESSAGES.nullArgument("CredentialStorage type");
        }

        try {
            for (CredentialStore credentialStore : this.storeSelector.getStoresForCredentialStorage(this, storageClass)) {
                T credentialStorage = (T) credentialStore.retrieveCurrentCredential(this, account, storageClass);

                if (credentialStorage != null) {
                    return credentialStorage;
                }
            }
        } catch (Exception e) {
            throw MESSAGES.credentialRetrievalFailed(account, storageClass, e);
        }

        return null;
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Account account, Class<T> storageClass) {
        checkIfIdentityTypeExists(account);

        if (storageClass == null) {
            throw MESSAGES.nullArgument("CredentialStorage type");
        }

        List<T> storages = new ArrayList<T>();

        try {
            for (CredentialStore credentialStore : this.storeSelector.getStoresForCredentialStorage(this, storageClass)) {
                storages.addAll(credentialStore.retrieveCredentials(this, account, storageClass));
            }
        } catch (Exception e) {
            throw MESSAGES.credentialRetrievalFailed(account, storageClass, e);
        }

        return storages;
    }

    private void checkUniqueness(IdentityType identityType) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        PropertyQuery<Serializable> propertyQuery = PropertyQueries.createQuery(identityType.getClass());

        propertyQuery.addCriteria(new AnnotatedPropertyCriteria(Unique.class));

        IdentityQuery<? extends IdentityType> identityQuery = createIdentityQuery(identityType.getClass());

        for (Property<Serializable> property : propertyQuery.getResultList()) {
            identityQuery.setParameter(AttributedType.QUERY_ATTRIBUTE.byName(property.getName()), property.getValue(identityType));
        }

        List<? extends IdentityType> result = identityQuery.getResultList();

        if (!result.isEmpty()) {
            // we need to check the unique property values again because some properties are not stored and are calculated
            // based on the values of other properties. Eg.: Group.path
            for (Property<Serializable> property : propertyQuery.getResultList()) {
                for (IdentityType storedType: result) {
                    if (property.getValue(storedType).equals(property.getValue(identityType))) {
                        throw MESSAGES.identityTypeAlreadyExists(identityType.getClass(), identityType.getId(), getPartition());
                    }
                }
            }
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

    private void removeAllAttributes(final IdentityType identityType) {
        AttributeStore<?> attributeStore = storeSelector.getStoreForAttributeOperation(this);

        if (attributeStore != null) {
            IdentityType storedType = lookupIdentityById(identityType.getClass(), identityType.getId());

            if (storedType != null) {
                for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                    attributeStore.removeAttribute(this, identityType, attribute.getName());
                }
            }
        }
    }

    private void addAttributes(final IdentityType identityType) {
        AttributeStore<?> attributeStore = storeSelector.getStoreForAttributeOperation(this);

        if (attributeStore != null) {
            for (Attribute<? extends Serializable> attribute : identityType.getAttributes()) {
                attributeStore.setAttribute(this, identityType, attribute);
            }
        }
    }

    private void removeAttributes(final IdentityType identityType) {
        AttributeStore<?> attributeStore = storeSelector.getStoreForAttributeOperation(this);

        if (attributeStore != null) {
            IdentityType storedType = lookupIdentityById(identityType.getClass(), identityType.getId());

            if (storedType != null) {
                for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                    if (identityType.getAttribute(attribute.getName()) == null) {
                        attributeStore.removeAttribute(this, identityType, attribute.getName());
                    }
                }
            }
        }
    }

    private PartitionManager getPartitionManager() {
        return (PartitionManager) this.storeSelector;
    }

}