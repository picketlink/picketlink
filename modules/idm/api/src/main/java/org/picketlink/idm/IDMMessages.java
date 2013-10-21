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
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.jboss.logging.Param;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.encoder.PasswordEncoder;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStore;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;

import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * <p>
 * {@link MessageBundle} interface for all exceptions thrown by the IDM.
 * </p>
 * <p>
 * Methods names follow the rule: [Context]+[Short Message Description]. For example, the name
 * <code>storeConfigLockedFeatureSet</code>. In this case the [Context] is storeConfig, meaning that this message is
 * used by the identity store configuration. The LockedFeatureSet is a short description for the message itself.
 * </p>
 * <p>
 * General messages may not have a [Context].
 * </p>
 * <p>
 * When creating new messages, you may or not identify messages with an identifier. The identifier is only used for
 * messages with an important meaning for users, from which they can easily identify the context of an exception or
 * failure.
 * </p>
 *
 * <p>
 * Messages with no identifier usually means they are used as root exceptions, with more details about an exception or
 * failure.
 * </p>
 *
 * @author Pedro Silva
 */
@MessageBundle(projectCode = "PLIDM")
public interface IDMMessages {

    IDMMessages MESSAGES = Messages.getBundle(IDMMessages.class);

    // General messages. Ids 1-99.
    @Message(id = 1, value = "Error creating instance for type [%s].")
    IdentityManagementException instantiationError(Class<?> type, @Cause Throwable t);

    @Message(id = 2, value = "Null argument: [%s].")
    IdentityManagementException nullArgument(String description);

    @Message(id = 3, value = "Error unmarshalling object.")
    IdentityManagementException unmarshallingError(@Cause Throwable t);

    @Message(id = 4, value = "Error marshalling object.")
    IdentityManagementException marshallingError(@Cause Throwable t);

    @Message(id = 5, value = "Unexpected type [%s].")
    IdentityManagementException unexpectedType(Class<?> unexpectedType);

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 6, value = "Could not initialize Partition Manager [%s].")
    IdentityManagementException partitionManagerInitializationFailed(Class<? extends PartitionManager> partitionManagerType, @Cause Throwable t);

    // credential API messages 200-299
    @Message(id = 200, value = "Credential validation failed [%s].")
    IdentityManagementException credentialValidationFailed(Credentials credentials, @Cause Throwable t);

    @Message(id = 201, value = "Credential update failed for account [%s] and type [%s].")
    IdentityManagementException credentialUpdateFailed(Account account, Object credential, @Cause Throwable t);

    @Message(id = 202, value = "No IdentityStore found for credential class [%s]")
    IdentityManagementException credentialNoStoreForCredentials(Class<?> credentialClass);

    @Message(id = 203, value = "Credentials class [%s] not supported by this handler [%s].")
    IdentityManagementException credentialUnsupportedType(Class<?> type, CredentialHandler handler);

    @Message(id = 204, value = "Credentials could not be retrieved for account [%s] and storage [%s].")
    <T extends CredentialStorage> IdentityManagementException credentialRetrievalFailed(Account account, Class<T> storageClass, @Cause Throwable t);

    @Message(value = "Invalid Realm or it was not provided.")
    IdentityManagementException credentialDigestInvalidRealm();

    @Message(value = "Invalid Password or it was not provided.")
    IdentityManagementException credentialInvalidPassword();

    @Message(value = "Could not encode password.")
    IdentityManagementException credentialCouldNotEncodePassword(@Cause UnsupportedEncodingException e);

    @Message(value = "No suitable CredentialHandler available for validating Credentials of type [%s].")
    IdentityManagementException credentialHandlerNotFoundForCredentialType(Class<?> class1);

    @Message(value = "No such algorithm [%s] for encoding passwords. Using PasswordEncoder [%s].")
    IdentityManagementException credentialInvalidEncodingAlgorithm(String algorithm, PasswordEncoder encoder, @Cause Throwable t);

    // identity store API messages 300-399
    @Message(id = 300, value = "No store found with type [%s].")
    IdentityManagementException storeNotFound(Class<? extends IdentityStore> partitionStoreClass);

    @Message(id = 301, value = "Error while trying to determine EntityManager - context parameter not set.")
    IdentityManagementException storeJpaCouldNotGetEntityManagerFromStoreContext();

    @Message(value = "Could not create context.")
    IdentityManagementException storeLdapCouldNotCreateContext(@Cause Throwable e);

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
                                                                  @Param IdentityOperation create);

    @Message(id = 410, value = "Could not create contextual PermissionManager for Partition [%s].")
    IdentityManagementException partitionCouldNotCreatePermissionManager(Partition partition);

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
                                                                      @Param IdentityOperation operation, Class<? extends AttributedType> typeToDisplay,
                                                                      IdentityOperation operationToDisplay);

    @Message(value = "Could not find AttributedType [%s] with the given identifier [%s] for Partition [%s]")
    IdentityManagementException attributedTypeNotFoundWithId(Class<? extends AttributedType> type, String id, Partition partition);

    // configuration api messages 700-799
    @Message(id = 700, value = "Could not create configuration.")
    SecurityConfigurationException configCouldNotCreateConfiguration(@Cause Exception sce);

    @Message(value = "Error initializing JpaIdentityStore - no entity classes configured.")
    SecurityConfigurationException configJpaStoreNoEntityClassesProvided();

    @Message(value = "Entity [%s] must have a field annotated with %s.")
    SecurityConfigurationException configJpaStoreRequiredMappingAnnotation(Class<?> entityType,
                                                                           Class<? extends Annotation> annotation);

    @Message(value = "Mapped attribute [%s.%s] does not map to any field for type [%s].")
    SecurityConfigurationException configJpaStoreMappedPropertyNotFound(final Class<?> entityType, String propertyName, Class<?> type);

    @Message(value = "At least one IdentityConfiguration must be provided")
    SecurityConfigurationException configNoIdentityConfigurationProvided();

    @Message(value = "Unknown IdentityStore class for configuration [%s].")
    SecurityConfigurationException configUnknownStoreForConfiguration(IdentityStoreConfiguration storeConfiguration);

    @Message(value = "Error while creating IdentityStore [%s] instance for configuration [%s].")
    SecurityConfigurationException configCouldNotCreateStore(Class<? extends IdentityStore> storeClass,
                                                             IdentityStoreConfiguration storeConfiguration,
                                                             @Cause Exception e);

    @Message(value = "You have provided more than one configuration. Use the buildAll method instead.")
    SecurityConfigurationException configBuildMultipleConfigurationExists();

    @Message(value = "Only a single identity store config can support partitions. Found [%s] and [%s].")
    SecurityConfigurationException configStoreMultiplePartitionConfigExists(IdentityStoreConfiguration config1,
                                                                            IdentityStoreConfiguration config2);

    @Message(value = "Duplicated supported types found for identity store configuration [%s].")
    SecurityConfigurationException configStoreDuplicatedSupportedType(IdentityStoreConfiguration storeConfiguration);

    @Message(value = "You must configure at least one identity store configuration.")
    SecurityConfigurationException configStoreNoIdentityStoreConfigProvided();

    @Message(value = "You must provide at least one configuration.")
    SecurityConfigurationException configNoConfigurationProvided();

    @Message(value = "Multiple configuration with the same name [%s].")
    SecurityConfigurationException configMultipleConfigurationsFoundWithSameName(String name);

    @Message(value = "Invalid configuration [%s].")
    SecurityConfigurationException configInvalidConfiguration(String name, @Cause Throwable t);

    @Message(value = "Multiple configuration with credential support.")
    SecurityConfigurationException configMultipleConfigurationsFoundWithCredentialSupport();

    // Permission management messages 800-899
    @Message(value = "Could not grant Permission [%s].")
    IdentityManagementException permissionGrantFailed(Permission permission, @Cause Throwable t);
}