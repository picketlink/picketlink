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

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.encoder.PasswordEncoder;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.spi.IdentityStore;

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
@MessageBundle(projectCode = IDMLog.PICKETLINK_IDM_PROJECT_CODE)
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

    @Message(id = 205, value = "The IdentityType returned is not an Account: [%s]")
    IdentityManagementException credentialInvalidAccountType(Class<? extends IdentityType> aClass);

    @Message(id = 206, value = "Multiple Account objects found with same login name [%s] for account type [%s].")
    IdentityManagementException credentialMultipleAccountsFoundForType(String loginName, Class<? extends Account> accountType);

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

    // configuration api messages 700-799
    @Message(id = 700, value = "Could not create configuration.")
    SecurityConfigurationException configCouldNotCreateConfiguration(@Cause Exception sce);

    @Message(id = 701, value = "Invalid configuration [%s].")
    SecurityConfigurationException configInvalidConfiguration(String name, @Cause Throwable t);

    @Message(id = 702, value = "You must provide at least one configuration.")
    SecurityConfigurationException configNoConfigurationProvided();

    @Message(id = 703, value = "You have provided more than one configuration. Use the buildAll method instead.")
    SecurityConfigurationException configBuildMultipleConfigurationExists();

    @Message(id = 704, value = "At least one IdentityConfiguration must be provided")
    SecurityConfigurationException configNoIdentityConfigurationProvided();

    @Message(id = 705, value = "You must configure at least one identity store.")
    SecurityConfigurationException configStoreNoIdentityStoreConfigProvided();

    @Message(id = 706, value = "Duplicated supported types [%s] found for identity store configuration. Check your identity store configuration for duplicated types, considering their hierarchy.")
    SecurityConfigurationException configStoreDuplicatedSupportedType(Class<?> supportedType);

    @Message(id = 707, value = "Multiple configuration with credential support.")
    SecurityConfigurationException configMultipleConfigurationsFoundWithCredentialSupport();

    @Message(value = "Error initializing JpaIdentityStore - no entity classes configured.")
    SecurityConfigurationException configJpaStoreNoEntityClassesProvided();

    @Message(value = "Entity [%s] must have a field annotated with %s.")
    SecurityConfigurationException configJpaStoreRequiredMappingAnnotation(Class<?> entityType,
                                                                           Class<? extends Annotation> annotation);

    @Message(value = "Mapped attribute [%s.%s] does not map to any field for type [%s].")
    SecurityConfigurationException configJpaStoreMappedPropertyNotFound(final Class<?> entityType, String propertyName, Class<?> type);

    @Message(value = "Unknown IdentityStore class for configuration [%s].")
    SecurityConfigurationException configUnknownStoreForConfiguration(IdentityStoreConfiguration storeConfiguration);

    @Message(value = "Error while creating IdentityStore [%s] instance for configuration [%s].")
    SecurityConfigurationException configCouldNotCreateStore(Class<? extends IdentityStore> storeClass,
                                                             IdentityStoreConfiguration storeConfiguration,
                                                             @Cause Exception e);

    @Message(value = "Only a single identity store config can support partitions. Found [%s] and [%s].")
    SecurityConfigurationException configStoreMultiplePartitionConfigExists(IdentityStoreConfiguration config1,
                                                                            IdentityStoreConfiguration config2);

    @Message(value = "Multiple configuration with the same name [%s].")
    SecurityConfigurationException configMultipleConfigurationsFoundWithSameName(String name);

    // Permission management messages 800-899

    @Message(value = "Could not grant permission to [%s] for resource [%s], operation [%s].")
    IdentityManagementException permissionGrantFailed(IdentityType assignee, Object resource, String operation, @Cause Throwable t);

    @Message(value = "Could not grant Permissions [%s].")
    IdentityManagementException permissionsGrantFailed(String permissions, @Cause Throwable t);

    @Message(value = "Could not revoke permission granted to [%s] for resource [%s], operation [%s].")
    IdentityManagementException permissionRevokeFailed(IdentityType assignee, Object resource, String operation, @Cause Throwable t);

    @Message(value = "Could not revoke Permissions [%s].")
    IdentityManagementException permissionsRevokeFailed(String permissions, @Cause Throwable t);

    @Message(value = "Failed to revoke all permissions for resource [%s].")
    IdentityManagementException permissionRevokeAllFailed(Object resource, @Cause Throwable t);


}