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

package org.picketlink.idm;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.jboss.logging.Param;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * {@link IDMMessages} interface for all exceptions thrown by the default IDM implementation.
 * </p>
 *
 * @author Pedro Silva
 */
@MessageBundle(projectCode = IDMLog.PICKETLINK_IDM_PROJECT_CODE)
public interface IDMInternalMessages extends IDMMessages {

    IDMInternalMessages MESSAGES = Messages.getBundle(IDMInternalMessages.class);

    @Message(id = 6, value = "Could not initialize Partition Manager [%s].")
    IdentityManagementException partitionManagerInitializationFailed(Class<? extends PartitionManager> partitionManagerType, @Cause Throwable t);

    // identity store API messages 300-399
    @Message(id = 300, value = "No store found with type [%s].")
    IdentityManagementException storeNotFound(Class<? extends IdentityStore> partitionStoreClass);

    @Message(id = 301, value = "Error while trying to determine EntityManager - context parameter not set.")
    IdentityManagementException storeJpaCouldNotGetEntityManagerFromStoreContext();

    @Message(id = 302, value = "Could not create context.")
    IdentityManagementException storeLdapCouldNotCreateContext(@Cause Throwable e);

    @Message(value = "Entry not found with ID [%s] using baseDN [%s].")
    IdentityManagementException storeLdapEntryNotFoundWithId(String entryUUID, String baseDN);

    @Message(value = "Could not load attributes for entry with ID [%s] using baseDN [%s].")
    IdentityManagementException storeLdapCouldNotLoadAttributesForEntry(String entryUUID, String baseDN);

    @Message(value = "Unexpected IdentityStore type. Expected [%s]. Actual [%s].")
    IdentityManagementException storeUnexpectedType(Class<? extends IdentityStore> expectedType,
                                                    Class<? extends IdentityStore> actualType);

    // partition API messages 400-499
    @Message(id = 401, value = "Could not create partition [%s] using configuration [%s].")
    IdentityManagementException partitionAddFailed(Partition partition, String configurationName,
                                                   @Cause Exception e);

    @Message(id = 402, value = "Could not update partition [%s].")
    IdentityManagementException partitionUpdateFailed(Partition partition, @Cause Exception e);

    @Message(id = 403, value = "Could not remove partition [%s].")
    IdentityManagementException partitionRemoveFailed(Partition partition, @Cause Exception e);

    @Message(id = 404, value = "Could not load partition for type [%s] and name [%s].")
    IdentityManagementException partitionGetFailed(Class<? extends Partition> partitionClass, String name,
                                                   @Cause Exception e);

    @Message(id = 405, value = "Could not create contextual IdentityManager for Partition [%s]. Partition not found " +
            "or it was null.")
    IdentityManagementException partitionCouldNotCreateIdentityManager(Partition partition);

    @Message(id = 406, value = "Partition [%s] not found with the given name [%s].")
    IdentityManagementException partitionNotFoundWithName(Class<? extends Partition> type, String name);

    @Message(id = 407, value = "No configuration found with the given name [%s].")
    IdentityManagementException partitionNoConfigurationFound(String name);

    @Message(id = 408, value = "Partition [%s] references an invalid or non-existent configuration.")
    IdentityManagementException partitionReferencesInvalidConfiguration(Partition partition);

    @Message(id = 409, value = "Partition management is not supported by the current configuration.")
    OperationNotSupportedException partitionManagementNoSupported(@Param Class<Partition> partitionClass,
                                                                  @Param IdentityStoreConfiguration.IdentityOperation create);

    @Message(id = 410, value = "Could not create contextual PermissionManager for Partition [%s].")
    IdentityManagementException partitionCouldNotCreatePermissionManager(Partition partition);

    @Message(id = 411, value = "Partition [%s] does not support type [%s].")
    IdentityManagementException partitionUnsupportedType(Partition partition, Class<? extends AttributedType> type);

    @Message(id = 412, value = "More than one partitions have been found with the given name [%s] and type [%s].")
    IdentityManagementException partitionFoundWithSameNameAndType(String name, Class<? extends Partition> partitionClass);


    @Message(value = "A Partition [%s] with name [%s] already exists.")
    IdentityManagementException partitionAlreadyExistsWithName(Class<? extends Partition> type, String name);

    @Message(value = "No configuration name defined for partition [%s].")
    IdentityManagementException partitionWithNoConfigurationName(Partition partition);

    // query API messages 500-599
    @Message(id = 500, value = "Could not query Relationship using query [%s].")
    IdentityManagementException queryRelationshipFailed(RelationshipQuery<?> query, @Cause Throwable t);

    @Message(id = 501, value = "Could not query IdentityType using query [%s].")
    IdentityManagementException queryIdentityTypeFailed(IdentityQuery<?> query, @Cause Throwable t);

    @Message(value = "Unsupported value for Query Parameter [%s]. Value: %s.")
    IdentityManagementException queryUnsupportedParameterValue(String parameterName, Object parameterValue);

    // attributed types management messages 600-699
    @Message(id = 600, value = "Could not add AttributedType [%s].")
    IdentityManagementException attributedTypeAddFailed(AttributedType identityType, @Cause Throwable t);

    @Message(id = 601, value = "Could not remove AttributedType [%s].")
    IdentityManagementException attributedTypeRemoveFailed(AttributedType identityType, @Cause Throwable t);

    @Message(id = 602, value = "Could not update AttributedType [%s].")
    IdentityManagementException attributedTypeUpdateFailed(AttributedType identityType, @Cause Throwable t);

    @Message(value = "IdentityType [%s] already exists with the given identifier [%s] for the given Partition [%s].")
    IdentityManagementException identityTypeAlreadyExists(Class<? extends IdentityType> type, String identifier,
                                                          Partition partition);

    @Message(id = 603, value = "Ambiguous AttributedType found with identifier [%s].")
    IdentityManagementException attributedTypeAmbiguosFoundWithId(String id);

    @Message(id = 604, value = "No identity store configuration found for requested type operation [%s.%s].")
    OperationNotSupportedException attributedTypeUnsupportedOperation(@Param Class<? extends AttributedType> type,
                                                                      @Param IdentityStoreConfiguration.IdentityOperation operation, Class<? extends AttributedType> typeToDisplay,
                                                                      IdentityStoreConfiguration.IdentityOperation operationToDisplay);

    @Message(id = 605, value = "Undefined partition for identity type [%s].")
    IdentityManagementException attributedUndefinedPartition(IdentityType identityType);

    @Message(value = "Could not find AttributedType [%s] with the given identifier [%s] for Partition [%s]")
    IdentityManagementException attributedTypeNotFoundWithId(Class<? extends AttributedType> type, String id, Partition partition);

    // Permission type management messages 800-899
    @Message(id = 800, value = "No PermissionStore configuration found for requested permission operation.")
    IdentityManagementException permissionUnsupportedOperation();

}