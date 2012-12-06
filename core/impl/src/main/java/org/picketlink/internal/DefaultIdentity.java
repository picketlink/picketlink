package org.picketlink.internal;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.util.ExceptionUtils;

import org.picketlink.Identity;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.AuthenticatorSelector;
import org.picketlink.authentication.UnexpectedCredentialException;
import org.picketlink.authentication.Authenticator.AuthenticationStatus;
import org.picketlink.authentication.event.AlreadyLoggedInEvent;
import org.picketlink.authentication.event.LoggedInEvent;
import org.picketlink.authentication.event.LoginFailedEvent;
import org.picketlink.authentication.event.PostAuthenticateEvent;
import org.picketlink.authentication.event.PostLoggedOutEvent;
import org.picketlink.authentication.event.PreAuthenticateEvent;
import org.picketlink.authentication.event.PreLoggedOutEvent;
import org.picketlink.credential.internal.DefaultLoginCredentials;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.User;

/**
 * Default Identity implementation
 */
@SuppressWarnings("UnusedDeclaration")
@SessionScoped
@Named("identity")
public class DefaultIdentity implements Identity
{
    private static final long serialVersionUID = 3696702275353144429L;

    @Inject
    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private AuthenticatorSelector authenticatorSelector;

    @Inject
    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private BeanManager beanManager;

    @Inject
    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private DefaultLoginCredentials loginCredential;

    /**
     * Flag indicating whether we are currently authenticating
     */
    private boolean authenticating;

    private User user;

    public boolean isLoggedIn() 
    {
        // If there is a user set, then the user is logged in.
        return this.user != null;
    }

    @Override
    public User getUser()
    {
        return this.user;
    }

    @Override
    public AuthenticationResult login()
    {
        try 
        {
            if (isLoggedIn())
            {
                if (isAuthenticationRequestWithDifferentUserId())
                {
                    throw new UnexpectedCredentialException("active user: " + this.user.getId() +
                            " provided credentials: " + this.loginCredential.getUserId());
                }

                beanManager.fireEvent(new AlreadyLoggedInEvent());
                return AuthenticationResult.SUCCESS;
            }

            boolean success = authenticate();

            if (success) 
            {
                beanManager.fireEvent(new LoggedInEvent()); 
                return AuthenticationResult.SUCCESS;
            }

            beanManager.fireEvent(new LoginFailedEvent(null));
            return AuthenticationResult.FAILED;
        } 
        catch (Throwable e) 
        {
            //X TODO discuss special handling of UnexpectedCredentialException
            beanManager.fireEvent(new LoginFailedEvent(e));

            if (e instanceof RuntimeException)
            {
                throw (RuntimeException)e;
            }

            ExceptionUtils.throwAsRuntimeException(e);
            //Attention: the following line is just for the compiler (and analysis tools) - it won't get executed
            throw new IllegalStateException(e);
        }
    }

    private boolean isAuthenticationRequestWithDifferentUserId()
    {
        return isLoggedIn() && this.loginCredential.getUserId() != null &&
                !this.loginCredential.getUserId().equals(this.user.getId());
    }

    protected boolean authenticate() throws AuthenticationException 
    {
        if (authenticating) 
        {
            authenticating = false; //X TODO discuss it
            throw new IllegalStateException("Authentication already in progress.");
        }

        try 
        {
            authenticating = true;

            beanManager.fireEvent(new PreAuthenticateEvent());

            Authenticator activeAuthenticator = authenticatorSelector.getSelectedAuthenticator();

            if (activeAuthenticator == null)
            {
                throw new AuthenticationException("No Authenticator has been configured.");
            }
            
            activeAuthenticator.authenticate();

            if (activeAuthenticator.getStatus() == null) 
            {
                throw new AuthenticationException("Authenticator must return a valid authentication status");
            }

            if (activeAuthenticator.getStatus() == AuthenticationStatus.SUCCESS)
            {
                postAuthenticate(activeAuthenticator);
                this.user = activeAuthenticator.getUser();
                return true;
            }
        } 
        catch (Throwable ex) 
        {
            if (ex instanceof AuthenticationException)
            {
                throw (AuthenticationException) ex;
            } 
            else 
            {
                throw new AuthenticationException("Authentication failed.", ex);
            }
        }
        finally
        {
            authenticating = false;
        }
        return false;
    }
    
    protected void postAuthenticate(Authenticator activeAuthenticator)
    {
        activeAuthenticator.postAuthenticate();

        if (!activeAuthenticator.getStatus().equals(AuthenticationStatus.SUCCESS))
        {
            return;
        }

        beanManager.fireEvent(new PostAuthenticateEvent());
    }

    @Override
    public void logout() 
    {
        logout(true);
    }

    protected void logout(boolean invalidateLoginCredential)
    {
        if (isLoggedIn())
        {
            beanManager.fireEvent(new PreLoggedOutEvent(this.user));

            PostLoggedOutEvent postLoggedOutEvent = new PostLoggedOutEvent(this.user);

            unAuthenticate(invalidateLoginCredential);

            beanManager.fireEvent(postLoggedOutEvent);
        }
    }

    /**
     * Resets all security state and loginCredential
     */
    private void unAuthenticate(boolean invalidateLoginCredential)
    {
        this.user = null;

        if (invalidateLoginCredential)
        {
            loginCredential.invalidate();
        }
    }
    
    public boolean hasPermission(Object resource, String operation)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasPermission(Class<?> resourceClass, Serializable identifier, String operation)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
