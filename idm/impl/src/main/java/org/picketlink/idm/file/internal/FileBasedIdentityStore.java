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

package org.picketlink.idm.file.internal;

import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.file.internal.FileIdentityQueryHelper.isQueryParameterEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.event.AgentCreatedEvent;
import org.picketlink.idm.event.AgentDeletedEvent;
import org.picketlink.idm.event.AgentUpdatedEvent;
import org.picketlink.idm.event.GroupCreatedEvent;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.GroupUpdatedEvent;
import org.picketlink.idm.event.RelationshipCreatedEvent;
import org.picketlink.idm.event.RelationshipDeletedEvent;
import org.picketlink.idm.event.RelationshipUpdatedEvent;
import org.picketlink.idm.event.RoleCreatedEvent;
import org.picketlink.idm.event.RoleDeletedEvent;
import org.picketlink.idm.event.RoleUpdatedEvent;
import org.picketlink.idm.event.UserCreatedEvent;
import org.picketlink.idm.event.UserDeletedEvent;
import org.picketlink.idm.event.UserUpdatedEvent;
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.model.annotation.RelationshipAttribute;
import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * <p>
 * File based {@link IdentityStore} implementation. By default, each new instance recreate the data files. This behavior can be
 * changed by configuring the <code>alwaysCreateFiles</code> property to false.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@CredentialHandlers({ PasswordCredentialHandler.class, X509CertificateCredentialHandler.class, DigestCredentialHandler.class })
public class FileBasedIdentityStore implements IdentityStore<FileIdentityStoreConfiguration>, CredentialStore, PartitionStore {

    private FileIdentityStoreConfiguration config;

    private FileCredentialStore credentialStore;
    private FilePartitionStore partitionStore;

    private FileDataSource fileDataSource;

    @Override
    public void setup(FileIdentityStoreConfiguration config) {
        this.fileDataSource = new FileDataSource();

        this.fileDataSource.init(config);

        this.config = config;

        this.credentialStore = new FileCredentialStore(this);
        this.partitionStore = new FilePartitionStore(this);
    }

    @Override
    public FileIdentityStoreConfiguration getConfig() {
        return this.config;
    }

    @Override
    public void add(SecurityContext context, AttributedType attributedType) {
        attributedType.setId(context.getIdGenerator().generate());

        if (IdentityType.class.isInstance(attributedType)) {
            @SuppressWarnings("unchecked")
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

            if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent agent = (Agent) attributedType;

                if (IDMUtil.isUserType(identityTypeClass)) {
                    addUser(context, (User) agent);
                } else {
                    addAgent(context, agent);
                }
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                addGroup(context, (Group) attributedType);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                addRole(context, (Role) attributedType);
            } else {
                throw MESSAGES.identityTypeUnsupportedType(identityTypeClass);
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;

            addRelationship(context, relationship);
        } else {
            throw MESSAGES.attributedTypeUnsupportedType(attributedType.getClass());
        }
    }

    @Override
    public void update(SecurityContext context, AttributedType attributedType) {
        if (IdentityType.class.isInstance(attributedType)) {
            @SuppressWarnings("unchecked")
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

            if (IDMUtil.isUserType(identityTypeClass)) {
                updateUser(context, (User) attributedType);
            } else if (IDMUtil.isAgentType(identityTypeClass)) {
                updateAgent(context, (Agent) attributedType);
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                updateGroup(context, (Group) attributedType);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                updateRole(context, (Role) attributedType);
            } else {
                throw MESSAGES.identityTypeUnsupportedType(identityTypeClass);
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            updateRelationship(context, (Relationship) attributedType);
        } else {
            throw MESSAGES.attributedTypeUnsupportedType(attributedType.getClass());
        }
    }

    @Override
    public void remove(SecurityContext context, AttributedType attributedType) {
        @SuppressWarnings("unchecked")
        Class<? extends IdentityType> attributedTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

        if (IdentityType.class.isInstance(attributedType)) {
            if (IDMUtil.isAgentType(attributedTypeClass)) {
                removeAgent(context, (Agent) attributedType);
            } else if (IDMUtil.isGroupType(attributedTypeClass)) {
                removeGroup(context, (Group) attributedType);
            } else if (IDMUtil.isRoleType(attributedTypeClass)) {
                removeRole(context, (Role) attributedType);
            } else {
                throw MESSAGES.identityTypeUnsupportedType(attributedTypeClass);
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            removeRelationship(context, (Relationship) attributedType);
        } else {
            throw MESSAGES.attributedTypeUnsupportedType(attributedTypeClass);
        }
    }

    @Override
    public Agent getAgent(SecurityContext context, String loginName) {
        if (Realm.class.isInstance(context.getPartition())) {

            Agent agent = getAgentsForCurrentRealm().get(loginName);

            if (agent != null) {
                configurePartition(agent);
            }

            return agent;
        } else {
            // FIXME throw exception
            throw new RuntimeException();
        }
    }

    @Override
    public User getUser(SecurityContext context, String loginName) {
        Agent agent = getAgent(context, loginName);

        if (!User.class.isInstance(agent)) {
            return null;
        }

        return (User) agent;
    }

    @Override
    public Role getRole(SecurityContext context, String roleName) {
        return lookupRole(roleName, context.getPartition());
    }

    @Override
    public Group getGroup(SecurityContext context, String groupPath) {
        Group group = null;

        if (groupPath != null) {
            if (!groupPath.startsWith("/")) {
                groupPath = "/" + groupPath;
            }

            group = lookupGroup(groupPath, context.getPartition());

            if (group != null) {
                Group parentGroup = group.getParentGroup();

                if (parentGroup != null) {
                    group.setParentGroup(getGroup(context, parentGroup.getPath()));
                }
            }
        }

        return group;
    }

    @Override
    public Group getGroup(SecurityContext context, String name, Group parent) {
        String path = "/" + name;

        if (parent != null) {
            Group parentGroup = (Group) lookupIdentityTypeById(context, parent.getId());

            path = parentGroup.getPath() + path;
        }

        return getGroup(context, path);
    }

    @Override
    public <T extends IdentityType> int countQueryResults(SecurityContext context, IdentityQuery<T> identityQuery) {
        int limit = identityQuery.getLimit();
        int offset = identityQuery.getOffset();

        identityQuery.setLimit(0);
        identityQuery.setOffset(0);

        int resultCount = identityQuery.getResultList().size();

        identityQuery.setLimit(limit);
        identityQuery.setOffset(offset);

        return resultCount;
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(SecurityContext context, IdentityType identityType, String attributeName) {
        throw MESSAGES.notImplentedYet();
    }

    @Override
    public void setAttribute(SecurityContext context, IdentityType identityType, Attribute<? extends Serializable> attribute) {
        throw MESSAGES.notImplentedYet();
    }

    @Override
    public void removeAttribute(SecurityContext context, IdentityType identityType, String attributeName) {
        throw MESSAGES.notImplentedYet();
    }

    @Override
    public <T extends Relationship> int countQueryResults(SecurityContext context, RelationshipQuery<T> query) {
        throw MESSAGES.notImplentedYet();
    }

    @Override
    public <T extends Relationship> List<T> fetchQueryResults(SecurityContext context, RelationshipQuery<T> query) {
        return fetchQueryResults(context, query, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(SecurityContext context, IdentityQuery<T> identityQuery) {
        Class<T> identityTypeClass = identityQuery.getIdentityType();

        @SuppressWarnings("rawtypes")
        Collection entries = new ArrayList<T>();

        Object[] partitionParameters = identityQuery.getParameter(IdentityType.PARTITION);
        Partition partition = null;

        if (partitionParameters != null && partitionParameters.length > 0) {
            partition = (Partition) partitionParameters[0];
        }

        if (IdentityType.class.equals(identityTypeClass)) {
            if (partition == null) {
                entries.addAll(getAgentsForCurrentRealm().values());
                entries.addAll(getRolesForCurrentPartition(context).values());
                entries.addAll(getGroupsForCurrentPartition(context).values());
            } else {
                entries.addAll(getAgentsForPartition(partition).values());
                entries.addAll(getRolesForPartition(partition).values());
                entries.addAll(getGroupsForPartition(partition).values());
            }
        } else if (IDMUtil.isAgentType(identityTypeClass)) {
            if (partition == null) {
                entries = getAgentsForCurrentRealm().values();
            } else {
                entries = getAgentsForPartition(partition).values();
            }
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            if (partition == null) {
                entries = getRolesForCurrentPartition(context).values();
            } else {
                entries = getRolesForPartition(partition).values();
            }
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            if (partition == null) {
                entries = getGroupsForCurrentPartition(context).values();
            } else {
                entries = getGroupsForPartition(partition).values();
            }
        } else {
            throw MESSAGES.identityTypeUnsupportedType(identityTypeClass);
        }

        List<T> result = new ArrayList<T>();

        FileIdentityQueryHelper queryHelper = new FileIdentityQueryHelper(identityQuery, this);

        for (Iterator<?> iterator = entries.iterator(); iterator.hasNext();) {
            IdentityType storedEntry = (IdentityType) iterator.next();

            if (!identityTypeClass.isAssignableFrom(storedEntry.getClass())) {
                continue;
            }

            if (!isQueryParameterEquals(identityQuery, IdentityType.ID, storedEntry.getId())) {
                continue;
            }

            if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent agent = (Agent) storedEntry;

                if (!isQueryParameterEquals(identityQuery, Agent.LOGIN_NAME, agent.getLoginName())) {
                    continue;
                }

                if (IDMUtil.isUserType(identityTypeClass)) {
                    User user = (User) storedEntry;

                    if (!isQueryParameterEquals(identityQuery, User.EMAIL, user.getEmail())) {
                        continue;
                    }

                    if (!isQueryParameterEquals(identityQuery, User.FIRST_NAME, user.getFirstName())) {
                        continue;
                    }

                    if (!isQueryParameterEquals(identityQuery, User.LAST_NAME, user.getLastName())) {
                        continue;
                    }
                }
            }

            if (IDMUtil.isRoleType(identityTypeClass)) {
                Role role = (Role) storedEntry;

                if (!isQueryParameterEquals(identityQuery, Role.NAME, role.getName())) {
                    continue;
                }
            }

            if (IDMUtil.isGroupType(identityTypeClass)) {
                Group group = (Group) storedEntry;

                if (!isQueryParameterEquals(identityQuery, Group.NAME, group.getName())) {
                    continue;
                }

                String parentGroupName = null;

                if (group.getParentGroup() != null) {
                    parentGroupName = group.getParentGroup().getName();
                }

                if (!isQueryParameterEquals(identityQuery, Group.PARENT, parentGroupName)) {
                    continue;
                }
            }

            if (!isQueryParameterEquals(identityQuery, IdentityType.ENABLED, storedEntry.isEnabled())) {
                continue;
            }

            if (!queryHelper.matchCreatedDateParameters(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchExpiryDateParameters(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchAttributes(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchHasRole(context, storedEntry)) {
                continue;
            }

            if (!queryHelper.matchMemberOf(context, storedEntry)) {
                continue;
            }

            if (!queryHelper.matchHasGroupRole(context, storedEntry)) {
                continue;
            }

            if (!queryHelper.matchRolesOf(context, storedEntry)) {
                continue;
            }

            if (!queryHelper.matchHasMember(context, storedEntry)) {
                continue;
            }

            configurePartition(storedEntry);

            result.add((T) storedEntry);
        }

        // Apply sorting
        Collections.sort(result, new FileSortingComparator<T>(identityQuery));

        // Apply pagination
        if (identityQuery.getLimit() > 0) {
            int numberOfItems = Math.min(identityQuery.getLimit(), result.size() - identityQuery.getOffset());
            result = result.subList(identityQuery.getOffset(), identityQuery.getOffset() + numberOfItems);
        }

        return result;
    }

    @Override
    public void storeCredential(SecurityContext context, Agent agent, CredentialStorage storage) {
        this.credentialStore.storeCredential(context, agent, storage);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(SecurityContext context, Agent agent, Class<T> storageClass) {
        return this.credentialStore.retrieveCredentials(context, agent, storageClass);
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(SecurityContext context, Agent agent, Class<T> storageClass) {
        return this.credentialStore.retrieveCurrentCredential(context, agent, storageClass);
    }

    @Override
    public void updateCredential(SecurityContext context, Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        this.credentialStore.updateCredential(context, agent, credential, effectiveDate, expiryDate);
    }

    @Override
    public void validateCredentials(SecurityContext context, Credentials credentials) {
        this.credentialStore.validateCredentials(context, credentials);
    }

    @Override
    public void createPartition(SecurityContext context, Partition partition) {
        this.partitionStore.createPartition(context, partition);
    }

    @Override
    public Realm getRealm(SecurityContext context, String realmName) {
        return this.partitionStore.getRealm(context, realmName);
    }

    @Override
    public Tier getTier(SecurityContext context, String tierName) {
        return this.partitionStore.getTier(context, tierName);
    }

    @Override
    public void removePartition(SecurityContext context, Partition partition) {
        this.partitionStore.removePartition(context, partition);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Relationship> T convertToRelationship(SecurityContext context, FileRelationship fileRelationship) {
        Class<T> relationshipType = null;

        try {
            relationshipType = (Class<T>) Class.forName(fileRelationship.getType());
        } catch (Exception e) {
            throw MESSAGES.classNotFound(fileRelationship.getType());
        }

        return cloneRelationship(context, fileRelationship, relationshipType);
    }

    /**
     * <p>
     * Returns the stored {@link Relationship} instances for the current {@link Partition}.
     * </p>
     *
     * @return
     */
    protected Map<String, List<FileRelationship>> getRelationshipsForCurrentPartition() {
        return getDataSource().getRelationships();
    }

    protected boolean hasParentGroup(Group childGroup, Group parentGroup) {
        if (childGroup.getParentGroup() != null && parentGroup != null) {
            if (childGroup.getParentGroup().getId().equals(parentGroup.getId())) {
                return true;
            }
        } else {
            return false;
        }

        return hasParentGroup(childGroup.getParentGroup(), parentGroup);
    }

    @SuppressWarnings("unchecked")
    private <T extends Relationship> T cloneRelationship(SecurityContext context, FileRelationship fileRelationship,
            Class<? extends Relationship> relationshipType) {
        T clonedRelationship = null;

        try {
            clonedRelationship = (T) relationshipType.newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(relationshipType.getName(), e);
        }

        Relationship storedRelationship = fileRelationship.getEntry();

        clonedRelationship.setId(storedRelationship.getId());

        List<Property<IdentityType>> relationshipIdentityTypes = PropertyQueries
                .<IdentityType> createQuery(clonedRelationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipIdentity.class)).getResultList();

        for (Property<IdentityType> annotatedProperty : relationshipIdentityTypes) {
            IdentityType identityType = lookupIdentityTypeById(context, fileRelationship.getIdentityTypeId(annotatedProperty.getName()));

            if (identityType == null) {
                return null;
            }

            annotatedProperty.setValue(clonedRelationship, identityType);
        }

        updateAttributedType(storedRelationship, clonedRelationship);

        return clonedRelationship;
    }

    /**
     * <p>
     * Persists the given {@link Role} instance.
     * </p>
     *
     * @param role
     */
    private void addRole(SecurityContext context, Role role) {
        Role fileRole = new SimpleRole(role.getName());

        fileRole.setPartition(context.getPartition());

        updateIdentityType(role, fileRole);

        storeRole(fileRole);
        context.getEventBridge().raiseEvent(new RoleCreatedEvent(role));
    }

    private void storeRole(Role role) {
        FilePartition filePartition = getDataSource().getPartition(role.getPartition().getId());

        filePartition.getRoles().put(role.getName(), new FileRole(role));

        getDataSource().flushRoles(filePartition);
    }

    /**
     * <p>
     * Persists the given {@link Group} instance.
     * </p>
     *
     * @param group
     */
    private void addGroup(SecurityContext context, Group group) {
        Group fileGroup = null;

        if (group.getParentGroup() != null) {
            Group parentGroup = (Group) lookupIdentityTypeById(context, group.getParentGroup().getId());

            fileGroup = new SimpleGroup(group.getName(), parentGroup);
        } else {
            fileGroup = new SimpleGroup(group.getName());
        }

        fileGroup.setPartition(context.getPartition());

        updateIdentityType(group, fileGroup);

        storeGroup(fileGroup);
        context.getEventBridge().raiseEvent(new GroupCreatedEvent(group));
    }

    private void storeGroup(Group fileGroup) {
        FilePartition partition = getDataSource().getPartition(fileGroup.getPartition().getId());

        partition.getGroups().put(fileGroup.getPath(), new FileGroup(fileGroup));

        getDataSource().flushGroups(partition);
    }

    /**
     * <p>
     * Persists the given {@link User} instance.
     * </p>
     *
     * @param user
     */
    private void addUser(SecurityContext context, User user) {
        if (Realm.class.isInstance(context.getPartition())) {
            Realm realm = (Realm) context.getPartition();

            User storedUser = new SimpleUser(user.getLoginName());

            storedUser.setFirstName(user.getFirstName());
            storedUser.setLastName(user.getLastName());
            storedUser.setEmail(user.getEmail());
            storedUser.setPartition(realm);

            updateIdentityType(user, storedUser);

            storeAgent(storedUser);
            context.getEventBridge().raiseEvent(new UserCreatedEvent(storedUser));
        } else {
            // FIXME throw exception
            throw new RuntimeException();
        }
    }

    /**
     * <p>
     * Persists the given {@link Agent} instance.
     * </p>
     *
     * @param agent
     */
    private void addAgent(SecurityContext context, Agent agent) {
        if (Realm.class.isInstance(context.getPartition())) {
            Realm realm = (Realm) context.getPartition();

            Agent storedAgent = new SimpleAgent(agent.getLoginName());

            storedAgent.setPartition(realm);

            updateIdentityType(agent, storedAgent);

            storeAgent(storedAgent);
            context.getEventBridge().raiseEvent(new AgentCreatedEvent(storedAgent));

        } else {
            // FIXME throw exception
            throw new RuntimeException();
        }
    }

    private void storeAgent(Agent storedAgent) {
        FilePartition filePartition = getDataSource().getPartition(storedAgent.getPartition().getId());

        filePartition.getAgents().put(storedAgent.getLoginName(), new FileAgent(storedAgent));

        getDataSource().flushAgents(filePartition);
    }

    /**
     * <p>
     * Persists the given {@link Relationship} instance.
     * </p>
     *
     * @param relationship
     */
    private void addRelationship(SecurityContext context, Relationship relationship) {
        if (relationship.getId() == null) {
            relationship.setId(context.getIdGenerator().generate());
        }

        Relationship newRelationship = null;

        try {
            newRelationship = relationship.getClass().newInstance();
        } catch (Exception e) {
            MESSAGES.instantiationError(relationship.getClass().getName(), e);
        }

        newRelationship.setId(relationship.getId());

        List<Property<IdentityType>> relationshipIdentityTypes = PropertyQueries
                .<IdentityType> createQuery(newRelationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipIdentity.class)).getResultList();

        for (Property<IdentityType> annotatedProperty : relationshipIdentityTypes) {
            annotatedProperty.setValue(newRelationship, annotatedProperty.getValue(relationship));
        }

        updateAttributedType(relationship, newRelationship);

        FileRelationship fileRelationship = new FileRelationship(newRelationship);

        Map<String, List<FileRelationship>> relationshipsMap = getDataSource().getRelationships();
        List<FileRelationship> relationships = relationshipsMap.get(newRelationship.getClass().getName());

        if (relationships == null) {
            relationships = new ArrayList<FileRelationship>();
            relationshipsMap.put(newRelationship.getClass().getName(), relationships);
        }

        relationships.add(fileRelationship);
        getDataSource().flushRelationships();

        context.getEventBridge().raiseEvent(new RelationshipCreatedEvent(relationship));
    }

    /**
     * <p>
     * Updates a previously stored {@link Role}.
     * </p>
     *
     * @param updatedRole
     * @return
     */
    private void updateRole(SecurityContext context, Role updatedRole) {
        Role storedRole = (Role) lookupIdentityTypeById(context, updatedRole.getId());

        if (storedRole != updatedRole) {
            updateIdentityType(updatedRole, storedRole);
        }

        storeRole(storedRole);
        context.getEventBridge().raiseEvent(new RoleUpdatedEvent(updatedRole));
    }

    /**
     * <p>
     * Updates a previously stored {@link Group}.
     * </p>
     *
     * @param updatedGroup
     */
    private void updateGroup(SecurityContext context, Group updatedGroup) {
        Group storedGroup = (Group) lookupIdentityTypeById(context, updatedGroup.getId());

        if (storedGroup != updatedGroup) {
            updateIdentityType(updatedGroup, storedGroup);
        }

        storeGroup(storedGroup);
        context.getEventBridge().raiseEvent(new GroupUpdatedEvent(updatedGroup));
    }

    /**
     * <p>
     * Updates a previously stored {@link User}.
     * </p>
     *
     * @param updatedUser
     * @return
     */
    private void updateUser(SecurityContext context, User updatedUser) {
        User storedUser = (User) lookupIdentityTypeById(context, updatedUser.getId());

        if (storedUser != updatedUser) {
            storedUser.setFirstName(updatedUser.getFirstName());
            storedUser.setLastName(updatedUser.getLastName());
            storedUser.setEmail(updatedUser.getEmail());

            updateIdentityType(updatedUser, storedUser);
        }

        storeAgent(storedUser);
        context.getEventBridge().raiseEvent(new UserUpdatedEvent(updatedUser));
    }

    /**
     * <p>
     * Updates a previously stored {@link Agent}.
     * </p>
     *
     * @param updatedAgent
     * @return
     */
    private void updateAgent(SecurityContext context, Agent updatedAgent) {
        Agent storedAgent = (Agent) lookupIdentityTypeById(context, updatedAgent.getId());

        if (storedAgent != updatedAgent) {
            updateIdentityType(updatedAgent, storedAgent);
        }

        storeAgent(storedAgent);
        context.getEventBridge().raiseEvent(new AgentUpdatedEvent(updatedAgent));
    }

    /**
     * <p>
     * Updates the given {@link Relationship} instance.
     * </p>
     *
     * @param relationship
     */
    private void updateRelationship(SecurityContext context, Relationship relationship) {
        List<FileRelationship> relationships = getDataSource().getRelationships().get(relationship.getClass().getName());

        for (FileRelationship fileRelationship : new ArrayList<FileRelationship>(relationships)) {
            Relationship storedRelationship = fileRelationship.getEntry();

            if (storedRelationship.getId().equals(relationship.getId())) {
                for (Object object : storedRelationship.getAttributes().toArray()) {
                    @SuppressWarnings("unchecked")
                    Attribute<? extends Serializable> attribute = (Attribute<? extends Serializable>) object;
                    storedRelationship.removeAttribute(attribute.getName());
                }

                for (Attribute<? extends Serializable> attrib : relationship.getAttributes()) {
                    storedRelationship.setAttribute(attrib);
                }
            }
        }

        context.getEventBridge().raiseEvent(new RelationshipUpdatedEvent(relationship));
    }

    /**
     * <p>
     * Update the common properties for a specific {@link IdentityType} instance from another instance.
     * </p>
     *
     * @param fromIdentityType
     * @param toIdentityType
     */
    private void updateIdentityType(IdentityType fromIdentityType, IdentityType toIdentityType) {
        toIdentityType.setEnabled(fromIdentityType.isEnabled());
        toIdentityType.setCreatedDate(fromIdentityType.getCreatedDate());
        toIdentityType.setExpirationDate(fromIdentityType.getExpirationDate());

        updateAttributedType(fromIdentityType, toIdentityType);

        fromIdentityType.setId(toIdentityType.getId());
        fromIdentityType.setPartition(toIdentityType.getPartition());
    }

    /**
     * <p>
     * Update the common properties for a specific {@link IdentityType} instance from another instance.
     * </p>
     *
     * @param fromIdentityType
     * @param toIdentityType
     */
    private void updateAttributedType(AttributedType fromIdentityType, AttributedType toIdentityType) {
        toIdentityType.setId(fromIdentityType.getId());

        for (Object object : toIdentityType.getAttributes().toArray()) {
            @SuppressWarnings("unchecked")
            Attribute<? extends Serializable> attribute = (Attribute<? extends Serializable>) object;
            toIdentityType.removeAttribute(attribute.getName());
        }

        List<Property<Serializable>> attributeProperties = PropertyQueries
                .<Serializable> createQuery(fromIdentityType.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipAttribute.class)).getResultList();

        for (Property<Serializable> attributeProperty : attributeProperties) {
            attributeProperty.setValue(toIdentityType, attributeProperty.getValue(fromIdentityType));
        }

        for (Attribute<? extends Serializable> attrib : fromIdentityType.getAttributes()) {
            toIdentityType.setAttribute(attrib);
        }
    }

    /**
     * <p>
     * Recursively lookup for a {@link Group} with the given name considering the given {@link Partition}. If {@link Partition}
     * is a {@link Tier} instance the parent tier will also be considered during the lookup.
     * </p>
     *
     * @param roleName
     * @param partition
     * @return
     */
    private Role lookupRole(String roleName, Partition partition) {
        Role role = getRolesForPartition(partition).get(roleName);

        if (role == null) {
            if (Tier.class.isInstance(partition)) {
                Tier tier = (Tier) partition;

                if (tier.getParent() != null) {
                    role = lookupRole(roleName, tier.getParent());
                }
            }
        }

        configurePartition(role);

        return role;
    }

    /**
     * <p>
     * Recursively lookup for a {@link Group} with the given name considering the given {@link Partition}. If {@link Partition}
     * is a {@link Tier} instance the parent tier will also be considered during the lookup.
     * </p>
     *
     * @param groupPath
     * @param partition
     * @return
     */
    private Group lookupGroup(String groupPath, Partition partition) {
        Group group = getGroupsForPartition(partition).get(groupPath);

        if (group == null) {
            if (Tier.class.isInstance(partition)) {
                Tier tier = (Tier) partition;

                if (tier.getParent() != null) {
                    group = lookupGroup(groupPath, tier.getParent());
                }
            }
        }

        configurePartition(group);

        return group;
    }

    /**
     * <p>
     * Removes the given {@link Relationship}.
     * </p>
     *
     * @param attributedTypeClass
     * @param relationship
     */
    private void removeRelationship(SecurityContext context, Relationship relationship) {
        if (relationship.getId() == null) {
            DefaultRelationshipQuery<?> query = null;

            if (GroupRole.class.isInstance(relationship)) {
                GroupRole groupRole = (GroupRole) relationship;

                query = new DefaultRelationshipQuery<GroupRole>(context, GroupRole.class, this);

                query.setParameter(GroupRole.ASSIGNEE, groupRole.getAssignee());
                query.setParameter(GroupRole.GROUP, groupRole.getGroup());
                query.setParameter(GroupRole.ROLE, groupRole.getRole());
            } else if (Grant.class.isInstance(relationship)) {
                Grant grant = (Grant) relationship;

                query = new DefaultRelationshipQuery<Grant>(context, Grant.class, this);

                query.setParameter(Grant.ASSIGNEE, grant.getAssignee());
                query.setParameter(Grant.ROLE, grant.getRole());
            } else if (GroupMembership.class.isInstance(relationship)) {
                GroupMembership groupMembership = (GroupMembership) relationship;

                query = new DefaultRelationshipQuery<GroupMembership>(context, GroupMembership.class, this);

                query.setParameter(GroupMembership.MEMBER, groupMembership.getMember());
                query.setParameter(GroupMembership.GROUP, groupMembership.getGroup());
            }

            @SuppressWarnings("unchecked")
            List<Relationship> result = (List<Relationship>) fetchQueryResults(context, query, true);

            if (!result.isEmpty()) {
                if (result.size() > 1) {
                    throw MESSAGES.relationshipAmbiguosFound(relationship);
                }

                relationship = result.get(0);
            } else {
                return;
            }
        }

        List<FileRelationship> relationships = getDataSource().getRelationships().get(relationship.getClass().getName());

        for (FileRelationship fileRelationship : new ArrayList<FileRelationship>(relationships)) {
            Relationship storedRelationship = fileRelationship.getEntry();

            if (storedRelationship.getId().equals(relationship.getId())) {
                relationships.remove(fileRelationship);
            }
        }

        getDataSource().flushRelationships();
        context.getEventBridge().raiseEvent(new RelationshipDeletedEvent(relationship));
    }

    /**
     * <p>
     * Removes the given {@link Role}.
     * </p>
     *
     * @param role
     * @return
     */
    private void removeRole(SecurityContext context, Role role) {
        Role storedRole = (Role) lookupIdentityTypeById(context, role.getId());

        FilePartition partition = getDataSource().getPartition(storedRole.getPartition().getId());

        removeRelationships(storedRole);

        partition.getRoles().remove(storedRole.getName());

        getDataSource().flushRoles(partition);

        context.getEventBridge().raiseEvent(new RoleDeletedEvent(role));
    }

    /**
     * <p>
     * Removes the given {@link Group}.
     * </p>
     *
     * @param group
     * @return
     */
    private void removeGroup(SecurityContext context, Group group) {
        Group storedGroup = (Group) lookupIdentityTypeById(context, group.getId());

        FilePartition partition = getDataSource().getPartition(storedGroup.getPartition().getId());

        removeRelationships(storedGroup);

        partition.getGroups().remove(storedGroup.getPath());

        getDataSource().flushGroups(partition);

        context.getEventBridge().raiseEvent(new GroupDeletedEvent(group));
    }

    /**
     * <p>
     * Removes the given {@link Agent}.
     * </p>
     *
     * @param agent
     */
    private void removeAgent(SecurityContext context, Agent agent) {
        Agent storedAgent = (Agent) lookupIdentityTypeById(context, agent.getId());

        FilePartition partition = getDataSource().getPartition(storedAgent.getPartition().getId());

        removeRelationships(storedAgent);

        partition.getAgents().remove(storedAgent.getLoginName());

        getDataSource().flushAgents(partition);

        this.credentialStore.removeCredentials(context, storedAgent);

        if (IDMUtil.isUserType(agent.getClass())) {
            context.getEventBridge().raiseEvent(new UserDeletedEvent((User) agent));
        }

        context.getEventBridge().raiseEvent(new AgentDeletedEvent(agent));
    }

    private void removeRelationships(IdentityType identityType) {
        Set<Entry<String, List<FileRelationship>>> allRelationships = getDataSource().getRelationships().entrySet();

        for (Entry<String, List<FileRelationship>> entry : allRelationships) {
            List<FileRelationship> relationships = entry.getValue();

            for (FileRelationship fileRelationship : new ArrayList<FileRelationship>(relationships)) {
                if (fileRelationship.hasIdentityType(identityType.getId())) {
                    relationships.remove(fileRelationship);
                }
            }
        }

        getDataSource().flushRelationships();
    }

    /**
     * <p>
     * Returns the stored {@link Group} instances for the given {@link Partition}.
     * </p>
     *
     * @param partition
     * @return
     */
    private Map<String, Group> getGroupsForPartition(Partition partition) {
        return getDataSource().getGroups(partition);
    }

    /**
     * <p>
     * Returns the stored {@link Role} instances for the given {@link Partition}.
     * </p>
     *
     * @param partition
     * @return
     */
    private Map<String, Role> getRolesForPartition(Partition partition) {
        return getDataSource().getRoles(partition);
    }

    /**
     * <p>
     * Returns the stored {@link Agent} instances for the given {@link Partition}.
     * </p>
     *
     * @param partition
     * @return
     */
    private Map<String, Agent> getAgentsForPartition(Partition partition) {
        return getDataSource().getAgents(partition);
    }

    /**
     * <p>
     * Returns the stored {@link Role} instances for the given {@link Partition}.
     * </p>
     *
     * @return
     */
    private Map<String, Role> getRolesForCurrentPartition(SecurityContext context) {
        return getDataSource().getRoles(context.getPartition());
    }

    /**
     * <p>
     * Returns the stored {@link Group} instances for the given {@link Partition}.
     * </p>
     *
     * @return
     */
    private Map<String, Group> getGroupsForCurrentPartition(SecurityContext context) {
        return getDataSource().getGroups(context.getPartition());
    }

    private Map<String, Agent> getAgentsForCurrentRealm() {
        // FIXME check that the active partition *is* a realm
        //return getDataSource().getAgents(getContext().getRealm());
        return null;
    }

    protected FileDataSource getDataSource() {
        return this.fileDataSource;
    }

    private void configurePartition(IdentityType identityType) {
        if (identityType != null && identityType.getPartition() != null) {
            Partition partition = this.partitionStore.lookupById(identityType.getPartition().getId());

            identityType.setPartition(partition);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Relationship> List<T> fetchQueryResults(SecurityContext context, RelationshipQuery<T> query, boolean matchExactGroup) {
        List<T> result = new ArrayList<T>();
        Class<T> relationshipType = query.getRelationshipType();
        List<FileRelationship> relationships = new ArrayList<FileRelationship>();

        if (Relationship.class.equals(query.getRelationshipType())) {
            Collection<List<FileRelationship>> allRelationships = getRelationshipsForCurrentPartition().values();

            for (List<FileRelationship> partitionRelationships : allRelationships) {
                relationships.addAll(partitionRelationships);
            }
        } else {
            List<FileRelationship> currentRealmRelationships = getRelationshipsForCurrentPartition().get(
                    relationshipType.getName());

            if (currentRealmRelationships != null) {
                relationships.addAll(currentRealmRelationships);
            }
        }

        if (relationships.isEmpty()) {
            return result;
        }

        for (FileRelationship storedRelationship : relationships) {
            boolean match = false;

            Object[] identityParameterValues = query.getParameter(Relationship.IDENTITY);

            if (identityParameterValues != null && identityParameterValues.length > 0) {
                for (Object parameterValue : identityParameterValues) {
                    String identityId = null;

                    if (String.class.isInstance(parameterValue)) {
                        identityId = (String) parameterValue;
                    } else if (IdentityType.class.isInstance(parameterValue)) {
                        IdentityType identityType = (IdentityType) parameterValue;
                        identityId = identityType.getId();
                    } else {
                        throw MESSAGES.queryUnsupportedParameterValue("Relationship.IDENTITY", parameterValue);
                    }

                    match = storedRelationship.hasIdentityType(identityId);
                }
            } else {
                if (query.getRelationshipType().getName().equals(storedRelationship.getType())) {
                    for (Entry<QueryParameter, Object[]> entry : query.getParameters().entrySet()) {
                        QueryParameter queryParameter = entry.getKey();
                        Object[] values = entry.getValue();

                        if (queryParameter instanceof RelationshipQueryParameter) {
                            RelationshipQueryParameter identityTypeParameter = (RelationshipQueryParameter) queryParameter;
                            match = matchIdentityType(context, storedRelationship, query, identityTypeParameter, matchExactGroup);
                        }

                        if (AttributedType.AttributeParameter.class.isInstance(queryParameter) && values != null) {
                            AttributedType.AttributeParameter customParameter = (AttributedType.AttributeParameter) queryParameter;
                            Attribute<Serializable> userAttribute = storedRelationship.getEntry().getAttribute(
                                    customParameter.getName());

                            Serializable userAttributeValue = null;

                            if (userAttribute != null) {
                                userAttributeValue = userAttribute.getValue();
                            }

                            if (userAttributeValue != null) {
                                int count = values.length;

                                for (Object value : values) {
                                    if (userAttributeValue.getClass().isArray()) {
                                        Object[] userValues = (Object[]) userAttributeValue;

                                        for (Object object : userValues) {
                                            if (object.equals(value)) {
                                                count--;
                                            }
                                        }
                                    } else {
                                        if (value.equals(userAttributeValue)) {
                                            count--;
                                        }
                                    }
                                }

                                match = count <= 0;
                            }
                        }

                        if (!match) {
                            break;
                        }
                    }
                }
            }

            if (match) {
                result.add((T) convertToRelationship(context, storedRelationship));
            }
        }

        return result;
    }

    private boolean matchIdentityType(SecurityContext context, FileRelationship storedRelationship,
            RelationshipQuery<?> query,
            RelationshipQueryParameter identityTypeParameter, boolean matchExactGroup) {
        Object[] values = query.getParameter(identityTypeParameter);
        int valuesMathCount = values.length;
        boolean match = false;

        try {
            IdentityType identityTypeRel = lookupIdentityTypeById(context,
                    storedRelationship.getIdentityTypeId(identityTypeParameter.getName()));

            for (Object object : values) {
                IdentityType identityType = (IdentityType) object;

                if (identityTypeRel.getClass().isInstance(identityType)) {
                    if (identityTypeRel.getId().equals(identityType.getId())) {
                        valuesMathCount--;
                    } else {
                        if ((GroupMembership.class.isInstance(storedRelationship.getEntry()) || GroupRole.class
                                .isInstance(storedRelationship.getEntry())) && !matchExactGroup) {
                            if (Group.class.isInstance(identityTypeRel)) {
                                Group groupParameter = (Group) identityType;
                                Group groupFromRel = (Group) identityTypeRel;

                                if (groupParameter.getPath().contains(groupFromRel.getPath())) {
                                    if (hasParentGroup(groupParameter, (Group) identityTypeRel)) {
                                        valuesMathCount--;
                                    }
                                }
                            }
                        }
                    }

                }
            }

            match = valuesMathCount <= 0;
        } catch (IdentityManagementException ignore) {
            // the identitype could not be found for the current partition. we should ignore that.
        }

        return match;
    }

    private IdentityType lookupIdentityTypeById(SecurityContext context, String identityTypeId) {
        if (identityTypeId == null) {
            throw MESSAGES.nullArgument("AttributedType identifier");
        }

        IdentityQuery<IdentityType> query = new DefaultIdentityQuery<IdentityType>(context, IdentityType.class, this);

        query.setParameter(IdentityType.ID, identityTypeId);

        List<IdentityType> results = query.getResultList();

        if (results.isEmpty()) {
            throw MESSAGES.attributedTypeNotFoundWithId(IdentityType.class, identityTypeId, context.getPartition());
        }

        return results.get(0);
    }

}