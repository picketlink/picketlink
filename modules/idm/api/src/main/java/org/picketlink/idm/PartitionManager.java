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

import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.model.Partition;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>Provides partition management operations and serves as a start point for managing identity data (eg.: users,
 * roles, credentials, etc) by providing factory methods for creating {@link IdentityManager} and {@link
 * RelationshipManager} instances, respectively.</p>
 *
 * <p>Partition management is only enabled if one of the provided configurations supports the {@link Partition}
 * type or any of its sub-types. Otherwise, all operations will behave in the context of the default partition.
 * Only a single configuration can support partitions.</p>
 *
 * <p>Each partition is linked to a single configuration name, what means that all operations on that
 * partition will be done using the stores for this specific configuration.</p>
 *
 * <p>Eg.: Partition A is using a file-store configuration and Partition B is using a jpa-store configuration. See the
 * Configuration API for more details about providing multiple configurations.</p>
 *
 * <p>Implementations of this interface should be thread-safe.</p>
 *
 * @author Shane Bryzak
 */
public interface PartitionManager extends Serializable {

    /**
     * <p>Creates an {@link IdentityManager} instance for the default partition.</p>
     *
     * <p>The default partition is always a {@link org.picketlink.idm.model.basic.Realm} type with name
     * <code>Realm.DEFAULT_REALM</code>. This partition must exists before calling this method,
     * otherwise an exception will be thrown.</p>
     *
     * @return A partition-scoped IdentityManager instance for the default realm. The returned instance is not
     *         thread-safe.
     *
     * @throws IdentityManagementException if the default partition does not exists or any error occurs during the
     * creation of the {@link IdentityManager} instance.
     */
    IdentityManager createIdentityManager() throws IdentityManagementException;

    /**
     * <p>Creates an {@link IdentityManager} for the specified partition.</p>
     *
     * @param partition The partition instance where identity management operations will be scoped. The given
     * <code>partition</code> must exists before calling this method, otherwise an exception will be
     * thrown.
     *
     * @return A partition-scoped IdentityManager instance for a given partition. The returned instance is not
     *         thread-safe.
     *
     * @throws IdentityManagementException if the default partition does not exists or any error occurs during the
     * creation of the instance.
     */
    IdentityManager createIdentityManager(Partition partition) throws IdentityManagementException;

    /**
     * Creates a {@link PermissionManager} for the default partition.
     *
     * @return
     *
     * @throws IdentityManagementException If any error occurs during the creation of the instance.
     */
    PermissionManager createPermissionManager() throws IdentityManagementException;

    /**
     * Creates a {@link PermissionManager} for the specified partition.
     *
     * @param partition The partition instance where permission operations will be scoped. The given
     * <code>partition</code> must exists before calling this method, otherwise an exception will be
     * thrown.
     *
     * @return A partition-scoped PermissionManager instance for a given partition.
     *
     * @throws IdentityManagementException if the default partition does not exists or any error occurs during the
     * creation of the instance.
     */
    PermissionManager createPermissionManager(Partition partition) throws IdentityManagementException;

    /**
     * <p>Creates an {@link RelationshipManager}.</p>
     *
     * @throws IdentityManagementException if any error occurs during the creation of the instance.
     */
    RelationshipManager createRelationshipManager() throws IdentityManagementException;

    /**
     * <p>Return the partition specified by the partition class and name.</p>
     *
     * @param partitionClass It can be any sub-type of Partition. In this case only partitions of a specific sub-type
     * will be considered. If it equals the Partition type this method may return any of its sub-types.
     * @param name The name of the partition. It can not me null.
     *
     * @return
     *
     * @throws IdentityManagementException if any error occurs during the retrieval.
     */
    <T extends Partition> T getPartition(Class<T> partitionClass, String name) throws IdentityManagementException;

    /**
     * <p>Return all {@link Partition} instances for a given <code>partitionClass</code>.</p>
     *
     * @param partitionClass It can be any sub-type of Partition. In this case only partitions of a specific sub-type
     * will be considered. If it equals the Partition type this method may return any of its sub-types.
     *
     * @return
     *
     * @throws IdentityManagementException if any error occurs during the retrieval.
     */
    <T extends Partition> List<T> getPartitions(Class<T> partitionClass) throws IdentityManagementException;

    /**
     * <p>Return the partition specified by the partition class and identifier.</p>
     *
     * <p>If <code>partitionClass</code> equals the {@link Partition} type this method may return any of its
     * sub-types with the given <code>id</code>.</p>
     *
     * @param partitionClass It can be any sub-type of Partition. In this case only partitions of a specific sub-type
     * will be considered. If it equals the Partition type this method may return any of its sub-types.
     * @param id The identifier of the partition. It can not be null.
     *
     * @return
     *
     * @throws IdentityManagementException if any error occurs during the retrieval.
     */
    <T extends Partition> T lookupById(final Class<T> partitionClass, String id) throws IdentityManagementException;

    /**
     * <p>Adds a partition to the default configuration.</p>
     *
     * <p>Only a single configuration may support partition. In this case the partition will be always created
     * with a reference to this configuration.</p>
     *
     * @param partition
     *
     * @throws IdentityManagementException if any error occurs during the creation.
     */
    void add(Partition partition) throws IdentityManagementException;

    /**
     * <p>Adds a new partition with a reference to the given <code>configurationName</code>.</p>
     *
     * @param partition
     * @param configurationName
     *
     * @throws IdentityManagementException if the <code>configurationName</code> does not exists or if any error occurs
     * during the creation.
     */
    void add(Partition partition, String configurationName) throws IdentityManagementException;

    /**
     * <p>Updates the attributes of the specified partition.</p>
     *
     * @param partition The given <code>partition</code> must exists before calling this method, otherwise an exception
     * will be
     * thrown.
     *
     * @throws IdentityManagementException if no partition exists or if any error occurs during the update.
     */
    void update(Partition partition) throws IdentityManagementException;

    /**
     * <p>Removes the specified partition.</p>
     *
     * <p>Before calling this method make sure the <code>partition</code> references a valid instance that points
     * to a partition already stored with its identifier.</p>
     *
     * @param partition The given <code>partition</code> must exists before calling this method, otherwise an exception
     * will be
     * thrown.
     *
     * @throws IdentityManagementException if no partition exists or if any error occurs during the update.
     */
    void remove(Partition partition) throws IdentityManagementException;

    /**
     * <p>Retrieves the configuration used to build this <code>PartitionManager</code>.</p>
     *
     * @return A collection with all the configuration used to build this partition manager.
     */
    Collection<IdentityConfiguration> getConfigurations();
}
