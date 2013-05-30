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
 * A {@link OAuthResponse} for Client Registration
 *
 * @author anil saldhana
 * @since Mar 6, 2013
 */
public class RegistrationResponse extends OAuthResponse {
    private static final long serialVersionUID = -2222583231025897760L;
    private String clientID, clientSecret, issued;
    private long expiresIn = 3600L;

    public String getClientID() {
        return clientID;
    }

    public RegistrationResponse setClientID(String clientID) {
        this.clientID = clientID;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public RegistrationResponse setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getIssued() {
        return issued;
    }

    public RegistrationResponse setIssued(String issuedAt) {
        this.issued = issuedAt;
        return this;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public RegistrationResponse setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    @Override
    public String asQueryParams() {
        return null;
    }

    @Override
    public String asJSON() {
        StringWriter sw = new StringWriter();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(OAuthConstants.CLIENT_ID, clientID);
        map.put(OAuthConstants.CLIENT_SECRET, clientSecret);
        map.put("issued", issued);
        map.put(OAuthConstants.EXPIRES_IN, expiresIn);

        ObjectMapper mapper = getObjectMapper();
        try {
            mapper.writeValue(sw, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }
}