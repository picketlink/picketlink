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

package org.picketlink.authentication;


import org.picketlink.idm.model.Account;

/**
 * <p>An Authenticator implementation is responsible for managing the user authentication process. This is a key
 * concept of how authentication is performed by PicketLink.</p>
 *
 * <p>During the authentication (eg.: Identity.login()) the {@link org.picketlink.Identity} will try to select the
 * proper authenticator based on the following premises:</p>
 *
 * <ul>
 * <li>If any class that implements this interface and annotated with {@link org.picketlink.annotations
 * .PicketLink} is provided, it will be used.</li>
 * <li>If any producer method or field annotated with {@link org.picketlink.annotations.PicketLink} that
 * references a implementation of this interface, it will be selected.</li>
 * </ul>
 *
 * <p>Please, not that implementations must be annotated with {@link org.picketlink.annotations.PicketLink},
 * otherwise they will not be recognized and selected during the authentication process.</p>
 *
 * <p>If multiple implementations exists for the same application, only one should be annotated with
 * {@link org.picketlink.annotations.PicketLink}. If you want to use multiple authenticators and select them at
 * runtime based on a specific condition you can use a producer method annotated with this annotation.</p>
 *
 * <p>In order to get a successful authentication attempt, considering that the implementation has successfully
 * checked the provided credentials, implementations need to:</p>
 *
 * <ul>
 * <li>Return a {@link AuthenticationStatus.SUCCESS} status.</li>
 * <li>Return an {@link Account} that maps to the owner of the provided credentials.</li>
 * </ul>
 *
 * <p>The other status can be used in case of failure or if the authentication was deferred.</p>
 *
 * <p>It is recommended that implementations be requested scoped.</p>
 *
 * @author Shane Bryzak
 */
public interface Authenticator {

    public enum AuthenticationStatus {
        SUCCESS,
        FAILURE,
        DEFERRED
    }

    /**
     * <p>Performs the authentication.</p>
     */
    void authenticate();

    /**
     * <p>Post-authentication logic. This method is always invoked after an authentication attempt.</p>
     */
    void postAuthenticate();

    /**
     * <p>Returns the current status of the authentication attempt.</p>
     *
     * @return
     */
    AuthenticationStatus getStatus();

    /**
     * <p>Returns a {@link Account} if a successful authentication was made. Otherwise it should
     * return null.</p>
     *
     * @return
     */
    Account getAccount();
}
