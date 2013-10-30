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
package org.picketlink.idm.jdbc.internal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.jdbc.internal.model.db.AttributeStorageUtil;
import org.picketlink.idm.jdbc.internal.model.db.GroupStorageUtil;
import org.picketlink.idm.jdbc.internal.model.db.RelationshipStorageUtil;
import org.picketlink.idm.jdbc.internal.model.db.RoleStorageUtil;
import org.picketlink.idm.jdbc.internal.model.db.UserStorageUtil;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.QueryParameter;

/**
 * A JDBC type for {@link IdentityType}
 * such as {@link User}, {@link Role}
 * @author Anil Saldhana
 * @since October 22, 2013
 */
public class IdentityManagedJdbcType extends AbstractJdbcType {
    protected String name;

    public IdentityManagedJdbcType() {
    }

    public IdentityManagedJdbcType(String name) {
        this.name = name;
    }

    public IdentityManagedJdbcType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the name
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public void setAttribute(Attribute<? extends Serializable> attribute) {
        if (type == null) {
            throw IDMMessages.MESSAGES.nullArgument("type");
        }
        removeAttribute(attribute.getName());

        AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
        attributeStorageUtil.setAttribute(dataSource, type.getId(), attribute);
    }

    @Override
    public void removeAttribute(String name) {
        if (type == null) {
            throw IDMMessages.MESSAGES.nullArgument("type");
        }
        AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
        attributeStorageUtil.deleteAttribute(dataSource, type.getId(), name);
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(String name) {
        if (type == null) {
            throw IDMMessages.MESSAGES.nullArgument("type");
        }
        AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
        return attributeStorageUtil.getAttribute(dataSource, type.getId(), name);
    }

    @Override
    public Collection<Attribute<? extends Serializable>> getAttributes() {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void delete(AttributedType attributedType) {
        String attributedTypeId = attributedType.getId();
        if (attributedType instanceof User) {
            UserStorageUtil userStorageUtil = new UserStorageUtil();
            userStorageUtil.deleteUser(dataSource, (User) attributedType);
        }else if (attributedType instanceof Role) {
            RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
            roleStorageUtil.deleteRole(dataSource, (Role) attributedType);
        }else if (attributedType instanceof Group) {
            GroupStorageUtil groupStorageUtil = new GroupStorageUtil();
            groupStorageUtil.deleteGroup(dataSource, (Group) attributedType);
        }else if (attributedType instanceof Agent) {
            UserStorageUtil userStorageUtil = new UserStorageUtil();
            userStorageUtil.deleteAgent(dataSource, (Agent) attributedType);
        }else {
            throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
        }
    }

    @Override
    public void deleteRelationships(AttributedType attributedType) {
        RelationshipStorageUtil relationshipStorageUtil = new RelationshipStorageUtil();
        if(attributedType instanceof User){
            List<Grant> grants = relationshipStorageUtil.loadGrantsForUser(dataSource, (User) attributedType);
            if(grants != null){
                for(Grant grant: grants){
                    relationshipStorageUtil.deleteGrant(dataSource,grant.getId());
                }
            }
            List<GroupMembership> groupMemberships =
                    relationshipStorageUtil.loadGroupMembershipsForUser(dataSource, (User) attributedType);
            if(groupMemberships != null){
                for(GroupMembership groupMembership: groupMemberships){
                    relationshipStorageUtil.deleteGroupMembership(dataSource,groupMembership.getId());
                }
            }
        } else if(attributedType instanceof Role){
            List<Grant> grants = relationshipStorageUtil.loadGrantsForRole(dataSource, (Role) attributedType);
            if(grants != null){
                for(Grant grant: grants){
                    relationshipStorageUtil.deleteGrant(dataSource,grant.getId());
                }
            }
        } else if(attributedType instanceof Group){
            List<GroupMembership> groupMemberships = relationshipStorageUtil.loadGroupMembershipForGroup(dataSource, (Group) attributedType);
            if(groupMemberships != null){
                for(GroupMembership groupMembership: groupMemberships){
                    relationshipStorageUtil.deleteGroupMembership(dataSource,groupMembership.getId());
                }
            }
        } else if(attributedType instanceof Agent){
            List<Grant> grants = relationshipStorageUtil.loadGrantsForAgent(dataSource, (Agent) attributedType);
            if(grants != null){
                for(Grant grant: grants){
                    relationshipStorageUtil.deleteGrant(dataSource,grant.getId());
                }
            }
            List<GroupMembership> groupMemberships = relationshipStorageUtil.loadGroupMembershipsForAgent(dataSource, (Agent) attributedType);
            if(groupMemberships != null){
                for(GroupMembership groupMembership: groupMemberships){
                    relationshipStorageUtil.deleteGroupMembership(dataSource,groupMembership.getId());
                }
            }
        } else {
            throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
        }
    }

    @Override
    public void persist(AttributedType attributedType) {
        String attributedTypeId = attributedType.getId();

        AttributedType storedType = load(attributedTypeId, attributedType);
        if (storedType != null) {
            update(storedType);
        } else {
            if (attributedType instanceof User) {
                // Fresh instance
                UserStorageUtil userStorageUtil = new UserStorageUtil();
                userStorageUtil.storeUser(dataSource, (User) attributedType);
            } else if (attributedType instanceof Role) {
                // Fresh instance
                RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
                roleStorageUtil.storeRole(dataSource, (Role) attributedType);
            } else if (attributedType instanceof Group) {
                // Fresh instance
                GroupStorageUtil groupStorageUtil = new GroupStorageUtil();
                groupStorageUtil.storeGroup(dataSource, (Group) attributedType);
            } else if (attributedType instanceof Agent) {
                // Fresh instance
                UserStorageUtil userStorageUtil = new UserStorageUtil();
                userStorageUtil.storeAgent(dataSource, (Agent) attributedType);
            } else {
                throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
            }
        }
    }

    @Override
    public AttributedType load(String id, AttributedType attributedType) {
        if (attributedType instanceof User || attributedType instanceof Agent) {
            UserStorageUtil userStorageUtil = new UserStorageUtil();
            return userStorageUtil.loadUser(dataSource, id);
        } else if (attributedType instanceof Role) {
            RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
            return roleStorageUtil.loadRole(dataSource, id);
        } else if (attributedType instanceof Group) {
            GroupStorageUtil groupStorageUtil = new GroupStorageUtil();
            return groupStorageUtil.loadGroup(dataSource, id);
        }
        throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
    }

    @Override
    public AttributedType load(String id, Class<? extends AttributedType> attributedType) {
        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
        GroupStorageUtil groupStorageUtil = new GroupStorageUtil();

        if (attributedType == User.class || attributedType == Agent.class) {
            return userStorageUtil.loadUser(dataSource, id);
        } else if (attributedType == Role.class) {
            return roleStorageUtil.loadRole(dataSource, id);
        }  else if (attributedType == Group.class) {
            return groupStorageUtil.loadGroup(dataSource, id);
        } else if (attributedType == IdentityType.class) {
            // Try User first
            AttributedType storedType = userStorageUtil.loadUser(dataSource, id);
            if (storedType != null) {
                return storedType;
            }
            // Role
            storedType = roleStorageUtil.loadRole(dataSource, id);
            if (storedType != null) {
                return storedType;
            }
            // Group
            storedType = groupStorageUtil.loadGroup(dataSource, id);
            if (storedType != null) {
                return storedType;
            }
            throw new RuntimeException("TODO: Cannot find identity type");

        }
        throw new RuntimeException();
    }

    @Override
    public List<? extends AttributedType> load(Map<QueryParameter, Object[]> params,
            Class<? extends AttributedType> attributedType) {
        List<AttributedType> result = new ArrayList<AttributedType>();
        AttributedType attributedType1 = null;

        if (attributedType == User.class) {
            UserStorageUtil userStorageUtil = new UserStorageUtil();
            attributedType1 = userStorageUtil.loadUser(dataSource, params);
        } else if (attributedType == Role.class) {
            RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
            attributedType1 = roleStorageUtil.loadRole(dataSource, params);
        } else if (attributedType == Group.class) {
            GroupStorageUtil groupStorageUtil = new GroupStorageUtil();
            attributedType1 = groupStorageUtil.loadGroup(dataSource, params);
        } else if (attributedType == Agent.class) {
            UserStorageUtil userStorageUtil = new UserStorageUtil();
            attributedType1 = userStorageUtil.loadUser(dataSource, params);
        }else
            throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());

        if (attributedType1 != null) {
            result.add(attributedType1);
        }
        return result;
    }

    @Override
    public void update(AttributedType attributedType) {
        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
        GroupStorageUtil groupStorageUtil = new GroupStorageUtil();
        if (attributedType instanceof User) {
            userStorageUtil.updateUser(dataSource, (User) attributedType);
        }else if (attributedType instanceof Role) {
            roleStorageUtil.updateRole(dataSource, (Role) attributedType);
        }else if (attributedType instanceof Group) {
            groupStorageUtil.updateGroup(dataSource, (Group) attributedType);
        }else if (attributedType instanceof Agent) {
            userStorageUtil.updateAgent(dataSource, (Agent) attributedType);
        }else {
            throw new RuntimeException(attributedType.getClass().getName());
        }
    }
}