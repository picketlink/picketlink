package org.picketlink.idm.jpa.internal;

import org.picketlink.idm.spi.JPAIdentityStoreConfiguration;

/**
 * 
 * @author Shane Bryzak
 *
 */
public class DefaultJPAIdentityStoreConfiguration implements JPAIdentityStoreConfiguration {
    
    private Class<?> identityClass;
    private Class<?> membershipClass;
    private Class<?> credentialClass;    
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

    public Class<?> getMembershipClass() {
        return membershipClass;
    }

    public void setMembershipClass(Class<?> membershipClass) {
        this.membershipClass = membershipClass;
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
