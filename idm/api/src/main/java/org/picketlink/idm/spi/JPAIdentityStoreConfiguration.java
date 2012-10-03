package org.picketlink.idm.spi;

/**
 * This interface defines the configuration parameters for a JPA based IdentityStore implementation.
 * 
 * @author Shane Bryzak
 *
 */
public interface JPAIdentityStoreConfiguration {
    Class<?> getIdentityClass();
    void setIdentityClass(Class<?> identityClass);

    Class<?> getCredentialClass();
    void setCredentialClass(Class<?> credentialClass);

    Class<?> getRelationshipClass();
    void setRelationshipClass(Class<?> relationshipClass);

    Class<?> getRoleTypeClass();
    void setRoleTypeClass(Class<?> roleTypeClass);
    
    Class<?> getAttributeClass();
    void setAttributeClass(Class<?> attributeClass);

    boolean isConfigured();
}
