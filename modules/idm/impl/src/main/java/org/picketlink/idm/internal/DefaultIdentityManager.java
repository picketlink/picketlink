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

import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.internal.util.IDMUtil.getFeatureGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Default implementation of the IdentityManager interface
 *
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class DefaultIdentityManager implements IdentityManager {

    private static final long serialVersionUID = -2835518073812662628L;

    private SecurityContext context;
    private StoreFactory storeFactory;

    public DefaultIdentityManager(SecurityContext context, StoreFactory storeFactory) {
        this.context = context;
        this.storeFactory = storeFactory;
        context.setIdentityManager(this);
    }

    @Override
    public void add(IdentityType identityType) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (Agent.class.isInstance(identityType)) {
            checkCurrentPartitionForAgents();

            Agent newAgent = (Agent) identityType;

            if (StringUtil.isNullOrEmpty(newAgent.getLoginName())) {
                throw MESSAGES.nullArgument("User loginName");
            }

            if (User.class.isInstance(newAgent)) {
                if (getUser(newAgent.getLoginName()) != null) {
                    throw MESSAGES.identityTypeAlreadyExists(newAgent.getClass(), newAgent.getLoginName(),
                            context.getPartition());
                }
            } else {
                if (getAgent(newAgent.getLoginName()) != null) {
                    throw MESSAGES.identityTypeAlreadyExists(newAgent.getClass(), newAgent.getLoginName(),
                            context.getPartition());
                }
            }
        } else if (Group.class.isInstance(identityType)) {
            Group newGroup = (Group) identityType;

            if (StringUtil.isNullOrEmpty(newGroup.getName())) {
                throw MESSAGES.nullArgument("Group name");
            }

            if (getGroup(newGroup.getPath()) != null) {
                throw MESSAGES.identityTypeAlreadyExists(newGroup.getClass(), newGroup.getName(), context.getPartition());
            }

            if (newGroup.getParentGroup() != null) {
                if (lookupIdentityById(Group.class, newGroup.getParentGroup().getId()) == null) {
                    throw MESSAGES.groupParentNotFoundWithId(newGroup.getParentGroup().getId(), context.getPartition());
                }
            }
        } else if (Role.class.isInstance(identityType)) {
            Role newRole = (Role) identityType;

            if (StringUtil.isNullOrEmpty(newRole.getName())) {
                throw MESSAGES.nullArgument("Role name");
            }

            if (getRole(newRole.getName()) != null) {
                throw MESSAGES.identityTypeAlreadyExists(newRole.getClass(), newRole.getName(), context.getPartition());
            }
        }

        try {
            storeFactory.getStoreForFeature(context, getFeatureGroup(identityType.getClass()), FeatureOperation.create).add(
                    context, identityType);
        } catch (Exception e) {
            throw MESSAGES.identityTypeAddFailed(identityType, e);
        }
    }

    @Override
    public void add(Relationship relationship) {
        try {
            storeFactory.getStoreForFeature(context, FeatureGroup.relationship, FeatureOperation.create,
                    relationship.getClass()).add(context, relationship);
        } catch (Exception e) {
            throw MESSAGES.relationshipAddFailed(relationship, e);
        }
    }

    @Override
    public void update(IdentityType identityType) {
        checkIfIdentityTypeExists(identityType);

        if (Agent.class.isInstance(identityType)) {
            checkCurrentPartitionForAgents();
        }

        try {
            storeFactory.getStoreForFeature(context, getFeatureGroup(identityType.getClass()), FeatureOperation.update).update(
                    context, identityType);
        } catch (Exception e) {
            throw MESSAGES.identityTypeUpdateFailed(identityType, e);
        }
    }

    @Override
    public void update(Relationship relationship) {
        try {
            storeFactory.getStoreForFeature(context, FeatureGroup.relationship, FeatureOperation.update,
                    relationship.getClass()).update(context, relationship);
        } catch (Exception e) {
            throw MESSAGES.relationshipUpdateFailed(relationship, e);
        }
    }

    @Override
    public void remove(IdentityType identityType) {
        checkIfIdentityTypeExists(identityType);

        if (Agent.class.isInstance(identityType)) {
            checkCurrentPartitionForAgents();
        }

        try {
            storeFactory.getStoreForFeature(context, getFeatureGroup(identityType.getClass()), FeatureOperation.delete).remove(
                    context, identityType);
        } catch (Exception e) {
            throw MESSAGES.identityTypeUpdateFailed(identityType, e);
        }
    }

    @Override
    public void remove(Relationship relationship) {
        if (relationship == null) {
            MESSAGES.nullArgument("Relationship");
        }

        try {
            storeFactory.getStoreForFeature(context, FeatureGroup.relationship, FeatureOperation.delete,
                    relationship.getClass()).remove(context, relationship);
        } catch (Exception e) {
            throw MESSAGES.relationshipRemoveFailed(relationship, e);
        }
    }

    public Agent getAgent(String loginName) {
        checkCurrentPartitionForAgents();

        return storeFactory.getStoreForFeature(context, FeatureGroup.agent, FeatureOperation.read).getAgent(context, loginName);
    }

    @Override
    public User getUser(String loginName) {
        checkCurrentPartitionForAgents();

        return storeFactory.getStoreForFeature(context, FeatureGroup.user, FeatureOperation.read).getUser(context, loginName);
    }

    @Override
    public Group getGroup(String path) {
        if (StringUtil.isNullOrEmpty(path)) {
            return null;
        }

        return storeFactory.getStoreForFeature(context, FeatureGroup.group, FeatureOperation.read).getGroup(context, path);
    }

    @Override
    public Group getGroup(String name, Group parent) {
        if (StringUtil.isNullOrEmpty(name) || parent == null) {
            return null;
        }

        if (name.startsWith("/")) {
            throw new IdentityManagementException("You should provide a group name and not a path");
        }

        if (lookupIdentityById(Group.class, parent.getId()) == null) {
            throw MESSAGES.groupParentNotFoundWithId(parent.getId(), context.getPartition());
        }

        return storeFactory.getStoreForFeature(context, FeatureGroup.group, FeatureOperation.read).getGroup(context, name,
                parent);
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        if (identityType == null) {
            MESSAGES.nullArgument("IdentityType");
        }

        if (group == null) {
            MESSAGES.nullArgument("Group");
        }

        boolean isMember = false;

        if (Agent.class.isInstance(identityType)) {
            isMember = getGroupMembership(identityType, group) != null;
        } else if (Group.class.isInstance(identityType)) {
            Group memberGroup = (Group) identityType;

            if (memberGroup.getId() != null) {
                memberGroup = lookupIdentityById(Group.class, memberGroup.getId());

                if (memberGroup != null) {
                    isMember = memberGroup.getPath().contains(group.getPath());
                }
            }
        } else {
            throw MESSAGES.relationshipUnsupportedGroupMemberType(identityType);
        }

        return isMember;
    }

    @Override
    public void addToGroup(Agent member, Group group) {
        checkIfIdentityTypeExists(member);
        checkIfIdentityTypeExists(group);

        if (getGroupMembership(member, group) == null) {
            add(new GroupMembership(member, group));
        }
    }

    @Override
    public void removeFromGroup(Agent member, Group group) {
        checkIfIdentityTypeExists(member);
        checkIfIdentityTypeExists(group);

        storeFactory.getStoreForFeature(context, FeatureGroup.relationship, FeatureOperation.delete, GroupMembership.class)
                .remove(context, new GroupMembership(member, group));
    }

    @Override
    public Role getRole(String name) {
        return storeFactory.getStoreForFeature(context, FeatureGroup.role, FeatureOperation.read).getRole(context, name);
    }

    @Override
    public boolean hasGroupRole(IdentityType assignee, Role role, Group group) {
        if (assignee == null) {
            MESSAGES.nullArgument("IdentityType");
        }

        if (role == null) {
            MESSAGES.nullArgument("Role");
        }

        if (group == null) {
            MESSAGES.nullArgument("Group");
        }

        return getGroupRole(assignee, role, group) != null;
    }

    @Override
    public void grantGroupRole(IdentityType assignee, Role role, Group group) {
        checkIfIdentityTypeExists(assignee);
        checkIfIdentityTypeExists(role);
        checkIfIdentityTypeExists(group);

        if (getGroupRole(assignee, role, group) == null) {
            add(new GroupRole(assignee, group, role));
        }
    }

    @Override
    public void revokeGroupRole(IdentityType assignee, Role role, Group group) {
        checkIfIdentityTypeExists(assignee);
        checkIfIdentityTypeExists(role);
        checkIfIdentityTypeExists(group);

        storeFactory.getStoreForFeature(context, FeatureGroup.relationship, FeatureOperation.delete, GroupRole.class).remove(
                context, new GroupRole(assignee, group, role));
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        if (identityType == null) {
            MESSAGES.nullArgument("IdentityType");
        }

        if (role == null) {
            MESSAGES.nullArgument("Role");
        }

        if (Role.class.isInstance(identityType)) {
            throw MESSAGES.relationshipUnsupportedGrantAssigneeType(identityType);
        }

        return getGrant(identityType, role) != null;
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        if (Role.class.isInstance(identityType)) {
            throw MESSAGES.relationshipUnsupportedGrantAssigneeType(identityType);
        }

        checkIfIdentityTypeExists(identityType);
        checkIfIdentityTypeExists(role);

        if (getGrant(identityType, role) == null) {
            add(new Grant(identityType, role));
        }
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        if (Role.class.isInstance(identityType)) {
            throw MESSAGES.relationshipUnsupportedGrantAssigneeType(identityType);
        }

        checkIfIdentityTypeExists(identityType);
        checkIfIdentityTypeExists(role);

        storeFactory.getStoreForFeature(context, FeatureGroup.relationship, FeatureOperation.delete, Grant.class).remove(
                context, new Grant(identityType, role));
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        checkCurrentPartitionForCredential();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.credential, FeatureOperation.validate);

        store.validateCredentials(context, credentials);
    }

    @Override
    public void updateCredential(Agent agent, Object value) {
        updateCredential(agent, value, new Date(), null);
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        checkCurrentPartitionForCredential();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.credential, FeatureOperation.update);

        store.updateCredential(context, agent, credential, effectiveDate, expiryDate);
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        return new DefaultIdentityQuery<T>(context, identityType, storeFactory.getStoreForFeature(context,
                getFeatureGroup(identityType), FeatureOperation.read));
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        return new DefaultRelationshipQuery<T>(context, relationshipType, storeFactory.getStoreForFeature(context,
                FeatureGroup.relationship, FeatureOperation.read, relationshipType));
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType class");
        }

        if (id == null) {
            throw MESSAGES.nullArgument("Identifier for [" + identityType + "]");
        }

        List<T> result = Collections.emptyList();

        if (IdentityType.class.equals(identityType)) {
            List<Class<? extends IdentityType>> types = new ArrayList<Class<? extends IdentityType>>();

            types.add(User.class);
            types.add(Agent.class);
            types.add(Group.class);
            types.add(Role.class);

            for (Class<? extends IdentityType> childType : types) {
                IdentityQuery<T> query = (IdentityQuery<T>) createIdentityQuery(childType);

                query.setParameter(IdentityType.ID, id);

                result = query.getResultList();

                if (!result.isEmpty()) {
                    break;
                }
            }
        } else {
            IdentityQuery<T> query = (IdentityQuery<T>) createIdentityQuery(identityType);

            query.setParameter(IdentityType.ID, id);

            result = query.getResultList();
        }

        T identity = null;

        if (!result.isEmpty()) {
            if (result.size() > 1) {
                throw MESSAGES.identityTypeAmbiguosFoundWithId(id);
            } else {
                identity = result.get(0);
            }
        }

        return identity;
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) {

    }

    private GroupRole getGroupRole(IdentityType identityType, Role role, Group group) {
        RelationshipQuery<GroupRole> query = createRelationshipQuery(GroupRole.class);

        query.setParameter(GroupRole.ASSIGNEE, identityType);
        query.setParameter(GroupRole.ROLE, role);
        query.setParameter(GroupRole.GROUP, group);

        List<GroupRole> result = query.getResultList();

        GroupRole groupRole = null;

        if (!result.isEmpty()) {
            groupRole = result.get(0);
        }
        return groupRole;
    }

    private GroupMembership getGroupMembership(IdentityType identityType, Group group) {
        RelationshipQuery<GroupMembership> query = createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, identityType);
        query.setParameter(GroupMembership.GROUP, group);

        List<GroupMembership> result = query.getResultList();

        GroupMembership groupMembership = null;

        if (!result.isEmpty()) {
            groupMembership = result.get(0);
        }

        return groupMembership;
    }

    /**
     * <p>
     * Check if the given {@link IdentityType} exists by using its identifier.
     * </p>
     *
     * @param identityType
     * @throws IdentityManagementException if no instance was found with the provided identifier.
     */
    private void checkIfIdentityTypeExists(IdentityType identityType) throws IdentityManagementException {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (lookupIdentityById(identityType.getClass(), identityType.getId()) == null) {
            throw MESSAGES.attributedTypeNotFoundWithId(identityType.getClass(), identityType.getId(),
                    this.context.getPartition());
        }
    }

    private Grant getGrant(IdentityType identityType, Role role) {
        RelationshipQuery<Grant> query = createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, identityType);
        query.setParameter(Grant.ROLE, role);

        List<Grant> result = query.getResultList();

        Grant grant = null;

        if (!result.isEmpty()) {
            grant = result.get(0);
        }

        return grant;
    }

    /**
     * <p>
     * Helper method to check if the current partition is a {@link Realm}. {@link Agent} instances can only be managed using a
     * {@link Realm}.
     * </p>
     *
     * @throws IdentityManagementException if the current partition is not a {@link Realm}.
     */
    private void checkCurrentPartitionForAgents() throws IdentityManagementException {
        if (!Realm.class.isInstance(this.context.getPartition())) {
            throw MESSAGES.partitionInvalidTypeForAgents(this.context.getPartition().getClass());
        }
    }

    /**
     * <p>
     * Helper method to check if the current partition is a {@link Realm}. Credentials can only be managed using a {@link Realm}
     * .
     * </p>
     *
     * @throws IdentityManagementException if the current partition is not a {@link Realm}.
     */
    private void checkCurrentPartitionForCredential() throws IdentityManagementException {
        if (!Realm.class.isInstance(this.context.getPartition())) {
            throw MESSAGES.partitionInvalidTypeForCredential(this.context.getPartition().getClass());
        }

    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        checkCurrentPartitionForCredential();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.credential, FeatureOperation.read);
        if (!CredentialStore.class.isInstance(store)) {
            throw MESSAGES.credentialInvalidCredentialStoreType(store.getClass());
        } else {
            CredentialStore<?> credStore = (CredentialStore<?>) store;
            return credStore.retrieveCurrentCredential(this.context, agent, storageClass);
        }
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
        checkCurrentPartitionForCredential();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.credential, FeatureOperation.read);
        if (!CredentialStore.class.isInstance(store)) {
            throw MESSAGES.credentialInvalidCredentialStoreType(store.getClass());
        } else {
            CredentialStore<?> credStore = (CredentialStore<?>) store;
            return credStore.retrieveCredentials(this.context, agent, storageClass);
        }
    }

}