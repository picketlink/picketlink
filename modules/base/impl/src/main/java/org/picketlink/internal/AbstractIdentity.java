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
import org.picketlink.authentication.levels.DifferentUserLoggedInExcpetion;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelManager;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.permission.spi.PermissionResolver;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

import static org.picketlink.log.BaseLog.AUTHENTICATION_LOGGER;

/**
 * <p>Base implementation for {@link org.picketlink.Identity} types.</p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 */
public abstract class AbstractIdentity implements Identity {

    private static final long serialVersionUID = 8655816330461907668L;

    @Inject
    private CDIEventBridge eventBridge;

    @Inject
    private DefaultLoginCredentials loginCredential;

    @Inject
    @PicketLink
    private Instance<Authenticator> authenticatorInstance;

    @Inject
    private Instance<IdmAuthenticator> idmAuthenticatorInstance;

    @Inject
    private Instance<PermissionResolver> permissionResolver;

    @Inject
    private Instance<SecurityLevelManager> securityLevelManager;

    /**
     * Flag indicating whether we are currently authenticating
     */
    private boolean authenticating;

    private Account account;

    private Level securityLevel;

    public boolean isLoggedIn() {
        // If there is an account set, then the account is logged in.
        return this.account != null;
    }

    @Override
    public Account getAccount() {
        return this.account;
    }

    @Override
    public Level getLevel() {
        if(securityLevel == null){
            securityLevel = getSecurityLevelManager().resolveSecurityLevel();
        }
        return securityLevel;
    }

    @Override
    public AuthenticationResult login() {
        try {
            if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
                AUTHENTICATION_LOGGER.debugf("Performing authentication using credentials [%s]. User id is [%s].", this.loginCredential
                    .getCredential(), this.loginCredential.getUserId());
            }

            Account validatedAccount = null;

            if (isLoggedIn()) {
                if (getSecurityLevelManager().resolveSecurityLevel().compareTo(securityLevel) <= 0) {
                    throw new UserAlreadyLoggedInException("active agent: " + this.account.toString());
                } else {
                    validatedAccount = authenticate();
                    if(validatedAccount != null){
                        //obtain username of the current user and the user which is trying to rise level
                        Property firstdeclaredField = getDefaultLoginNameProperty(validatedAccount.getClass());
                        Object firstUsername = firstdeclaredField.getValue(validatedAccount);
                        Property seconddeclaredField = getDefaultLoginNameProperty(this.account.getClass());
                        Object secondUsername = seconddeclaredField.getValue(this.account);
                        //if the second user does not have username or they does not match, then exception is thrown
                        if(secondUsername == null || !secondUsername.equals(firstUsername)){
                            throw new DifferentUserLoggedInExcpetion("active agent: "+ this.account.toString() +
                                " but agent: "+ validatedAccount.toString() + " is trying to log in");
                        }
                    }
                }
            }
            else{
                validatedAccount = authenticate();
            }

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
        } finally {
            if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
                AUTHENTICATION_LOGGER
                    .debugf("Authentication is finished using credentials [%s]. User id is [%s].", this.loginCredential
                        .getCredential(), this.loginCredential.getUserId());
            }
        }
    }

    protected void handleSuccessfulLoginAttempt(Account validatedAccount) {
        AUTHENTICATION_LOGGER.debugf("Authentication was successful for credentials [%s]. User id is [%s].", this.loginCredential.getCredential(), this.loginCredential.getUserId());
        this.account = validatedAccount;
        securityLevel = getSecurityLevelManager().resolveSecurityLevel();
        eventBridge.fireEvent(new LoggedInEvent());
    }

    protected void handleUnsuccesfulLoginAttempt(Throwable e) {
        if (e != null) {
            if (UnexpectedCredentialException.class.isInstance(e)) {
                //X TODO discuss special handling of UnexpectedCredentialException
            } else if (UserAlreadyLoggedInException.class.isInstance(e)) {
                eventBridge.fireEvent(new AlreadyLoggedInEvent());
            } else if (LockedAccountException.class.isInstance(e)) {
                eventBridge.fireEvent(new LockedAccountEvent());
            }
        }

        if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
            AUTHENTICATION_LOGGER.debugf("Authentication failed for credentials [%s]. User id is [%s].", this.loginCredential
                .getCredential(), this.loginCredential.getUserId(), e);
        }

        eventBridge.fireEvent(new LoginFailedEvent(e));
    }

    protected Account authenticate() throws AuthenticationException {
        Account validatedAccount = null;

        if (authenticating) {
            authenticating = false; //X TODO discuss it
            throw new IllegalStateException("Authentication already in progress.");
        }

        try {
            authenticating = true;

            eventBridge.fireEvent(new PreAuthenticateEvent());

            Authenticator authenticator = getAuthenticator();

            if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
                AUTHENTICATION_LOGGER.debugf("Authentication is going to be performed by authenticator [%s]", authenticator);
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
            throw e;
        } catch (Throwable ex) {
            throw new AuthenticationException("Authentication failed.", ex);
        } finally {
            authenticating = false;
        }

        return validatedAccount;
    }

    private Authenticator getAuthenticator() throws AuthenticationException {
        Authenticator authenticator = authenticatorInstance.isUnsatisfied() ? idmAuthenticatorInstance.get() : authenticatorInstance.get();

        if (authenticator == null) {
            throw new AuthenticationException("No Authenticator has been configured.");
        }
        return authenticator;
    }

    protected void postAuthenticate(Authenticator authenticator) {
        authenticator.postAuthenticate();

        if (!authenticator.getStatus().equals(Authenticator.AuthenticationStatus.SUCCESS)) {
            return;
        }

        eventBridge.fireEvent(new PostAuthenticateEvent());
    }

    @Override
    public void logout() {
        logout(true);
    }

    protected void logout(boolean invalidateLoginCredential) {
        if (isLoggedIn()) {
            eventBridge.fireEvent(new PreLoggedOutEvent(this.account));

            PostLoggedOutEvent postLoggedOutEvent = new PostLoggedOutEvent(this.account);

            unAuthenticate(invalidateLoginCredential);

            eventBridge.fireEvent(postLoggedOutEvent);
        }
    }

    /**
     * Resets all security state and loginCredential
     */
    private void unAuthenticate(boolean invalidateLoginCredential) {
        this.account = null;

        this.securityLevel = getSecurityLevelManager().resolveSecurityLevel();

        if (invalidateLoginCredential) {
            loginCredential.invalidate();
        }
    }

    public boolean hasPermission(Object resource, String operation) {
        return isLoggedIn() && getPermissionResolver().resolvePermission(this.account, resource, operation);
    }

    public boolean hasPermission(Class<?> resourceClass, Serializable identifier, String operation) {
        return isLoggedIn() && getPermissionResolver().resolvePermission(this.account, resourceClass, identifier, operation);
    }

    protected Property getDefaultLoginNameProperty(Class<? extends Account> accountType) {
        List<Property<Object>> properties = PropertyQueries
            .createQuery(accountType)
            .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class)).getResultList();

        for (Property property : properties) {
            StereotypeProperty stereotypeProperty = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);

            if (StereotypeProperty.Property.IDENTITY_USER_NAME.equals(stereotypeProperty.value())) {
                return property;
            }
        }

        throw IDMMessages.MESSAGES.credentialUnknownUserNameProperty(accountType);
    }

    private SecurityLevelManager getSecurityLevelManager() {
        return securityLevelManager.get();
    }

    private PermissionResolver getPermissionResolver() {
        return permissionResolver.get();
    }
}