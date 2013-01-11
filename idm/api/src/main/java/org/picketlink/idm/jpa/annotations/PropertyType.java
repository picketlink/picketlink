package org.picketlink.idm.jpa.annotations;

/**
 * This enum is used in conjunction with the @IDMProperty annotation to mark an entity bean
 * property for the storage of Identity Management related state. 
 * 
 * @author Shane Bryzak
 */
public enum PropertyType {
    /**
     * The unique identifier of an identity 
     */
    IDENTITY_ID,
    /**
     * The discriminator identifies the particular identity type (such as user, role, group, etc) of the Identity
     */
    IDENTITY_DISCRIMINATOR,
    /**
     * 
     */
    IDENTITY_KEY,
    /**
     * The name of the identity.  For User identities, this will be the username, for Groups, the group name
     * and for Roles, the role name
     */
    IDENTITY_NAME,
    /**
     * Indicates whether the identity type is enabled
     */
    IDENTITY_ENABLED,
    /**
     * Creation date
     */
    IDENTITY_CREATION_DATE,
    /**
     * Expiry date
     */
    IDENTITY_EXPIRY_DATE,
    /**
     * Credential value
     */
    CREDENTIAL_VALUE,
    /**
     * Connects an attribute entity back to its owning identity
     */
    ATTRIBUTE_IDENTITY,
    /**
     * Attribute name
     */
    ATTRIBUTE_NAME,
    /**
     * Attribute type
     */
    ATTRIBUTE_TYPE,
    /**
     * Attribute value
     */
    ATTRIBUTE_VALUE,
    /**
     * The parent group of a group
     */
    GROUP_PARENT,
    /**
     * The login name used by an Agent (or User) to authenticate - this property should be unique
     */
    AGENT_LOGIN_NAME,
    /**
     * User's first name
     */
    USER_FIRST_NAME,
    /**
     * User's last name
     */
    USER_LAST_NAME,
    /**
     * User's e-mail address
     */
    USER_EMAIL,
    /**
     * The partition that an identity belongs to
     */
    IDENTITY_PARTITION,
    /**
     * The type of partition
     */
    PARTITION_TYPE,
    /**
     * The name of a partition
     */
    PARTITION_NAME,
    /**
     * The parent partition of a partition, where supported (such as for Tiers)
     */
    PARTITION_PARENT,
    /**
     * The identity that a credential belongs to
     */
    CREDENTIAL_IDENTITY,
    /**
     * Credential type
     */
    CREDENTIAL_TYPE,
    /**
     * The effective date of a credential
     */
    CREDENTIAL_EFFECTIVE_DATE,
    /**
     * The expiry date of a credential
     */
    CREDENTIAL_EXPIRY_DATE,
    /**
     * Connects a credential attribute value back to its owning credential
     */
    CREDENTIAL_ATTRIBUTE_CREDENTIAL,
    /**
     * The name of a credential attribute
     */
    CREDENTIAL_ATTRIBUTE_NAME,
    /**
     * The value of a credential attribute
     */
    CREDENTIAL_ATTRIBUTE_VALUE,
    /**
     * The unique identifier of a relationship
     */
    RELATIONSHIP_ID,
    /**
     * The fully qualified class name of the relationship type
     */
    RELATIONSHIP_CLASS,
    /**
     * Represents an identity that participates in a relationship
     */
    RELATIONSHIP_IDENTITY,
    /**
     * Relates a relationship identity back to its owning relationship
     */
    RELATIONSHIP_IDENTITY_RELATIONSHIP,
    /**
     * Describes the role of an identity within a relationship
     */
    RELATIONSHIP_DESCRIPTOR,
    /**
     * The name of a relationship attribute
     */
    RELATIONSHIP_ATTRIBUTE_NAME,
    /**
     * The value of a relationship attribute
     */
    RELATIONSHIP_ATTRIBUTE_VALUE,
    /**
     * Connects a relationship attribute back to its owning relationship
     */
    RELATIONSHIP_ATTRIBUTE_RELATIONSHIP
}
