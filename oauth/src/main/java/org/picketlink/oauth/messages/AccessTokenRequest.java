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
 * OAuth2 Access Token Request
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 *
 */
public class AccessTokenRequest {
    private String grantType, code, redirectURI, clientID;

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

    public String getRedirectURI() {
        return redirectURI;
    }

    public AccessTokenRequest setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
        return this;
    }

    public String getClientID() {
        return clientID;
    }

    public AccessTokenRequest setClientID(String clientID) {
        this.clientID = clientID;
        return this;
    }
}