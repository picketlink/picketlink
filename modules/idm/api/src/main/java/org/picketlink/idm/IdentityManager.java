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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * Manages all Identity Management related operations.
 *
 * @author Shane Bryzak
 */
public interface IdentityManager extends Serializable {

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

    // Agent

    /**
     * <p>
     * Returns an {@link Agent} with the given <code>loginName</code>. {@link User} are also agents, so if the
     * <code>loginName</code> maps to the an {@link User} it will be returned.
     * </p>
     *
     * @param loginName
     * @throws IdentityManagementException If cannot retrieve the {@link Agent}.
     */
    Agent getAgent(String loginName) throws IdentityManagementException;

    // User

    /**
     * <p>
     * Returns an {@link User} with the given <code>loginName</code>.
     * </p>
     *
     * @param loginName
     * @return If there is no {@link User} with the given <code>loginName</code> this method returns null.
     */
    User getUser(String loginName);

    // Roles

    /**
     * <p>
     * Returns an {@link Role} with the given <code>name</code>.
     * </p>
     *
     * @param loginName
     * @return If there is no {@link Role} with the given <code>name</code> this method returns null.
     */
    Role getRole(String name);

    // Group

    /**
     * <p>
     * Returns the {@link Group} with the specified <code>groupPath</code>. Eg.: /groupA/groupB/groupC.
     * </p>
     * <p>
     * You can also provide the name only. In this case, the group returned will be the root group. Eg.: /Administrators.
     * </p>
     *
     * @param groupPath
     * @return if there is no {@link Group} with the given <code>groupPath</code> this method returns null.
     */
    Group getGroup(String groupPath);

    /**
     * <p>
     * Returns the {@link Group} with the given name and child of the given parent {@link Group}.
     * </p>
     *
     * @param groupName
     * @param parent Must be a {@link Group} instance with a valid identifier.
     * @return if there is no {@link Group} this method returns null.
     */
    Group getGroup(String groupName, Group parent);

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
     * Updates a credential for the given {@link Agent}.
     * </p>
     *
     * @param agent
     * @param credential The <code>credential</code> must be a object supported by any {@link CredentialHandler}. Examples of
     *        credentials are the {@link Password} and {@link Digest} types.
     */
    void updateCredential(Agent agent, Object credential);

    /**
     * <p>
     * Updates a credential for the given {@link Agent}.
     * </p>
     * <p>
     * This methods also allows to specify the expiration and effective date for the credential.
     * </p>
     *
     * @param agent
     * @param credential The <code>credential</code> must be a object supported by any {@link CredentialHandler}. Examples of
     *        credentials are the {@link Password} and {@link Digest} types.
     */
    void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate);

    /**
     * Returns the current stored credential value for the specific agent and credential storage class
     *
     * @param agent
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass);

    /**
     * Returns a list of all stored credential values for the specified agent and credential storage class
     *
     * @param agent
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass);

    // Attributes

    /**
     *
     * @param identityType
     * @param attributeName
     */
    void loadAttribute(IdentityType identityType, String attributeName);

}
