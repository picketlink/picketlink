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
package org.picketlink.oauth.grants;

import org.picketlink.oauth.messages.AccessTokenRequest;

/**
 * OAuth2 Resource Owner Password Credentials Grant Workflow
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class ResourceOwnerPasswordCredentialsGrant extends OAuthGrant {

    public static final String GRANT_TYPE = "password";

    private PasswordAccessTokenRequest request;

    public PasswordAccessTokenRequest getRequest() {
        return request;
    }

    public ResourceOwnerPasswordCredentialsGrant setAccessTokenRequest(PasswordAccessTokenRequest request) {
        this.request = request;
        return this;
    }

    @Override
    public void validate() {
        if (request != null) {
            if (request.getUsername() == null) {
                throw new RuntimeException("Username cannot be null");
            }
            if (request.getPassword() == null) {
                throw new RuntimeException("Password cannot be null");
            }
            if (request.getGrantType() == null) {
                throw new RuntimeException("grant_type must not be null");
            }
            if (request.getGrantType().equals("password") == false) {
                throw new RuntimeException("grant_type must be password");
            }
        }
    }

    /**
     * A special {@link AccessTokenRequest} that is used for passing password
     *
     * @author anil saldhana
     */
    public class PasswordAccessTokenRequest extends AccessTokenRequest {
        private static final long serialVersionUID = -5439167033982907992L;
        private String username, password;

        public String getUsername() {
            return username;
        }

        public PasswordAccessTokenRequest setUsername(String username) {
            this.username = username;
            return this;
        }

        public String getPassword() {
            return password;
        }

        public PasswordAccessTokenRequest setPassword(String password) {
            this.password = password;
            return this;
        }
    }
}