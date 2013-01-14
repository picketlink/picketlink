package org.picketlink.idm.jpa.annotations;

/**
 * This enum defines the valid entity types that may be used to store Identity Management state 
 *  
 * @author Shane Bryzak
 */
public enum EntityType {
    /**
     * 
     */
    IDENTITY_TYPE,
    /**
     * 
     */
    IDENTITY_CREDENTIAL,
    /**
     * 
     */
    IDENTITY_ATTRIBUTE,
    /**
     * 
     */
    IDENTITY_ROLE_NAME, 
    /**
     * 
     */
    RELATIONSHIP,
    /**
     * 
     */
    RELATIONSHIP_IDENTITY,
    /**
     * 
     */
    RELATIONSHIP_ATTRIBUTE
}
