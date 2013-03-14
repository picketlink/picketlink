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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;

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
    void setup(T config, SecurityContext context);

    /**
     * Returns the configuration for this IdentityStore instance
     *
     * @return
     */
    T getConfig();

    /**
     * Returns the current context for this IdentityStore instance
     *
     * @return
     */
    SecurityContext getContext();

    // General

    /**
     * Persists the specified IdentityType
     *
     * @param identityType
     */
    void add(AttributedType value);

    /**
     * Updates the specified IdentityType
     *
     * @param identityType
     */
    void update(AttributedType value);

    /**
     * Removes the specified IdentityType
     *
     * @param identityType
     */
    void remove(AttributedType value);

    // Agent

    Agent getAgent(String id);

    // User

    /**
     * Returns the User with the specified id value.
     *
     * @param ctx
     * @param id
     * @return
     */
    User getUser(String id);

    // Group

    /**
     * <p>Returns the {@link Group} with the specified path. Eg.: /groupA/groupB/groupC.</p>
     *
     * @param ctx
     * @param groupPath
     * @return
     */
    Group getGroup(String groupPath);

    /**
     * Returns the Group with the specified name and parent group
     *
     * @param ctx
     * @param name The name of the Group to return
     * @return
     */
    Group getGroup(String name, Group parent);

    // Role

    /**
     * Returns the specified role
     *
     * @param ctx
     * @param name The name of the Role to return
     * @return A Role instance, or null if the Role with the specified name wasn't found
     */
    Role getRole(String name);

    // Identity query

    <V extends IdentityType> List<V> fetchQueryResults(IdentityQuery<V> identityQuery);

    <V extends IdentityType> int countQueryResults(IdentityQuery<V> identityQuery);

    // Relationship query

    <V extends Relationship> List<V> fetchQueryResults(RelationshipQuery<V> query);

    <V extends Relationship> int countQueryResults(RelationshipQuery<V> query);

    // Attributes

    /**
     * Sets the specified Attribute value for the specified IdentityType
     *
     * @param ctx
     * @param identityType
     * @param attribute
     */
    void setAttribute(IdentityType identityType,
            Attribute<? extends Serializable> attribute);

    /**
     * Returns the Attribute value with the specified name, for the specified IdentityType
     * @param ctx
     * @param identityType
     * @param attributeName
     * @return
     */
    <V extends Serializable> Attribute<V> getAttribute(IdentityType identityType, String attributeName);

    /**
     * Removes the specified Attribute value, for the specified IdentityType
     *
     * @param ctx
     * @param identityType
     * @param attributeName
     */
    void removeAttribute(IdentityType identityType, String attributeName);

    // Credentials

    /**
     * Validates the specified credentials.  Each IdentityStore implementation typically supports
     * a concrete set of Credentials types, and will generally obtain a CredentialHandler instance
     * from the IdentityStoreInvocationContext to process credential validation.
     *
     * @param credentials
     */
    void validateCredentials(Credentials credentials);

    /**
     * Updates the specified credential value for the specified Agent.
     *
     * @param agent
     * @param credential
     */
    void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate);

}
