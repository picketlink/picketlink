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
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.event.CredentialUpdatedEvent;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.event.IdentityTypeCreatedEvent;
import org.picketlink.idm.event.IdentityTypeDeletedEvent;
import org.picketlink.idm.event.IdentityTypeUpdatedEvent;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.query.internal.DefaultQueryBuilder;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

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
public class ContextualIdentityManager extends AbstractAttributedTypeManager<IdentityType> implements IdentityManager {

    private final DefaultPartitionManager partitionManager;
    private final RelationshipManager relationshipManager;
    private final PermissionManager permissionManager;

    public ContextualIdentityManager(Partition partition,
                                     DefaultPartitionManager partitionManager) {
        super(partitionManager.getStoreSelector(), partitionManager.getConfiguration(), partition);

        this.partitionManager = partitionManager;

        IdentityContext identityContext = getIdentityContext();

        if (getStoreSelector().getStoreForPermissionOperation(identityContext) != null) {
            this.permissionManager = this.partitionManager.createPermissionManager(partition);
        } else {
            this.permissionManager = null;
        }


        this.relationshipManager = this.partitionManager.createRelationshipManager();
    }

    @Override
    protected void doAdd(IdentityType attributedType) {
        IdentityContext identityContext = getIdentityContext();
        IdentityStore identityStore = getStoreSelector().getStoreForIdentityOperation(identityContext, IdentityStore.class, attributedType.getClass(), IdentityOperation.create);

        identityStore.add(identityContext, attributedType);

        configureDefaultPartition(identityContext, attributedType, identityStore, this.partitionManager);
    }

    @Override
    protected void fireAttributedTypeAddedEvent(IdentityType attributedType) {
        fireEvent(new IdentityTypeCreatedEvent(attributedType, this.partitionManager));
    }

    @Override
    protected void doUpdate(IdentityType attributedType) {
        if (attributedType.getPartition() == null) {
            throw MESSAGES.attributedUndefinedPartition(attributedType);
        }

        IdentityContext identityContext = getIdentityContext();

        getStoreSelector().getStoreForIdentityOperation(identityContext, IdentityStore.class, attributedType.getClass(), IdentityOperation.update)
                .update(identityContext, attributedType);
    }

    @Override
    protected void fireAttributedTypeUpdatedEvent(IdentityType attributedType) {
        fireEvent(new IdentityTypeUpdatedEvent(attributedType, this.partitionManager));
    }

    @Override
    protected void fireAttributedTypeRemovedEvent(IdentityType attributedType) {
        fireEvent(new IdentityTypeDeletedEvent(attributedType, this.partitionManager));
    }

    @Override
    protected void doRemove(IdentityType attributedType) {
        RelationshipQuery<Relationship> query = this.relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, attributedType);

        for (Relationship relationship : query.getResultList()) {
            this.relationshipManager.remove(relationship);
        }

        if (this.permissionManager != null) {
            List<Permission> permissions = this.permissionManager.listPermissions(attributedType);

            for (Permission permission : permissions) {
                this.permissionManager.revokePermission(attributedType, permission.getResourceClass(), permission.getOperation());
            }
        }

        IdentityContext identityContext = getIdentityContext();

        getStoreSelector().getStoreForIdentityOperation(identityContext, IdentityStore.class, attributedType.getClass(), IdentityOperation.delete)
                .remove(identityContext, attributedType);
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        return lookupById(identityType, id);
    }

    @Override
    public <C extends IdentityType> C lookupById(Class<C> attributedType, String id) throws IdentityManagementException {
        if (attributedType == null) {
            throw MESSAGES.nullArgument("IdentityType class");
        }

        if (id == null) {
            throw MESSAGES.nullArgument("Identifier");
        }

        IdentityQueryBuilder queryBuilder = getQueryBuilder();
        IdentityQuery<C> query = queryBuilder.createIdentityQuery(attributedType);

        query.where(queryBuilder.equal(IdentityType.ID, id));

        List<C> result = query.getResultList();

        C identity = null;

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

        return new DefaultIdentityQuery(getQueryBuilder(), getIdentityContext(), identityType, this.partitionManager, getStoreSelector());
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        if (credentials == null) {
            throw MESSAGES.nullArgument("Credentials");
        }

        try {
            IdentityContext identityContext = getIdentityContext();

            getStoreSelector().getStoreForCredentialOperation(identityContext, credentials.getClass()).validateCredentials(identityContext, credentials);
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
        checkIfExists(account);

        if (credential == null) {
            throw MESSAGES.nullArgument("Credential");
        }

        try {
            IdentityContext identityContext = getIdentityContext();

            getStoreSelector().getStoreForCredentialOperation(identityContext, credential.getClass()).updateCredential(identityContext, account, credential, effectiveDate, expiryDate);

            fireEvent(new CredentialUpdatedEvent(account, credential, effectiveDate, expiryDate, this.partitionManager));
        } catch (Exception e) {
            throw MESSAGES.credentialUpdateFailed(account, credential, e);
        }
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Account account, Class<T> storageClass) {
        checkIfExists(account);

        if (storageClass == null) {
            throw MESSAGES.nullArgument("CredentialStorage type");
        }

        try {
            IdentityContext identityContext = getIdentityContext();

            for (CredentialStore credentialStore : getStoreSelector().getStoresForCredentialStorage(identityContext, storageClass)) {
                T credentialStorage = (T) credentialStore.retrieveCurrentCredential(identityContext, account, storageClass);

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
        checkIfExists(account);

        if (storageClass == null) {
            throw MESSAGES.nullArgument("CredentialStorage type");
        }

        List<T> storages = new ArrayList<T>();

        try {
            IdentityContext identityContext = getIdentityContext();

            for (CredentialStore credentialStore : getStoreSelector().getStoresForCredentialStorage(identityContext, storageClass)) {
                storages.addAll(credentialStore.retrieveCredentials(identityContext, account, storageClass));
            }
        } catch (Exception e) {
            throw MESSAGES.credentialRetrievalFailed(account, storageClass, e);
        }

        return storages;
    }

    @Override
    public void removeCredential(Account account, Class<? extends CredentialStorage> storageClass) {
        checkIfExists(account);

        if (storageClass == null) {
            throw MESSAGES.nullArgument("CredentialStorage type");
        }

        try {
            IdentityContext identityContext = getIdentityContext();

            for (CredentialStore credentialStore : getStoreSelector().getStoresForCredentialStorage(identityContext, storageClass)) {
                credentialStore.removeCredential(identityContext, account, storageClass);
            }
        } catch (Exception e) {
            throw MESSAGES.credentialRetrievalFailed(account, storageClass, e);
        }
    }

    @Override
    public IdentityQueryBuilder getQueryBuilder() {
        return new DefaultQueryBuilder(getIdentityContext(), this.partitionManager, getStoreSelector());
    }

    @Override
    protected IdentityContext createIdentityContext(Partition partition, EventBridge eventBridge, IdGenerator idGenerator) {
        IdentityContext identityContext = super.createIdentityContext(partition, eventBridge, idGenerator);

        identityContext.setParameter(IDENTITY_MANAGER_CTX_PARAMETER, this);

        return identityContext;
    }

    @Override
    protected void checkIfExists(IdentityType identityType) throws IdentityManagementException {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (lookupIdentityById(identityType.getClass(), identityType.getId()) == null) {
            throw MESSAGES.attributedTypeNotFoundWithId(identityType.getClass(), identityType.getId(),
                    identityType.getPartition());
        }
    }

    @Override
    protected void checkUniqueness(IdentityType identityType) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        PropertyQuery<Serializable> propertyQuery = PropertyQueries.createQuery(identityType.getClass());

        propertyQuery.addCriteria(new AnnotatedPropertyCriteria(Unique.class));

        IdentityQueryBuilder queryBuilder = getQueryBuilder();
        IdentityQuery<? extends IdentityType> identityQuery = queryBuilder.createIdentityQuery(identityType.getClass());

        for (Property<Serializable> property : propertyQuery.getResultList()) {
            identityQuery.where(queryBuilder
                .equal(AttributedType.QUERY_ATTRIBUTE.byName(property.getName()), property.getValue(identityType)));
        }

        List<? extends IdentityType> result = identityQuery.getResultList();

        if (!result.isEmpty()) {
            // we need to check the unique property values again because some properties are not stored and are calculated
            // based on the values of other properties. Eg.: Group.path
            for (Property<Serializable> property : propertyQuery.getResultList()) {
                for (IdentityType storedType: result) {
                    if (property.getValue(storedType).equals(property.getValue(identityType))) {
                        throw MESSAGES.identityTypeAlreadyExists(identityType.getClass(), identityType.getId(), identityType.getPartition());
                    }
                }
            }
        }
    }
}