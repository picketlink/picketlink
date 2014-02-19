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

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.permission.acl.spi.PermissionStore;

import java.util.Set;

/**
 * Returns the correct IdentityStore instances for certain operation types, for a given Partition
 *
 * @author Shane Bryzak
 */
public interface StoreSelector {

    /**
     * @param context
     * @param feature
     * @param operation
     *
     * @return
     */
    <T extends IdentityStore<?>> T getStoreForIdentityOperation(IdentityContext context, Class<T> storeType,
                                                                Class<? extends AttributedType> type, IdentityOperation operation);

    /**
     * @param context
     *
     * @return
     */
    <T extends CredentialStore<?>> T getStoreForCredentialOperation(IdentityContext context, Class<?> credentialClass);

    /**
     * Returns the IdentityStore that manages relationships of the specified type, for the specified partition/s.
     *
     * @param relationship
     *
     * @return
     */
    IdentityStore<?> getStoreForRelationshipOperation(IdentityContext context, Class<? extends Relationship> relationshipClass,
                                                      Relationship relationship, IdentityOperation operation);

    /**
     * <p>Returns all available {@link IdentityStore} instances that support the given {@link IdentityType}.</p>
     *
     * @param context The current context.
     * @param relationshipClass The type used to restrict which stores should be selected and returned. If
     * the value is {@link IdentityType} all stores that support any subtype of this interface
     * will be returned.
     * @param partitions A set with the partitions that should be used to restrict which stores should be selected. If empty
     * only the type will be considered.
     *
     * @return A list of stores that support the given type.
     *
     * @throws IdentityManagementException If no store was found.
     */
    Set<IdentityStore<?>> getStoresForRelationshipQuery(IdentityContext context, Class<? extends Relationship> relationshipClass,
                                                        Set<Partition> partitions) throws IdentityManagementException;

    /**
     * Returns a PermissionStore instance
     *
     * @param context
     *
     * @return
     */
    PermissionStore getStoreForPermissionOperation(IdentityContext context);

    /**
     * Returns the PartitionStore that manages partitions.
     *
     * @return
     */
    <T extends PartitionStore<?>> T getStoreForPartitionOperation(IdentityContext context, Class<? extends Partition> partitionClass);

    /**
     * Returns the AttributeStore that manages attributes.
     *
     * @return
     */
    <T extends AttributeStore<?>> T getStoreForAttributeOperation(IdentityContext context);

    /**
     * <p>Returns all available {@link CredentialStore} instances that support the given {@link CredentialStorage}
     * type.</p>
     *
     * @param context
     *
     * @return
     */
    Set<CredentialStore<?>> getStoresForCredentialStorage(IdentityContext context, Class<? extends CredentialStorage> storageClass);

    /**
     * <p>Returns all available {@link IdentityStore} instances that support the given {@link IdentityType}.</p>
     *
     * @param context The current context.
     * @param identityType The type used to restrict which stores should be selected and returned. If
     * the value is {@link IdentityType} all stores that support any subtype of this interface
     * will be returned.
     *
     * @return A list of stores that support the given type.
     *
     * @throws IdentityManagementException If no store was found.
     */
    Set<IdentityStore<?>> getStoresForIdentityQuery(IdentityContext context, Class<? extends IdentityType> identityType) throws IdentityManagementException;
}
