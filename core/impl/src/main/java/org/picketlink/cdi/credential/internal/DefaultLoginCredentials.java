package org.picketlink.cdi.credential.internal;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;

import org.picketlink.cdi.authentication.event.LoginFailedEvent;
import org.picketlink.cdi.authentication.event.PostAuthenticateEvent;
import org.picketlink.cdi.credential.Credential;
import org.picketlink.cdi.credential.LoginCredentials;

/**
 * The default LoginCredentials implementation.  This implementation allows for a
 * username and plain text password to be set, and uses the PasswordCredential
 * implementation of the Credential interface for authentication.
 */
@Named("loginCredentials")
@RequestScoped
public class DefaultLoginCredentials implements LoginCredentials
{
    private Credential credential;

    private String userId;

    @Override
    public String getUserId()
    {
        return userId;
    }

    @Override
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public Credential getCredential()
    {
        return credential;
    }

    public void setCredential(Credential credential)
    {
        this.credential = credential;
        // TODO manager.fireEvent(new CredentialsUpdatedEvent(this.credential));
    }
    
    public String getPassword()
    {        
        return credential != null && credential.getValue() instanceof String ? (String) credential.getValue() : null;
    }

    /**
     * Convenience method that allows a plain text password credential to be set
     */
    public void setPassword(final String password)
    {
        this.credential = new Credential<String>() {
            @Override
            public String getValue()
            {
                return password;
            }
        };
    }

    public void invalidate()
    {
        credential = null;
        userId = null;
    }

    protected void setValid(@Observes PostAuthenticateEvent event)
    {
        invalidate();
    }

    protected void afterLogin(@Observes PostAuthenticateEvent event)
    {
        invalidate();
    }

    protected void loginFailed(@Observes LoginFailedEvent event)
    {
        invalidate();
    }

    @Override
    public String toString() 
    {
        return "LoginCredential[" + (userId != null ? userId : "unknown" ) + "]";
    }
}
