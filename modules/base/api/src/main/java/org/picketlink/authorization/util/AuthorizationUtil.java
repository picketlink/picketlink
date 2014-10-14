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
package org.picketlink.authorization.util;

import org.picketlink.Identity;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.IdentityStereotype;
import org.picketlink.idm.model.annotation.RelationshipStereotype;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.query.IdentityQueryBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype.GROUP;
import static org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype.ROLE;
import static org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype.GRANT;
import static org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype.GROUP_MEMBERSHIP;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.IDENTITY_GROUP_NAME;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.IDENTITY_ROLE_NAME;

/**
 * <p>Provides some comon authorization methods.</p>
 *
 * @author Pedro Igor
 */
public class AuthorizationUtil {

    /**
     * <p>Checks if the user is logged in.</p>
     *
     * @param identity The {@link org.picketlink.Identity} instance representing an authenticated user.
     *
     * @return True if the user is logged in. Otherwise, returns false.
     */
    public static boolean isLoggedIn(Identity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("You must provide an Identity instance.");
        }

        return identity.isLoggedIn();
    }

    /**
     * <p>Checks if the user has permissions to a resource considering an operation.</p>
     *
     * @param identity The {@link org.picketlink.Identity} instance representing an authenticated user.
     * @param resource The resource.
     * @param resourceClass The resource class if specified.
     * @param resourceIdentifier The resource identifier, if specified.
     * @param operation The operation.
     */
    public static boolean hasPermission(Identity identity, Object resource, Class<?> resourceClass, Serializable resourceIdentifier, String operation) {
        if (resource == null && resourceClass == null) {
            throw new IllegalArgumentException("You must provide a resource or resourceClass in order to check a permission.");
        }

        if (isNullOrEmpty(operation)) {
            throw new IllegalArgumentException("You must provide an operation in order to check a permission.");
        }

        if (!isLoggedIn(identity)) {
            return false;
        }

        if (resource != null && !isNullOrEmpty(resource.toString())) {
            return identity.hasPermission(resource, operation);
        } else if (resourceClass != null) {
            if (resourceIdentifier == null || isNullOrEmpty(resourceIdentifier.toString())) {
                resourceIdentifier = null;
            }

            return identity.hasPermission(resourceClass, resourceIdentifier, operation);
        }

        return false;
    }

    /**
     * <p>Checks if an authenticated user is granted with a role with the given name.</p>
     *
     * @param identity The {@link org.picketlink.Identity} instance representing an authenticated user.
     * @param partitionManager
     * @param roleName The role name.
     *
     * @return True if the user is granted with the role. Otherwise, returns false.
     */
    public static boolean hasRole(Identity identity, PartitionManager partitionManager, String roleName) {
        if (!isLoggedIn(identity)) {
            return false;
        }

        List<Class<? extends IdentityType>> roleTypes = new ArrayList<Class<? extends IdentityType>>();
        List<Class<? extends Relationship>> grantRelationshipTypes = new ArrayList<Class<? extends Relationship>>();

        // let's get all role and grant relationship types supported by the configuration
        for (IdentityConfiguration configuration : partitionManager.getConfigurations()) {
            for (IdentityStoreConfiguration storeConfiguration : configuration.getStoreConfiguration()) {
                for (Class<? extends AttributedType> attributedType : storeConfiguration.getSupportedTypes().keySet()) {
                    if (IdentityType.class.isAssignableFrom(attributedType)) {
                        IdentityStereotype identityStereotype = attributedType.getAnnotation(IdentityStereotype.class);

                        if (identityStereotype != null && ROLE.equals(identityStereotype.value())) {
                            roleTypes.add((Class<? extends IdentityType>) attributedType);
                        }
                    }

                    if (Relationship.class.isAssignableFrom(attributedType)) {
                        RelationshipStereotype relationshipStereotype = attributedType.getAnnotation(RelationshipStereotype.class);

                        if (relationshipStereotype != null && GRANT.equals(relationshipStereotype.value())) {
                            grantRelationshipTypes.add((Class<? extends Relationship>) attributedType);
                        }
                    }
                }
            }
        }

        List<IdentityType> roles = new ArrayList<IdentityType>();

        // now we need to get the role instance by its name against all stored partitions
        for (Class<? extends IdentityType> attributedType : roleTypes) {
            List<Property<Object>> identityStereotypeProperties = PropertyQueries
                .createQuery(attributedType)
                .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
                .getResultList();

            for (Property<Object> property : identityStereotypeProperties) {
                StereotypeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);
                StereotypeProperty.Property stereotypeProperty = attributeProperty.value();

                if (IDENTITY_ROLE_NAME.equals(stereotypeProperty)) {
                    for (Partition partition : partitionManager.getPartitions(Partition.class)) {
                        IdentityManager identityManager = partitionManager.createIdentityManager(partition);
                        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();

                        List<? extends IdentityType> result = queryBuilder
                            .createIdentityQuery(attributedType)
                            .where(queryBuilder.equal(AttributedType.QUERY_ATTRIBUTE.byName(property.getName()), roleName))
                            .getResultList();

                        if (!result.isEmpty()) {
                            roles.add(result.get(0));
                        }
                    }
                }
            }
        }

        RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

        // now we check the relationship between the authenticated account and roles considering the grant types supported by the configuration.
        for (IdentityType role : roles) {
            for (Class<? extends Relationship> relationshipType : grantRelationshipTypes) {
                List<Property<Object>> relationshipStereotypeProperties = PropertyQueries
                    .createQuery(relationshipType)
                    .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
                    .getResultList();
                Property roleProperty = null;
                Property accountProperty = null;

                for (Property<Object> property : relationshipStereotypeProperties) {
                    StereotypeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);
                    StereotypeProperty.Property stereotypeProperty = attributeProperty.value();

                    if (StereotypeProperty.Property.RELATIONSHIP_GRANT_ROLE.equals(stereotypeProperty)) {
                        roleProperty = property;
                    } else if (StereotypeProperty.Property.RELATIONSHIP_GRANT_ASSIGNEE.equals(stereotypeProperty)) {
                        accountProperty = property;
                    }
                }

                if (roleProperty != null && accountProperty != null) {
                    List<? extends Relationship> result = relationshipManager
                        .createRelationshipQuery(relationshipType)
                        .setParameter(Relationship.RELATIONSHIP_QUERY_ATTRIBUTE.byName(roleProperty.getName()), role)
                        .setParameter(Relationship.RELATIONSHIP_QUERY_ATTRIBUTE.byName(accountProperty.getName()), identity
                            .getAccount())
                        .getResultList();

                    if (!result.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * <p>Checks if an authenticated user is member of the a group with the given name.</p>
     *
     * @param identity The {@link org.picketlink.Identity} instance representing an authenticated user.
     * @param partitionManager
     * @param groupName The group name.
     *
     * @return True if the user is granted with the role. Otherwise, returns false.
     */
    public static boolean isMember(Identity identity, PartitionManager partitionManager, String groupName) {
        if (!isLoggedIn(identity)) {
            return false;
        }

        List<Class<? extends IdentityType>> groupTypes = new ArrayList<Class<? extends IdentityType>>();
        List<Class<? extends Relationship>> groupMembershipTypes = new ArrayList<Class<? extends Relationship>>();

        // let's get all group and grant relationship types supported by the configuration
        for (IdentityConfiguration configuration : partitionManager.getConfigurations()) {
            for (IdentityStoreConfiguration storeConfiguration : configuration.getStoreConfiguration()) {
                for (Class<? extends AttributedType> attributedType : storeConfiguration.getSupportedTypes().keySet()) {
                    if (IdentityType.class.isAssignableFrom(attributedType)) {
                        IdentityStereotype identityStereotype = attributedType.getAnnotation(IdentityStereotype.class);

                        if (identityStereotype != null && GROUP.equals(identityStereotype.value())) {
                            groupTypes.add((Class<? extends IdentityType>) attributedType);
                        }
                    }

                    if (Relationship.class.isAssignableFrom(attributedType)) {
                        RelationshipStereotype relationshipStereotype = attributedType.getAnnotation(RelationshipStereotype.class);

                        if (relationshipStereotype != null && GROUP_MEMBERSHIP.equals(relationshipStereotype.value())) {
                            groupMembershipTypes.add((Class<? extends Relationship>) attributedType);
                        }
                    }
                }
            }
        }

        List<IdentityType> groups = new ArrayList<IdentityType>();

        // now we need to get the group instance by its name against all stored partitions
        for (Class<? extends IdentityType> attributedType : groupTypes) {
            List<Property<Object>> identityStereotypeProperties = PropertyQueries
                .createQuery(attributedType)
                .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
                .getResultList();

            for (Property<Object> property : identityStereotypeProperties) {
                StereotypeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);
                StereotypeProperty.Property stereotypeProperty = attributeProperty.value();

                if (IDENTITY_GROUP_NAME.equals(stereotypeProperty)) {
                    for (Partition partition : partitionManager.getPartitions(Partition.class)) {
                        IdentityManager identityManager = partitionManager.createIdentityManager(partition);
                        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();

                        List<? extends IdentityType> result = queryBuilder
                            .createIdentityQuery(attributedType)
                            .where(queryBuilder.equal(AttributedType.QUERY_ATTRIBUTE.byName(property.getName()), groupName))
                            .getResultList();

                        if (!result.isEmpty()) {
                            groups.add(result.get(0));
                        }
                    }
                }
            }
        }

        RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

        // now we check the relationship between the authenticated account and groups considering the group membership types supported by the configuration.
        for (IdentityType group : groups) {
            for (Class<? extends Relationship> relationshipType : groupMembershipTypes) {
                List<Property<Object>> relationshipStereotypeProperties = PropertyQueries
                    .createQuery(relationshipType)
                    .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
                    .getResultList();
                Property groupProperty = null;
                Property memberProperty = null;

                for (Property<Object> property : relationshipStereotypeProperties) {
                    StereotypeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);
                    StereotypeProperty.Property stereotypeProperty = attributeProperty.value();

                    if (StereotypeProperty.Property.RELATIONSHIP_GROUP_MEMBERSHIP_GROUP.equals(stereotypeProperty)) {
                        groupProperty = property;
                    } else if (StereotypeProperty.Property.RELATIONSHIP_GROUP_MEMBERSHIP_MEMBER.equals(stereotypeProperty)) {
                        memberProperty = property;
                    }
                }

                if (groupProperty != null && memberProperty != null) {
                    List<? extends Relationship> result = relationshipManager
                        .createRelationshipQuery(relationshipType)
                        .setParameter(Relationship.RELATIONSHIP_QUERY_ATTRIBUTE.byName(groupProperty.getName()), group)
                        .setParameter(Relationship.RELATIONSHIP_QUERY_ATTRIBUTE.byName(memberProperty.getName()), identity
                            .getAccount())
                        .getResultList();

                    if (!result.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * <p>Checks if an authenticated user is associated with a partition with the given type and name.</p>
     *
     * @param identity The {@link org.picketlink.Identity} instance representing an authenticated user.
     * @param partitionType The required type of the partition. If null, all partition types should be considered.
     * @param partitionNames The name of the partitions that should be associated with an authenticated user.
     *
     * @return True if the user is associated with the partition. Otherwise, returns false.
     */
    public static boolean hasPartition(Identity identity, Class<?> partitionType, String... partitionNames) {
        if (!isLoggedIn(identity)) {
            return false;
        }

        Account account = identity.getAccount();
        Partition partition = account.getPartition();

        if (partitionType != null && !partitionType.isInstance(partition)) {
            return false;
        }

        // this is the case when the type is being checked and the name is empty or null.
        if (partitionNames == null) {
            return true;
        } else if (partitionNames.length == 1) {
            if (isNullOrEmpty(partitionNames[0])) {
                return true;
            }
        }

        for (String partitionName : partitionNames) {
            if (!isNullOrEmpty(partitionName)) {
                if (partition.getName().equals(partitionName)) {
                    return true;
                }
            }
        }

        return false;
    }
}