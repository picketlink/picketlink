package org.jboss.picketlink.cdi.authentication;

import org.jboss.picketlink.idm.model.User;

/**
 * Abstract base class that Authenticator implementations can extend for convenience. 
 * 
 * @author Shane Bryzak
 *
 */
public abstract class BaseAuthenticator implements Authenticator
{
    private AuthenticationStatus status = AuthenticationStatus.FAILURE;
    private User user;
    
    public AuthenticationStatus getStatus()
    {
        return status;
    }

    protected void setStatus(AuthenticationStatus status)
    {
        this.status = status;
    }
    
    protected void setUser(User user)
    {
        this.user = user;
    }
    
    public User getUser()
    {
        return user;
    }

    public void postAuthenticate()
    {
        // No-op, override if any post-authentication processing is required.
    }
}
