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

package org.picketlink.idm.credential.internal;

import java.util.Date;

import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.CredentialStore;

/**
 * <p>Utility class with helper methods for the Credential API.</p>
 * 
 * @author Pedro Silva
 *
 */
public final class CredentialUtils {

    /**
     * <p>
     * Checks if the specified {@link CredentialStorage} maps to the current credential.
     * </p>
     * 
     * @param credential
     * @return
     */
    public static boolean isCurrentCredential(CredentialStorage credential) {
        boolean isCurrent = true;

        Date actualDate = new Date();

        if (credential.getEffectiveDate() != null) {
            if (credential.getEffectiveDate().compareTo(actualDate) > 0) {
                isCurrent = false;
            }
        }

        if (isCurrent) {
            if (credential.getExpiryDate() != null) {
                if (credential.getExpiryDate().compareTo(actualDate) <= 0) {
                    isCurrent = false;
                }
            }
        }

        return isCurrent;
    }
    
    public static boolean isLastCredentialExpired(Agent agent, CredentialStore store, Class<? extends CredentialStorage> storageClass) {
        return isCredentialExpired(getCurrentCredential(agent, store, storageClass));
    }

    /**
     * <p>Returns the current credential for the given {@link Agent}.</p>
     * 
     * @param agent
     * @param store
     * @param storageClass
     * @return
     */
    public static <T extends CredentialStorage> T getCurrentCredential(Agent agent, CredentialStore store,
            Class<T> storageClass) {
        T lastCredential = null;
        Date actualDate = new Date();

        for (T storedCredential : store.retrieveCredentials(agent, storageClass)) {
            if (storedCredential.getEffectiveDate().compareTo(actualDate) <= 0) {
                if (lastCredential == null || lastCredential.getEffectiveDate().compareTo(storedCredential.getEffectiveDate()) <= 0) {
                    lastCredential = storedCredential;
                }
            }
        }
        
        return lastCredential;
    }
    
    /**
     * <p>Checks if the given {@link CredentialStorage} holds an expired credential.</p>
     * 
     * @param credentialStorage
     * @return
     */
    public static boolean isCredentialExpired(CredentialStorage credentialStorage) {
        return credentialStorage != null && credentialStorage.getExpiryDate() != null && new Date().compareTo(credentialStorage.getExpiryDate()) > 0;
    }
}
