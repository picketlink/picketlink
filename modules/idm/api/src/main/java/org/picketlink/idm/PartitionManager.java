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

import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.model.Partition;

import java.io.Serializable;

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
     * <p>The default partition is always a {@link org.picketlink.idm.model.sample.Realm} type with name
     * <code>Realm.DEFAULT_REALM</code>. This partition must exists before calling this method,
     * otherwise an exception will be thrown.</p>
     *
     * <p>The returned {@link IdentityManager} instance is not thread-safe.</p>
     *
     * @return
     * @throws IdentityManagementException if the default partition does not exists or any error occurs during the
     * creation of the {@link IdentityManager} instance.
     */
    IdentityManager createIdentityManager() throws IdentityManagementException;

    /**
     * <p>Creates an {@link IdentityManager} for the specified partition</p>
     *
     * <p>The given <code>partition</code> must exists before calling this method, otherwise an exception will be thrown.</p>
     *
     * <p>The returned {@link IdentityManager} instance is not thread-safe.</p>
     *
     * @param partition
     * @return
     * @throws IdentityManagementException if the default partition does not exists or any error occurs during the
     * creation of the instance.
     */
    IdentityManager createIdentityManager(Partition partition) throws IdentityManagementException;

    /**
     * <p>Creates an {@link RelationshipManager} for the specified partition.</p>
     *
     * <p>The returned {@link RelationshipManager} instance is not thread-safe.</p>
     *
     * @return
     * @throws IdentityManagementException if any error occurs during the creation of the instance.
     */
    RelationshipManager createRelationshipManager() throws IdentityManagementException;

    /**
     * <p>Return the partition specified by the partition class and name.</p>
     *
     * <p>The <code>partitionClass</code> can be any sub-type of {@link Partition}. In this case only partitions of a
     * specific sub-type will be considered.
     * </p>
     *
     * <p>If <code>partitionClass</code> equals the {@link Partition} type this method may return any of its
     * sub-types with the given <code>name</code>.</p>
     *
     * @param partitionClass
     * @param name
     * @return
     * @throws IdentityManagementException if any error occurs during the retrieval.
     */
    <T extends Partition> T getPartition(Class<T> partitionClass, String name) throws IdentityManagementException;

    /**
     * <p>Return the partition specified by the partition class and identifier.</p>
     *
     * <p>The <code>partitionClass</code> can be any sub-type of {@link Partition}. In this case only partitions of a
     * specific sub-type will be considered.
     * </p>
     *
     * <p>If <code>partitionClass</code> equals the {@link Partition} type this method may return any of its
     * sub-types with the given <code>id</code>.</p>
     *
     * @param partitionClass
     * @param id
     * @return
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
     * @throws IdentityManagementException if any error occurs during the creation.
     * @throws OperationNotSupportedException if partition management is not enabled/supported.
     */
    void add(Partition partition) throws IdentityManagementException, OperationNotSupportedException;

    /**
     * <p>Adds a new partition with a reference to the given <code>configurationName</code>.</p>
     *
     * @param partition
     * @param configurationName
     * @throws IdentityManagementException if the <code>configurationName</code> does not exists or if any error occurs
     * during the creation.
     * @throws OperationNotSupportedException if partition management is not enabled/supported.
     */
    void add(Partition partition, String configurationName) throws IdentityManagementException, OperationNotSupportedException;

    /**
     * <p>Updates the attributes of the specified partition.</p>
     *
     * <p>Before calling this method make sure the <code>partition</code> references a valid instance that points
     * to a partition already stored with its identifier.</p>
     *
     * @param partition
     * @throws IdentityManagementException if no partition exists with the id of the given <code>partition</code> or
     * if any error occurs during the update.
     * @throws OperationNotSupportedException if partition management is not enabled/supported.
     */
    void update(Partition partition) throws IdentityManagementException, OperationNotSupportedException;

    /**
     * <p>Removes the specified partition.</p>
     *
     * <p>Before calling this method make sure the <code>partition</code> references a valid instance that points
     * to a partition already stored with its identifier.</p>
     *
     * @param partition
     * @throws IdentityManagementException if no partition exists with the id of the given <code>partition</code> or
     * if any error occurs during the update.
     * @throws OperationNotSupportedException if partition management is not enabled/supported.
     */
    void remove(Partition partition) throws IdentityManagementException, OperationNotSupportedException;

}
