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
import org.picketlink.idm.model.IdentityType.AttributeParameter;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
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
        if (IdentityType.class.isInstance(attributedType)) {
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) attributedType.getClass();

            if (IDMUtil.isUserType(identityTypeClass)) {
                User storedUser = addUser((User) attributedType);

                UserCreatedEvent event = new UserCreatedEvent(storedUser);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent storedAgent = addAgent((Agent) attributedType);

                AgentCreatedEvent event = new AgentCreatedEvent(storedAgent);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                Group storedGroup = addGroup((Group) attributedType);

                GroupCreatedEvent event = new GroupCreatedEvent(storedGroup);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedGroup);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                Role storedRole = addRole((Role) attributedType);

                RoleCreatedEvent event = new RoleCreatedEvent(storedRole);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedRole);
                getContext().getEventBridge().raiseEvent(event);
            } else {
                throw new IdentityManagementException("Unsupported IdentityType [" + identityTypeClass.getName() + "].");
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            List<Property<IdentityType>> annotatedTypes = PropertyQueries.<IdentityType> createQuery(attributedType.getClass())
                    .addCriteria(new AnnotatedPropertyCriteria(RelationshipIdentity.class)).getResultList();

            FileRelationshipStorage fileRelationship = new FileRelationshipStorage();

            for (Property<IdentityType> property : annotatedTypes) {
                fileRelationship.getIdentityTypes().put(property.getName(), property.getValue(attributedType));
            }

            Collection<Attribute<? extends Serializable>> attributes = attributedType.getAttributes();

            for (Attribute<? extends Serializable> attribute : attributes) {
                fileRelationship.getAttributes().put(attribute.getName(), attribute.getValue().toString());
            }

            List<FileRelationshipStorage> relationships = getConfig().getRelationships().get(
                    attributedType.getClass().getName());

            if (!getConfig().getRelationships().containsKey(attributedType.getClass().getName())) {
                relationships = new ArrayList<FileRelationshipStorage>();
                getConfig().getRelationships().put(attributedType.getClass().getName(), relationships);
            }

            relationships.add(fileRelationship);
            flushRelationships();
        } else {
            throw new IdentityManagementException("Unsupported AttributedType [" + attributedType.getClass().getName() + "].");
        }
    }

    @Override
    public void update(AttributedType identityType) {
        if (IdentityType.class.isInstance(identityType)) {
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) identityType.getClass();

            if (IDMUtil.isUserType(identityTypeClass)) {
                User updatedUser = (User) identityType;

                if (updatedUser.getLoginName() == null) {
                    throw new IdentityManagementException("No identifier was provided.");
                }

                User storedUser = getUser(updatedUser.getLoginName());

                if (storedUser == null) {
                    throw new RuntimeException("User [" + updatedUser.getLoginName() + "] does not exists.");
                }

                updateUser(updatedUser, storedUser);

                UserUpdatedEvent event = new UserUpdatedEvent(storedUser);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent updatedAgent = (Agent) identityType;

                if (updatedAgent.getLoginName() == null) {
                    throw new IdentityManagementException("No identifier was provided.");
                }

                Agent storedAgent = getAgent(updatedAgent.getLoginName());

                if (storedAgent == null) {
                    throw new RuntimeException("Agent [" + updatedAgent.getLoginName() + "] does not exists.");
                }

                updateAgent(updatedAgent, storedAgent);

                AgentUpdatedEvent event = new AgentUpdatedEvent(storedAgent);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                Group updatedGroup = (Group) identityType;

                if (updatedGroup.getName() == null) {
                    throw new IdentityManagementException("No identifier was provided.");
                }

                Group storedGroup = getGroup(updatedGroup.getName());

                if (storedGroup == null) {
                    throw new RuntimeException("No group found with the given name [" + updatedGroup.getName() + "].");
                }

                updateGroup(updatedGroup, storedGroup);

                GroupUpdatedEvent event = new GroupUpdatedEvent(storedGroup);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedGroup);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                Role updatedRole = (Role) identityType;

                if (updatedRole.getName() == null) {
                    throw new IdentityManagementException("No identifier was provided.");
                }

                Role storedRole = getRole(updatedRole.getName());

                if (storedRole == null) {
                    throw new RuntimeException("No role found with the given name [" + updatedRole.getName() + "].");
                }

                updateRole(updatedRole, storedRole);

                RoleUpdatedEvent event = new RoleUpdatedEvent(storedRole);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedRole);
                getContext().getEventBridge().raiseEvent(event);
            } else {
                throw new IdentityManagementException("Unsupported IdentityType [" + identityTypeClass.getName() + "].");
            }
        }
    }

    @Override
    public void remove(AttributedType identityType) {
        if (IdentityType.class.isInstance(identityType)) {
            Class<? extends IdentityType> identityTypeClass = (Class<? extends IdentityType>) identityType.getClass();

            if (IDMUtil.isUserType(identityTypeClass)) {
                User user = (User) identityType;

                if (user.getLoginName() == null) {
                    throw new IdentityManagementException("No identifier was provided.");
                }

                User storedUser = getUser(user.getLoginName());

                if (storedUser == null) {
                    throw new RuntimeException("User [" + user.getLoginName() + "] doest not exists.");
                }

                removeUser(storedUser);

                UserDeletedEvent event = new UserDeletedEvent(storedUser);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isAgentType(identityTypeClass)) {
                Agent agent = (Agent) identityType;

                if (agent.getLoginName() == null) {
                    throw new IdentityManagementException("No identifier was provided.");
                }

                Agent storedAgent = getAgent(agent.getLoginName());

                if (storedAgent == null) {
                    throw new RuntimeException("Agent [" + agent.getLoginName() + "] doest not exists.");
                }

                removeAgent(storedAgent);

                AgentDeletedEvent event = new AgentDeletedEvent(storedAgent);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isGroupType(identityTypeClass)) {
                Group group = (Group) identityType;

                if (group.getName() == null) {
                    throw new IdentityManagementException("No identifier was provided.");
                }

                Group storedGroup = getGroup(group.getName());

                if (storedGroup == null) {
                    throw new RuntimeException("Group [" + group.getName() + "] doest not exists.");
                }

                removeGroup(storedGroup);

                GroupDeletedEvent event = new GroupDeletedEvent(storedGroup);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedGroup);
                getContext().getEventBridge().raiseEvent(event);
            } else if (IDMUtil.isRoleType(identityTypeClass)) {
                Role role = (Role) identityType;

                if (role.getName() == null) {
                    throw new IdentityManagementException("No identifier was provided.");
                }

                Role storedRole = getRole(role.getName());

                if (storedRole == null) {
                    throw new RuntimeException("Role [" + role.getName() + "] doest not exists.");
                }

                removeRole(storedRole);

                RoleDeletedEvent event = new RoleDeletedEvent(storedRole);
                // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedRole);
                getContext().getEventBridge().raiseEvent(event);
            }
        }
    }

    private Role addRole(Role role) {
        SimpleRole fileRole = new SimpleRole(role.getName());

        updateCommonProperties(role, fileRole);

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

        updateCommonProperties(group, fileGroup);

        getConfig().getGroups().put(fileGroup.getName(), fileGroup);
        flushGroups();

        return fileGroup;
    }

    private User addUser(User user) {
        User storedUser = new SimpleUser(user.getLoginName());

        storedUser.setFirstName(user.getFirstName());
        storedUser.setLastName(user.getLastName());
        storedUser.setEmail(user.getEmail());

        updateCommonProperties(user, storedUser);

        getConfig().getUsers().put(storedUser.getLoginName(), storedUser);
        flushUsers();

        return storedUser;
    }

    private Agent addAgent(Agent user) {
        Agent storedAgent = new SimpleAgent(user.getLoginName());

        updateCommonProperties(user, storedAgent);

        getConfig().getUsers().put(storedAgent.getLoginName(), storedAgent);
        flushUsers();

        return storedAgent;
    }

    private Role updateRole(Role updatedRole, Role storedRole) {
        if (storedRole != updatedRole) {
            updateCommonProperties(updatedRole, storedRole);
        }

        getConfig().getRoles().put(storedRole.getName(), storedRole);
        flushRoles();

        return storedRole;
    }

    private Group updateGroup(Group updatedGroup, Group storedGroup) {
        if (storedGroup != updatedGroup) {
            updateCommonProperties(updatedGroup, storedGroup);
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

            updateCommonProperties(updatedUser, storedUser);
        }

        getConfig().getUsers().put(storedUser.getLoginName(), storedUser);
        flushUsers();

        return updatedUser;
    }

    private Agent updateAgent(Agent updatedAgent, Agent storedAgent) {
        if (storedAgent != updatedAgent) {
            updateCommonProperties(updatedAgent, storedAgent);
        }

        getConfig().getUsers().put(storedAgent.getLoginName(), storedAgent);
        flushUsers();

        return updatedAgent;
    }

    private Role removeRole(Role role) {
        getConfig().getRoles().remove(role.getName());

        // for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
        // Role roleMembership = membership.getRole();
        //
        // if (roleMembership != null && roleMembership.getName().equals(role.getName())) {
        // getConfig().getMemberships().remove(membership);
        // }
        // }

        flushRoles();
        flushRelationships();

        return role;
    }

    private Group removeGroup(Group group) {
        getConfig().getGroups().remove(group.getName());

        // for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
        // Group groupMembership = membership.getGroup();
        //
        // if (groupMembership != null && groupMembership.getName().equals(group.getName())) {
        // getConfig().getMemberships().remove(membership);
        // }
        // }

        flushGroups();
        flushRelationships();

        return group;
    }

    private User removeUser(User user) {
        getConfig().getUsers().remove(user.getLoginName());

        // for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
        // IdentityType member = membership.getMember();
        //
        // if (IDMUtil.isUserType(member.getClass())) {
        // User userMember = (User) member;
        //
        // if (userMember.getLoginName().equals(user.getLoginName())) {
        // getConfig().getMemberships().remove(membership);
        // }
        // }
        // }

        flushUsers();
        flushRelationships();

        return user;
    }

    private Agent removeAgent(Agent user) {
        getConfig().getUsers().remove(user.getLoginName());

        // for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
        // IdentityType member = membership.getMember();
        //
        // if (IDMUtil.isAgentType(member.getClass())) {
        // Agent userMember = (Agent) member;
        //
        // if (userMember.getLoginName().equals(user.getLoginName())) {
        // getConfig().getMemberships().remove(membership);
        // }
        // }
        // }

        flushUsers();
        flushRelationships();

        return user;
    }

    @Override
    public Agent getAgent(String loginName) {
        return getConfig().getUsers().get(loginName);
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
            entries = getConfig().getUsers().entrySet();
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            entries = getConfig().getRoles().entrySet();
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            entries = getConfig().getGroups().entrySet();
        } else if (IDMUtil.isAgentType(identityTypeClass)) {
            entries = getConfig().getUsers().entrySet();
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

        if (identityQuery.getParameters().containsKey(IdentityType.HAS_ROLE)
                || identityQuery.getParameters().containsKey(IdentityType.MEMBER_OF)
                || identityQuery.getParameters().containsKey(IdentityType.HAS_GROUP_ROLE)
                || identityQuery.getParameters().containsKey(IdentityType.ROLE_OF)
                || identityQuery.getParameters().containsKey(IdentityType.HAS_MEMBER)) {

            for (T fileUser : new ArrayList<T>(result)) {
                for (Entry<QueryParameter, Object[]> parameters : identityQuery.getParameters().entrySet()) {
                    QueryParameter queryParameter = parameters.getKey();
                    Object[] values = parameters.getValue();

                    int valuesMatchCount = values.length;

                    // for (GroupRole membership : getConfig().getRelationships()) {
                    // if (IDMUtil.isAgentType(fileUser.getClass()) && IDMUtil.isAgentType(membership.getMember().getClass())) {
                    // Agent selectedAgent = (Agent) fileUser;
                    // Agent memberAgent = (Agent) membership.getMember();
                    //
                    // if (!selectedAgent.getLoginName().equals(memberAgent.getLoginName())) {
                    // continue;
                    // }
                    // }
                    //
                    // if (queryParameter.equals(IdentityType.HAS_GROUP_ROLE) && membership.getGroup() != null
                    // && membership.getRole() != null) {
                    // for (Object groupNames : values) {
                    // GroupRole groupRole = (GroupRole) groupNames;
                    //
                    // if (groupRole.getGroup().getName().equals(membership.getGroup().getName())
                    // && groupRole.getRole().getName().equals(membership.getRole().getName())) {
                    // valuesMatchCount--;
                    // }
                    // }
                    // } else if (queryParameter.equals(IdentityType.HAS_ROLE) && membership.getRole() != null) {
                    // for (Object roleNames : values) {
                    // if (roleNames.equals(membership.getRole().getName())) {
                    // valuesMatchCount--;
                    // }
                    // }
                    // } else if (queryParameter.equals(IdentityType.MEMBER_OF) && membership.getGroup() != null) {
                    // for (Object groupNames : values) {
                    // if (groupNames.equals(membership.getGroup().getName())) {
                    // valuesMatchCount--;
                    // }
                    // }
                    // } else if (queryParameter.equals(IdentityType.ROLE_OF) && membership.getRole() != null) {
                    // for (Object member : values) {
                    // Agent agent = (Agent) member;
                    //
                    // if (agent != null && agent.getKey().equals(membership.getMember().getKey())
                    // && membership.getRole().getKey().equals(fileUser.getKey())) {
                    // valuesMatchCount--;
                    // }
                    // }
                    // } else if (queryParameter.equals(IdentityType.HAS_MEMBER) && membership.getGroup() != null) {
                    // for (Object member : values) {
                    // Agent agent = (Agent) member;
                    //
                    // if (agent != null && agent.getKey().equals(membership.getMember().getKey())
                    // && membership.getGroup().getKey().equals(fileUser.getKey())) {
                    // valuesMatchCount--;
                    // }
                    // }
                    // }
                    // }

                    if (valuesMatchCount > 0) {
                        result.remove(fileUser);
                    }
                }
            }
        }

        findByCustomAttributes(result, identityQuery);

        return result;
    }

    @SuppressWarnings("rawtypes")
    private void findByCustomAttributes(List<? extends IdentityType> identityTypes, IdentityQuery identityQuery) {
        Set<Entry<QueryParameter, Object[]>> entrySet = identityQuery.getParameters().entrySet();

        for (IdentityType fileUser : new ArrayList<IdentityType>(identityTypes)) {
            for (Entry<QueryParameter, Object[]> entry : entrySet) {
                QueryParameter queryParameter = entry.getKey();
                Object[] queryParameterValues = entry.getValue();

                if (IdentityType.AttributeParameter.class.isInstance(queryParameter) && queryParameterValues != null) {
                    IdentityType.AttributeParameter customParameter = (AttributeParameter) queryParameter;
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
    private void updateCommonProperties(IdentityType fromIdentityType, IdentityType toIdentityType) {
        toIdentityType.setEnabled(fromIdentityType.isEnabled());
        toIdentityType.setCreatedDate(fromIdentityType.getCreatedDate());
        toIdentityType.setExpirationDate(fromIdentityType.getExpirationDate());

        for (Object object : toIdentityType.getAttributes().toArray()) {
            Attribute<? extends Serializable> attribute = (Attribute<? extends Serializable>) object;
            toIdentityType.removeAttribute(attribute.getName());
        }

        for (Attribute<? extends Serializable> attrib : fromIdentityType.getAttributes()) {
            toIdentityType.setAttribute(attrib);
        }
    }

    /**
     * <p>
     * Checks if the given {@link GroupRole} instance has the provide {@link Group} and {@link Role} combination.
     * </p>
     * 
     * @param membership
     * @param group
     * @param role
     * @return
     */
    private boolean hasGroupRole(GroupRole membership, Group group, Role role) {
        boolean match = false;

        if (role != null && group != null) {
            match = membership.getRole() != null && role.getName().equals(membership.getRole().getName())
                    && membership.getGroup() != null && group.getName().equals(membership.getGroup().getName());
        } else if (group != null) {
            match = membership.getGroup() != null && group.getName().equals(membership.getGroup().getName());
        } else if (role != null) {
            match = membership.getRole() != null && role.getName().equals(membership.getRole().getName());
        }

        return match;
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
            if (fileCredentialStorage.getEffectiveDate().after(actualDate)
                    && fileCredentialStorage.getEffectiveDate().compareTo(actualDate) != 0) {
                isCurrent = false;
            }
        }

        if (fileCredentialStorage.getExpiryDate() != null) {
            if (fileCredentialStorage.getExpiryDate().before(actualDate)
                    && fileCredentialStorage.getExpiryDate().compareTo(actualDate) != 0) {
                isCurrent = false;
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
     * Flush all changes made to users to the filesystem.
     * </p>
     */
    synchronized void flushUsers() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getUsersFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getUsers());
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @Override
    public <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query) {
        List<T> result = new ArrayList<T>();
        
        List<FileRelationshipStorage> relationships = getConfig().getRelationships().get(query.getRelationshipType().getName());

        for (FileRelationshipStorage storedRelationship : relationships) {
            if (GroupRole.class.isAssignableFrom(query.getRelationshipType())) {
                Agent member = (Agent) query.getParameter(GroupRole.MEMBER)[0];
                Role role = (Role) query.getParameter(GroupRole.ROLE)[0];
                Group group = (Group) query.getParameter(GroupRole.GROUP)[0];

                Agent memberRel = (Agent) storedRelationship.getIdentityTypes().get(GroupRole.MEMBER.getName());

                if (memberRel == null || member == null || !member.getLoginName().equals(memberRel.getLoginName())) {
                    continue;
                }

                Role roleRel = (Role) storedRelationship.getIdentityTypes().get(GroupRole.ROLE.getName());

                if (roleRel == null || role == null || !role.getName().equals(roleRel.getName())) {
                    continue;
                }
                
                Group groupRel = (Group) storedRelationship.getIdentityTypes().get(GroupRole.GROUP.getName());
                
                if (groupRel == null || group == null || !group.getName().equals(groupRel.getName())) {
                    continue;
                }
            }
            
            if (GroupMembership.class.isAssignableFrom(query.getRelationshipType())) {
                Agent member = (Agent) query.getParameter(GroupMembership.MEMBER)[0];
                Group group = (Group) query.getParameter(GroupMembership.GROUP)[0];

                Agent memberRel = (Agent) storedRelationship.getIdentityTypes().get(GroupMembership.MEMBER.getName());

                if (memberRel == null || member == null || !member.getLoginName().equals(memberRel.getLoginName())) {
                    continue;
                }

                Group groupRel = (Group) storedRelationship.getIdentityTypes().get(GroupMembership.GROUP.getName());
                
                if (groupRel == null || group == null || !group.getName().equals(groupRel.getName())) {
                    continue;
                }
            }
            
            if (Grant.class.isAssignableFrom(query.getRelationshipType())) {
                Agent assignee = (Agent) query.getParameter(Grant.ASSIGNEE)[0];
                Role role = (Role) query.getParameter(Grant.ROLE)[0];

                Agent assigneeRel = (Agent) storedRelationship.getIdentityTypes().get(Grant.ASSIGNEE.getName());

                if (assigneeRel == null || assignee == null || !assignee.getLoginName().equals(assigneeRel.getLoginName())) {
                    continue;
                }

                Role roleRel = (Role) storedRelationship.getIdentityTypes().get(Grant.ROLE.getName());

                if (roleRel == null || role == null || !role.getName().equals(roleRel.getName())) {
                    continue;
                }
            }
            
            T relationship = null;
            
            try {
                relationship = query.getRelationshipType().newInstance();

                Set<Entry<String, IdentityType>> identityTypes = storedRelationship.getIdentityTypes().entrySet();
                
                for (Entry<String, IdentityType> entry : identityTypes) {
                    List<Property<IdentityType>> annotatedTypes = PropertyQueries.<IdentityType> createQuery(query.getRelationshipType())
                            .addCriteria(new NamedPropertyCriteria(entry.getKey())).getResultList();
                    
                    Property<IdentityType> property = annotatedTypes.get(0);
                    
                    property.setValue(relationship, entry.getValue());
                }
                
                Map<String, String> attributes = storedRelationship.getAttributes();
                
                for (Entry<String, IdentityType> entry : identityTypes) {
                    relationship.setAttribute(new Attribute<Serializable>(entry.getKey(), entry.getValue()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            result.add(relationship);
        }

        return result;
    }

    @Override
    public <T extends Relationship> int countQueryResults(RelationshipQuery<T> query) {
        return 0;
    }

}
