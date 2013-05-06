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

import org.picketlink.oauth.common.UUIDGenerator;
import org.picketlink.oauth.common.ValueGenerator;
import org.picketlink.oauth.messages.AccessTokenResponse;
import org.picketlink.oauth.messages.AccessTokenResponse.TokenType;

/**
 * Base class for grants that involve a access token
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public abstract class AccessTokenEnabledGrant extends OAuthGrant {

    protected ValueGenerator valueGenerator = new UUIDGenerator();

    protected String accessToken;

    protected String refreshToken;

    protected long accessTokenExpiry = 3600L;

    protected TokenType tokenType;
    protected String scope;

    public ValueGenerator getValueGenerator() {
        return valueGenerator;
    }

    public AccessTokenEnabledGrant setValueGenerator(ValueGenerator valueGenerator) {
        this.valueGenerator = valueGenerator;
        return this;
    }

    public AccessTokenEnabledGrant setAccessTokenExpiry(long expiry) {
        this.accessTokenExpiry = expiry;
        return this;
    }

    public AccessTokenEnabledGrant setAccessToken(String code) {
        this.accessToken = code;
        return this;
    }

    public AccessTokenEnabledGrant setRefreshToken(String code) {
        this.refreshToken = code;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public AccessTokenEnabledGrant setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public AccessTokenEnabledGrant setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public AccessTokenResponse accessTokenResponse() {
        AccessTokenResponse response = new AccessTokenResponse();

        response.setAccessToken(accessToken);
        response.setState(state);
        response.setTokenType(tokenType);
        response.setScope(scope);
        response.setExpires(accessTokenExpiry);

        return response;
    }
}