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
import org.picketlink.idm.model.Partition;

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
     *
     * @return
     */
    RelationshipManager createRelationshipManager();

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

}
