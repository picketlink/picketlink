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
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * IdentityStore representation providing minimal SPI
 *
 * @author Boleslaw Dawidowicz
 * @author Shane Bryzak
 */
public interface IdentityStore<T extends IdentityStoreConfiguration> {

    /**
     * Sets the configuration and context in which the IdentityStore will execute its operations
     *
     * @param config
     * @param context
     */
    void setup(T config);

    /**
     * Returns the configuration for this IdentityStore instance
     *
     * @return
     */
    T getConfig();

    // General

    /**
     * Persists the specified IdentityType
     *
     * @param identityType
     */
    void add(SecurityContext context, AttributedType value);

    /**
     * Updates the specified IdentityType
     *
     * @param identityType
     */
    void update(SecurityContext context, AttributedType value);

    /**
     * Removes the specified IdentityType
     *
     * @param identityType
     */
    void remove(SecurityContext context, AttributedType value);

    // Agent

    /**
     * Returns the Agent with the specified login name
     *
     * @param context
     * @param loginName
     * @return
     */
    Agent getAgent(SecurityContext context, String loginName);

    // User

    /**
     * Returns the User with the specified login name.
     *
     * @param context
     * @param loginName
     * @return
     */
    User getUser(SecurityContext context, String loginName);

    // Group

    /**
     * <p>Returns the {@link Group} with the specified path. Eg.: /groupA/groupB/groupC.</p>
     *
     * @param ctx
     * @param groupPath
     * @return
     */
    Group getGroup(SecurityContext context, String groupPath);

    /**
     * Returns the Group with the specified name and parent group
     *
     * @param ctx
     * @param name The name of the Group to return
     * @return
     */
    Group getGroup(SecurityContext context, String name, Group parent);

    // Role

    /**
     * Returns the specified role
     *
     * @param ctx
     * @param name The name of the Role to return
     * @return A Role instance, or null if the Role with the specified name wasn't found
     */
    Role getRole(SecurityContext context, String name);

    // Identity query

    <V extends IdentityType> List<V> fetchQueryResults(SecurityContext context, IdentityQuery<V> identityQuery);

    <V extends IdentityType> int countQueryResults(SecurityContext context, IdentityQuery<V> identityQuery);

    // Relationship query

    <V extends Relationship> List<V> fetchQueryResults(SecurityContext context, RelationshipQuery<V> query);

    <V extends Relationship> int countQueryResults(SecurityContext context, RelationshipQuery<V> query);

    // Attributes

    /**
     * Sets the specified Attribute value for the specified IdentityType
     *
     * @param ctx
     * @param identityType
     * @param attribute
     */
    void setAttribute(SecurityContext context, IdentityType identityType,
            Attribute<? extends Serializable> attribute);

    /**
     * Returns the Attribute value with the specified name, for the specified IdentityType
     * @param ctx
     * @param identityType
     * @param attributeName
     * @return
     */
    <V extends Serializable> Attribute<V> getAttribute(SecurityContext context, IdentityType identityType, String attributeName);

    /**
     * Removes the specified Attribute value, for the specified IdentityType
     *
     * @param ctx
     * @param identityType
     * @param attributeName
     */
    void removeAttribute(SecurityContext context, IdentityType identityType, String attributeName);

    // Credentials

    /**
     * Validates the specified credentials.  Each IdentityStore implementation typically supports
     * a concrete set of Credentials types, and will generally obtain a CredentialHandler instance
     * from the IdentityStoreInvocationContext to process credential validation.
     *
     * @param credentials
     */
    void validateCredentials(SecurityContext context, Credentials credentials);

    /**
     * Updates the specified credential value for the specified Agent.
     *
     * @param agent
     * @param credential
     */
    void updateCredential(SecurityContext context, Agent agent, Object credential, Date effectiveDate, Date expiryDate);

}
