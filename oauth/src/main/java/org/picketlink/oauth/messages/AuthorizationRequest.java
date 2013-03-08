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

import org.picketlink.oauth.common.OAuthConstants;

/**
 * OAuth2 Authorization Request
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class AuthorizationRequest extends OAuthRequest {
    private static final long serialVersionUID = 2046706058742396970L;
    private String responseType, clientId, redirectUri, scope, state, location;

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

    public String getRedirectUri() {
        return redirectUri;
    }

    public AuthorizationRequest setRedirectUri(String redirectURI) {
        this.redirectUri = redirectURI;
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

    public String getLocation() {
        return location;
    }

    public AuthorizationRequest setLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public String asJSON() {
        return null;
    }

    @Override
    public String asQueryParams() {
        String AMP = "&";
        String EQ = "=";
        StringBuilder builder = new StringBuilder();

        // private String responseType, clientId, redirectURI, scope, state, location;
        if (responseType != null) {
            builder.append(OAuthConstants.RESPONSE_TYPE).append(EQ).append(encode(responseType)).append(AMP);
        }
        if (clientId != null) {
            builder.append(OAuthConstants.CLIENT_ID).append(EQ).append(encode(clientId)).append(AMP);
        }
        if (redirectUri != null) {
            builder.append(OAuthConstants.REDIRECT_URI).append(EQ).append(encode(redirectUri)).append(AMP);
        }
        if (scope != null) {
            builder.append(OAuthConstants.SCOPE).append(EQ).append(encode(scope)).append(AMP);
        }
        if (state != null) {
            builder.append(OAuthConstants.STATE).append(EQ).append(encode(state)).append(AMP);
        }

        return builder.toString();
    }
}