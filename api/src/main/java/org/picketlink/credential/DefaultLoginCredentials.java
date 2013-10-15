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
import javax.inject.Named;

import org.picketlink.idm.credential.AbstractBaseCredentials;
import org.picketlink.idm.credential.Password;

/**
 * The default Credentials implementation.  This implementation allows for a
 * userId and credential to be set
 */
@Named("loginCredentials")
@RequestScoped
public class DefaultLoginCredentials extends AbstractBaseCredentials {

    private Object credential;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id can not be null.");
        }

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
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null.");
        }

        this.credential = new Password(password.toCharArray());
    }

    @Override
    public void invalidate() {
        this.credential = null;
        this.userId = null;
    }

    @Override
    public String toString() {
        return "DefaultLoginCredentials[" + (userId != null ? userId : "unknown") + "]";
    }
}
