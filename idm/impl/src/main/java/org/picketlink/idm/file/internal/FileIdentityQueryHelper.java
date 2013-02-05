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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;

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

    public boolean matchRolesOf(IdentityType identityType) {
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
                Agent agent = (Agent) object;

                if (agent != null) {
                    for (FileRelationship storedRelationship : new ArrayList<FileRelationship>(relationships)) {
                        Grant grant = identityStore.convertToRelationship(storedRelationship);

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

    public boolean matchHasMember(IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.HAS_MEMBER);

        if (values != null) {
            Group currentGroup = (Group) identityType;

            List<FileRelationship> relationships = identityStore.getRelationshipsForCurrentPartition().get(
                    GroupMembership.class.getName());

            if (relationships == null) {
                return false;
            }
            int valuesMatchCount = values.length;

            for (Object object : values) {
                Agent agent = (Agent) object;

                for (FileRelationship storedRelationship : new ArrayList<FileRelationship>(relationships)) {
                    GroupMembership grant = identityStore.convertToRelationship(storedRelationship);

                    if (grant != null) {
                        if (!grant.getGroup().getId().equals(currentGroup.getId())) {
                            continue;
                        }

                        if (grant.getMember().getId().equals(agent.getId())) {
                            valuesMatchCount--;
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

    public boolean matchHasGroupRole(IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.HAS_GROUP_ROLE);

        if (values != null) {
            int valuesMatchCount = values.length;

            for (Object object : values) {
                GroupRole groupRole = (GroupRole) object;

                if (groupRole != null) {
                    RelationshipQuery<GroupRole> query = new DefaultRelationshipQuery<GroupRole>(GroupRole.class, identityStore);

                    query.setParameter(GroupRole.MEMBER, identityType);
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

    public boolean matchMemberOf(IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.MEMBER_OF);

        if (values != null) {
            int valuesMatchCount = values.length;

            for (Object groupName : values) {
                Group group = identityStore.getGroup(groupName.toString());

                if (group != null) {
                    RelationshipQuery<GroupMembership> query = new DefaultRelationshipQuery<GroupMembership>(
                            GroupMembership.class, identityStore);

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

    public boolean matchHasRole(IdentityType identityType) {
        Object[] values = identityQuery.getParameter(IdentityType.HAS_ROLE);

        if (values != null) {
            int valuesMatchCount = values.length;

            for (Object roleName : values) {
                Role role = identityStore.getRole(roleName.toString());

                if (role != null) {
                    RelationshipQuery<Grant> query = new DefaultRelationshipQuery<Grant>(Grant.class, identityStore);

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
