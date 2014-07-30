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
     * Stores the specified credential state.
     *
     * @param context The contextual invocation context.
     * @param account The account which credentials should be removed.
     * @param storage The credential storage instance to be stored.
     */
    void storeCredential(IdentityContext context, Account account, CredentialStorage storage);

    /**
     * Returns the currently active credential state of the specified {@link T}, for the specified {@link org.picketlink.idm.model.Account}.
     *
     * @param context The contextual invocation context.
     * @param account The account which credentials should be removed.
     * @param storageClass The credential storage type specifying which credential types should be removed.
     *
     * @return
     */
    <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass);

    /**
     * Returns a list of all credential state of the specified {@link T}, for the specified {@link org.picketlink.idm.model.Account}.
     *
     * @param context The contextual invocation context.
     * @param account The account which credentials should be removed.
     * @param storageClass The credential storage type specifying which credential types should be removed.
     *
     * @return
     */
    <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass);

    /**
     * <p>Removes all credentials stored by a certain {@link org.picketlink.idm.credential.storage.CredentialStorage} associated
     * with the given {@link org.picketlink.idm.model.Account}.</p>
     *
     * @param context The contextual invocation context.
     * @param account The account which credentials should be removed.
     * @param storageClass The credential storage type specifying which credential types should be removed.
     */
    void removeCredential(IdentityContext context, Account account, Class<? extends CredentialStorage> storageClass);
}