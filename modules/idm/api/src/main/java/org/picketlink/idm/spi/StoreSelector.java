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

package org.picketlink.idm.spi;

import java.util.Set;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;

/**
 * Returns the correct IdentityStore instances for certain operation types, for a given Partition
 *
 * @author Shane Bryzak
 *
 */
public interface StoreSelector {

    /**
     *
     * @param context
     * @param feature
     * @param operation
     * @return
     */
    <T extends IdentityStore<?>> T getStoreForIdentityOperation(Class<T> storeType, Partition partition, Class<? extends AttributedType> type,
            IdentityOperation operation);

    /**
     *
     * @param context
     * @return
     */
    IdentityStore<?> getStoreForCredentialOperation(Class<?> credentialClass, Partition partition);

    /**
     * Returns the IdentityStore that manages relationships of the specified type, for the specified partition/s.
     *
     * @param relationship
     * @return
     */
    IdentityStore<?> getStoreForRelationshipOperation(Class<? extends Relationship> relationshipClass, Set<Partition> partitions);

    /**
     *
     *
     * @return
     */
    PartitionStore<?> getStoreForPartitionOperation();
}
