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
package org.picketlink.idm.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.internal.util.RelationshipMetadata;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.StoreSelector;

/**
 * Default implementation for RelationshipManager.
 *
 * This class is not thread-safe!
 *
 * @author Shane Bryzak
 *
 */
public class ContextualRelationshipManager extends AbstractIdentityContext implements RelationshipManager {

    private StoreSelector storeSelector;
    private RelationshipMetadata relationshipMetadata;

    public ContextualRelationshipManager(EventBridge eventBridge, IdGenerator idGenerator, StoreSelector storeSelector,
            RelationshipMetadata relationshipMetadata) {
        super(null, eventBridge, idGenerator);
        this.storeSelector = storeSelector;
        this.relationshipMetadata = relationshipMetadata;
    }


    @Override
    public void add(Relationship relationship) throws IdentityManagementException {
        storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(),
                relationshipMetadata.getRelationshipPartitions(relationship)).add(this, relationship);
    }

    @Override
    public void update(Relationship relationship) {
        storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(),
                relationshipMetadata.getRelationshipPartitions(relationship)).update(this, relationship);
    }

    @Override
    public void remove(Relationship relationship) {
        storeSelector.getStoreForRelationshipOperation(this, relationship.getClass(),
                relationshipMetadata.getRelationshipPartitions(relationship)).remove(this, relationship);
    }

    @Override
    public boolean isMember(IdentityType identity, Group group) {
        RelationshipQuery<GroupMembership> query = createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, identity);
        query.setParameter(GroupMembership.GROUP, getGroups(group));

        return !query.getResultList().isEmpty();
    }

    private Group[] getGroups(Group group) {
        List<Group> groups = new ArrayList<Group>();

        groups.add(group);

        if (group.getParentGroup() != null) {
            groups.addAll(Arrays.asList(getGroups(group.getParentGroup())));
        }

        return groups.toArray(new Group[groups.size()]);
    }

    @Override
    public void addToGroup(Account member, Group group) {
        add(new GroupMembership(member, group));
    }

    @Override
    public void removeFromGroup(Account member, Group group) {
        RelationshipQuery<GroupMembership> query = createRelationshipQuery( GroupMembership.class);
        query.setParameter(GroupMembership.MEMBER, member);
        query.setParameter(GroupMembership.GROUP, group);
        for (GroupMembership membership : query.getResultList()) {
            remove(membership);
        }
    }

    @Override
    public boolean hasGroupRole(IdentityType assignee, Role role, Group group) {
        RelationshipQuery<GroupRole> query = createRelationshipQuery(GroupRole.class);

        query.setParameter(GroupRole.ASSIGNEE, assignee);
        query.setParameter(GroupRole.GROUP, getGroups(group));
        query.setParameter(GroupRole.ROLE, role);

        return !query.getResultList().isEmpty();
    }

    @Override
    public void grantGroupRole(IdentityType assignee, Role role, Group group) {
        add(new GroupRole(assignee, group, role));
    }

    @Override
    public void revokeGroupRole(IdentityType assignee, Role role, Group group) {
        RelationshipQuery<GroupRole> query = createRelationshipQuery(GroupRole.class);
        query.setParameter(GroupRole.ASSIGNEE, assignee);
        query.setParameter(GroupRole.GROUP, group);
        query.setParameter(GroupRole.ROLE, role);
        for (GroupRole groupRole : query.getResultList()) {
            remove(groupRole);
        }
    }

    @Override
    public boolean hasRole(IdentityType assignee, Role role) {
        RelationshipQuery<Grant> query = createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, assignee);
        query.setParameter(GroupRole.ROLE, role);

        return !query.getResultList().isEmpty();
    }

    @Override
    public void grantRole(IdentityType assignee, Role role) {
        add(new Grant(assignee, role));
    }

    @Override
    public void revokeRole(IdentityType assignee, Role role) {
        RelationshipQuery<Grant> query = createRelationshipQuery(Grant.class);
        query.setParameter(Grant.ASSIGNEE, assignee);
        query.setParameter(GroupRole.ROLE, role);
        for (Grant grant : query.getResultList()) {
            remove(grant);
        }
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipClass) {
        return new DefaultRelationshipQuery<T>(this, relationshipClass, storeSelector);
    }
}
