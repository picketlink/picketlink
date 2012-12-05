package org.picketlink.credential.internal;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;

import org.picketlink.authentication.event.LoginFailedEvent;
import org.picketlink.authentication.event.PostAuthenticateEvent;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.credential.LoginCredentials;
import org.picketlink.idm.credential.PasswordCredential;

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

    public String getUserId()
    {
        return userId;
    }

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
        return credential != null && credential instanceof PasswordCredential ? 
                new String(((PasswordCredential) credential).getPassword()) : null;
    }

    /**
     * Convenience method that allows a plain text password credential to be set
     */
    public void setPassword(final String password)
    {
        this.credential = new PasswordCredential(password.toCharArray());
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
