package org.picketlink.authentication;

import org.picketlink.idm.model.User;


/**
 * An Authenticator implementation is responsible for managing the user authentication process. 
 * 
 * @author Shane Bryzak
 */
public interface Authenticator
{
    public enum AuthenticationStatus 
    {
        SUCCESS, 
        FAILURE, 
        DEFERRED
    }

    void authenticate();

    void postAuthenticate();

    AuthenticationStatus getStatus();

    User getUser();
}
