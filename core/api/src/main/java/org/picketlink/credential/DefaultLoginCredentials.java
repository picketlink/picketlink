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

package org.picketlink.credential;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;

import org.picketlink.authentication.event.LoginFailedEvent;
import org.picketlink.authentication.event.PostAuthenticateEvent;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Agent;

/**
 * The default Credentials implementation.  This implementation allows for a
 * userId and credential to be set
 */
@Named("loginCredentials")
@RequestScoped
public class DefaultLoginCredentials implements Credentials {
    private Object credential;

    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Object getCredential() {
        return credential;
    }

    public void setCredential(Object credential) {
        this.credential = credential;
    }

    public String getPassword() {
        if (credential != null && credential instanceof Password) {
            Password ptp = (Password) credential;
            return new String(ptp.getValue());
        }
        return null;
    }

    /**
     * Convenience method that allows a plain text password credential to be set
     */
    public void setPassword(final String password) {
        this.credential = new Password(password.toCharArray());
    }

    public void invalidate() {
        credential = null;
        userId = null;
    }

    protected void setValid(@Observes PostAuthenticateEvent event) {
        invalidate();
    }

    protected void afterLogin(@Observes PostAuthenticateEvent event) {
        invalidate();
    }

    protected void loginFailed(@Observes LoginFailedEvent event) {
        invalidate();
    }

    @Override
    public String toString() {
        return "LoginCredential[" + (userId != null ? userId : "unknown" ) + "]";
    }

    @Override
    public Agent getValidatedAgent() {
        return null;
    }

    @Override
    public Status getStatus() {
        return null;
    }
}
