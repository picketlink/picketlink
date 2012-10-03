package org.picketlink.idm.jpa.annotations;

/**
 * This enum is used in conjunction with the @IDMProperty annotation to mark an entity bean
 * property for the storage of Identity Management related state. 
 * 
 * @author Shane Bryzak
 */
public enum PropertyType {
    KEY, ENABLED, CREATION_DATE, EXPIRY_DATE, ID, NAME, TYPE, VALUE, RELATIONSHIP_FROM, 
    RELATIONSHIP_TO, CREDENTIAL, CREDENTIAL_TYPE, ATTRIBUTE
}
