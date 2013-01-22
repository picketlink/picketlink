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
