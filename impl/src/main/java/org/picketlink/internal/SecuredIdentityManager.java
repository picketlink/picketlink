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
import java.util.List;

import javax.enterprise.inject.Typed;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;

/**
 * Decorator for IdentityManager that provides secured identity management operations
 * 
 * @author Shane Bryzak
 *
 */
@Typed(SecuredIdentityManager.class)
public class SecuredIdentityManager implements IdentityManager {
    private static final long serialVersionUID = -8197103563768366958L;

    private IdentityManager decorated;

    public SecuredIdentityManager(IdentityManager decorated) {
        this.decorated = decorated;
    }

    @Override
    public void add(IdentityType value) {
        decorated.add(value);
    }

    @Override
    public void update(IdentityType value) {
        decorated.update(value);
    }

    @Override
    public void remove(IdentityType value) {
        decorated.remove(value);
    }

    @Override
    public void add(Relationship value) {
        decorated.add(value);
    }

    @Override
    public void update(Relationship value) {
        decorated.update(value);
    }

    @Override
    public void remove(Relationship value) {
        decorated.remove(value);
    }

    @Override
    public Agent getAgent(String loginName) {
        return decorated.getAgent(loginName);
    }

    @Override
    public User getUser(String id) {
        return decorated.getUser(id);
    }

    @Override
    public Group getGroup(String groupId) {
        return decorated.getGroup(groupId);
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        return decorated.getGroup(groupName, parent);
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        return decorated.isMember(identityType, group);
    }

    @Override
    public void addToGroup(Agent identityType, Group group) {
        decorated.addToGroup(identityType, group);
    }

    @Override
    public void removeFromGroup(Agent member, Group group) {
        decorated.removeFromGroup(member, group);
    }

    @Override
    public Role getRole(String name) {
        return decorated.getRole(name);
    }

    @Override
    public boolean hasGroupRole(IdentityType identityType, Role role, Group group) { 
        return decorated.hasGroupRole(identityType, role, group);
    }
 
    @Override
    public void grantGroupRole(IdentityType identityType, Role role, Group group) {
        decorated.grantGroupRole(identityType, role, group);
    }

    @Override
    public void revokeGroupRole(IdentityType member, Role role, Group group) {
        decorated.revokeGroupRole(member, role, group);
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) { 
        return decorated.hasRole(identityType, role);
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) { 
        decorated.grantRole(identityType, role);
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) { 
        decorated.revokeRole(identityType, role);
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String value) { 
        return decorated.lookupIdentityById(identityType, value);
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) { 
        return decorated.createIdentityQuery(identityType);
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) { 
        return decorated.createRelationshipQuery(relationshipType);
    }

    @Override
    public void validateCredentials(Credentials credentials) { 
        decorated.validateCredentials(credentials);
    }

    @Override
    public void updateCredential(Agent agent, Object value) { 
        decorated.updateCredential(agent, value);
    }

    @Override
    public void updateCredential(Agent agent, Object value, Date effectiveDate, Date expiryDate) { 
        decorated.updateCredential(agent, value, effectiveDate, expiryDate);
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) { 
        decorated.loadAttribute(identityType, attributeName);
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        return decorated.retrieveCurrentCredential(agent, storageClass);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
        return decorated.retrieveCredentials(agent, storageClass);
    }
}