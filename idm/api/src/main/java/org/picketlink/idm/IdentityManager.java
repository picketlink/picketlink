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

import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Manages all Identity Management related operations.
 * 
 * @author Shane Bryzak
 */
public interface IdentityManager extends Serializable {

    /**
     * This method must be invoked to set up the IdentityManager instance before any identity management operations may be
     * performed.
     * 
     * @param configuration
     */
    void bootstrap(IdentityConfiguration configuration, IdentityStoreInvocationContextFactory contextFactory);

    /**
     * Sets the IdentityStoreFactory implementation to be used to create IdentityStore instances
     * 
     * @param factory
     */
    void setIdentityStoreFactory(StoreFactory factory);

    // Identity CRUD methods

    void add(IdentityType value);

    void update(IdentityType value);

    void remove(IdentityType value);

    // Relationships

    void add(Relationship value);

    void update(Relationship value);

    void remove(Relationship value);

    // Agent

    Agent getAgent(String id);

    // User

    User getUser(String id);

    // Group

    /**
     * <p>
     * Returns the {@link Group} with the specified path. Eg.: /groupA/groupB/groupC.
     * </p>
     * 
     * @param groupPath
     * @return
     */
    Group getGroup(String groupPath);

    /**
     * <p>
     * Returns a {@link Group} with the specified name and parent group. The parent group must be a valid/stored group with a
     * valid identifier.
     * </p>
     * 
     * @param groupName
     * @param parent
     * @return
     */
    Group getGroup(String groupName, Group parent);

    boolean isMember(IdentityType identityType, Group group);

    void addToGroup(Agent agent, Group group);

    void removeFromGroup(IdentityType identityType, Group group);

    // Roles

    Role getRole(String name);

    boolean hasGroupRole(IdentityType identityType, Role role, Group group);

    void grantGroupRole(Agent agent, Role role, Group group);

    void revokeGroupRole(IdentityType identityType, Role role, Group group);

    boolean hasRole(IdentityType identityType, Role role);

    void grantRole(IdentityType identityType, Role role);

    void revokeRole(IdentityType identityType, Role role);

    // Query API

    <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String value);

    <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType);

    <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType);

    // Credential management

    void validateCredentials(Credentials credentials);

    void updateCredential(Agent agent, Object value);

    void updateCredential(Agent agent, Object value, Date effectiveDate, Date expiryDate);

    // Attributes

    void loadAttribute(IdentityType identityType, String attributeName);

    // Realm

    void createRealm(Realm realm);

    void removeRealm(Realm realm);

    Realm getRealm(String name);

    // Tier

    void createTier(Tier tier);

    void removeTier(Tier tier);

    Tier getTier(String id);

    // Context

    IdentityManager forRealm(Realm realm);

    IdentityManager forTier(Tier tier);

}
