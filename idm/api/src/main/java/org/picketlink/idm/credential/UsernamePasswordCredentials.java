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

package org.picketlink.idm.credential;



/**
 * Represents the credentials typically used by standard username/password authentication.
 *
 * @author Shane Bryzak
 */
public class UsernamePasswordCredentials extends AbstractBaseCredentials {

    private String username;

    private Password password;

    public UsernamePasswordCredentials() {

    }

    public UsernamePasswordCredentials(String userName, Password password) {
        this.username = userName;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public UsernamePasswordCredentials setUsername(String username) {
        this.username = username;
        return this;
    }

    public Password getPassword() {
        return password;
    }

    public UsernamePasswordCredentials setPassword(Password password) {
        this.password = password;
        return this;
    }

    @Override
    public void invalidate() {
        setStatus(Status.INVALID);
        password.clear();
    }
}
