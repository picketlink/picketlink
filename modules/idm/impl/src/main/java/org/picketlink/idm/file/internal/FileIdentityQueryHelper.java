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

import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.SecurityContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author Pedro Silva
 *
 */
public class FileIdentityQueryHelper {

    private IdentityQuery<?> identityQuery;
    private FileBasedIdentityStore identityStore;

    public FileIdentityQueryHelper(IdentityQuery<?> identityQuery, FileBasedIdentityStore identityStore) {
        this.identityQuery = identityQuery;
        this.identityStore = identityStore;
    }

    public boolean matchCreatedDateParameters(IdentityType identityType) {
        if (identityQuery.getParameter(IdentityType.CREATED_DATE) != null
                || identityQuery.getParameter(IdentityType.CREATED_BEFORE) != null
                || identityQuery.getParameter(IdentityType.CREATED_AFTER) != null) {
            Date createdDate = identityType.getCreatedDate();

            if (createdDate != null) {
                if (!isQueryParameterEquals(identityQuery, IdentityType.CREATED_DATE, createdDate)) {
                    return false;
                }

                if (!isQueryParameterLessThan(identityQuery, IdentityType.CREATED_BEFORE, createdDate.getTime())) {
                    return false;
                }

                if (!isQueryParameterGreaterThan(identityQuery, IdentityType.CREATED_AFTER, createdDate.getTime())) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean matchRolesOf(SecurityContext context, IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.ROLE_OF);

        if (values != null) {
            Role currentRole = (Role) identityType;

            List<FileRelationship> relationships = identityStore.getRelationshipsForCurrentPartition().get(
                    Grant.class.getName());

            if (relationships == null) {
                return false;
            }

            int valuesMatchCount = values.length;

            for (Object object : values) {
                IdentityType agent = (IdentityType) object;

                if (agent != null) {
                    for (FileRelationship storedRelationship : new ArrayList<FileRelationship>(relationships)) {
                        Grant grant = identityStore.convertToRelationship(context, storedRelationship);

                        if (grant != null) {
                            if (!grant.getRole().getId().equals(currentRole.getId())) {
                                continue;
                            }

                            if (grant.getAssignee().getId().equals(agent.getId())) {
                                valuesMatchCount--;
                            }
                        }
                    }
                }

                if (valuesMatchCount > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean matchHasMember(SecurityContext context, IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.HAS_MEMBER);

        if (values != null) {
            Group currentGroup = (Group) identityType;

            int valuesMatchCount = values.length;

            for (Object object : values) {
                if (Agent.class.isInstance(object)) {
                    List<FileRelationship> relationships = identityStore.getRelationshipsForCurrentPartition().get(
                            GroupMembership.class.getName());

                    if (relationships == null) {
                        return false;
                    }

                    Agent agent = (Agent) object;

                    DefaultRelationshipQuery<GroupMembership> query = new DefaultRelationshipQuery<GroupMembership>(
                            context, GroupMembership.class, this.identityStore);

                    query.setParameter(GroupMembership.MEMBER, agent);

                    List<GroupMembership> result = query.getResultList();

                    for (GroupMembership groupMembership : result) {
                        if (groupMembership != null) {
                            if (!groupMembership.getGroup().getId().equals(currentGroup.getId())) {
                                continue;
                            }

                            if (groupMembership.getMember().getId().equals(agent.getId())) {
                                valuesMatchCount--;
                            }
                        }
                    }
                } else if (Group.class.isInstance(object)) {
                    Group group = (Group) object;

                    if (group.getParentGroup() == null) {
                        return false;
                    }

                    if (this.identityStore.hasParentGroup(group, currentGroup)) {
                        valuesMatchCount--;
                    }
                } else {
                    throw MESSAGES.queryUnsupportedParameterValue("Group.HAS_MEMBER", object);
                }

                if (valuesMatchCount > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean matchHasGroupRole(SecurityContext context, IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.HAS_GROUP_ROLE);

        if (values != null) {
            int valuesMatchCount = values.length;

            for (Object object : values) {
                GroupRole groupRole = (GroupRole) object;

                if (groupRole != null) {
                    RelationshipQuery<GroupRole> query = new DefaultRelationshipQuery<GroupRole>(context,
                            GroupRole.class, identityStore);

                    query.setParameter(GroupRole.ASSIGNEE, identityType);
                    query.setParameter(GroupRole.GROUP, groupRole.getGroup());
                    query.setParameter(GroupRole.ROLE, groupRole.getRole());

                    List<GroupRole> relationships = query.getResultList();

                    if (!relationships.isEmpty()) {
                        valuesMatchCount--;
                    }
                }
            }

            if (valuesMatchCount > 0) {
                return false;
            }
        }

        return true;
    }

    public boolean matchMemberOf(SecurityContext context, IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.MEMBER_OF);

        if (values != null) {
            int valuesMatchCount = values.length;

            for (Object group : values) {
                if (!Group.class.isInstance(group)) {
                    throw MESSAGES.queryUnsupportedParameterValue("IdentityType.MEMBER_OF", group);
                }

                if (group != null) {
                    RelationshipQuery<GroupMembership> query = new DefaultRelationshipQuery<GroupMembership>(
                            context, GroupMembership.class, identityStore);

                    query.setParameter(GroupMembership.MEMBER, identityType);
                    query.setParameter(GroupMembership.GROUP, group);

                    List<GroupMembership> relationships = query.getResultList();

                    if (!relationships.isEmpty()) {
                        valuesMatchCount--;
                    }
                }
            }

            if (valuesMatchCount > 0) {
                return false;
            }
        }

        return true;
    }

    public boolean matchHasRole(SecurityContext context, IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.HAS_ROLE);

        if (values != null) {
            int valuesMatchCount = values.length;

            for (Object role : values) {
                if (!Role.class.isInstance(role)) {
                    throw MESSAGES.queryUnsupportedParameterValue("IdentityType.HAS_ROLE", role);
                }

                if (role != null) {
                    RelationshipQuery<Grant> query = new DefaultRelationshipQuery<Grant>(context, Grant.class, identityStore);

                    query.setParameter(Grant.ASSIGNEE, identityType);
                    query.setParameter(Grant.ROLE, role);

                    List<Grant> relationships = query.getResultList();

                    if (!relationships.isEmpty()) {
                        valuesMatchCount--;
                    }
                }
            }

            if (valuesMatchCount > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Checks if the
     * </p>
     *
     * @param identityType
     * @param parameters
     * @return
     */
    public boolean matchAttributes(IdentityType identityType) {
        Map<QueryParameter, Object[]> attributeParameters = this.identityQuery
                .getParameters(AttributedType.AttributeParameter.class);

        boolean match = false;

        if (!attributeParameters.isEmpty()) {
            for (Entry<QueryParameter, Object[]> parameterEntry : attributeParameters.entrySet()) {
                AttributedType.AttributeParameter parameter = (AttributedType.AttributeParameter) parameterEntry.getKey();
                Object[] parameterValues = parameterEntry.getValue();

                Attribute<Serializable> identityTypeAttribute = identityType.getAttribute(parameter.getName());

                if (identityTypeAttribute != null && identityTypeAttribute.getValue() != null) {
                    int valuesMatchCount = parameterValues.length;

                    for (Object value : parameterValues) {
                        if (identityTypeAttribute.getValue().getClass().isArray()) {
                            Object[] userValues = (Object[]) identityTypeAttribute.getValue();

                            for (Object object : userValues) {
                                if (object.equals(value)) {
                                    valuesMatchCount--;
                                }
                            }
                        } else {
                            if (value.equals(identityTypeAttribute.getValue())) {
                                valuesMatchCount--;
                            }
                        }
                    }

                    match = valuesMatchCount <= 0;

                    if (!match) {
                        return false;
                    }
                }
            }
        } else {
            match = true;
        }

        return match;
    }

    public static boolean isQueryParameterEquals(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
            Serializable valueToCompare) {
        Object[] values = identityQuery.getParameter(queryParameter);

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

    public static boolean isQueryParameterEquals(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
            Date valueToCompare) {
        Object[] values = identityQuery.getParameter(queryParameter);

        if (values == null) {
            return true;
        }
        if (values.length > 0 && valueToCompare != null && valueToCompare.equals(values[0])) {
            return true;
        }

        return false;
    }

    public static boolean isQueryParameterGreaterThan(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
            Long valueToCompare) {
        return isQueryParameterGreaterOrLessThan(identityQuery, queryParameter, valueToCompare, true);
    }

    public static boolean isQueryParameterLessThan(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
            Long valueToCompare) {
        return isQueryParameterGreaterOrLessThan(identityQuery, queryParameter, valueToCompare, false);
    }

    public static boolean isQueryParameterGreaterOrLessThan(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
            Long valueToCompare, boolean greaterThan) {
        Object[] values = identityQuery.getParameter(queryParameter);

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

    public boolean matchExpiryDateParameters(IdentityType storedEntry) {
        if (identityQuery.getParameter(IdentityType.EXPIRY_DATE) != null
                || identityQuery.getParameter(IdentityType.EXPIRY_BEFORE) != null
                || identityQuery.getParameter(IdentityType.EXPIRY_AFTER) != null) {
            Date expiryDate = storedEntry.getExpirationDate();

            if (!isQueryParameterEquals(identityQuery, IdentityType.EXPIRY_DATE, expiryDate)) {
                return false;
            }

            Long expiryDateInMillis = null;

            if (expiryDate != null) {
                expiryDateInMillis = expiryDate.getTime();
            }

            if (!isQueryParameterLessThan(identityQuery, IdentityType.EXPIRY_BEFORE, expiryDateInMillis)) {
                return false;
            }

            if (!isQueryParameterGreaterThan(identityQuery, IdentityType.EXPIRY_AFTER, expiryDateInMillis)) {
                return false;
            }
        }

        return true;
    }

}
