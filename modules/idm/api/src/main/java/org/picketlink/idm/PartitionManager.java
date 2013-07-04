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
package org.picketlink.idm;

import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityContext;

/**
 * Provides partition management operations
 *
 * @author Shane Bryzak
 */
public interface PartitionManager {

    /**
     * Creates an IdentityManager instance for the default partition
     *
     * @return
     * @throws SecurityConfigurationException
     */
    IdentityManager createIdentityManager() throws SecurityConfigurationException;

    /**
     * Creates an IdentityManager for the specified partition
     * @param partition
     * @return
     * @throws SecurityConfigurationException
     * @throws IdentityManagementException
     */
    IdentityManager createIdentityManager(Partition partition) throws SecurityConfigurationException, IdentityManagementException;

    /**
     * Return the partition specified by the partition class and name
     *
     * @param partitionClass
     * @param name
     * @return
     */
    <T extends Partition> T getPartition(Class<T> partitionClass, String name);

    /**
     * Adds a partition to the default configuration
     *
     * @param partition
     */
    void add(Partition partition);

    /**
     * Adds a new partition to the configuration specified by name
     *
     * @param partition
     * @param configurationName
     */
    void add(Partition partition, String configurationName);

    /**
     * Updates the attributes of the specified partition
     *
     * @param partition
     */
    void update(Partition partition);

    /**
     * Removes the specified partition
     *
     * @param partition
     */
    void remove(Partition partition);

    // Relationship management

    /**
     * <p>
     * Adds the given {@link Relationship} instance to the configured identity store.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot add the provided {@link Relationship} instance.
     */
    void add(IdentityContext context, Relationship relationship) throws IdentityManagementException;

    /**
     * <p>
     * Updates the given {@link Relationship} instance. The instance must have an identifier, otherwise a exception will be
     * thrown.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot update the provided {@link Relationship} instance.
     */
    void update(IdentityContext context, Relationship relationship);

    /**
     * <p>
     * Removes the given {@link Relationship} instance. The instance must have an identifier, otherwise a exception will be
     * thrown.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot remove the provided {@link Relationship} instance.
     */
    void remove(IdentityContext context, Relationship relationship);

    /**
     * <p>
     * Checks if the given {@link IdentityType} is a member of a specific {@link Group}.
     * </p>
     *
     * @param identityType Must be a {@link Agent} or {@link Group} instance.
     * @param group
     * @return true if the {@link IdentityType} is a member of the provided {@link Group}.
     */
    boolean isMember(IdentityContext context, IdentityType identityType, Group group);

    /**
     * <p>
     * Adds the given {@link Agent} as a member of the provided {@link Group}.
     * </p>
     *
     * @param agent
     * @param group
     */
    void addToGroup(IdentityContext context, Agent agent, Group group);

    /**
     * <p>
     * Removes the given {@link Agent} from the provided {@link Group}.
     * </p>
     *
     * @param member
     * @param group
     */
    void removeFromGroup(IdentityContext context, Agent member, Group group);

    /**
     * <p>
     * Checks if the given {@link IdentityType}, {@link Role} and {@link Group} instances maps to a {@link GroupRole}
     * relationship.
     * </p>
     *
     * @param assignee
     * @param role
     * @param group
     * @return
     */
    boolean hasGroupRole(IdentityContext context, IdentityType assignee, Role role, Group group);

    /**
     * <p>
     * Creates a {@link GroupRole} relationship for the given {@link IdentityType}, {@link Role} and {@link Group} instances.
     * </p>
     *
     * @param assignee
     * @param role
     * @param group
     */
    void grantGroupRole(IdentityContext context, IdentityType assignee, Role role, Group group);

    /**
     * <p>
     * Revokes a {@link GroupRole} relationship for the given {@link IdentityType}, {@link Role} and {@link Group} instances.
     * </p>
     *
     * @param assignee
     * @param role
     * @param group
     */
    void revokeGroupRole(IdentityContext context, IdentityType assignee, Role role, Group group);

    /**
     * <p>
     * Checks if the given {@link Role} is granted to the provided {@link IdentityType}.
     * </p>
     *
     * @param identityType
     * @param role
     * @return
     */
    boolean hasRole(IdentityContext context, IdentityType identityType, Role role);


    /**
     * <p>
     * Grants the given {@link Role} to the provided {@link IdentityType}.
     * </p>
     *
     * @param identityType
     * @param role
     */
    void grantRole(IdentityContext context, IdentityType identityType, Role role);

    /**
     * <p>
     * Revokes the given {@link Role} from the provided {@link IdentityType}.
     * </p>
     *
     * @param identityType
     * @param role
     */
    void revokeRole(IdentityContext context, IdentityType identityType, Role role);

    /**
     * <p>
     * Creates an {@link RelationshipQuery} that can be used to query for {@link Relationship} instances.
     * </p>
     * <p>
     * The first argument tells which {@link Relationship} type should be returned. If you provide the {@link Relationship} base
     * interface any {@link Relationship} instance that matches the provided query parameters will be returned.
     * </p>
     *
     * @param identityType
     * @return
     */
    <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(IdentityContext context, Class<T> relationshipType);
}
