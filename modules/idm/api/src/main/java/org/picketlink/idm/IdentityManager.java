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
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;

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

    // Relationships

    /**
     * <p>
     * Adds the given {@link Relationship} instance to the configured identity store.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot add the provided {@link Relationship} instance.
     */
    void add(Relationship relationship) throws IdentityManagementException;

    /**
     * <p>
     * Updates the given {@link Relationship} instance. The instance must have an identifier, otherwise a exception will be
     * thrown.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot update the provided {@link Relationship} instance.
     */
    void update(Relationship relationship);

    /**
     * <p>
     * Removes the given {@link Relationship} instance. The instance must have an identifier, otherwise a exception will be
     * thrown.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot remove the provided {@link Relationship} instance.
     */
    void remove(Relationship relationship);

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

    /**
     * <p>
     * Checks if the given {@link IdentityType} is a member of a specific {@link Group}.
     * </p>
     *
     * @param identityType Must be a {@link Agent} or {@link Group} instance.
     * @param group
     * @return true if the {@link IdentityType} is a member of the provided {@link Group}.
     */
    boolean isMember(IdentityType identityType, Group group);

    /**
     * <p>
     * Adds the given {@link Agent} as a member of the provided {@link Group}.
     * </p>
     *
     * @param agent
     * @param group
     */
    void addToGroup(Agent agent, Group group);

    /**
     * <p>
     * Removes the given {@link Agent} from the provided {@link Group}.
     * </p>
     *
     * @param member
     * @param group
     */
    void removeFromGroup(Agent member, Group group);

    /**
     * <p>
     * Checks if the given {@link IdentityType}, {@link Role} and {@link Group} instances maps to a {@link GroupRole}
     * relationship.
     * </p>
     *
     * @param assignee
     * @param role
     * @param group
     * @return
     */
    boolean hasGroupRole(IdentityType assignee, Role role, Group group);

    /**
     * <p>
     * Creates a {@link GroupRole} relationship for the given {@link IdentityType}, {@link Role} and {@link Group} instances.
     * </p>
     *
     * @param assignee
     * @param role
     * @param group
     */
    void grantGroupRole(IdentityType assignee, Role role, Group group);

    /**
     * <p>
     * Revokes a {@link GroupRole} relationship for the given {@link IdentityType}, {@link Role} and {@link Group} instances.
     * </p>
     *
     * @param assignee
     * @param role
     * @param group
     */
    void revokeGroupRole(IdentityType assignee, Role role, Group group);

    /**
     * <p>
     * Checks if the given {@link Role} is granted to the provided {@link IdentityType}.
     * </p>
     *
     * @param identityType
     * @param role
     * @return
     */
    boolean hasRole(IdentityType identityType, Role role);

    /**
     * <p>
     * Grants the given {@link Role} to the provided {@link IdentityType}.
     * </p>
     *
     * @param identityType
     * @param role
     */
    void grantRole(IdentityType identityType, Role role);

    /**
     * <p>
     * Revokes the given {@link Role} from the provided {@link IdentityType}.
     * </p>
     *
     * @param identityType
     * @param role
     */
    void revokeRole(IdentityType identityType, Role role);

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

    /**
     * <p>
     * Creates an {@link RelationshipQuery} that can be used to query for {@link Relationship} instances.
     * </p>
     * <p>
     * The first argument tells which {@link Relationship} type should be returned. If you provide the {@link Relationship} base
     * interface any {@link Relationship} instance that matches the provided query parameters will be returned.
     * </p>
     *
     * @param identityType
     * @return
     */
    <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType);

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
