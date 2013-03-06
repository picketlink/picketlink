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
import org.picketlink.oauth.messages.AccessTokenResponse;
import org.picketlink.oauth.messages.AuthorizationRequest;
import org.picketlink.oauth.messages.AuthorizationResponse;

/**
 * Represents the OAuth2 Authorization Code Grant Workflow
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class AuthorizationCodeGrant extends AccessTokenEnabledGrant {

    public static final String GRANT_TYPE = "authorization_code";

    private String state, authorizationCode;

    private AuthorizationRequest authorizationRequest;

    private AccessTokenRequest accessTokenRequest;

    public AuthorizationCodeGrant setAccessTokenRequest(AccessTokenRequest accessTokenRequest) {
        this.accessTokenRequest = accessTokenRequest;
        return this;
    }

    public AuthorizationCodeGrant setAuthorizationRequest(AuthorizationRequest request) {
        this.authorizationRequest = request;

        this.state = request.getState();
        return this;
    }

    public AuthorizationCodeGrant setAuthorizationCode(String code) {
        this.authorizationCode = code;
        return this;
    }

    public void validate() {
        if (authorizationRequest != null) {
            if (authorizationRequest.getResponseType().equals("code") == false) {
                throw new RuntimeException("response_type should be : code");
            }

            if (authorizationRequest.getClientId() == null) {
                throw new RuntimeException("client_id should be present");
            }
        }

        if (accessTokenRequest != null) {
            if (accessTokenRequest.getGrantType() == null) {
                throw new RuntimeException("grant_type should not be null");
            }
            if (accessTokenRequest.getGrantType().equals("authorization_code") == false) {
                throw new RuntimeException("grant_type should be authorization_code");
            }
            if (accessTokenRequest.getCode() == null) {
                throw new RuntimeException("code should not be null");
            }
        }
    }

    public AuthorizationResponse authorizationResponse() {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setCode(authorizationCode);
        response.setState(state);
        return response;
    }

    public AccessTokenResponse accessTokenResponse() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpires(accessTokenExpiry);
        return response;
    }
}