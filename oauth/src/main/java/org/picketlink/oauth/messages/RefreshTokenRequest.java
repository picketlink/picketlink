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
 * OAuth2 Refresh Token Request
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class RefreshTokenRequest extends OAuthRequest {
    private static final long serialVersionUID = 8487545851197134924L;
    private String grantType;
    private String refreshToken;

    private String scope;

    public String getGrantType() {
        return grantType;
    }

    public RefreshTokenRequest setGrantType(String grantType) {
        this.grantType = grantType;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public RefreshTokenRequest setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public RefreshTokenRequest setScope(String scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public String asJSON() {
        return null;
    }

    @Override
    public String asQueryParams() {
        return null;
    }
}