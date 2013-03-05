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
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.IdentityStore;

/**
 * @author Pedro Silva
 * 
 */
@MessageBundle(projectCode = "PLIDM")
public interface IDMMessages {

    IDMMessages MESSAGES = Messages.getBundle(IDMMessages.class);

    @Message(id = 1, value = "Feature set has already been locked, no additional features may be added.")
    SecurityConfigurationException lockedFeatureSet();

    @Message(id = 2, value = "Error creating instance for CredentialHandler [%s].")
    SecurityConfigurationException failInstantiateCredentialHandler(Class<? extends CredentialHandler> type, @Cause Throwable t);

    @Message(id = 3, value = "Credentials class [%s] not supported by this handler.")
    IdentityManagementException unsupportedCredentialType(Class<?> type);

    @Message(id = 4, value = "Provided IdentityStore [%s] is not an instance of CredentialStore.")
    IdentityManagementException invalidCredentialStoreType(@SuppressWarnings("rawtypes") Class<? extends IdentityStore> type);

    @Message(id = 5, value = "Invalid Realm or it was not provided.")
    IdentityManagementException invalidRealm();

    @Message(id = 6, value = "Invalid Password or it was not provided.")
    IdentityManagementException invalidPassword();

    @Message(id = 7, value = "Error unmarshalling object.")
    IdentityManagementException unmarshallingError(@Cause Throwable t);

    @Message(id = 8, value = "Error marshalling object.")
    IdentityManagementException marshallingError(@Cause Throwable t);

    @Message (id = 9, value="Unsupported partition type [%s].")
    IdentityManagementException unsupportedPartitionType(String typeName);

    @Message (id = 10, value="Unsupported IdentityType [%s].")
    IdentityManagementException unsupportedIdentityType(Class<? extends IdentityType> type);

    @Message (id = 11, value="Could not add AttributedType [%s].")
    IdentityManagementException failToAddAttributedType(AttributedType attributedType, @Cause Throwable t);

    @Message (id = 12, value="Unsupported AttributedType [%s].")
    IdentityManagementException unsupportedAttributedType(Class<? extends AttributedType> type);

    @Message (id = 13, value="Could not find AttributedType with the given identifier [%s] for Partition [%s]")
    IdentityManagementException attributedTypeNotFoundWithId(String id, Partition partition);

    @Message (id = 14, value="Method not implemented, yet.")
    RuntimeException notImplentedYet();

    @Message(id = 15, value = "Error creating instance for Relationship type [%s].")
    IdentityManagementException failInstantiateRelationshipType(Class<? extends Relationship> type, @Cause Throwable t);

    @Message(id = 16, value = "Could not find class [%s].")
    IdentityManagementException couldNotFindClass(String type);

    @Message(id = 17, value = "Null argument: [%s].")
    IdentityManagementException nullArgument(String description);

    @Message (id = 18, value="No Group found with the given name [%s] for Partition [%s].")
    IdentityManagementException groupNotFoundWithPath(String path, Partition partition);

    @Message (id = 19, value="Ambiguos relationship found [%s].")
    IdentityManagementException ambiguosRelationshipFound(Relationship relationship);

    @Message (id = 20, value="Partition not found with id [%s].")
    IdentityManagementException partitionNotFoundWithId(String id);

    @Message (id = 21, value="Unsupported value for Query Parameter [%s]. Value: %s")
    IdentityManagementException unsupportedQueryParameterValue(String parameterName, Object parameterValue);

    @Message (id = 22, value="No suitable CredentialHandler available for validating Credentials of type [%s].")
    IdentityManagementException credentialHandlerNotFoundForCredentialType(Class<?> class1);

    @Message(id = 23, value = "Error creating instance for CredentialStorage [%s].")
    SecurityConfigurationException failInstantiateCredentialStorage(Class<? extends CredentialStorage> type, @Cause Throwable t);

}
