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
package org.picketlink.oauth.registration;

/**
 * Represents the registration of a client application
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
public class OAuthClientRegistration {
    private String clientID;
    private String clientSecret;
    private String applicationName;
    private String applicationURL;
    private String applicationDescription;
    private String redirectURL;

    private String issuedAt;
    private long expires;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public OAuthClientRegistration setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public OAuthClientRegistration setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public String getApplicationURL() {
        return applicationURL;
    }

    public OAuthClientRegistration setApplicationURL(String applicationURL) {
        this.applicationURL = applicationURL;
        return this;
    }

    public String getApplicationDescription() {
        return applicationDescription;
    }

    public OAuthClientRegistration setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
        return this;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public OAuthClientRegistration setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
        return this;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public OAuthClientRegistration setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
        return this;
    }

    public long getExpires() {
        return expires;
    }

    public OAuthClientRegistration setExpires(long expires) {
        this.expires = expires;
        return this;
    }

    // Push to datastore
    public void persist() {
    }

    // Load from datastore
    public void retrieve() {
    }
}