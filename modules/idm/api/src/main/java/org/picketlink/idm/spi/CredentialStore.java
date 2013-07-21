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

package org.picketlink.idm.spi;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.model.Account;

import java.util.List;

/**
 * A special type of IdentityStore that supports the storage of raw credential state also
 *
 * @author Shane Bryzak
 *
 */
public interface CredentialStore<T extends IdentityStoreConfiguration> extends IdentityStore<T> {

    /**
     * Store the specified credential state
     *
     * @param storage
     */
    void storeCredential(IdentityContext context, Account account, CredentialStorage storage);

    /**
     * Return the currently active credential state of the specified class, for the specified Account
     *
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass);

    /**
     * Returns a List of all credential state of the specified class, for the specified Account
     *
     * @param account
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass);

}