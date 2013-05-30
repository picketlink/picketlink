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
import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.password.PasswordEncoder;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStore;

import javax.naming.NamingException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.AnnotatedElement;

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

    @Message(id = 1, value = "Unsupported IdentityType [%s].")
    IdentityManagementException identityTypeUnsupportedType(Class<? extends IdentityType> type);

    @Message(id = 2, value = "IdentityType [%s] already exists with the given identifier [%s] for the given Partition [%s].")
    IdentityManagementException identityTypeAlreadyExists(Class<? extends IdentityType> type, String identifier,
            Partition partition);

    @Message(id = 3, value = "Ambiguous IdentityType found with identifier [%s].")
    IdentityManagementException identityTypeAmbiguosFoundWithId(String id);

    @Message (id = 52, value = "Could not add IdentityType [%s].")
    IdentityManagementException identityTypeAddFailed(IdentityType identityType, @Cause Throwable t);

    @Message (id = 53, value = "Could not remove IdentityType [%s].")
    IdentityManagementException identityTypeRemoveFailed(IdentityType identityType, @Cause Throwable t);

    @Message (id = 54, value = "Could not update IdentityType [%s].")
    IdentityManagementException identityTypeUpdateFailed(IdentityType identityType, @Cause Throwable t);

    @Message (id = 55, value = "Could not query IdentityType using query [%s].")
    IdentityManagementException identityTypeQueryFailed(IdentityQuery<?> query, @Cause Throwable t);

    @Message(id = 4, value = "Unsupported AttributedType [%s].")
    IdentityManagementException attributedTypeUnsupportedType(Class<? extends AttributedType> type);

    @Message(id = 5, value = "Could not find AttributedType [%s] with the given identifier [%s] for Partition [%s]")
    IdentityManagementException attributedTypeNotFoundWithId(Class<? extends AttributedType> type, String id,
            Partition partition);

    @Message(id = 6, value = "Method not implemented, yet.")
    RuntimeException notImplentedYet();

    @Message(id = 7, value = "Error creating instance for type [%s].")
    IdentityManagementException instantiationError(String type, @Cause Throwable t);

    @Message(id = 8, value = "Could not find class [%s].")
    IdentityManagementException classNotFound(String type);

    @Message(id = 9, value = "Null argument: [%s].")
    IdentityManagementException nullArgument(String description);

    @Message(id = 10, value = "Error unmarshalling object.")
    IdentityManagementException unmarshallingError(@Cause Throwable t);

    @Message(id = 11, value = "Error marshalling object.")
    IdentityManagementException marshallingError(@Cause Throwable t);

    @Message(id = 12, value = "Could not create contextual IdentityManager for Partition [%s]. Partition not found or it was null.")
    IdentityManagementException couldNotCreateContextualIdentityManager(Partition partition);

    @Message(id = 13, value = "QueryParameter [%s] is not supported for sorting.")
    IdentityManagementException notSortableQueryParameter(QueryParameter queryParam);

    @Message(id = 14, value = "Feature set has already been locked, no additional features may be added.")
    SecurityConfigurationException storeConfigLockedFeatureSet();

    @Message(id = 15, value = "The specified realm [%s] has not been configured.")
    SecurityConfigurationException storeConfigRealmNotConfigured(String realmId);

    @Message(id = 16, value = "No identity store configuration found that supports the relationship type [%s] and operation [%s].")
    SecurityConfigurationException storeConfigUnsupportedRelationshipType(Class<? extends Relationship> type, FeatureOperation operation);

    @Message(id = 17, value = "The IdentityStoreConfiguration [%s] specified is not supported by this IdentityStoreFactory implementation.")
    SecurityConfigurationException storeConfigUnsupportedConfiguration(IdentityStoreConfiguration config);

    @Message(id = 18, value = "No identity store configuration found for requested operation [%s.%s].")
    OperationNotSupportedException storeConfigUnsupportedOperation(@Param FeatureGroup feature,
            @Param FeatureOperation operation, FeatureGroup featureToDisplay, FeatureOperation operationToDisplay);

    @Message(id = 19, value = "Error creating instance for CredentialHandler [%s].")
    IdentityManagementException credentialCredentialHandlerInstantiationError(Class<? extends CredentialHandler> type,
            @Cause Throwable t);

    @Message(id = 20, value = "Credentials class [%s] not supported by this handler [%s].")
    IdentityManagementException credentialUnsupportedType(Class<?> type, CredentialHandler handler);

    @Message(id = 21, value = "Provided IdentityStore [%s] is not an instance of CredentialStore.")
    IdentityManagementException credentialInvalidCredentialStoreType(
            @SuppressWarnings("rawtypes") Class<? extends IdentityStore> type);

    @Message(id = 22, value = "Invalid Realm or it was not provided.")
    IdentityManagementException credentialDigestInvalidRealm();

    @Message(id = 23, value = "Invalid Password or it was not provided.")
    IdentityManagementException credentialInvalidPassword();

    @Message(id = 24, value = "Could not encode password.")
    IdentityManagementException credentialCouldNotEncodePassword(@Cause UnsupportedEncodingException e);

    @Message(id = 25, value = "No suitable CredentialHandler available for validating Credentials of type [%s].")
    IdentityManagementException credentialHandlerNotFoundForCredentialType(Class<?> class1);

    @Message(id = 26, value = "No Group found with the given path [%s] for Partition [%s].")
    IdentityManagementException groupNotFoundWithPath(String path, Partition partition);

    @Message(id = 27, value = "No Parent Group found with the given id [%s] for Partition [%s].")
    IdentityManagementException groupParentNotFoundWithId(String id, Partition partition);

    @Message(id = 28, value = "Ambiguos relationship found [%s].")
    IdentityManagementException relationshipAmbiguosFound(Relationship relationship);

    @Message(id = 29, value = "Unsupported IdentityType. Group members are only Agent or Group instances. You provided [%s].")
    IdentityManagementException relationshipUnsupportedGroupMemberType(IdentityType identityType);

    @Message(id = 30, value = "Unsupported type for the Grant assignee. Roles are granted for Agent and Group only. You provided [%s].")
    IdentityManagementException relationshipUnsupportedGrantAssigneeType(IdentityType identityType);

    @Message (id = 56, value = "Could not add Relationship [%s].")
    IdentityManagementException relationshipAddFailed(Relationship relationship, @Cause Throwable t);

    @Message (id = 57, value = "Could not remove Relationship [%s].")
    IdentityManagementException relationshipRemoveFailed(Relationship relationship, @Cause Throwable t);

    @Message (id = 58, value = "Could not update Relationship [%s].")
    IdentityManagementException relationshipUpdateFailed(Relationship relationship, @Cause Throwable t);

    @Message (id = 59, value = "Could not query Relationship using query [%s].")
    IdentityManagementException relationshipQueryFailed(RelationshipQuery<?> query, @Cause Throwable t);

    @Message(id = 31, value = "Partition not found with id [%s].")
    IdentityManagementException partitionNotFoundWithId(String id);

    @Message(id = 32, value = "Unsupported partition type [%s].")
    IdentityManagementException partitionUnsupportedType(String typeName);

    @Message(id = 33, value = "A Partition [%s] with name [%s] already exists.")
    IdentityManagementException partitionAlreadyExistsWithName(Class<? extends Partition> type, String name);

    @Message(id = 34, value = "Partition [%s] not found with the given name [%s].")
    IdentityManagementException partitionNotFoundWithName(Class<? extends Partition> type, String name);

    @Message(id = 35, value = "Partition [%s] could not be removed. There are IdentityTypes associated with it. Remove them first.")
    IdentityManagementException partitionCouldNotRemoveWithIdentityTypes(Partition partition);

    @Message(id = 36, value = "Partition [%s] could not be removed. There are child partitions associated with it. Remove them first.")
    IdentityManagementException partitionCouldNotRemoveWithChilds(Partition partition);

    @Message(id = 37, value = "Unsupported value for Query Parameter [%s]. Value: %s.")
    IdentityManagementException queryUnsupportedParameterValue(String parameterName, Object parameterValue);

    @Message(id = 38, value = "Error while trying to determine EntityManager - context parameter not set.")
    IdentityManagementException jpaStoreCouldNotGetEntityManagerFromStoreContext();

    @Message(id = 39, value = "Ambiguous property [%s] property in class [%s]")
    SecurityConfigurationException jpaConfigAmbiguosPropertyForClass(String name, Class<?> targetClass);

    @Message(id = 40, value = "Model property [%s] has not been configured.")
    SecurityConfigurationException jpaConfigModelPropertyNotConfigured(String name);

    @Message(id = 41, value = "Error initializing JpaIdentityStore - identityClass not set.")
    SecurityConfigurationException jpaConfigIdentityClassNotProvided();

    @Message(id = 42, value = "Error initializing JpaIdentityStore - partitionClass not set.")
    SecurityConfigurationException jpaConfigPartitionClassNotProvided();

    @Message(id = 43, value = "Multiple properties defined for attribute [%s] - Property: %s.%s, Property: %s.%s")
    SecurityConfigurationException jpaConfigMultiplePropertiesForAttribute(String attribName, Class<?> property,
            AnnotatedElement annotatedElement, Class<?> anotherProperty, AnnotatedElement anotherAnnotatedElement);

    @Message(id = 44, value = "Could not retrieve LDAP attribute [%s]")
    IdentityManagementException ldapStoreFailToRetrieveAttribute(String entryUuid, @Cause Throwable t);

    @Message(id = 45, value = "Unknown Base DN [%s] for IdentityType.")
    IdentityManagementException ldapStoreUnknownBaseDNForIdentityType(String nameInNamespace);

    @Message(id = 46, value = "Could not execute search on server.")
    IdentityManagementException ldapStoreSearchFailed(@Cause NamingException nme);

    @Message(id = 47, value = "Could not create GroupRole entry.")
    IdentityManagementException ldapStoreCouldNotCreateGroupRoleEntry(@Cause NamingException e);

    @Message(id = 48, value = "Could not remove GroupRole entry.")
    IdentityManagementException ldapStoreCouldNotRemoveGroupRoleEntry(@Cause NamingException e);

    @Message(id = 49, value = "User baseDN not provided.")
    SecurityConfigurationException ldapConfigUserDNNotProvided();

    @Message(id = 50, value = "Role baseDN not provided.")
    SecurityConfigurationException ldapConfigRoleDNNotProvided();

    @Message(id = 51, value = "Group baseDN not provided.")
    SecurityConfigurationException ldapConfigGroupDNNotProvided();

    @Message(id = 60, value = "Could not initialize filesystem.")
    SecurityConfigurationException fileConfigFailedToInitializeFilesystem(@Cause Throwable t);

    @Message(id = 61, value = "No discriminator could be determined for type [%s].")
    SecurityConfigurationException jpaConfigDiscriminatorNotFoundForIdentityType(Class<? extends IdentityType> type);

    @Message(id = 62, value = "Could not create context.")
    IdentityManagementException ldapCouldNotCreateContext(@Cause Throwable e);

    @Message(id = 63, value = "Could not find Users BaseDN [%s].")
    IdentityManagementException ldapCouldNotFindUsersBaseDN(String baseDN, @Cause Throwable e);

    @Message(id = 64, value = "Could not find Roles BaseDN [%s].")
    IdentityManagementException ldapCouldNotFindRolesBaseDN(String baseDN, @Cause Throwable e);

    @Message(id = 65, value = "Could not find Groups BaseDN [%s].")
    IdentityManagementException ldapCouldNotFindGroupsBaseDN(String baseDN, @Cause Throwable e);

    @Message(id = 66, value = "Could not find Agents BaseDN [%s].")
    IdentityManagementException ldapCouldNotFindAgentsBaseDN(String baseDN, @Cause Throwable e);

    @Message(id = 67, value = "Agents can only be stored and managed from a Realm. Current partition is [%s].")
    IdentityManagementException partitionInvalidTypeForAgents(Class<? extends Partition> partitionType);

    @Message(id = 68, value = "The specified tier [%s] has not been configured.")
    SecurityConfigurationException storeConfigTierNotConfigured(String tierId);

    @Message(id = 69, value = "Could not create IdentityManagerFactory implementation [%s].")
    SecurityConfigurationException configurationCouldNotCreateIdentityManagerFactoryImpl(String typeName, @Cause Throwable t);

    @Message(id = 70, value = "The default Realm was not configured for any identity store. Check your configuration.")
    SecurityConfigurationException configurationDefaultRealmNotDefined();

    @Message(id = 71, value = "The same feature [%s] was configured for different IdentityStoreConfiguration [%s, %s].")
    SecurityConfigurationException configurationAmbiguousFeatureForStore(FeatureGroup feature, IdentityStoreConfiguration config1, IdentityStoreConfiguration config2);

    @Message(id = 72, value = "No such algorithm [%s] for encoding passwords. Using PasswordEncoder [%s].")
    IdentityManagementException credentialInvalidEncodingAlgorithm(String algorithm, PasswordEncoder encoder, @Cause Throwable t);

    @Message(id = 73, value = "Credentials can only be managed from a Realm. Current partition is [%s].")
    IdentityManagementException partitionInvalidTypeForCredential(Class<? extends Partition> partitionType);

    @Message(id = 74, value = "The same IdentityType [%s] was configured for different IdentityStoreConfiguration [%s, %s].")
    SecurityConfigurationException configurationAmbiguousIdentityTypeForStore(Class<? extends IdentityType> identityType, IdentityStoreConfiguration config1, IdentityStoreConfiguration config2);

    @Message(id = 75, value = "The same Relationship [%s] was configured for different IdentityStoreConfiguration [%s, %s].")
    SecurityConfigurationException configurationAmbiguousRelationshipForStore(Class<? extends Relationship> relationship, IdentityStoreConfiguration config1, IdentityStoreConfiguration config2);

    @Message(id = 76, value = "No identity store configuration found that supports the identity type [%s] and operation [%s].")
    SecurityConfigurationException storeConfigUnsupportedIdentityType(Class<? extends IdentityType> type, FeatureOperation operation);

}