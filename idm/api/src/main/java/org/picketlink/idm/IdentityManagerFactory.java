package org.picketlink.idm;

/**
 * Creates IdentityManager instances.
 * 
 * @author Shane Bryzak
 *
 */
public interface IdentityManagerFactory {
    IdentityManager createIdentityManager();
    IdentityManager createIdentityManager(String realm);
    IdentityManager createIdentityManager(String realm, String tier);

}
