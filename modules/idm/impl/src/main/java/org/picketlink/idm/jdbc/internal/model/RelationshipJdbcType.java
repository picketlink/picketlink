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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.jdbc.internal.model.db.AttributeStorageUtil;
import org.picketlink.idm.jdbc.internal.model.db.RelationshipStorageUtil;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * JDBC Type for {@link Relationship}
 * @author Anil Saldhana
 * @since October 25, 2013
 */
public class RelationshipJdbcType extends AbstractJdbcType {
    @Override
    public void delete(AttributedType attributedType) {
        RelationshipStorageUtil relationshipStorageUtil = new RelationshipStorageUtil();
        if (attributedType instanceof Grant) {
            relationshipStorageUtil.deleteGrant(dataSource, attributedType.getId());
        } else if (attributedType instanceof GroupMembership) {
            relationshipStorageUtil.deleteGroupMembership(dataSource, attributedType.getId());
        } else {
            throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
        }
    }

    @Override
    public void deleteRelationships(AttributedType attributedType) {
        throw new RuntimeException(attributedType.getClass().getName());
    }

    @Override
    public void persist(AttributedType attributedType) {
        RelationshipStorageUtil relationshipStorageUtil = new RelationshipStorageUtil();
        if (attributedType instanceof Grant) {
            relationshipStorageUtil.storeGrant(dataSource, (Grant) attributedType);
        } else if (attributedType instanceof GroupMembership) {
            relationshipStorageUtil.storeGroupMembership(dataSource, (GroupMembership) attributedType);
        } else
            throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
    }

    @Override
    public AttributedType load(String id, AttributedType attributedType) {
        throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
    }

    @Override
    public AttributedType load(String id, Class<? extends AttributedType> attributedType) {
        throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
    }

    @Override
    public List<? extends AttributedType> load(Map<QueryParameter, Object[]> params,
            Class<? extends AttributedType> attributedType) {
        List<AttributedType> result = new ArrayList<AttributedType>();
        RelationshipStorageUtil relationshipStorageUtil = new RelationshipStorageUtil();

        if (attributedType == Relationship.class) {
            Set<QueryParameter> queryParameterSet = params.keySet();
            QueryParameter queryParameter = queryParameterSet.iterator().next(); // Consider the first
            Object[] paramValues = params.get(queryParameter);
            //Fallback on looking through the map
            if(paramValues == null){
                if(queryParameter instanceof AttributeParameter){
                    paramValues = getValuesFromParamMap(params, (AttributeParameter) queryParameter);
                }
            }
            Object paramValue = paramValues[0]; // Consider first
            if (paramValue instanceof User) {
                User user = (User) paramValue;

                // Let us get all the grants
                result.addAll(relationshipStorageUtil.loadGrantsForUser(dataSource, user));

                // Let us get all the group memberships
                result.addAll(relationshipStorageUtil.loadGroupMembershipsForUser(dataSource, user));
            }else if (paramValue instanceof Role) {
                Role role = (Role) paramValue;

                // Let us get all the grants
                result.addAll(relationshipStorageUtil.loadGrantsForRole(dataSource, role));
            }else if (paramValue instanceof Group) {
                Group group = (Group) paramValue;

                // Let us get all the grants
                result.addAll(relationshipStorageUtil.loadGroupMembershipForGroup(dataSource, group));
            }else if (paramValue instanceof Agent) {
                Agent agent = (Agent) paramValue;

                // Let us get all the grants
                result.addAll(relationshipStorageUtil.loadGrantsForAgent(dataSource, agent));

                // Let us get all the group memberships
                result.addAll(relationshipStorageUtil.loadGroupMembershipsForAgent(dataSource, agent));
            } else{
                throw IDMMessages.MESSAGES.unexpectedType(paramValue.getClass());
            }
        } else if (attributedType == Grant.class) {
            Set<QueryParameter> queryParameterSet = params.keySet();
            QueryParameter queryParameter = queryParameterSet.iterator().next(); // Consider the first
            Object[] paramValues = params.get(queryParameter);
            //Fallback on looking through the map
            if(paramValues == null){
                if(queryParameter instanceof AttributeParameter){
                    paramValues = getValuesFromParamMap(params, (AttributeParameter) queryParameter);
                }
            }
            Object paramValue = paramValues[0]; // Consider first
            if (paramValue instanceof User) {
                User user = (User) paramValue;
                // Let us get all the grants
                result.addAll(relationshipStorageUtil.loadGrantsForUser(dataSource, user));
            }else if (paramValue instanceof Role) {
                Role role = (Role) paramValue;
                // Let us get all the grants
                result.addAll(relationshipStorageUtil.loadGrantsForRole(dataSource, role));
            } else if (queryParameter == IdentityType.ID) {
                Object[] idObj = params.get(IdentityType.ID);
                String idOfType = (String) idObj[0];
                RelationshipStorageUtil relationshipStorageUtil1 = new RelationshipStorageUtil();
                Grant grant = relationshipStorageUtil.loadGrant(dataSource, idOfType);
                if (grant != null) {
                    result.add(grant);
                }
            } else {
                throw IDMMessages.MESSAGES.unexpectedType(paramValue.getClass());
            }
        } else if (attributedType == GroupMembership.class) {
            Set<QueryParameter> queryParameterSet = params.keySet();
            QueryParameter queryParameter = queryParameterSet.iterator().next(); // Consider the first
            Object[] paramValues = params.get(queryParameter);
            //Fallback on looking through the map
            if(paramValues == null){
                if(queryParameter instanceof AttributeParameter){
                    paramValues = getValuesFromParamMap(params, (AttributeParameter) queryParameter);
                }
            }
            Object paramValue = paramValues[0]; // Consider first
            if (paramValue instanceof User) {
                User user = (User) paramValue;
                // Let us get all the grants
                result.addAll(relationshipStorageUtil.loadGroupMembershipsForUser(dataSource, user));
            } else if (paramValue instanceof Group) {
                Group group = (Group) paramValue;
                // Let us get all the grants
                result.addAll(relationshipStorageUtil.loadGroupMembershipForGroup(dataSource, group));
            }else if (queryParameter == IdentityType.ID) {
                Object[] idObj = params.get(IdentityType.ID);
                String idOfType = (String) idObj[0];
                RelationshipStorageUtil relationshipStorageUtil1 = new RelationshipStorageUtil();
                GroupMembership groupMembership = relationshipStorageUtil.loadGroupMembership(dataSource, idOfType);
                if (groupMembership != null) {
                    result.add(groupMembership);
                }
            } else
                throw IDMMessages.MESSAGES.unexpectedType(paramValue.getClass());
        } else
            throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
        return result;
    }

    @Override
    public void update(AttributedType attributedType) {
        throw new RuntimeException();
    }

    @Override
    public void setAttribute(Attribute<? extends Serializable> attribute) {
        throw IDMMessages.MESSAGES.unexpectedType(attribute.getClass());
    }

    @Override
    public void removeAttribute(String name) {
        throw new RuntimeException();
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(String name) {
        throw new RuntimeException();
    }

    @Override
    public Collection<Attribute<? extends Serializable>> getAttributes() {
        // Scan the attribute table
        Collection<Attribute<? extends Serializable>> list = new ArrayList<Attribute<? extends Serializable>>();
        AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
        List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource, id);
        if (attributeList.isEmpty() == false) {
            for (Attribute att : attributeList) {
                list.add(att);
            }
        }
        return list;
    }
}