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
import java.lang.reflect.AnnotatedElement;

import javax.naming.NamingException;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.jboss.logging.Param;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.QueryParameter;
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

    @Message(id = 9, value = "Unsupported partition type [%s].")
    IdentityManagementException unsupportedPartitionType(String typeName);

    @Message(id = 10, value = "Unsupported IdentityType [%s].")
    IdentityManagementException unsupportedIdentityType(Class<? extends IdentityType> type);

    @Message(id = 11, value = "Could not add AttributedType [%s].")
    IdentityManagementException failToAddAttributedType(AttributedType attributedType, @Cause Throwable t);

    @Message(id = 12, value = "Unsupported AttributedType [%s].")
    IdentityManagementException unsupportedAttributedType(Class<? extends AttributedType> type);

    @Message(id = 13, value = "Could not find AttributedType [%s] with the given identifier [%s] for Partition [%s]")
    IdentityManagementException attributedTypeNotFoundWithId(Class<? extends AttributedType> type, String id, Partition partition);

    @Message(id = 14, value = "Method not implemented, yet.")
    RuntimeException notImplentedYet();

    @Message(id = 15, value = "Error creating instance for Relationship type [%s].")
    IdentityManagementException failInstantiateRelationshipType(String type, @Cause Throwable t);

    @Message(id = 16, value = "Could not find class [%s].")
    IdentityManagementException couldNotFindClass(String type);

    @Message(id = 17, value = "Null argument: [%s].")
    IdentityManagementException nullArgument(String description);

    @Message(id = 18, value = "No Group found with the given path [%s] for Partition [%s].")
    IdentityManagementException groupNotFoundWithPath(String path, Partition partition);

    @Message(id = 19, value = "Ambiguos relationship found [%s].")
    IdentityManagementException ambiguosRelationshipFound(Relationship relationship);

    @Message(id = 20, value = "Partition not found with id [%s].")
    IdentityManagementException partitionNotFoundWithId(String id);

    @Message(id = 21, value = "Unsupported value for Query Parameter [%s]. Value: %s.")
    IdentityManagementException unsupportedQueryParameterValue(String parameterName, Object parameterValue);

    @Message(id = 22, value = "No suitable CredentialHandler available for validating Credentials of type [%s].")
    IdentityManagementException credentialHandlerNotFoundForCredentialType(Class<?> class1);

    @Message(id = 23, value = "Error creating instance for CredentialStorage [%s].")
    SecurityConfigurationException failInstantiateCredentialStorage(Class<? extends CredentialStorage> type, @Cause Throwable t);

    @Message(id = 24, value = "Could not create contextual IdentityManager for Partition [%s]. Partition not found or it was null.")
    IdentityManagementException couldNotCreateContextualIdentityManager(Class<? extends Partition> type);

    @Message(id = 25, value = "IdentityType [%s] already exists with the given identifier [%s] for the given Partition [%s].")
    IdentityManagementException identityTypeAlreadyExists(Class<? extends IdentityType> type, String identifier,
            Partition partition);

    @Message(id = 26, value = "No Parent Group found with the given id [%s] for Partition [%s].")
    IdentityManagementException groupParentNotFoundWithId(String id, Partition partition);

    @Message(id = 27, value = "Unsupported IdentityType. Group members are only Agent or Group instances. You provided [%s].")
    IdentityManagementException unsupportedGroupMemberType(IdentityType identityType);

    @Message(id = 28, value = "Unsupported type for the Grant assignee. Roles are granted for Agent and Group only. You provided [%s].")
    IdentityManagementException unsupportedGrantAssigneeType(IdentityType identityType);

    @Message(id = 29, value = "A Partition [%s] with name [%s] already exists.")
    IdentityManagementException partitionAlreadyExistsWithName(Class<? extends Partition> type, String name);

    @Message(id = 30, value = "Ambiguous IdentityType found with identifier [%s].")
    IdentityManagementException ambiguosIdentityTypeFoundWithId(String id);

    @Message(id = 31, value = "Partition [%s] not found with the given name [%s].")
    IdentityManagementException partitionNotFoundWithName(Class<? extends Partition> type, String name);

    @Message(id = 32, value = "Partition [%s] could not be removed. There are IdentityTypes associated with it. Remove them first.")
    IdentityManagementException couldNotRemovePartitionWithIdentityTypes(Partition partition);

    @Message(id = 33, value = "The specified realm [%s] has not been configured.")
    SecurityConfigurationException realmNotConfigured(String realmName);

    @Message(id = 34, value = "No identity store configuration found that supports the relationship type [%s].")
    SecurityConfigurationException unsupportedRelationshipType(Class<? extends Relationship> type);

    @Message(id = 35, value = "No identity store configuration found for requested operation [%s.%s].")
    OperationNotSupportedException operationNotSupported(@Param FeatureGroup feature, @Param FeatureOperation operation,
            FeatureGroup featureToDisplay, FeatureOperation operationToDisplay);

    @Message(id = 36, value = "Exception while creating new IdentityStore instance [%s].")
    SecurityConfigurationException failInstantiateIdentityStore(Class<? extends IdentityStore<?>> identityStoreClass, @Cause Throwable t);

    @Message (id = 37, value="The IdentityStoreConfiguration specified is not supported by this IdentityStoreFactory implementation.")
    SecurityConfigurationException unsupportedStoreConfiguration();

    @Message (id = 38, value="Could not instantiate IdentityType class [%s].")
    IdentityManagementException failInstantiateIdentityClass(Class<?> identityClass, @Cause Throwable t);

    @Message (id = 39, value="QueryParameter [%s] is not supported for sorting.")
    IdentityManagementException notSortableQueryParameter(QueryParameter queryParam);

    @Message (id = 40, value="Could not instantiate Partition class [%s].")
    IdentityManagementException failInstantiatePartitionClass(Class<?> partitionClass, @Cause Throwable t);

    @Message (id = 41, value="Partition [%s] could not be removed. There are child partitions associated with it. Remove them first.")
    IdentityManagementException couldNotRemovePartitionWithChilds(Partition partition);

    @Message (id = 42, value="Could not instantiate Attribute class [%s].")
    IdentityManagementException failInstantiateAttributeClass(Class<?> attributeClass, @Cause Throwable t);
    
    @Message (id = 43, value="Could not instantiate Credential class [%s].")
    IdentityManagementException failInstantiateCredentialClass(Class<?> credentialClass, @Cause Throwable t);

    @Message (id = 44, value="Could not instantiate Credential Attribute class [%s].")
    IdentityManagementException failInstantiateCredentialAttributeClass(Class<?> attributeClass, @Cause Throwable t);

    @Message (id = 45, value="Error while trying to determine EntityManager - context parameter not set.")
    IdentityManagementException couldNotGetEntityManagerFromStoreContext();

    @Message (id = 46, value="Could not instantiate Relationship Attribute class [%s].")
    IdentityManagementException failInstantiateRelationshipAttributeClass(Class<?> attributeClass, @Cause Throwable t);

    @Message (id = 47, value="Could not instantiate Relationship class [%s].")
    IdentityManagementException failInstantiateRelationshipClass(Class<?> relationshipClass, @Cause Throwable t);
    
    @Message (id = 48, value="Could not instantiate Relationship Identity class [%s].")
    IdentityManagementException failInstantiateRelationshipIdentityClass(Class<?> relationshipIdentityClass, @Cause Throwable t);

    @Message (id = 49, value="Ambiguous property [%s] property in class [%s]")
    SecurityConfigurationException ambiguosPropertyForClass(String name, Class<?> targetClass);

    @Message (id = 50, value="Model property [%s] has not been configured.")
    IdentityManagementException jpaConfigModelPropertyNotConfigured(String name);

    @Message (id = 51, value="Error initializing JpaIdentityStore - identityClass not set.")
    SecurityConfigurationException jpaConfigIdentityClassNotProvided();

    @Message (id = 52, value="Error initializing JpaIdentityStore - partitionClass not set.")
    SecurityConfigurationException jpaConfigPartitionClassNotProvided();

    @Message (id = 53, value="Multiple properties defined for attribute [%s] - Property: %s.%s, Property: %s.%s")
    SecurityConfigurationException jpaConfigMultiplePropertiesForAttribute(String attribName, Class<?> property, AnnotatedElement annotatedElement, Class<?> anotherProperty, AnnotatedElement anotherAnnotatedElement);

    @Message (id = 54, value="Could not retrieve LDAP attribute [%s]")
    IdentityManagementException ldapStoreFailToRetrieveAttribute(String entryUuid, @Cause Throwable t);

    @Message (id = 55, value="Unknown Base DN [%s] for IdentityType.")
    IdentityManagementException ldapStoreUnknownBaseDNForIdentityType(String nameInNamespace);

    @Message (id = 56, value="Could not execute search on server.")
    IdentityManagementException ldapStoreSearchFailed(@Cause NamingException nme);

    @Message (id = 57, value="Could not create GroupRole entry.")
    IdentityManagementException ldapStoreCouldNotCreateGroupRoleEntry(@Cause NamingException e);

    @Message (id = 58, value="Could not remove GroupRole entry.")
    IdentityManagementException ldapStoreCouldNotRemoveGroupRoleEntry(@Cause NamingException e);

    @Message (id = 59, value="User baseDN not provided.")
    SecurityConfigurationException ldapConfigUserDNNotProvided();

    @Message (id = 60, value="Role baseDN not provided.")
    SecurityConfigurationException ldapConfigRoleDNNotProvided();

    @Message (id = 61, value="Group baseDN not provided.")
    SecurityConfigurationException ldapConfigGroupDNNotProvided();

    @Message (id = 62, value="Could not encode password.")
    IdentityManagementException credentialCouldNotEncodePassword(@Cause UnsupportedEncodingException e);

}
