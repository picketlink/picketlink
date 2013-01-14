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

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.credential.spi.annotations.Stored;
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
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityTypeQueryParameter;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

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
public class FileBasedIdentityStore implements IdentityStore<FileIdentityStoreConfiguration>, CredentialStore {

    private FileIdentityStoreConfiguration config;
    private IdentityStoreInvocationContext context;

    @Override
    public void setup(FileIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;
    }

    @Override
    public FileIdentityStoreConfiguration getConfig() {
        return this.config;
    }

    @Override
    public IdentityStoreInvocationContext getContext() {
        return this.context;
    }

    @Override
    public void add(AttributedType attributedType) {
        attributedType.setId(generateUUID());

        Object eventToFire = null;

        if (IdentityType.class.isInstance(attributedType)) {
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

            if (IDMUtil.isUserType(identityTypeClass)) {
                User storedUser = addUser((User) attributedType);

                eventToFire = new UserCreatedEvent(storedUser);
            } else if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent storedAgent = addAgent((Agent) attributedType);

                eventToFire = new AgentCreatedEvent(storedAgent);
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                Group storedGroup = addGroup((Group) attributedType);

                eventToFire = new GroupCreatedEvent(storedGroup);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                Role storedRole = addRole((Role) attributedType);

                eventToFire = new RoleCreatedEvent(storedRole);
            } else {
                throw new IdentityManagementException("Unsupported IdentityType [" + identityTypeClass.getName() + "].");
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;

            addRelationship(relationship);

            eventToFire = new RelationshipCreatedEvent(relationship);
        } else {
            throw new IdentityManagementException("Unsupported AttributedType [" + attributedType.getClass().getName() + "].");
        }

        getContext().getEventBridge().raiseEvent(eventToFire);
    }

    private void addRelationship(Relationship relationship) {
        List<Property<IdentityType>> relationshipIdentityTypes = PropertyQueries
                .<IdentityType> createQuery(relationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipIdentity.class)).getResultList();

        FileRelationshipStorage fileRelationship = new FileRelationshipStorage();
        
        fileRelationship.setId(relationship.getId());
        fileRelationship.setType(relationship.getClass().getName());

        for (Property<IdentityType> property : relationshipIdentityTypes) {
            fileRelationship.getIdentityTypes().put(property.getName(), property.getValue(relationship));
        }

        updateRelationshipAttributes(relationship, fileRelationship);

        List<FileRelationshipStorage> relationships = getConfig().getRelationships().get(relationship.getClass().getName());

        if (!getConfig().getRelationships().containsKey(relationship.getClass().getName())) {
            relationships = new ArrayList<FileRelationshipStorage>();
            getConfig().getRelationships().put(relationship.getClass().getName(), relationships);
        }

        relationships.add(fileRelationship);
        flushRelationships();
    }

    private void updateRelationshipAttributes(Relationship relationship, FileRelationshipStorage fileRelationship) {
        fileRelationship.getAttributes().clear();

        Collection<Attribute<? extends Serializable>> attributes = relationship.getAttributes();

        for (Attribute<? extends Serializable> attribute : attributes) {
            fileRelationship.getAttributes().put(attribute.getName(), attribute.getValue());
        }
    }

    @Override
    public void update(AttributedType attributedType) {
        Object eventToFire = null;

        if (IdentityType.class.isInstance(attributedType)) {
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

            if (IDMUtil.isUserType(identityTypeClass)) {
                User updatedUser = (User) attributedType;

                User storedUser = getStoredUser(updatedUser);

                updateUser(updatedUser, storedUser);

                eventToFire = new UserUpdatedEvent(storedUser);
            } else if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent updatedAgent = (Agent) attributedType;

                Agent storedAgent = getStoredAgent(updatedAgent);

                updateAgent(updatedAgent, storedAgent);

                eventToFire = new AgentUpdatedEvent(storedAgent);
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                Group updatedGroup = (Group) attributedType;

                Group storedGroup = getStoredGroup(updatedGroup);

                updateGroup(updatedGroup, storedGroup);

                eventToFire = new GroupUpdatedEvent(storedGroup);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                Role updatedRole = (Role) attributedType;

                Role storedRole = getStoredRole(updatedRole);

                updateRole(updatedRole, storedRole);

                eventToFire = new RoleUpdatedEvent(storedRole);
            } else {
                throw new IdentityManagementException("Unsupported IdentityType [" + identityTypeClass.getName() + "].");
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;

            List<FileRelationshipStorage> relationships = getConfig().getRelationships().get(
                    attributedType.getClass().getName());

            for (FileRelationshipStorage storedRelationship : new ArrayList<FileRelationshipStorage>(relationships)) {
                if (storedRelationship.getId().equals(relationship.getId())) {
                    updateAttributedType(relationship, convertToRelationship(storedRelationship));
                    updateRelationshipAttributes(relationship, storedRelationship);
                }
            }

            flushRelationships();

            eventToFire = new RelationshipUpdatedEvent(relationship);
        } else {
            throw new IdentityManagementException("Unsupported AttributedType [" + attributedType.getClass().getName() + "].");
        }

        getContext().getEventBridge().raiseEvent(eventToFire);
    }

    private Role getStoredRole(Role role) {
        if (role.getName() == null) {
            throw new IdentityManagementException("No identifier was provided.");
        }

        Role storedRole = getRole(role.getName());

        if (storedRole == null) {
            throw new RuntimeException("No role found with the given name [" + role.getName() + "].");
        }
        return storedRole;
    }

    private Group getStoredGroup(Group group) {
        if (group.getName() == null) {
            throw new IdentityManagementException("No identifier was provided.");
        }

        Group storedGroup = getGroup(group.getName());

        if (storedGroup == null) {
            throw new RuntimeException("No group found with the given name [" + group.getName() + "].");
        }
        return storedGroup;
    }

    private Agent getStoredAgent(Agent agent) {
        if (agent.getLoginName() == null) {
            throw new IdentityManagementException("No identifier was provided.");
        }

        Agent storedAgent = getAgent(agent.getLoginName());

        if (storedAgent == null) {
            throw new RuntimeException("Agent [" + agent.getLoginName() + "] does not exists.");
        }
        return storedAgent;
    }

    private User getStoredUser(User user) {
        if (user.getLoginName() == null) {
            throw new IdentityManagementException("No identifier was provided.");
        }

        User storedUser = getUser(user.getLoginName());

        if (storedUser == null) {
            throw new RuntimeException("User [" + user.getLoginName() + "] does not exists.");
        }

        return storedUser;
    }

    @Override
    public void remove(AttributedType attributedType) {
        Class<? extends IdentityType> attributedTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

        Object eventToFire = null;

        if (IdentityType.class.isInstance(attributedType)) {
            if (IDMUtil.isUserType(attributedTypeClass)) {
                User user = (User) attributedType;

                User storedUser = getStoredUser(user);

                removeUser(storedUser);

                eventToFire = new UserDeletedEvent(storedUser);
            } else if (IDMUtil.isAgentType(attributedTypeClass)) {
                Agent agent = (Agent) attributedType;

                Agent storedAgent = getStoredAgent(agent);

                removeAgent(storedAgent);

                eventToFire = new AgentDeletedEvent(storedAgent);
            } else if (IDMUtil.isGroupType(attributedTypeClass)) {
                Group group = (Group) attributedType;

                Group storedGroup = getStoredGroup(group);

                removeGroup(storedGroup);

                eventToFire = new GroupDeletedEvent(storedGroup);
            } else if (IDMUtil.isRoleType(attributedTypeClass)) {
                Role role = (Role) attributedType;

                Role storedRole = getStoredRole(role);

                removeRole(storedRole);

                eventToFire = new RoleDeletedEvent(storedRole);
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;

            List<FileRelationshipStorage> relationships = getConfig().getRelationships().get(attributedTypeClass.getName());

            for (FileRelationshipStorage storedRelationship : new ArrayList<FileRelationshipStorage>(relationships)) {
                if (storedRelationship.getId().equals(relationship.getId())) {
                    relationships.remove(storedRelationship);
                }
            }

            flushRelationships();

            eventToFire = new RelationshipDeletedEvent(relationship);
        } else {
            throw new IdentityManagementException("Unsupported AttributedType [" + attributedType.getClass().getName() + "].");
        }

        getContext().getEventBridge().raiseEvent(eventToFire);
    }

    private Role addRole(Role role) {
        SimpleRole fileRole = new SimpleRole(role.getName());

        updateIdentityType(role, fileRole);

        getConfig().getRoles().put(fileRole.getName(), fileRole);
        flushRoles();

        return fileRole;
    }

    private Group addGroup(Group group) {
        SimpleGroup fileGroup = null;

        if (group.getParentGroup() != null) {
            fileGroup = new SimpleGroup(group.getName(), getGroup(group.getParentGroup().getName()));
        } else {
            fileGroup = new SimpleGroup(group.getName());
        }

        updateIdentityType(group, fileGroup);

        getConfig().getGroups().put(fileGroup.getName(), fileGroup);
        flushGroups();

        return fileGroup;
    }

    private User addUser(User user) {
        User storedUser = new SimpleUser(user.getLoginName());

        storedUser.setFirstName(user.getFirstName());
        storedUser.setLastName(user.getLastName());
        storedUser.setEmail(user.getEmail());

        updateIdentityType(user, storedUser);

        getConfig().getAgents().put(storedUser.getLoginName(), storedUser);
        flushAgents();

        return storedUser;
    }

    private Agent addAgent(Agent user) {
        Agent storedAgent = new SimpleAgent(user.getLoginName());

        updateIdentityType(user, storedAgent);

        getConfig().getAgents().put(storedAgent.getLoginName(), storedAgent);
        flushAgents();

        return storedAgent;
    }

    private Role updateRole(Role updatedRole, Role storedRole) {
        if (storedRole != updatedRole) {
            updateIdentityType(updatedRole, storedRole);
        }

        getConfig().getRoles().put(storedRole.getName(), storedRole);
        flushRoles();

        return storedRole;
    }

    private Group updateGroup(Group updatedGroup, Group storedGroup) {
        if (storedGroup != updatedGroup) {
            updateIdentityType(updatedGroup, storedGroup);
        }

        getConfig().getGroups().put(storedGroup.getName(), storedGroup);
        flushGroups();

        return storedGroup;
    }

    private User updateUser(User updatedUser, User storedUser) {
        if (storedUser != updatedUser) {
            storedUser.setFirstName(updatedUser.getFirstName());
            storedUser.setLastName(updatedUser.getLastName());
            storedUser.setEmail(updatedUser.getEmail());

            updateIdentityType(updatedUser, storedUser);
        }

        getConfig().getAgents().put(storedUser.getLoginName(), storedUser);
        flushAgents();

        return updatedUser;
    }

    private Agent updateAgent(Agent updatedAgent, Agent storedAgent) {
        if (storedAgent != updatedAgent) {
            updateIdentityType(updatedAgent, storedAgent);
        }

        getConfig().getAgents().put(storedAgent.getLoginName(), storedAgent);
        flushAgents();

        return updatedAgent;
    }

    private Role removeRole(Role role) {
        getConfig().getRoles().remove(role.getName());

        removeRelationships(role);

        flushRoles();
        flushRelationships();

        return role;
    }

    private void removeRelationships(AttributedType role) {
        Set<Entry<String, List<FileRelationshipStorage>>> entrySet = getConfig().getRelationships().entrySet();

        for (Entry<String, List<FileRelationshipStorage>> entry : entrySet) {
            List<FileRelationshipStorage> relationships = entry.getValue();

            for (FileRelationshipStorage fileRelationshipStorage : new ArrayList<FileRelationshipStorage>(relationships)) {
                Collection<IdentityType> identityTypes = fileRelationshipStorage.getIdentityTypes().values();

                for (IdentityType identityType : identityTypes) {
                    if (role.getClass().isInstance(identityType)) {
                        if (role.getId().equals(identityType.getId())) {
                            remove(convertToRelationship(fileRelationshipStorage));
                        }
                    }
                }
            }
        }
    }

    private Group removeGroup(Group group) {
        getConfig().getGroups().remove(group.getName());

        removeRelationships(group);

        flushGroups();
        flushRelationships();

        return group;
    }

    private User removeUser(User user) {
        getConfig().getAgents().remove(user.getLoginName());

        removeRelationships(user);

        flushAgents();
        flushRelationships();

        return user;
    }

    private Agent removeAgent(Agent agent) {
        getConfig().getAgents().remove(agent.getLoginName());

        removeRelationships(agent);

        flushAgents();
        flushRelationships();

        return agent;
    }

    @Override
    public Agent getAgent(String loginName) {
        return getConfig().getAgents().get(loginName);
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
    public Role getRole(String role) {
        return getConfig().getRoles().get(role);
    }

    @Override
    public Group getGroup(String groupId) {
        return getConfig().getGroups().get(groupId);
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        Class<T> identityTypeClass = identityQuery.getIdentityType();

        Set<?> entries = null;

        if (IDMUtil.isUserType(identityTypeClass)) {
            entries = getConfig().getAgents().entrySet();
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            entries = getConfig().getRoles().entrySet();
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            entries = getConfig().getGroups().entrySet();
        } else if (IDMUtil.isAgentType(identityTypeClass)) {
            entries = getConfig().getAgents().entrySet();
        }

        List<T> result = new ArrayList<T>();

        for (Iterator<?> iterator = entries.iterator(); iterator.hasNext();) {
            Entry<String, IdentityType> entry = (Entry<String, IdentityType>) iterator.next();

            IdentityType storedIdentityType = entry.getValue();

            if (!identityTypeClass.isAssignableFrom(storedIdentityType.getClass())) {
                continue;
            }

            if (IDMUtil.isUserType(identityTypeClass)) {
                User user = (User) storedIdentityType;

                if (!isQueryParameterEquals(identityQuery.getParameters(), User.LOGIN_NAME, user.getLoginName())) {
                    continue;
                }

                if (!isQueryParameterEquals(identityQuery.getParameters(), User.EMAIL, user.getEmail())) {
                    continue;
                }

                if (!isQueryParameterEquals(identityQuery.getParameters(), User.FIRST_NAME, user.getFirstName())) {
                    continue;
                }

                if (!isQueryParameterEquals(identityQuery.getParameters(), User.LAST_NAME, user.getLastName())) {
                    continue;
                }
            }

            if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent agent = (Agent) storedIdentityType;

                if (!isQueryParameterEquals(identityQuery.getParameters(), Agent.LOGIN_NAME, agent.getLoginName())) {
                    continue;
                }
            }

            if (IDMUtil.isRoleType(identityTypeClass)) {
                Role role = (Role) storedIdentityType;

                if (!isQueryParameterEquals(identityQuery.getParameters(), Role.NAME, role.getName())) {
                    continue;
                }
            }

            if (IDMUtil.isGroupType(identityTypeClass)) {
                Group group = (Group) storedIdentityType;

                if (!isQueryParameterEquals(identityQuery.getParameters(), Group.NAME, group.getName())) {
                    continue;
                }

                String parentGroupName = null;

                if (group.getParentGroup() != null) {
                    parentGroupName = group.getParentGroup().getName();
                }

                if (!isQueryParameterEquals(identityQuery.getParameters(), Group.PARENT, parentGroupName)) {
                    continue;
                }
            }

            if (!isQueryParameterEquals(identityQuery.getParameters(), IdentityType.ENABLED, storedIdentityType.isEnabled())) {
                continue;
            }

            Date createdDate = storedIdentityType.getCreatedDate();

            if (createdDate != null) {
                if (!isQueryParameterEquals(identityQuery.getParameters(), IdentityType.CREATED_DATE, createdDate)) {
                    continue;
                }

                if (!isQueryParameterLessThan(identityQuery.getParameters(), IdentityType.CREATED_BEFORE, createdDate.getTime())) {
                    continue;
                }

                if (!isQueryParameterGreaterThan(identityQuery.getParameters(), IdentityType.CREATED_AFTER,
                        createdDate.getTime())) {
                    continue;
                }
            }

            Date expiryDate = storedIdentityType.getExpirationDate();

            if (!isQueryParameterEquals(identityQuery.getParameters(), IdentityType.EXPIRY_DATE, expiryDate)) {
                continue;
            }

            Long expiryDateInMillis = null;

            if (expiryDate != null) {
                expiryDateInMillis = expiryDate.getTime();
            }

            if (!isQueryParameterLessThan(identityQuery.getParameters(), IdentityType.EXPIRY_BEFORE, expiryDateInMillis)) {
                continue;
            }

            if (!isQueryParameterGreaterThan(identityQuery.getParameters(), IdentityType.EXPIRY_AFTER, expiryDateInMillis)) {
                continue;
            }

            result.add((T) storedIdentityType);
        }

        for (T storedEntry : new ArrayList<T>(result)) {
            Object[] values = identityQuery.getParameter(IdentityType.HAS_ROLE);

            if (values != null) {
                int valuesMatchCount = values.length;

                for (Object roleName : values) {
                    Role role = getRole(roleName.toString());

                    RelationshipQuery<Grant> query = new DefaultRelationshipQuery<Grant>(Grant.class, this);

                    query.setParameter(Grant.ASSIGNEE, storedEntry);
                    query.setParameter(Grant.ROLE, role);

                    List<Grant> relationships = query.getResultList();

                    if (!relationships.isEmpty()) {
                        valuesMatchCount--;
                    }
                }

                if (valuesMatchCount > 0) {
                    result.remove(storedEntry);
                }
            }

            values = identityQuery.getParameter(IdentityType.MEMBER_OF);

            if (values != null) {
                int valuesMatchCount = values.length;

                for (Object groupName : values) {
                    Group group = getGroup(groupName.toString());

                    RelationshipQuery<GroupMembership> query = new DefaultRelationshipQuery<GroupMembership>(
                            GroupMembership.class, this);

                    query.setParameter(GroupMembership.MEMBER, storedEntry);
                    query.setParameter(GroupMembership.GROUP, group);

                    List<GroupMembership> relationships = query.getResultList();

                    if (!relationships.isEmpty()) {
                        valuesMatchCount--;
                    }
                }

                if (valuesMatchCount > 0) {
                    result.remove(storedEntry);
                }
            }

            values = identityQuery.getParameter(IdentityType.HAS_GROUP_ROLE);

            if (values != null) {
                int valuesMatchCount = values.length;

                for (Object object : values) {
                    GroupRole groupRole = (GroupRole) object;

                    RelationshipQuery<GroupRole> query = new DefaultRelationshipQuery<GroupRole>(GroupRole.class, this);

                    query.setParameter(GroupRole.MEMBER, storedEntry);
                    query.setParameter(GroupRole.GROUP, groupRole.getGroup());
                    query.setParameter(GroupRole.ROLE, groupRole.getRole());

                    List<GroupRole> relationships = query.getResultList();

                    if (!relationships.isEmpty()) {
                        valuesMatchCount--;
                    }
                }

                if (valuesMatchCount > 0) {
                    result.remove(storedEntry);
                }
            }

            values = identityQuery.getParameter(IdentityType.ROLE_OF);

            if (values != null) {
                Role currentRole = (Role) storedEntry;

                List<FileRelationshipStorage> relationships = getConfig().getRelationships().get(Grant.class.getName());

                if (relationships == null) {
                    result.remove(storedEntry);
                } else {
                    int valuesMatchCount = values.length;

                    for (Object object : values) {
                        Agent agent = (Agent) object;

                        for (FileRelationshipStorage storedRelationship : new ArrayList<FileRelationshipStorage>(relationships)) {
                            Grant grant = convertToRelationship(storedRelationship);

                            if (!grant.getRole().getId().equals(currentRole.getId())) {
                                continue;
                            }

                            if (grant.getAssignee().getId().equals(agent.getId())) {
                                valuesMatchCount--;
                            }
                        }
                    }

                    if (valuesMatchCount > 0) {
                        result.remove(storedEntry);
                    }
                }
            }

            values = identityQuery.getParameter(IdentityType.HAS_MEMBER);

            if (values != null) {
                Group currentGroup = (Group) storedEntry;

                List<FileRelationshipStorage> relationships = getConfig().getRelationships().get(
                        GroupMembership.class.getName());

                if (relationships == null) {
                    result.remove(storedEntry);
                } else {
                    int valuesMatchCount = values.length;

                    for (Object object : values) {
                        Agent agent = (Agent) object;

                        for (FileRelationshipStorage storedRelationship : new ArrayList<FileRelationshipStorage>(relationships)) {
                            GroupMembership grant = convertToRelationship(storedRelationship);

                            if (!grant.getGroup().getId().equals(currentGroup.getId())) {
                                continue;
                            }

                            if (grant.getMember().getId().equals(agent.getId())) {
                                valuesMatchCount--;
                            }
                        }
                    }

                    if (valuesMatchCount > 0) {
                        result.remove(storedEntry);
                    }
                }
            }

        }

        findByCustomAttributes(result, identityQuery);

        return result;
    }

    private void findByCustomAttributes(List<? extends AttributedType> identityTypes, IdentityQuery<?> identityQuery) {
        Set<Entry<QueryParameter, Object[]>> entrySet = identityQuery.getParameters().entrySet();

        for (AttributedType fileUser : new ArrayList<AttributedType>(identityTypes)) {
            for (Entry<QueryParameter, Object[]> entry : entrySet) {
                QueryParameter queryParameter = entry.getKey();
                Object[] queryParameterValues = entry.getValue();

                if (AttributedType.AttributeParameter.class.isInstance(queryParameter) && queryParameterValues != null) {
                    AttributedType.AttributeParameter customParameter = (AttributedType.AttributeParameter) queryParameter;
                    Attribute<Serializable> userAttribute = fileUser.getAttribute(customParameter.getName());
                    boolean match = false;

                    if (userAttribute != null && userAttribute.getValue() != null) {
                        int count = queryParameterValues.length;

                        for (Object value : queryParameterValues) {
                            if (userAttribute.getValue().getClass().isArray()) {
                                Object[] userValues = (Object[]) userAttribute.getValue();

                                for (Object object : userValues) {
                                    if (object.equals(value)) {
                                        count--;
                                    }
                                }
                            } else {
                                if (value.equals(userAttribute.getValue())) {
                                    count--;
                                }
                            }
                        }

                        if (count <= 0) {
                            match = true;
                        }
                    }

                    if (!match) {
                        identityTypes.remove(fileUser);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Updated the common properties for a specific {@link IdentityType} instance from another instance.
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
    }

    private void updateAttributedType(AttributedType fromIdentityType, AttributedType toIdentityType) {
        toIdentityType.setId(fromIdentityType.getId());

        for (Object object : toIdentityType.getAttributes().toArray()) {
            Attribute<? extends Serializable> attribute = (Attribute<? extends Serializable>) object;
            toIdentityType.removeAttribute(attribute.getName());
        }

        for (Attribute<? extends Serializable> attrib : fromIdentityType.getAttributes()) {
            toIdentityType.setAttribute(attrib);
        }
    }

    private boolean isQueryParameterEquals(Map<QueryParameter, Object[]> parameters, QueryParameter queryParameter,
            Serializable valueToCompare) {
        Object[] values = parameters.get(queryParameter);

        if (values == null) {
            return true;
        }

        Object value = values[0];

        if (Date.class.isInstance(valueToCompare)) {
            Date parameterDate = (Date) value;
            value = parameterDate.getTime();

            Date toCompareDate = (Date) valueToCompare;
            valueToCompare = toCompareDate.getTime();
        }

        if (values.length > 0 && valueToCompare != null && valueToCompare.equals(value)) {
            return true;
        }

        return false;
    }

    private boolean isQueryParameterEquals(Map<QueryParameter, Object[]> parameters, QueryParameter queryParameter,
            Date valueToCompare) {
        Object[] values = parameters.get(queryParameter);

        if (values == null) {
            return true;
        }
        if (values.length > 0 && valueToCompare != null && valueToCompare.equals(values[0])) {
            return true;
        }

        return false;
    }

    private boolean isQueryParameterGreaterThan(Map<QueryParameter, Object[]> parameters, QueryParameter queryParameter,
            Long valueToCompare) {
        return isQueryParameterGreaterOrLessThan(parameters, queryParameter, valueToCompare, true);
    }

    private boolean isQueryParameterLessThan(Map<QueryParameter, Object[]> parameters, QueryParameter queryParameter,
            Long valueToCompare) {
        return isQueryParameterGreaterOrLessThan(parameters, queryParameter, valueToCompare, false);
    }

    private boolean isQueryParameterGreaterOrLessThan(Map<QueryParameter, Object[]> parameters, QueryParameter queryParameter,
            Long valueToCompare, boolean greaterThan) {
        Object[] values = parameters.get(queryParameter);

        if (values == null) {
            return true;
        }

        long value = 0;

        if (Date.class.isInstance(values[0])) {
            Date parameterDate = (Date) values[0];
            value = parameterDate.getTime();
        } else {
            value = Long.valueOf(values[0].toString());
        }

        if (values.length > 0 && valueToCompare != null) {
            if (greaterThan && valueToCompare >= value) {
                return true;
            }

            if (!greaterThan && valueToCompare <= value) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        CredentialHandler handler = getContext().getCredentialValidator(credentials.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for validating Credentials of type [" + credentials.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.validate(credentials, this);
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        CredentialHandler handler = getContext().getCredentialUpdater(credential.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for updating Credentials of type [" + credential.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.update(agent, credential, this, effectiveDate, expiryDate);
    }

    @Override
    public void storeCredential(Agent agent, CredentialStorage storage) {
        Map<String, List<FileCredentialStorage>> agentCredentials = getConfig().getCredentials().get(agent.getLoginName());

        if (agentCredentials == null) {
            agentCredentials = new HashMap<String, List<FileCredentialStorage>>();
        }

        List<FileCredentialStorage> credentials = agentCredentials.get(storage.getClass().getName());

        if (credentials == null) {
            credentials = new ArrayList<FileCredentialStorage>();
        }

        for (FileCredentialStorage fileCredentialStorage : credentials) {
            if (isCurrentCredential(fileCredentialStorage)) {
                fileCredentialStorage.setExpiryDate(new Date());
            }
        }

        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storage.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        FileCredentialStorage credential = new FileCredentialStorage();

        for (Property<Object> property : annotatedTypes) {
            credential.getStoredFields().put(property.getName(), (Serializable) property.getValue(storage));
        }

        if (credential.getEffectiveDate() == null) {
            credential.setEffectiveDate(new Date());
        }

        credentials.add(credential);
        agentCredentials.put(storage.getClass().getName(), credentials);
        getConfig().getCredentials().put(agent.getLoginName(), agentCredentials);

        flushCredentials();
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        Map<String, List<FileCredentialStorage>> agentCredentials = getConfig().getCredentials().get(agent.getLoginName());

        if (agentCredentials == null) {
            agentCredentials = new HashMap<String, List<FileCredentialStorage>>();
        }

        List<FileCredentialStorage> credentials = agentCredentials.get(storageClass.getName());

        if (credentials != null) {
            for (FileCredentialStorage fileCredentialStorage : credentials) {
                if (isCurrentCredential(fileCredentialStorage)) {
                    return convertToCredentialStorage(storageClass, fileCredentialStorage);
                }
            }
        }

        return null;
    }

    private boolean isCurrentCredential(FileCredentialStorage fileCredentialStorage) {
        boolean isCurrent = true;

        Date actualDate = new Date();

        if (fileCredentialStorage.getEffectiveDate() != null) {
            if (fileCredentialStorage.getEffectiveDate().compareTo(actualDate) > 0) {
                isCurrent = false;
            }
        }

        if (isCurrent) {
            if (fileCredentialStorage.getExpiryDate() != null) {
                if (fileCredentialStorage.getExpiryDate().compareTo(actualDate) <= 0) {
                    isCurrent = false;
                }
            }
        }

        return isCurrent;
    }

    private <T> T convertToCredentialStorage(Class<T> storageClass, FileCredentialStorage fileCredentialStorage) {
        T storage = null;

        try {
            storage = storageClass.newInstance();
        } catch (Exception e) {
            throw new IdentityManagementException("Could not create CredentialStorage instance for class ["
                    + storageClass.getName() + "].", e);
        }

        Set<Entry<String, Serializable>> storedFieldsEntrySet = fileCredentialStorage.getStoredFields().entrySet();

        for (Entry<String, Serializable> storedFieldEntry : storedFieldsEntrySet) {
            List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storageClass)
                    .addCriteria(new NamedPropertyCriteria(storedFieldEntry.getKey())).getResultList();

            if (annotatedTypes.isEmpty()) {
                throw new IdentityManagementException("Could not find property [" + storedFieldEntry.getKey()
                        + "] on CredentialStorage [" + storageClass.getName() + "].");
            } else if (annotatedTypes.size() > 1) {
                throw new IdentityManagementException("Ambiguos property [" + storedFieldEntry.getKey()
                        + "] on CredentialStorage [" + storageClass.getName() + "].");
            }

            Property<Object> property = annotatedTypes.get(0);

            property.setValue(storage, storedFieldEntry.getValue());
        }
        return storage;
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
        ArrayList<T> storedCredentials = new ArrayList<T>();

        Map<String, List<FileCredentialStorage>> agentCredentials = getConfig().getCredentials().get(agent.getLoginName());

        if (agentCredentials == null) {
            agentCredentials = new HashMap<String, List<FileCredentialStorage>>();
        }

        List<FileCredentialStorage> credentials = agentCredentials.get(storageClass.getName());

        if (credentials != null) {
            for (FileCredentialStorage fileCredentialStorage : credentials) {
                storedCredentials.add(convertToCredentialStorage(storageClass, fileCredentialStorage));
            }
        }

        return storedCredentials;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        throw createNotImplementedYetException();
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

    private IdentityManagementException createNotImplementedYetException() {
        return new IdentityManagementException("Not implemented yet.");
    }

    /**
     * <p>
     * Flush all changes made to agents to the filesystem.
     * </p>
     */
    synchronized void flushAgents() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getAgentsFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getAgents());
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing agent changes to file system.", e);
        }
    }

    /**
     * <p>
     * Flush all changes made to roles to the filesystem.
     * </p>
     */
    synchronized void flushRoles() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getRolesFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getRoles());
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing agent changes to file system.", e);
        }
    }

    /**
     * <p>
     * Flush all changes made to groups to the filesystem.
     * </p>
     */
    synchronized void flushGroups() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getGroupsFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getGroups());
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing agent changes to file system.", e);
        }
    }

    /**
     * <p>
     * Flush all changes made to memberships to the filesystem.
     * </p>
     */
    synchronized void flushRelationships() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getMembershipsFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getRelationships());
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing agent changes to file system.", e);
        }
    }

    /**
     * <p>
     * Flush all changes made to credentials to the filesystem.
     * </p>
     */
    synchronized void flushCredentials() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getCredentialsFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getCredentials());
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing agent changes to file system.", e);
        }
    }

    @Override
    public <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query) {
        List<T> result = new ArrayList<T>();

        Class<T> relationshipType = query.getRelationshipType();
        List<FileRelationshipStorage> relationships = getConfig().getRelationships().get(relationshipType.getName());

        if (relationships == null) {
            return result;
        }

        for (FileRelationshipStorage storedRelationship : relationships) {
            boolean match = false;

            if (query.getRelationshipType().getName().equals(storedRelationship.getType())) {
                Set<Entry<QueryParameter, Object[]>> parameters = query.getParameters().entrySet();

                for (Entry<QueryParameter, Object[]> entry : parameters) {
                    QueryParameter queryParameter = entry.getKey();
                    Object[] values = entry.getValue();

                    if (entry.getKey() instanceof IdentityTypeQueryParameter) {
                        IdentityTypeQueryParameter identityTypeParameter = (IdentityTypeQueryParameter) entry.getKey();
                        int valuesMathCount = values.length;

                        IdentityType identityTypeRel = storedRelationship.getIdentityTypes().get(
                                identityTypeParameter.getName());

                        if (IdentityTypeQueryParameter.class.isInstance(identityTypeParameter) && identityTypeRel != null) {
                            for (Object object : values) {
                                IdentityType identityType = (IdentityType) object;

                                if (identityTypeRel.getClass().isInstance(identityType)
                                        && identityTypeRel.getId().equals(identityType.getId())) {
                                    valuesMathCount--;
                                }
                            }
                        }

                        match = valuesMathCount <= 0;
                    }

                    if (AttributedType.AttributeParameter.class.isInstance(queryParameter) && values != null) {
                        AttributedType.AttributeParameter customParameter = (AttributedType.AttributeParameter) queryParameter;
                        Serializable userAttributeValue = storedRelationship.getAttributes().get(customParameter.getName());

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

    private <T extends Relationship> T convertToRelationship(FileRelationshipStorage storedRelationship) {
        T relationship = null;
        Class<T> relationshipType = null;

        try {
            relationshipType = (Class<T>) Class.forName(storedRelationship.getType());
        } catch (ClassNotFoundException e1) {
            throw new IdentityManagementException("Could not get Relationship type [" + storedRelationship.getType() + "]");
        }

        try {
            relationship = relationshipType.newInstance();

            relationship.setId(storedRelationship.getId());

            Set<Entry<String, IdentityType>> identityTypes = storedRelationship.getIdentityTypes().entrySet();

            for (Entry<String, IdentityType> entry : identityTypes) {
                List<Property<IdentityType>> annotatedTypes = PropertyQueries.<IdentityType> createQuery(relationshipType)
                        .addCriteria(new NamedPropertyCriteria(entry.getKey())).getResultList();

                Property<IdentityType> property = annotatedTypes.get(0);

                property.setValue(relationship, entry.getValue());
            }

            Set<Entry<String, Serializable>> attributes = storedRelationship.getAttributes().entrySet();

            for (Entry<String, Serializable> entry : attributes) {
                relationship.setAttribute(new Attribute<Serializable>(entry.getKey(), entry.getValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relationship;
    }

    @Override
    public <T extends Relationship> int countQueryResults(RelationshipQuery<T> query) {
        return 0;
    }

    private String generateUUID() {
        return getContext().getIdGenerator().generate();
    }
}
