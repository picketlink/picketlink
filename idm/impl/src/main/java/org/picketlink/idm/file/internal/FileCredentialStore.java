/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.file.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.CredentialUtils;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

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

    public void validateCredentials(Credentials credentials) {
        CredentialHandler handler = getContext().getCredentialValidator(credentials.getClass(), this.identityStore);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for validating Credentials of type [" + credentials.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }

        handler.validate(credentials, this.identityStore);
    }

    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        CredentialHandler handler = getContext().getCredentialUpdater(credential.getClass(), this.identityStore);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for updating Credentials of type [" + credential.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.update(agent, credential, this.identityStore, effectiveDate, expiryDate);
    }

    @Override
    public void storeCredential(Agent agent, CredentialStorage storage) {
        List<FileCredentialStorage> credentials = getCredentials(agent, storage.getClass());

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
        flushCredentials();
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        return (T) CredentialUtils.getCurrentCredential(agent, this, storageClass);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageTyper) {
        ArrayList<T> storedCredentials = new ArrayList<T>();

        List<FileCredentialStorage> credentials = getCredentials(agent, storageTyper);

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
    public void removeCredentials(Agent agent) {
        getCredentialsForCurrentPartition().remove(agent.getLoginName());
        flushCredentials();
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
            throw new IdentityManagementException("Could not create CredentialStorage instance for class ["
                    + storageClass.getName() + "].", e);
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
    private List<FileCredentialStorage> getCredentials(Agent agent, Class<? extends CredentialStorage> storageType) {
        Map<String, List<FileCredentialStorage>> agentCredentials = getCredentialsForCurrentPartition().get(
                agent.getLoginName());

        if (agentCredentials == null) {
            agentCredentials = new HashMap<String, List<FileCredentialStorage>>();
        }

        List<FileCredentialStorage> credentials = agentCredentials.get(storageType.getName());

        if (credentials == null) {
            credentials = new ArrayList<FileCredentialStorage>();
        }

        agentCredentials.put(storageType.getName(), credentials);
        getCredentialsForCurrentPartition().put(agent.getLoginName(), agentCredentials);

        return credentials;
    }

    private IdentityStoreInvocationContext getContext() {
        return this.identityStore.getContext();
    }

    private FileIdentityStoreConfiguration getConfig() {
        return this.identityStore.getConfig();
    }

    private Map<String, Map<String, List<FileCredentialStorage>>> getCredentialsForCurrentPartition() {
        return getConfig().getCredentials(getContext());
    }

    private void flushCredentials() {
        getConfig().flushCredentials(getContext());
    }
}
