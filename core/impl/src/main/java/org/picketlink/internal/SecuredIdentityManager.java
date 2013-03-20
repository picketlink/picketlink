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

import javax.enterprise.inject.Typed;

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
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Extends the default IdentityManager implementation by providing secured identity management operations
 * 
 * @author Shane Bryzak
 *
 */
@Typed(SecuredIdentityManager.class)
public class SecuredIdentityManager extends DefaultIdentityManager implements IdentityManager {

    public SecuredIdentityManager(SecurityContext context, StoreFactory storeFactory) {
        super(context, storeFactory);
    }

    private static final long serialVersionUID = -8197103563768366958L;

    @Override
    public void add(IdentityType value) {
        super.add(value);
    }

    @Override
    public void update(IdentityType value) {
        super.update(value);
    }

    @Override
    public void remove(IdentityType value) {
        super.remove(value);
    }

    @Override
    public void add(Relationship value) {
        super.add(value);
    }

    @Override
    public void update(Relationship value) {
        super.update(value);
    }

    @Override
    public void remove(Relationship value) {
        super.remove(value);
    }

    @Override
    public Agent getAgent(String loginName) {
        return super.getAgent(loginName);
    }

    @Override
    public User getUser(String id) {
        return super.getUser(id);
    }

    @Override
    public Group getGroup(String groupId) {
        return super.getGroup(groupId);
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        return super.getGroup(groupName, parent);
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        return super.isMember(identityType, group);
    }

    @Override
    public void addToGroup(Agent identityType, Group group) {
        super.addToGroup(identityType, group);
    }

    @Override
    public void removeFromGroup(Agent member, Group group) {
        super.removeFromGroup(member, group);
    }

    @Override
    public Role getRole(String name) {
        return super.getRole(name);
    }

    @Override
    public boolean hasGroupRole(IdentityType identityType, Role role, Group group) { 
        return super.hasGroupRole(identityType, role, group);
    }
 
    @Override
    public void grantGroupRole(IdentityType identityType, Role role, Group group) {
        super.grantGroupRole(identityType, role, group);
    } 
    
    @Override
    public void revokeGroupRole(IdentityType member, Role role, Group group) {
        super.revokeGroupRole(member, role, group);
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) { 
        return super.hasRole(identityType, role);
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) { 
        super.grantRole(identityType, role);
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) { 
        super.revokeRole(identityType, role);
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String value) { 
        return super.lookupIdentityById(identityType, value);
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) { 
        return super.createIdentityQuery(identityType);
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) { 
        return super.createRelationshipQuery(relationshipType);
    }

    @Override
    public void validateCredentials(Credentials credentials) { 
        super.validateCredentials(credentials);
    }

    @Override
    public void updateCredential(Agent agent, Object value) { 
        super.updateCredential(agent, value);
    }

    @Override
    public void updateCredential(Agent agent, Object value, Date effectiveDate, Date expiryDate) { 
        super.updateCredential(agent, value, effectiveDate, expiryDate);
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) { 
        super.loadAttribute(identityType, attributeName);
    }
}