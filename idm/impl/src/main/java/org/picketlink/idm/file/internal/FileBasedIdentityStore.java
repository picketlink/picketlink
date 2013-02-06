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

package org.picketlink.idm.file.internal;

import static org.picketlink.idm.file.internal.FileIdentityQueryHelper.isQueryParameterEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
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
    private IdentityStoreInvocationContext context;

    private FileCredentialStore credentialStore;
    private FilePartitionStore partitionStore;

    @Override
    public void setup(FileIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;

        this.credentialStore = new FileCredentialStore(this);
        this.partitionStore = new FilePartitionStore(this);

        if (this.context.getRealm() == null) {
            Realm defaultRealm = getRealm(Realm.DEFAULT_REALM);

            if (defaultRealm == null) {
                defaultRealm = new Realm(Realm.DEFAULT_REALM);
                createPartition(defaultRealm);
            }

            this.context.setRealm(defaultRealm);
        }
    }

    @Override
    public FileIdentityStoreConfiguration getConfig() {
        return this.config;
    }

    @Override
    public IdentityStoreInvocationContext getContext() {
        return this.context;
    }

    private Partition getCurrentPartition() {
        return getContext().getPartition();
    }

    @Override
    public void add(AttributedType attributedType) {
        attributedType.setId(getContext().getIdGenerator().generate());

        if (IdentityType.class.isInstance(attributedType)) {
            @SuppressWarnings("unchecked")
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

            if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent agent = (Agent) attributedType;

                if (agent.getLoginName() == null) {
                    throw new IdentityManagementException("No login name was provided.");
                }

                if (getAgent(agent.getLoginName()) != null) {
                    throw new IdentityManagementException("Agent already exists with the given login name ["
                            + agent.getLoginName() + "] for the given Realm [" + getContext().getRealm().getName() + "]");
                }

                if (IDMUtil.isUserType(identityTypeClass)) {
                    addUser((User) agent);
                } else {
                    addAgent(agent);
                }
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                addGroup((Group) attributedType);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                addRole((Role) attributedType);
            } else {
                throw createUnsupportedIdentityTypeException(identityTypeClass);
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;

            addRelationship(relationship);

            if (GroupRole.class.isInstance(relationship)) {
                GroupRole groupRole = (GroupRole) relationship;

                addRelationship(new Grant(groupRole.getMember(), groupRole.getRole()));
                addRelationship(new GroupMembership(groupRole.getMember(), groupRole.getGroup()));
            }
        } else {
            throw createUnsupportedAttributedType(attributedType.getClass());
        }
    }

    @Override
    public void update(AttributedType attributedType) {
        checkNotNulId(attributedType);

        if (IdentityType.class.isInstance(attributedType)) {
            @SuppressWarnings("unchecked")
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

            if (IDMUtil.isUserType(identityTypeClass)) {
                updateUser((User) attributedType);
            } else if (IDMUtil.isAgentType(identityTypeClass)) {
                updateAgent((Agent) attributedType);
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                updateGroup((Group) attributedType);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                updateRole((Role) attributedType);
            } else {
                throw createUnsupportedIdentityTypeException(identityTypeClass);
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            updateRelationship((Relationship) attributedType);
        } else {
            throw createUnsupportedAttributedType(attributedType.getClass());
        }
    }

    @Override
    public void remove(AttributedType attributedType) {
        checkNotNulId(attributedType);

        @SuppressWarnings("unchecked")
        Class<? extends IdentityType> attributedTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

        if (IdentityType.class.isInstance(attributedType)) {
            if (IDMUtil.isAgentType(attributedTypeClass)) {
                removeAgent((Agent) attributedType);
            } else if (IDMUtil.isGroupType(attributedTypeClass)) {
                removeGroup((Group) attributedType);
            } else if (IDMUtil.isRoleType(attributedTypeClass)) {
                removeRole((Role) attributedType);
            } else {
                throw createUnsupportedIdentityTypeException(attributedTypeClass);
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            removeRelationship((Relationship) attributedType);
        } else {
            throw createUnsupportedAttributedType(attributedTypeClass);
        }
    }

    @Override
    public Agent getAgent(String loginName) {
        Agent agent = getAgentsForCurrentRealm().get(loginName);

        configurePartition(agent);

        return agent;
    }

    private void configurePartition(IdentityType identityType) {
        if (identityType != null && identityType.getPartition() != null) {
            Partition partition = this.partitionStore.lookupById(identityType.getPartition().getId());

            if (partition == null) {
                throw new IdentityManagementException("IdentityType is associated with a non-existent partition with id ["
                        + identityType.getPartition().getId() + "]");
            }

            identityType.setPartition(partition);
        }
    }

    @Override
    public User getUser(String loginName) {
        Agent agent = getAgent(loginName);

        if (!User.class.isInstance(agent)) {
            return null;
        }

        return (User) agent;
    }

    @Override
    public Role getRole(String roleName) {
        return lookupRole(roleName, getCurrentPartition());
    }

    @Override
    public Group getGroup(String groupName) {
        Group group = lookupGroup(groupName, getCurrentPartition());

        if (group != null && group.getParentGroup() != null) {
            group.setParentGroup(getGroup(group.getParentGroup().getName()));
        }

        return group;
    }

    @Override
    public Group getGroup(String name, Group parent) {
        Group group = getGroup(name);
        Group parentGroup = group.getParentGroup();

        if (parentGroup == null || !parentGroup.getName().equals(parent.getName())) {
            group = null;
        }

        return group;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
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
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        throw createNotImplementedYetException();
    }

    @Override
    public void setAttribute(IdentityType identityType, Attribute<? extends Serializable> attribute) {
        throw createNotImplementedYetException();
    }

    @Override
    public void removeAttribute(IdentityType identityType, String attributeName) {
        throw createNotImplementedYetException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query) {
        List<T> result = new ArrayList<T>();

        Class<T> relationshipType = query.getRelationshipType();
        List<FileRelationship> relationships = getRelationshipsForCurrentPartition().get(relationshipType.getName());

        if (relationships == null) {
            return result;
        }

        for (FileRelationship storedRelationship : relationships) {
            boolean match = false;

            if (query.getRelationshipType().getName().equals(storedRelationship.getType())) {
                Set<Entry<QueryParameter, Object[]>> parameters = query.getParameters().entrySet();

                for (Entry<QueryParameter, Object[]> entry : parameters) {
                    QueryParameter queryParameter = entry.getKey();
                    Object[] values = entry.getValue();

                    if (queryParameter instanceof RelationshipQueryParameter) {
                        RelationshipQueryParameter identityTypeParameter = (RelationshipQueryParameter) queryParameter;
                        int valuesMathCount = values.length;

                        IdentityType identityTypeRel = lookupIdentityTypeById(storedRelationship
                                .getIdentityTypeId(identityTypeParameter.getName()));

                        if (identityTypeRel != null) {
                            for (Object object : values) {
                                IdentityType identityType = (IdentityType) object;

                                if (identityTypeRel.getClass().isInstance(identityType)
                                        && identityTypeRel.getId().equals(identityType.getId())) {
                                    valuesMathCount--;
                                }
                            }

                            match = valuesMathCount <= 0;
                        }
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

            if (match) {
                result.add((T) convertToRelationship(storedRelationship));
            }
        }

        return result;
    }

    private IdentityType lookupIdentityTypeById(String identityTypeId) {
        IdentityQuery<IdentityType> query = new DefaultIdentityQuery<IdentityType>(IdentityType.class, this);

        query.setParameter(IdentityType.ID, identityTypeId);

        List<IdentityType> results = query.getResultList();

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    @Override
    public <T extends Relationship> int countQueryResults(RelationshipQuery<T> query) {
        throw createNotImplementedYetException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
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
                entries.addAll(getRolesForCurrentPartition().values());
                entries.addAll(getGroupsForCurrentPartition().values());
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
                entries = getRolesForCurrentPartition().values();
            } else {
                entries = getRolesForPartition(partition).values();
            }
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            if (partition == null) {
                entries = getGroupsForCurrentPartition().values();
            } else {
                entries = getGroupsForPartition(partition).values();
            }
        } else {
            throw createUnsupportedIdentityTypeException(identityTypeClass);
        }

        List<T> result = new ArrayList<T>();

        int typesCount = 0;

        for (Iterator<?> iterator = entries.iterator(); iterator.hasNext();) {
            IdentityType storedEntry = (IdentityType) iterator.next();

            if (!identityTypeClass.isAssignableFrom(storedEntry.getClass())) {
                continue;
            }
            
            typesCount++;

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

            FileIdentityQueryHelper queryHelper = new FileIdentityQueryHelper(identityQuery, this);

            if (!queryHelper.matchCreatedDateParameters(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchExpiryDateParameters(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchAttributes(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchHasRole(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchMemberOf(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchHasGroupRole(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchRolesOf(storedEntry)) {
                continue;
            }

            if (!queryHelper.matchHasMember(storedEntry)) {
                continue;
            }

            if (identityQuery.getLimit() > 0) {
                if (result.size() == identityQuery.getLimit()) {
                    return result;
                }

                if (identityQuery.getOffset() > 0) {
                    if (typesCount <= identityQuery.getOffset()) {
                        continue;
                    }
                }
            }

            configurePartition(storedEntry);
            
            result.add((T) storedEntry);
        }

        return result;
    }

    @Override
    public void storeCredential(Agent agent, CredentialStorage storage) {
        this.credentialStore.storeCredential(agent, storage);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
        return this.credentialStore.retrieveCredentials(agent, storageClass);
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        return this.credentialStore.retrieveCurrentCredential(agent, storageClass);
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        this.credentialStore.updateCredential(agent, credential, effectiveDate, expiryDate);
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        this.credentialStore.validateCredentials(credentials);
    }

    @Override
    public void createPartition(Partition partition) {
        this.partitionStore.createPartition(partition);
    }

    @Override
    public Realm getRealm(String realmName) {
        return this.partitionStore.getRealm(realmName);
    }

    @Override
    public Tier getTier(String tierName) {
        return this.partitionStore.getTier(tierName);
    }

    @Override
    public void removePartition(Partition partition) {
        this.partitionStore.removePartition(partition);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Relationship> T convertToRelationship(FileRelationship fileRelationship) {
        Class<T> relationshipType = null;

        try {
            relationshipType = (Class<T>) Class.forName(fileRelationship.getType());
        } catch (Exception e) {
            throw new IdentityManagementException("Could not find relationship type.");
        }

        return cloneRelationship(fileRelationship, relationshipType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Relationship> T cloneRelationship(FileRelationship fileRelationship, Class<?> relationshipType) {
        T clonedRelationship = null;

        try {
            clonedRelationship = (T) relationshipType.newInstance();

            Relationship storedRelationship = fileRelationship.getEntry();

            clonedRelationship.setId(storedRelationship.getId());

            List<Property<IdentityType>> relationshipIdentityTypes = PropertyQueries
                    .<IdentityType> createQuery(clonedRelationship.getClass())
                    .addCriteria(new AnnotatedPropertyCriteria(RelationshipIdentity.class)).getResultList();

            for (Property<IdentityType> annotatedProperty : relationshipIdentityTypes) {
                IdentityType identityType = lookupIdentityTypeById(fileRelationship.getIdentityTypeId(annotatedProperty
                        .getName()));

                if (identityType == null) {
                    return null;
                }

                annotatedProperty.setValue(clonedRelationship, identityType);
            }

            updateAttributedType(storedRelationship, clonedRelationship);
        } catch (Exception e) {
            throw new IdentityManagementException("Could not create relationship instance.", e);
        }

        return clonedRelationship;
    }

    /**
     * <p>
     * Looks up the given {@link Role}.
     * </p>
     * 
     * @param role
     * @return
     * @throws IdentityManagementException case the given {@link Role} does not exists.
     */
    private Role lookupRole(Role role) throws IdentityManagementException {
        if (role.getName() == null) {
            throw new IdentityManagementException("No name was provided.");
        }

        Role storedRole = getRole(role.getName());

        if (storedRole == null) {
            throw new IdentityManagementException("No Role found with the given name [" + role.getName()
                    + "] for the current Partition [" + getCurrentPartition().getName() + "].");
        }

        return storedRole;
    }

    /**
     * <p>
     * Looks up the given {@link Group}.
     * </p>
     * 
     * @param group
     * @return
     * @throws IdentityManagementException case the given {@link Role} does not exists.
     */
    private Group lookupGroup(Group group) throws IdentityManagementException {
        if (group.getName() == null) {
            throw new IdentityManagementException("No name was provided.");
        }

        Group storedGroup = getGroup(group.getName());

        if (storedGroup == null) {
            throw new IdentityManagementException("No Group found with the given name [" + group.getName()
                    + "] for the current Partition [" + getCurrentPartition().getName() + "].");
        }

        return storedGroup;
    }

    /**
     * <p>
     * Looks up the given {@link Agent}.
     * </p>
     * 
     * @param agent
     * @return
     * @throws IdentityManagementException case the given {@link Role} does not exists.
     */
    private Agent lookupAgent(Agent agent) throws IdentityManagementException {
        if (agent.getLoginName() == null) {
            throw new IdentityManagementException("No loginName was provided.");
        }

        Agent storedAgent = getAgent(agent.getLoginName());

        if (storedAgent == null) {
            throw new IdentityManagementException("No Agent found with the given loginName [" + agent.getLoginName()
                    + "] for the current Partition [" + getContext().getRealm().getName() + "]");
        }

        return storedAgent;
    }

    /**
     * <p>
     * Looks up the given {@link User}.
     * </p>
     * 
     * @param user
     * @return
     * @throws IdentityManagementException case the given {@link Role} does not exists.
     */
    private User lookupUser(User user) throws IdentityManagementException {
        if (user.getLoginName() == null) {
            throw new IdentityManagementException("No login name was provided.");
        }

        User storedUser = getUser(user.getLoginName());

        if (storedUser == null) {
            throw new IdentityManagementException("No User found with the given login name [" + user.getLoginName()
                    + "] for the current Partition [" + getContext().getRealm().getName() + "]");
        }

        return storedUser;
    }

    /**
     * <p>
     * Persists the given {@link Role} instance.
     * </p>
     * 
     * @param role
     */
    private void addRole(Role role) {
        if (role.getName() == null) {
            throw new IdentityManagementException("No name was provided.");
        }

        if (getRole(role.getName()) != null) {
            throw new IdentityManagementException("Role already exists with the given name [" + role.getName()
                    + "] for the given Partition [" + getCurrentPartition().getName() + "]");
        }

        Role fileRole = new SimpleRole(role.getName());

        fileRole.setPartition(getCurrentPartition());

        updateIdentityType(role, fileRole);

        storeRole(fileRole);
        getContext().getEventBridge().raiseEvent(new RoleCreatedEvent(role));
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
    private void addGroup(Group group) {
        if (group.getName() == null) {
            throw new IdentityManagementException("No name was provided.");
        }

        if (getGroup(group.getName()) != null) {
            throw new IdentityManagementException("Group already exists with the given name [" + group.getName()
                    + "] for the given Partition [" + getCurrentPartition().getName() + "]");
        }

        Group fileGroup = null;

        if (group.getParentGroup() != null) {
            Group lookupGroup = lookupGroup(group.getParentGroup());

            if (lookupGroup == null) {
                throw new IdentityManagementException("No parent group found with the given name ["
                        + group.getParentGroup().getName() + "].");
            }

            fileGroup = new SimpleGroup(group.getName(), lookupGroup);
        } else {
            fileGroup = new SimpleGroup(group.getName());
        }

        fileGroup.setPartition(getCurrentPartition());

        updateIdentityType(group, fileGroup);

        storeGroup(fileGroup);
        getContext().getEventBridge().raiseEvent(new GroupCreatedEvent(group));
    }

    private void storeGroup(Group fileGroup) {
        FilePartition partition = getDataSource().getPartition(fileGroup.getPartition().getId());

        partition.getGroups().put(fileGroup.getName(), new FileGroup(fileGroup));

        getDataSource().flushGroups(partition);
    }

    /**
     * <p>
     * Persists the given {@link User} instance.
     * </p>
     * 
     * @param user
     */
    private void addUser(User user) {
        User storedUser = new SimpleUser(user.getLoginName());

        storedUser.setFirstName(user.getFirstName());
        storedUser.setLastName(user.getLastName());
        storedUser.setEmail(user.getEmail());
        storedUser.setPartition(getContext().getRealm());

        updateIdentityType(user, storedUser);

        storeAgent(storedUser);
        getContext().getEventBridge().raiseEvent(new UserCreatedEvent(storedUser));
    }

    /**
     * <p>
     * Persists the given {@link Agent} instance.
     * </p>
     * 
     * @param agent
     */
    private void addAgent(Agent agent) {
        Agent storedAgent = new SimpleAgent(agent.getLoginName());

        storedAgent.setPartition(getContext().getRealm());

        updateIdentityType(agent, storedAgent);

        storeAgent(storedAgent);
        getContext().getEventBridge().raiseEvent(new AgentCreatedEvent(storedAgent));
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
    private void addRelationship(Relationship relationship) {
        if (relationship.getId() == null) {
            relationship.setId(getContext().getIdGenerator().generate());
        }

        Relationship newRelationship = null;

        try {
            newRelationship = relationship.getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
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

        getContext().getEventBridge().raiseEvent(new RelationshipCreatedEvent(relationship));
    }

    /**
     * <p>
     * Updates a previously stored {@link Role}.
     * </p>
     * 
     * @param updatedRole
     * @return
     */
    private void updateRole(Role updatedRole) {
        Role storedRole = lookupRole(updatedRole);

        if (storedRole != updatedRole) {
            updateIdentityType(updatedRole, storedRole);
        }

        storeRole(storedRole);
        getContext().getEventBridge().raiseEvent(new RoleUpdatedEvent(updatedRole));
    }

    /**
     * <p>
     * Updates a previously stored {@link Group}.
     * </p>
     * 
     * @param updatedGroup
     */
    private void updateGroup(Group updatedGroup) {
        Group storedGroup = lookupGroup(updatedGroup);

        if (storedGroup != updatedGroup) {
            updateIdentityType(updatedGroup, storedGroup);
        }

        storeGroup(storedGroup);
        getContext().getEventBridge().raiseEvent(new GroupUpdatedEvent(updatedGroup));
    }

    /**
     * <p>
     * Updates a previously stored {@link User}.
     * </p>
     * 
     * @param updatedUser
     * @return
     */
    private void updateUser(User updatedUser) {
        User storedUser = lookupUser(updatedUser);

        if (storedUser != updatedUser) {
            storedUser.setFirstName(updatedUser.getFirstName());
            storedUser.setLastName(updatedUser.getLastName());
            storedUser.setEmail(updatedUser.getEmail());

            updateIdentityType(updatedUser, storedUser);
        }

        storeAgent(storedUser);
        getContext().getEventBridge().raiseEvent(new UserUpdatedEvent(updatedUser));
    }

    /**
     * <p>
     * Updates a previously stored {@link Agent}.
     * </p>
     * 
     * @param updatedAgent
     * @return
     */
    private void updateAgent(Agent updatedAgent) {
        Agent storedAgent = lookupAgent(updatedAgent);

        if (storedAgent != updatedAgent) {
            updateIdentityType(updatedAgent, storedAgent);
        }

        storeAgent(storedAgent);
        getContext().getEventBridge().raiseEvent(new AgentUpdatedEvent(updatedAgent));
    }

    /**
     * <p>
     * Updates the given {@link Relationship} instance.
     * </p>
     * 
     * @param relationship
     */
    private void updateRelationship(Relationship relationship) {
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

        getContext().getEventBridge().raiseEvent(new RelationshipUpdatedEvent(relationship));
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

        List<Property<Serializable>> attributeProperties = PropertyQueries.<Serializable> createQuery(fromIdentityType.getClass())
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
     * @param groupName
     * @param partition
     * @return
     */
    private Group lookupGroup(String groupName, Partition partition) {
        Group group = getGroupsForPartition(partition).get(groupName);

        if (group == null) {
            if (Tier.class.isInstance(partition)) {
                Tier tier = (Tier) partition;

                if (tier.getParent() != null) {
                    group = lookupGroup(groupName, tier.getParent());
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
    private void removeRelationship(Relationship relationship) {
        List<FileRelationship> relationships = getDataSource().getRelationships().get(relationship.getClass().getName());

        for (FileRelationship fileRelationship : new ArrayList<FileRelationship>(relationships)) {
            Relationship storedRelationship = fileRelationship.getEntry();

            if (storedRelationship.getId().equals(relationship.getId())) {
                relationships.remove(fileRelationship);
            }
        }

        getDataSource().flushRelationships();
        getContext().getEventBridge().raiseEvent(new RelationshipDeletedEvent(relationship));
    }

    /**
     * <p>
     * Removes the given {@link Role}.
     * </p>
     * 
     * @param role
     * @return
     */
    private void removeRole(Role role) {
        Role storedRole = lookupRole(role);

        FilePartition partition = getDataSource().getPartition(storedRole.getPartition().getId());

        partition.getRoles().remove(storedRole.getName());

        getDataSource().flushRoles(partition);

        removeRelationships(storedRole);

        getContext().getEventBridge().raiseEvent(new RoleDeletedEvent(role));
    }

    /**
     * <p>
     * Removes the given {@link Group}.
     * </p>
     * 
     * @param group
     * @return
     */
    private void removeGroup(Group group) {
        Group storedGroup = lookupGroup(group);

        FilePartition partition = getDataSource().getPartition(storedGroup.getPartition().getId());

        partition.getGroups().remove(storedGroup.getName());

        getDataSource().flushGroups(partition);

        removeRelationships(storedGroup);

        getContext().getEventBridge().raiseEvent(new GroupDeletedEvent(group));
    }

    /**
     * <p>
     * Removes the given {@link Agent}.
     * </p>
     * 
     * @param agent
     */
    private void removeAgent(Agent agent) {
        Agent storedAgent = lookupAgent(agent);

        FilePartition partition = getDataSource().getPartition(storedAgent.getPartition().getId());

        partition.getAgents().remove(storedAgent.getLoginName());

        getDataSource().flushAgents(partition);
        removeRelationships(storedAgent);

        this.credentialStore.removeCredentials(storedAgent);

        if (IDMUtil.isUserType(agent.getClass())) {
            getContext().getEventBridge().raiseEvent(new UserDeletedEvent((User) agent));
        }

        getContext().getEventBridge().raiseEvent(new AgentDeletedEvent(agent));
    }

    public void removeRelationships(IdentityType identityType) {
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
     * Returns the stored {@link Relationship} instances for the current {@link Partition}.
     * </p>
     * 
     * @return
     */
    protected Map<String, List<FileRelationship>> getRelationshipsForCurrentPartition() {
        return getDataSource().getRelationships();
    }

    /**
     * <p>
     * Returns the stored {@link Role} instances for the given {@link Partition}.
     * </p>
     * 
     * @return
     */
    private Map<String, Role> getRolesForCurrentPartition() {
        return getDataSource().getRoles(getCurrentPartition());
    }

    /**
     * <p>
     * Returns the stored {@link Group} instances for the given {@link Partition}.
     * </p>
     * 
     * @return
     */
    private Map<String, Group> getGroupsForCurrentPartition() {
        return getDataSource().getGroups(getCurrentPartition());
    }

    /**
     * <p>
     * Returns the stored {@link Agent} instances for the given {@link Partition}.
     * </p>
     * 
     * @return
     */
    private Map<String, Agent> getAgentsForCurrentRealm() {
        return getDataSource().getAgents(getContext().getRealm());
    }

    private IdentityManagementException createNotImplementedYetException() {
        return new IdentityManagementException("Not implemented yet.");
    }

    private IdentityManagementException createUnsupportedIdentityTypeException(Class<? extends IdentityType> identityTypeClass) {
        return new IdentityManagementException("Unsupported IdentityType [" + identityTypeClass.getName() + "].");
    }

    private IdentityManagementException createUnsupportedAttributedType(Class<? extends AttributedType> type) {
        return new IdentityManagementException("Unsupported AttributedType [" + type.getName() + "].");
    }

    /**
     * <p>
     * Checks if the identifier for the given {@link AttributedType} is properly setted.
     * </p>
     * 
     * @param attributedType
     */
    private void checkNotNulId(AttributedType attributedType) throws IdentityManagementException {
        if (attributedType.getId() == null) {
            throw new IdentityManagementException("No identifier provided.");
        }
    }

    private FileDataSource getDataSource() {
        return getConfig().getDataSource();
    }

}
