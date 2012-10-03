package org.picketlink.idm.jpa.internal;

import org.picketlink.idm.spi.JPAIdentityStoreConfiguration;

/**
 * 
 * @author Shane Bryzak
 *
 */
public class DefaultJPAIdentityStoreConfiguration implements JPAIdentityStoreConfiguration {
    
    private Class<?> identityClass;
    private Class<?> credentialClass;
    private Class<?> relationshipClass;
    private Class<?> roleTypeClass;
    private Class<?> attributeClass;    

    public Class<?> getIdentityClass() {
        return identityClass;
    }

    public void setIdentityClass(Class<?> identityClass) {
        this.identityClass = identityClass;
    }

    public Class<?> getCredentialClass() {
        return credentialClass;
    }

    public void setCredentialClass(Class<?> credentialClass) {
        this.credentialClass = credentialClass;
    }

    public Class<?> getRelationshipClass() {
        return relationshipClass;
    }

    public void setRelationshipClass(Class<?> relationshipClass) {
        this.relationshipClass = relationshipClass;
    }

    public Class<?> getRoleTypeClass() {
        return roleTypeClass;
    }

    public void setRoleTypeClass(Class<?> roleTypeClass) {
        this.roleTypeClass = roleTypeClass;
    }

    public Class<?> getAttributeClass() {
        return attributeClass;
    }

    public void setAttributeClass(Class<?> attributeClass) {
        this.attributeClass = attributeClass;
    }

    public boolean isConfigured() {
        return identityClass != null;
    }
}
