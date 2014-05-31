/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.internal;

import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.authentication.Authenticator;
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
import org.picketlink.authentication.internal.IdmAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.permission.spi.PermissionResolver;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * <p>Base implementation for {@link org.picketlink.Identity} types.</p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 */
@Named("identity")
public abstract class AbstractIdentity implements Identity {

    private static final long serialVersionUID = 8655816330461907668L;

    @Inject
    private BeanManager beanManager;

    @Inject
    private DefaultLoginCredentials loginCredential;

    @Inject
    @PicketLink
    private Instance<Authenticator> authenticatorInstance;

    @Inject
    private Instance<IdmAuthenticator> idmAuthenticatorInstance;

    @Inject
    private transient PermissionResolver permissionResolver;

    /**
     * Flag indicating whether we are currently authenticating
     */
    private boolean authenticating;

    private Account account;

    public boolean isLoggedIn() {
        // If there is an account set, then the account is logged in.
        return this.account != null;
    }

    @Override
    public Account getAccount() {
        return this.account;
    }

    @Override
    public AuthenticationResult login() {
        try {
            if (isLoggedIn()) {
                throw new UserAlreadyLoggedInException("active agent: " + this.account.toString());
            }

            Account validatedAccount = authenticate();

            if (validatedAccount != null) {
                if (!validatedAccount.isEnabled()) {
                    throw new LockedAccountException("Account [" + validatedAccount + "] is disabled.");
                }

                handleSuccessfulLoginAttempt(validatedAccount);
                return AuthenticationResult.SUCCESS;
            }

            handleUnsuccesfulLoginAttempt(null);
            return AuthenticationResult.FAILED;
        } catch (Throwable e) {
            handleUnsuccesfulLoginAttempt(e);

            if (AuthenticationException.class.isInstance(e)) {
                throw (AuthenticationException) e;
            }

            throw new AuthenticationException("Login failed with a unexpected error.", e);
        }
    }

    protected void handleSuccessfulLoginAttempt(Account validatedAccount) {
        this.account = validatedAccount;
        beanManager.fireEvent(new LoggedInEvent());
    }

    protected void handleUnsuccesfulLoginAttempt(Throwable e) {
        if (e != null) {
            if (UnexpectedCredentialException.class.isInstance(e)) {
                //X TODO discuss special handling of UnexpectedCredentialException
            } else if (UserAlreadyLoggedInException.class.isInstance(e)) {
                beanManager.fireEvent(new AlreadyLoggedInEvent());
            } else if (LockedAccountException.class.isInstance(e)) {
                beanManager.fireEvent(new LockedAccountEvent());
            }
        }

        beanManager.fireEvent(new LoginFailedEvent(e));
    }

    protected Account authenticate() throws AuthenticationException {
        Account validatedAccount = null;

        if (authenticating) {
            authenticating = false; //X TODO discuss it
            throw new IllegalStateException("Authentication already in progress.");
        }

        try {
            authenticating = true;

            beanManager.fireEvent(new PreAuthenticateEvent());

            Authenticator authenticator = authenticatorInstance.isUnsatisfied() ?
                idmAuthenticatorInstance.get() :
                authenticatorInstance.get();

            if (authenticator == null) {
                throw new AuthenticationException("No Authenticator has been configured.");
            }

            authenticator.authenticate();

            if (authenticator.getStatus() == null) {
                throw new AuthenticationException("Authenticator must return a valid authentication status");
            }

            if (authenticator.getStatus() == Authenticator.AuthenticationStatus.SUCCESS) {
                validatedAccount = authenticator.getAccount();
                postAuthenticate(authenticator);
            }
        } catch (AuthenticationException e) {
            throw (AuthenticationException) e;
        } catch (Throwable ex) {
            throw new AuthenticationException("Authentication failed.", ex);
        } finally {
            authenticating = false;
        }

        return validatedAccount;
    }

    protected void postAuthenticate(Authenticator authenticator) {
        authenticator.postAuthenticate();

        if (!authenticator.getStatus().equals(Authenticator.AuthenticationStatus.SUCCESS)) {
            return;
        }

        beanManager.fireEvent(new PostAuthenticateEvent());
    }

    @Override
    public void logout() {
        logout(true);
    }

    protected void logout(boolean invalidateLoginCredential) {
        if (isLoggedIn()) {
            beanManager.fireEvent(new PreLoggedOutEvent(this.account));

            PostLoggedOutEvent postLoggedOutEvent = new PostLoggedOutEvent(this.account);

            unAuthenticate(invalidateLoginCredential);

            beanManager.fireEvent(postLoggedOutEvent);
        }
    }

    /**
     * Resets all security state and loginCredential
     */
    private void unAuthenticate(boolean invalidateLoginCredential) {
        this.account = null;

        if (invalidateLoginCredential) {
            loginCredential.invalidate();
        }
    }

    public boolean hasPermission(Object resource, String operation) {
        return permissionResolver.resolvePermission(account, resource, operation);
    }

    public boolean hasPermission(Class<?> resourceClass, Serializable identifier, String operation) {
        return permissionResolver.resolvePermission(account, resourceClass, identifier, operation);
    }

}
