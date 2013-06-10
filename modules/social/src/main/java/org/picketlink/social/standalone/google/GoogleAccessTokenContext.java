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
package org.picketlink.social.standalone.google;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

/**
 * Encapsulate informations about Google+ access token
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GoogleAccessTokenContext implements Serializable {

    private static final long serialVersionUID = -7038197192745766989L;

    private final GoogleTokenResponse tokenData;
    private final Set<String> scopes = new HashSet<String>();


    public GoogleAccessTokenContext(GoogleTokenResponse tokenData, String scopeAsString) {
        if (tokenData == null) {
            throw new IllegalArgumentException("tokenData can't be null");
        }
        if (scopeAsString== null) {
            throw new IllegalArgumentException("scope can't be null");
        }

        this.tokenData = tokenData;

        if (scopeAsString.length() > 0) {
            String[] scopes = scopeAsString.split(" ");
            for (String scope : scopes) {
                this.scopes.add(scope);
            }
        }
    }

    public GoogleTokenResponse getTokenData() {
        return tokenData;
    }

    public String getScopesAsString() {
        Iterator<String> iterator = scopes.iterator();
        StringBuilder result;

        if (iterator.hasNext()) {
            result = new StringBuilder(iterator.next());
        } else {
            return "";
        }

        while (iterator.hasNext()) {
            result.append(" " + iterator.next());
        }
        return result.toString();
    }

    public boolean addScope(String scope) {
        return scopes.add(scope);
    }

    public boolean isScopeAvailable(String scope) {
        return scopes.contains(scope);
    }

    @Override
    public String toString() {
        return new StringBuilder("GoogleAccessTokenContext [tokenData=")
                .append(tokenData)
                .append(", scope=")
                .append(getScopesAsString()).append("]").toString();
    }

    @Override
    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }
        if (that == null) {
            return false;
        }

        if (!(that.getClass().equals(this.getClass()))) {
            return false;
        }

        GoogleAccessTokenContext thatt = (GoogleAccessTokenContext)that;
        return this.scopes.equals(thatt.scopes) && this.tokenData.equals(thatt.tokenData);
    }

    @Override
    public int hashCode() {
        return scopes.hashCode() * 13 + tokenData.hashCode();
    }
}
