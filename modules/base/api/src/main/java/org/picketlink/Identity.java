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
package org.picketlink;

import org.picketlink.authentication.AuthenticationException;
import org.picketlink.idm.model.Account;

import javax.inject.Qualifier;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Represents the identity of the current user, and provides an API for authentication and authorization.
 *
 * @author Shane Bryzak
 */
public interface Identity extends Serializable {

    public enum AuthenticationResult {
        SUCCESS, FAILED
    }

    /**
     * <p>Defines a {@link javax.inject.Qualifier} that may be applied to an injection point to indicate that a stateless {@link org.picketlink.Identity}
     * is expected. In this case, the authentication state for an user is not shared between different requests.</p>
     *
     * <p>This qualifier should be used by {@link org.picketlink.Identity} implementations to provide a stateless behavior to the authentication process.
     * So any authentication data will be lost once the request processing is finished.</p>
     *
     * @author Pedro Igor
     */
    @Qualifier
    @Retention(RUNTIME)
    @Target({TYPE, FIELD})
    public @interface Stateless {

    }

    /**
     * Simple check that returns true if the user is logged in, without attempting to authenticate
     *
     * @return true if the user is logged in
     */
    boolean isLoggedIn();

    Account getAccount();

    /**
     * Attempts to authenticate the user.  This method raises the following events in response
     * to whether authentication is successful or not.  The following events may be raised
     * during the call to login():
     * <p/>
     * {@link org.picketlink.authentication.event.LoggedInEvent}
     * - raised when authentication is successful
     * {@link org.picketlink.authentication.event.LoginFailedEvent}
     * - raised when authentication fails
     * {@link org.picketlink.authentication.event.AlreadyLoggedInEvent}
     * - raised if the user is already authenticated
     * {@link org.picketlink.authentication.event.LockedAccountEvent}
     * - raised if the user is locked
     *
     * @return AuthenticationResult returns SUCCESS if user is authenticated,
     *         FAILED if authentication FAILED, or
     *         EXCEPTION if an EXCEPTION occurred during authentication. These response
     *         values may be used to control user navigation.  For deferred authentication methods, such as Open ID
     *         the login() method will return an immediate result of FAILED (and subsequently fire
     *         a LoginFailedEvent) however in these conditions it is the responsibility of the Authenticator
     *         implementation to take over the authentication process, for example by redirecting the user to
     *         a third party authentication service such as an OpenID provider.
     *
     * @throws AuthenticationException if some unexpected error occurs or a subclass which identifies why the login
     * failed.
     */
    AuthenticationResult login() throws AuthenticationException;

    /**
     * Logs out the currently authenticated user
     */
    void logout();

    /**
     * Tests if the currently authenticated user has permission to perform the specified operation on
     * the specified resource.  This method should be preferred over the overloaded hasPermission() method
     * if a reference to the resource in question is already available.
     *
     * @param resource The resource for which the permission is required
     * @param operation The operation that the user wishes to perform on the resource
     *
     * @return true if the current user has the permission.
     */
    boolean hasPermission(Object resource, String operation);

    /**
     * As above, however this method should be used when a reference to the resource is not available, or
     * is expensive to retrieve, for example looking up an entity from a relational database.
     *
     * @param resourceClass The class of the resource
     * @param identifier The identifier of the resource, for example may be a primary key value if an entity
     * @param operation The operation that the user wishes to perform on the resource
     *
     * @return true if the current user has the permission.
     */
    boolean hasPermission(Class<?> resourceClass, Serializable identifier, String operation);
}
