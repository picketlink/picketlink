package org.picketlink.idm.credential;


/**
 * Represents the credentials the current user will use to authenticate
 * 
 * Only valid during the authentication process
 * 
 * @author Shane Bryzak
 */
public interface LoginCredentials {
    void invalidate();
}
