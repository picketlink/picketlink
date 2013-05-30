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
 * OAuth2 Access Token Request
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class AccessTokenRequest extends OAuthRequest {
    private static final long serialVersionUID = 5069340399891368370L;
    private String grantType, code, redirectUri, clientId, location;

    public String getGrantType() {
        return grantType;
    }

    public AccessTokenRequest setGrantType(String grantType) {
        this.grantType = grantType;
        return this;
    }

    public String getCode() {
        return code;
    }

    public AccessTokenRequest setCode(String code) {
        this.code = code;
        return this;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public AccessTokenRequest setRedirectUri(String redirectURI) {
        this.redirectUri = redirectURI;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public AccessTokenRequest setClientId(String clientID) {
        this.clientId = clientID;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public AccessTokenRequest setLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public String asJSON() {
        StringWriter sw = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        if (grantType != null) {
            map.put(OAuthConstants.GRANT_TYPE, encode(grantType));
        }
        if (clientId != null) {
            map.put(OAuthConstants.CLIENT_ID, encode(clientId));
        }
        if (redirectUri != null) {
            map.put(OAuthConstants.REDIRECT_URI, encode(redirectUri));
        }
        if (code != null) {
            map.put(OAuthConstants.CODE, encode(code));
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

    @Override
    public String asQueryParams() {
        String AMP = "&";
        String EQ = "=";
        StringBuilder builder = new StringBuilder();

        // private String responseType, clientId, redirectURI, scope, state, location;
        if (grantType != null) {
            builder.append(OAuthConstants.GRANT_TYPE).append(EQ).append(encode(grantType)).append(AMP);
        }
        if (clientId != null) {
            builder.append(OAuthConstants.CLIENT_ID).append(EQ).append(encode(clientId)).append(AMP);
        }
        if (redirectUri != null) {
            builder.append(OAuthConstants.REDIRECT_URI).append(EQ).append(encode(redirectUri)).append(AMP);
        }
        if (code != null) {
            builder.append(OAuthConstants.CODE).append(EQ).append(encode(code)).append(AMP);
        }

        return builder.toString();
    }
}