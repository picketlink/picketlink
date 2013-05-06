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

package org.picketlink.idm.file.internal;

import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.credential.internal.CredentialUtils.getCurrentCredential;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.SecurityContext;

/**
 * <p>
 * {@link CredentialStore} implementation that stored the credentials using the {@link FileBasedIdentityStore}.
 * </p>
 *
 * @author Pedro Silva
 *
 */
public class FileCredentialStore implements CredentialStore {

    private FileBasedIdentityStore identityStore;

    public FileCredentialStore(FileBasedIdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    public void validateCredentials(SecurityContext context, Credentials credentials) {
        CredentialHandler handler = context.getCredentialValidator(credentials.getClass(), this.identityStore);

        if (handler == null) {
            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credentials.getClass());
        }

        handler.validate(context, credentials, this.identityStore);
    }

    public void updateCredential(SecurityContext context, Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        CredentialHandler handler = context.getCredentialUpdater(credential.getClass(), this.identityStore);

        if (handler == null) {
            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credential.getClass());
        }

        handler.update(context, agent, credential, this.identityStore, effectiveDate, expiryDate);
    }

    @Override
    public void storeCredential(SecurityContext context, Agent agent, CredentialStorage storage) {
        List<FileCredentialStorage> credentials = getCredentials(context, agent, storage.getClass());

        FileCredentialStorage credential = new FileCredentialStorage();

        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storage.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        for (Property<Object> property : annotatedTypes) {
            credential.getStoredFields().put(property.getName(), (Serializable) property.getValue(storage));
        }

        if (credential.getEffectiveDate() == null) {
            credential.setEffectiveDate(new Date());
        }

        credentials.add(credential);

        flushCredentials(context);
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(SecurityContext context, Agent agent, Class<T> storageClass) {
        return getCurrentCredential(context, agent, this, storageClass);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(SecurityContext context, Agent agent, Class<T> storageTyper) {
        ArrayList<T> storedCredentials = new ArrayList<T>();

        List<FileCredentialStorage> credentials = getCredentials(context, agent, storageTyper);

        for (FileCredentialStorage fileCredentialStorage : credentials) {
            storedCredentials.add(convertToCredentialStorage(storageTyper, fileCredentialStorage));
        }

        return storedCredentials;
    }

    /**
     * <p>
     * Remove all stored credentials for the given {@link Agent}.
     * </p>
     *
     * @param agent
     */
    public void removeCredentials(SecurityContext context, Agent agent) {
        getCredentialsForCurrentPartition(context).remove(agent.getLoginName());
        flushCredentials(context);
    }

    /**
     * <p>
     * Converts a {@link FileCredentialStorage} to a specific {@link CredentialStorage} instance.
     * </p>
     *
     * @param storageClass
     * @param fileCredentialStorage
     * @return
     */
    private <T extends CredentialStorage> T convertToCredentialStorage(Class<T> storageClass,
            FileCredentialStorage fileCredentialStorage) {
        T storage = null;

        try {
            storage = storageClass.newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(storageClass.getName(), e);
        }

        Set<Entry<String, Serializable>> storedFields = fileCredentialStorage.getStoredFields().entrySet();

        for (Entry<String, Serializable> storedField : storedFields) {
            List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storageClass)
                    .addCriteria(new NamedPropertyCriteria(storedField.getKey())).getResultList();

            if (annotatedTypes.isEmpty()) {
                throw new IdentityManagementException("Could not find property [" + storedField.getKey()
                        + "] on CredentialStorage [" + storageClass.getName() + "].");
            } else if (annotatedTypes.size() > 1) {
                throw new IdentityManagementException("Ambiguos property [" + storedField.getKey() + "] on CredentialStorage ["
                        + storageClass.getName() + "].");
            }

            annotatedTypes.get(0).setValue(storage, storedField.getValue());
        }

        return storage;
    }

    /**
     * <p>
     * Returns the stored credentials for the given {@link Agent}.
     * </p>
     *
     * @param agent
     * @param storageType
     * @return
     */
    private List<FileCredentialStorage> getCredentials(SecurityContext context, Agent agent,
            Class<? extends CredentialStorage> storageType) {
        Map<String, List<FileCredentialStorage>> agentCredentials = getCredentialsForCurrentPartition(context).get(
                agent.getLoginName());

        if (agentCredentials == null) {
            agentCredentials = new HashMap<String, List<FileCredentialStorage>>();
        }

        List<FileCredentialStorage> credentials = agentCredentials.get(storageType.getName());

        if (credentials == null) {
            credentials = new ArrayList<FileCredentialStorage>();
        }

        agentCredentials.put(storageType.getName(), credentials);
        getCredentialsForCurrentPartition(context).put(agent.getLoginName(), agentCredentials);

        return credentials;
    }

    private Map<String, Map<String, List<FileCredentialStorage>>> getCredentialsForCurrentPartition(SecurityContext context) {
        Realm realm = (Realm) context.getPartition();

        return getDataSource().getCredentials(realm);
    }

    private void flushCredentials(SecurityContext context) {
        Realm realm = (Realm) context.getPartition();

        getDataSource().flushCredentials(realm);
    }

    private FileDataSource getDataSource() {
        return this.identityStore.getDataSource();
    }
}
