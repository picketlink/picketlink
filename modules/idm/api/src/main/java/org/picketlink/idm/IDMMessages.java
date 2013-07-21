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
import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.jboss.logging.Param;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.encoder.PasswordEncoder;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStore;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * <p>
 * {@link MessageBundle} interface for all exceptions thrown by the IDM.
 * </p>
 * <p>
 * Methods names follow the rule: [Context]+[Short Message Description]. For example, the name
 * <code>storeConfigLockedFeatureSet</code>. In this case the [Context] is storeConfig, meaning that this message is used by the
 * identity store configuration. The LockedFeatureSet is a short description for the message itself.
 * </p>
 * <p>General messages may not have a [Context].</p>
 *
 * @author Pedro Silva
 *
 */
@MessageBundle(projectCode = "PLIDM")
public interface IDMMessages {

    IDMMessages MESSAGES = Messages.getBundle(IDMMessages.class);

    // General messages. Ids 1-99.
    @Message(id = 1, value = "IdentityType [%s] already exists with the given identifier [%s] for the given Partition [%s].")
    IdentityManagementException identityTypeAlreadyExists(Class<? extends IdentityType> type, String identifier,
            Partition partition);

    @Message(id = 2, value = "Ambiguous AttributedType found with identifier [%s].")
    IdentityManagementException attributedTypeAmbiguosFoundWithId(String id);

    @Message(id = 3, value = "Could not add AttributedType [%s].")
    IdentityManagementException attributedTypeAddFailed(AttributedType identityType, @Cause Throwable t);

    @Message(id = 4, value = "Could not remove AttributedType [%s].")
    IdentityManagementException attributedTypeRemoveFailed(AttributedType identityType, @Cause Throwable t);

    @Message(id = 5, value = "Could not update AttributedType [%s].")
    IdentityManagementException attributedTypeUpdateFailed(AttributedType identityType, @Cause Throwable t);

    @Message(id = 6, value = "Could not query IdentityType using query [%s].")
    IdentityManagementException identityTypeQueryFailed(IdentityQuery<?> query, @Cause Throwable t);

    @Message(id = 9, value = "Could not find AttributedType [%s] with the given identifier [%s] for Partition [%s]")
    IdentityManagementException attributedTypeNotFoundWithId(Class<? extends AttributedType> type, String id,
            Partition partition);

    @Message(id = 11, value = "Error creating instance for type [%s].")
    IdentityManagementException instantiationError(Class<?> type, @Cause Throwable t);

    @Message(id = 13, value = "Null argument: [%s].")
    IdentityManagementException nullArgument(String description);

    @Message(id = 14, value = "Error unmarshalling object.")
    IdentityManagementException unmarshallingError(@Cause Throwable t);

    @Message(id = 15, value = "Error marshalling object.")
    IdentityManagementException marshallingError(@Cause Throwable t);

    @Message(id = 16, value = "Could not create contextual IdentityManager for Partition [%s]. Partition not found or it was null.")
    IdentityManagementException couldNotCreateContextualIdentityManager(Partition partition);

    @Message(id = 22, value = "No identity store configuration found for requested type operation [%s.%s].")
    OperationNotSupportedException storeConfigUnsupportedOperation(@Param Class<? extends AttributedType> type,
            @Param IdentityOperation operation, Class<? extends AttributedType> typeToDisplay,
            IdentityOperation operationToDisplay);

    @Message(id = 23, value = "Error creating instance for CredentialHandler [%s].")
    IdentityManagementException credentialCredentialHandlerInstantiationError(Class<? extends CredentialHandler> type,
            @Cause Throwable t);

    @Message(id = 24, value = "Credentials class [%s] not supported by this handler [%s].")
    IdentityManagementException credentialUnsupportedType(Class<?> type, CredentialHandler handler);

    @Message(id = 25, value = "Provided IdentityStore [%s] is not an instance of CredentialStore.")
    IdentityManagementException credentialInvalidCredentialStoreType(
            @SuppressWarnings("rawtypes") Class<? extends IdentityStore> type);

    @Message(id = 26, value = "Invalid Realm or it was not provided.")
    IdentityManagementException credentialDigestInvalidRealm();

    @Message(id = 27, value = "Invalid Password or it was not provided.")
    IdentityManagementException credentialInvalidPassword();

    @Message(id = 28, value = "Could not encode password.")
    IdentityManagementException credentialCouldNotEncodePassword(@Cause UnsupportedEncodingException e);

    @Message(id = 29, value = "No suitable CredentialHandler available for validating Credentials of type [%s].")
    IdentityManagementException credentialHandlerNotFoundForCredentialType(Class<?> class1);

    @Message(id = 36, value = "Could not remove Relationship [%s].")
    IdentityManagementException relationshipRemoveFailed(Relationship relationship, @Cause Throwable t);

    @Message(id = 38, value = "Could not query Relationship using query [%s].")
    IdentityManagementException relationshipQueryFailed(RelationshipQuery<?> query, @Cause Throwable t);

    @Message(id = 41, value = "A Partition [%s] with name [%s] already exists.")
    IdentityManagementException partitionAlreadyExistsWithName(Class<? extends Partition> type, String name);

    @Message(id = 42, value = "Partition [%s] not found with the given name [%s].")
    IdentityManagementException partitionNotFoundWithName(Class<? extends Partition> type, String name);

    @Message(id = 45, value = "Unsupported value for Query Parameter [%s]. Value: %s.")
    IdentityManagementException queryUnsupportedParameterValue(String parameterName, Object parameterValue);

    @Message(id = 46, value = "Error while trying to determine EntityManager - context parameter not set.")
    IdentityManagementException jpaStoreCouldNotGetEntityManagerFromStoreContext();

    @Message(id = 50, value = "Error initializing JpaIdentityStore - no entity classes configured.")
    SecurityConfigurationException jpaConfigNoEntityClassesProvided();

    @Message(id = 63, value = "Could not create context.")
    IdentityManagementException ldapCouldNotCreateContext(@Cause Throwable e);

    @Message(id = 73, value = "No such algorithm [%s] for encoding passwords. Using PasswordEncoder [%s].")
    IdentityManagementException credentialInvalidEncodingAlgorithm(String algorithm, PasswordEncoder encoder, @Cause Throwable t);
}