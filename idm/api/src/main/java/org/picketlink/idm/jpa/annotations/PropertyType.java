package org.picketlink.idm.jpa.annotations;

/**
 * This enum is used in conjunction with the @IDMProperty annotation to mark an entity bean
 * property for the storage of Identity Management related state. 
 * 
 * @author Shane Bryzak
 */
public enum PropertyType {
    /**
     * The discriminator identifies the particular identity type (such as user, role, group, etc) of the Identity
     */
    DISCRIMINATOR,
    /**
     * 
     */
    KEY,
    /**
     * Indicates whether the identity type is enabled
     */
    ENABLED,
    /**
     * Creation date
     */
    CREATION_DATE,
    /**
     * Expiry date
     */
    EXPIRY_DATE,
    /**
     * 
     */
    ID,
    /**
     * 
     */
    NAME,
    /**
     * 
     */
    VALUE,
    /**
     * Credential value
     */
    CREDENTIAL,
    /**
     * Credential type
     */
    CREDENTIAL_TYPE,
    /**
     * 
     */
    ATTRIBUTE_TYPE,
    /**
     * 
     */
    PARENT_GROUP,
    /**
     * 
     */
    FIRST_NAME,
    /**
     * 
     */
    LAST_NAME,
    /**
     * 
     */
    EMAIL,
    /**
     * 
     */
    PARTITION,
    /**
     * 
     */
    PARTITION_TYPE,
    /**
     * 
     */
    PARENT_PARTITION,
    /**
     * 
     */
    CREDENTIAL_EFFECTIVE_DATE,
    /**
     * 
     */
    CREDENTIAL_EXPIRY_DATE,
    /**
     * 
     */
    IDENTITY_TYPE,
    /**
     * 
     */
    RELATIONSHIP_CLASS,
    /**
     * 
     */
    RELATIONSHIP_IDENTITY,
    /**
     * 
     */
    RELATIONSHIP_DESCRIPTOR,
    /**
     * 
     */
    RELATIONSHIP_ATTRIBUTE_NAME,
    /**
     * 
     */
    RELATIONSHIP_ATTRIBUTE_VALUE
}
