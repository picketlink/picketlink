package org.picketlink.idm.config;

import java.util.Set;

import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStore.Feature;

/**
 * This interface defines the configuration parameters for a JPA based IdentityStore implementation.
 * 
 * @author Shane Bryzak
 *
 */
public class JPAIdentityStoreConfiguration extends IdentityStoreConfiguration {

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

    @Override
    public Set<Feature> getSupportedFeatures() {
        // TODO Auto-generated method stub
        return null;
    }
}
