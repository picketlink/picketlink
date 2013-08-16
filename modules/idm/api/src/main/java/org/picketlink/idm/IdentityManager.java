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
package org.picketlink.idm;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;

import java.util.Date;
import java.util.List;

/**
 * <p>Manages all Identity Management related operations.</p>
 *
 * @author Shane Bryzak
 */
public interface IdentityManager {

    /**
     * The active IdentityManager instance may be stored in the IdentityContext under this parameter name
     */
    String IDENTITY_MANAGER_CTX_PARAMETER = "IDENTITY_MANAGER_CTX_PARAMETER";

    // Identity CRUD methods

    /**
     * <p>
     * Adds the given {@link IdentityType} instance to the configured identity store.
     * </p>
     *
     * @param identityType
     * @throws IdentityManagementException If cannot store the provided {@link IdentityType} instance.
     */
    void add(IdentityType identityType) throws IdentityManagementException;

    /**
     * <p>
     * Updates the given {@link IdentityType} instance. The instance must have an identifier, otherwise a exception will be
     * thrown.
     * </p>
     *
     * @param identityType
     * @throws IdentityManagementException If cannot update the provided {@link IdentityType} instance.
     */
    void update(IdentityType identityType) throws IdentityManagementException;

    /**
     * <p>
     * Removes the given {@link IdentityType} instance from the configured identity store. The instance must have an identifier,
     * otherwise a exception will be thrown.
     * </p>
     *
     * @param value
     * @throws IdentityManagementException If cannot remove the provided {@link IdentityType} instance.
     */
    void remove(IdentityType value) throws IdentityManagementException;

    // Query API

    /**
     * <p>
     * Retrieves an {@link IdentityType} with the given identifier.
     * </p>
     * <p>
     * The first argument tells which {@link IdentityType} type should be returned. If you provide the {@link IdentityType} base
     * interface any {@link IdentityType} instance that matches the given identifier will be returned.
     * </p>
     *
     * @param identityType
     * @param id
     * @return If no {@link IdentityType} is found with the given identifier this method returns null.
     */
    <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id);

    /**
     * <p>
     * Creates an {@link IdentityQuery} that can be used to query for {@link IdentityType} instances.
     * </p>
     * <p>
     * The first argument tells which {@link IdentityType} type should be returned. If you provide the {@link IdentityType} base
     * interface any {@link IdentityType} instance that matches the provided query parameters will be returned.
     * </p>
     *
     * @param identityType
     * @return
     */
    <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType);

    // Credential management

    /**
     * <p>
     * Validates the given {@link Credentials}.
     * </p>
     * <p>
     * To check the validation status you should use the <code>Credentials.getStatus</code> method.
     * </p>
     *
     * @param credentials
     */
    void validateCredentials(Credentials credentials);

    /**
     * <p>
     * Updates a credential for the given {@link Account}.
     * </p>
     *
     * @param agent
     * @param credential The <code>credential</code> must be a object supported by any {@link CredentialHandler}. Examples of
     *        credentials are the {@link Password} and {@link Digest} types.
     */
    void updateCredential(Account account, Object credential);

    /**
     * <p>
     * Updates a credential for the given {@link Account}.
     * </p>
     * <p>
     * This methods also allows to specify the expiration and effective date for the credential.
     * </p>
     *
     * @param agent
     * @param credential The <code>credential</code> must be a object supported by any {@link CredentialHandler}. Examples of
     *        credentials are the {@link Password} and {@link Digest} types.
     */
    void updateCredential(Account account, Object credential, Date effectiveDate, Date expiryDate);

    /**
     * Returns the current stored credential value for the specific account and credential storage class
     *
     * @param agent
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> T retrieveCurrentCredential(Account account, Class<T> storageClass);

    /**
     * Returns a list of all stored credential values for the specified account and credential storage class
     *
     * @param agent
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> List<T> retrieveCredentials(Account account, Class<T> storageClass);

}
