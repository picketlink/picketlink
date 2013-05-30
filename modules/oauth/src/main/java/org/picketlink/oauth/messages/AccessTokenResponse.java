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

import org.codehaus.jackson.map.ObjectMapper;
import org.picketlink.oauth.common.OAuthConstants;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Access Token Response
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class AccessTokenResponse extends OAuthResponse {
    private static final long serialVersionUID = -9064571507653000060L;

    public enum TokenType {
        BEARER, MAC
    };

    private TokenType tokenType;

    private String accessToken, refreshToken, state, scope;
    private long expires;

    public String getAccessToken() {
        return accessToken;
    }

    public AccessTokenResponse setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public AccessTokenResponse setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public AccessTokenResponse setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public long getExpires() {
        return expires;
    }

    public AccessTokenResponse setExpires(long expires) {
        this.expires = expires;
        return this;
    }

    public String getState() {
        return state;
    }

    public AccessTokenResponse setState(String state) {
        this.state = state;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public AccessTokenResponse setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String asQueryParams() {
        String AMP = "&";
        String EQ = "=";
        StringBuilder builder = new StringBuilder();

        // private String responseType, clientId, redirectURI, scope, state, location;
        if (accessToken != null) {
            builder.append(OAuthConstants.ACCESS_TOKEN).append(EQ).append(encode(accessToken)).append(AMP);
        }
        if (tokenType != null) {
            builder.append(OAuthConstants.TOKEN_TYPE).append(EQ).append(encode(tokenType.name())).append(AMP);
        }
        builder.append(OAuthConstants.EXPIRES_IN).append(EQ).append(encode(expires + "")).append(AMP);

        if (refreshToken != null) {
            builder.append(OAuthConstants.CODE).append(EQ).append(encode(refreshToken)).append(AMP);
        }

        return builder.toString();
    }

    @Override
    public String asJSON() {
        StringWriter sw = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(OAuthConstants.ACCESS_TOKEN, accessToken);
        if (tokenType != null) {
            map.put(OAuthConstants.TOKEN_TYPE, tokenType.name());
        }
        map.put(OAuthConstants.EXPIRES_IN, expires);
        if (refreshToken != null) {
            map.put(OAuthConstants.REFRESH_TOKEN, refreshToken);
        }
        // TODO: parameters

        ObjectMapper mapper = getObjectMapper();
        try {
            mapper.writeValue(sw, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }
}