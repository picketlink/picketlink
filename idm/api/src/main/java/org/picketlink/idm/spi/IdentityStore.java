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
    void setup(T config, IdentityStoreInvocationContext context);

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
    IdentityStoreInvocationContext getContext();

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
     * Returns the Group with the specified Group ID.
     * 
     * @param ctx
     * @param groupId
     * @return
     */
    Group getGroup(String groupId);

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
