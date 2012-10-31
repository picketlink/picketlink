/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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