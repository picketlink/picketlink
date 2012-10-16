package org.picketlink.idm.jpa.annotations;

/**
 * This enum is used in conjunction with the @IDMProperty annotation to mark an entity bean
 * property for the storage of Identity Management related state. 
 * 
 * @author Shane Bryzak
 */
public enum PropertyType {
    DISCRIMINATOR, KEY, ENABLED, CREATION_DATE, EXPIRY_DATE, ID, NAME, VALUE, MEMBER,  
    GROUP, ROLE, CREDENTIAL, CREDENTIAL_TYPE, ATTRIBUTE_TYPE
}
