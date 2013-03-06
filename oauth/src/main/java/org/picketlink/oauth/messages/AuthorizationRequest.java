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
package org.picketlink.oauth.messages;

/**
 * OAuth2 Authorization Request
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class AuthorizationRequest {
    private String responseType, clientId, redirectURI, scope, state;

    public String getResponseType() {
        return responseType;
    }

    public AuthorizationRequest setResponseType(String responseType) {
        this.responseType = responseType;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public AuthorizationRequest setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public AuthorizationRequest setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public AuthorizationRequest setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getState() {
        return state;
    }

    public AuthorizationRequest setState(String state) {
        this.state = state;
        return this;
    }
}