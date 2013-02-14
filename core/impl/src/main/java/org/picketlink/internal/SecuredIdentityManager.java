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

package org.picketlink.internal;

import java.util.Date;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.internal.DefaultIdentityManager;
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

/**
 * Extends the default IdentityManager implementation by providing secured identity management operations
 * 
 * @author Shane Bryzak
 *
 */
public class SecuredIdentityManager extends DefaultIdentityManager implements IdentityManager {

    // Poor man's @Veto
    public SecuredIdentityManager(String foo) {}
    
    @Override
    public void add(IdentityType value) {
       
        // TODO Auto-generated method stub
        super.add(value);
    }

    @Override
    public void update(IdentityType value) {
        // TODO Auto-generated method stub
        super.update(value);
    }

    @Override
    public void remove(IdentityType value) {
        // TODO Auto-generated method stub
        super.remove(value);
    }

    @Override
    public void add(Relationship value) {
        // TODO Auto-generated method stub
        super.add(value);
    }

    @Override
    public void update(Relationship value) {
        // TODO Auto-generated method stub
        super.update(value);
    }

    @Override
    public void remove(Relationship value) {
        // TODO Auto-generated method stub
        super.remove(value);
    }

    @Override
    public Agent getAgent(String loginName) {
        return super.getAgent(loginName);
    }

    @Override
    public User getUser(String id) {
        // TODO Auto-generated method stub
        return super.getUser(id);
    }

    @Override
    public Group getGroup(String groupId) {
        // TODO Auto-generated method stub
        return super.getGroup(groupId);
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        // TODO Auto-generated method stub
        return super.getGroup(groupName, parent);
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        // TODO Auto-generated method stub
        return super.isMember(identityType, group);
    }

    @Override
    public void addToGroup(Agent agent, Group group) {
        // TODO Auto-generated method stub
        super.addToGroup(agent, group);
    }

    @Override
    public void removeFromGroup(IdentityType identityType, Group group) {
        // TODO Auto-generated method stub
        super.removeFromGroup(identityType, group);
    }

    @Override
    public Role getRole(String name) {
        // TODO Auto-generated method stub
        return super.getRole(name);
    }

    @Override
    public boolean hasGroupRole(IdentityType identityType, Role role, Group group) {
        // TODO Auto-generated method stub
        return super.hasGroupRole(identityType, role, group);
    }

    @Override
    public void grantGroupRole(Agent agent, Role role, Group group) {
        // TODO Auto-generated method stub
        super.grantGroupRole(agent, role, group);
    }

    @Override
    public void revokeGroupRole(IdentityType identityType, Role role, Group group) {
        // TODO Auto-generated method stub
        super.revokeGroupRole(identityType, role, group);
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        return super.hasRole(identityType, role);
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        super.grantRole(identityType, role);
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        super.revokeRole(identityType, role);
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String value) {
        // TODO Auto-generated method stub
        return super.lookupIdentityById(identityType, value);
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        // TODO Auto-generated method stub
        return super.createIdentityQuery(identityType);
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        // TODO Auto-generated method stub
        return super.createRelationshipQuery(relationshipType);
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        // TODO Auto-generated method stub
        super.validateCredentials(credentials);
    }

    @Override
    public void updateCredential(Agent agent, Object value) {
        // TODO Auto-generated method stub
        super.updateCredential(agent, value);
    }

    @Override
    public void updateCredential(Agent agent, Object value, Date effectiveDate, Date expiryDate) {
        // TODO Auto-generated method stub
        super.updateCredential(agent, value, effectiveDate, expiryDate);
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) {
        // TODO Auto-generated method stub
        super.loadAttribute(identityType, attributeName);
    }

    @Override
    public void createRealm(Realm realm) {
        // TODO Auto-generated method stub
        super.createRealm(realm);
    }

    @Override
    public void removeRealm(Realm realm) {
        // TODO Auto-generated method stub
        super.removeRealm(realm);
    }

    @Override
    public Realm getRealm(String name) {
        // TODO Auto-generated method stub
        return super.getRealm(name);
    }

    @Override
    public void createTier(Tier tier) {
        // TODO Auto-generated method stub
        super.createTier(tier);
    }

    @Override
    public void removeTier(Tier tier) {
        // TODO Auto-generated method stub
        super.removeTier(tier);
    }

    @Override
    public Tier getTier(String id) {
        // TODO Auto-generated method stub
        return super.getTier(id);
    }

    @Override
    public IdentityManager forRealm(Realm realm) {
        // TODO Auto-generated method stub
        return super.forRealm(realm);
    }

    @Override
    public IdentityManager forTier(Tier tier) {
        // TODO Auto-generated method stub
        return super.forTier(tier);
    }

}
