/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.internal;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.picketlink.Identity;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.Authenticator.AuthenticationStatus;
import org.picketlink.authentication.AuthenticatorSelector;
import org.picketlink.authentication.LockedAccountException;
import org.picketlink.authentication.UnexpectedCredentialException;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.authentication.event.AlreadyLoggedInEvent;
import org.picketlink.authentication.event.LockedAccountEvent;
import org.picketlink.authentication.event.LoggedInEvent;
import org.picketlink.authentication.event.LoginFailedEvent;
import org.picketlink.authentication.event.PostAuthenticateEvent;
import org.picketlink.authentication.event.PostLoggedOutEvent;
import org.picketlink.authentication.event.PreAuthenticateEvent;
import org.picketlink.authentication.event.PreLoggedOutEvent;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.Agent;

/**
 * Default Identity implementation
 */
@SessionScoped
@Named("identity")
public class DefaultIdentity implements Identity
{
    private static final long serialVersionUID = 3696702275353144429L;

    @Inject
    private AuthenticatorSelector authenticatorSelector;

    @Inject
    private BeanManager beanManager;

    @Inject
    private DefaultLoginCredentials loginCredential;

    /**
     * Flag indicating whether we are currently authenticating
     */
    private boolean authenticating;

    private Agent agent;

    public boolean isLoggedIn() 
    {
        // If there is a agent set, then the agent is logged in.
        return this.agent != null;
    }

    @Override
    public Agent getAgent()
    {
        return this.agent;
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
                    throw new UnexpectedCredentialException("active agent: " + this.agent.getLoginName() +
                            " provided credentials: [" + this.loginCredential.getUserId() + "]");
                }
                
                throw new UserAlreadyLoggedInException("active agent: " + this.agent.getLoginName());
            }

            Agent validatedAgent = authenticate();

            if (validatedAgent != null) 
            {
                if (!validatedAgent.isEnabled()) {
                    throw new LockedAccountException(validatedAgent);
                }

                handleSuccessfulLoginAttempt(validatedAgent); 
                return AuthenticationResult.SUCCESS;
            }

            handleUnsuccesfulLoginAttempt(null);
            return AuthenticationResult.FAILED;
        } 
        catch (Throwable e) 
        {
            handleUnsuccesfulLoginAttempt(e);
            
            if (AuthenticationException.class.isInstance(e)) {
                throw (AuthenticationException) e;
            }
            
            throw new AuthenticationException("Login failed with a unexpected error.", e);            
//            ExceptionUtils.throwAsRuntimeException(e);
//            //Attention: the following line is just for the compiler (and analysis tools) - it won't get executed
//            throw new IllegalStateException(e);
        }
    }

    protected void handleSuccessfulLoginAttempt(Agent validatedAgent) {
        this.agent = validatedAgent;
        beanManager.fireEvent(new LoggedInEvent());
    }

    protected void handleUnsuccesfulLoginAttempt(Throwable e) {
        if (e != null) {
            if (UnexpectedCredentialException.class.isInstance(e)) {
              //X TODO discuss special handling of UnexpectedCredentialException                
            } else if (UserAlreadyLoggedInException.class.isInstance(e)) {
                beanManager.fireEvent(new AlreadyLoggedInEvent());
            } else if (LockedAccountException.class.isInstance(e)) {
                LockedAccountException lockedException = (LockedAccountException) e;
                beanManager.fireEvent(new LockedAccountEvent(lockedException.getLockedAccount()));
            }
        }
        
        beanManager.fireEvent(new LoginFailedEvent(e));
    }

    private boolean isAuthenticationRequestWithDifferentUserId()
    {
        return isLoggedIn() && this.loginCredential.getUserId() != null &&
                !this.loginCredential.getUserId().equals(this.agent.getLoginName());
    }

    protected Agent authenticate() throws AuthenticationException 
    {
        Agent validatedAgent = null;
        
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
                validatedAgent = activeAuthenticator.getUser();
                postAuthenticate(activeAuthenticator);
            } 
        } 
        catch (AuthenticationException e) {
            throw (AuthenticationException) e;
        } catch (Throwable ex) 
        {
            throw new AuthenticationException("Authentication failed.", ex);
        }
        finally
        {
            authenticating = false;
        }
        
        return validatedAgent;
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
            beanManager.fireEvent(new PreLoggedOutEvent(this.agent));

            PostLoggedOutEvent postLoggedOutEvent = new PostLoggedOutEvent(this.agent);

            unAuthenticate(invalidateLoginCredential);

            beanManager.fireEvent(postLoggedOutEvent);
        }
    }

    /**
     * Resets all security state and loginCredential
     */
    private void unAuthenticate(boolean invalidateLoginCredential)
    {
        this.agent = null;

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
