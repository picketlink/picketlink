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

import org.picketlink.oauth.messages.AuthorizationRequest;

/**
 * Represents OAuth2 Implicit Grant Workflow
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class ImplicitGrant extends AccessTokenEnabledGrant {
    private AuthorizationRequest authorizationRequest;

    public AuthorizationRequest getAuthorizationRequest() {
        return authorizationRequest;
    }

    public ImplicitGrant setAuthorizationRequest(AuthorizationRequest authorizationRequest) {
        this.authorizationRequest = authorizationRequest;
        this.state = authorizationRequest.getState();
        return this;
    }

    @Override
    public void validate() {
        if (authorizationRequest != null) {
            if (authorizationRequest.getResponseType() == null) {
                throw new RuntimeException("response_type must not be null");
            }
            if (authorizationRequest.getResponseType().equals("token") == false) {
                throw new RuntimeException("response_type must be token");
            }
            if (authorizationRequest.getClientId() == null) {
                throw new RuntimeException("client_id must not be null");
            }
        }
    }
}