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
 * Represents OAuth2 Client Credentials Grant Workflow
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class ClientCredentialsGrant extends AccessTokenEnabledGrant {

    private AccessTokenRequest accessTokenRequest;

    public AccessTokenRequest getAccessTokenRequest() {
        return accessTokenRequest;
    }

    public ClientCredentialsGrant setAccessTokenRequest(AccessTokenRequest accessTokenRequest) {
        this.accessTokenRequest = accessTokenRequest;
        return this;
    }

    @Override
    public void validate() {
        if (accessTokenRequest != null) {
            if (accessTokenRequest.getGrantType().equals("client_credentials") == false) {
                throw new RuntimeException("grant_type must be client_credentials");
            }
        }
    }

}