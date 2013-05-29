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
 * A {@link OAuthRequest} for client registration
 *
 * @author anil saldhana
 * @since Mar 6, 2013
 */
public class RegistrationRequest extends OAuthRequest {
    private static final long serialVersionUID = -1064982266781723708L;
    private String clientName, clientUrl, clientDescription, clientRedirecturl, clientIcon, location;

    public String getClientName() {
        return clientName;
    }

    public RegistrationRequest setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public String getClientURL() {
        return clientUrl;
    }

    public RegistrationRequest setClientUrl(String clientURL) {
        this.clientUrl = clientURL;
        return this;
    }

    public String getClientDescription() {
        return clientDescription;
    }

    public RegistrationRequest setClientDescription(String clientDescription) {
        this.clientDescription = clientDescription;
        return this;
    }

    public String getClientRedirectURI() {
        return clientRedirecturl;
    }

    public RegistrationRequest setClientRedirecturl(String clientRedirectURI) {
        this.clientRedirecturl = clientRedirectURI;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public RegistrationRequest setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getClientIcon() {
        return clientIcon;
    }

    public RegistrationRequest setClient_Icon(String icon) {
        this.clientIcon = icon;
        return this;
    }

    @Override
    public String asJSON() {
        StringWriter sw = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(OAuthConstants.CLIENT_NAME, encode(clientName));
        if (clientUrl != null) {
            map.put(OAuthConstants.CLIENT_URL, encode(clientUrl));
        }
        if (clientDescription != null) {
            map.put(OAuthConstants.CLIENT_DESCRIPTION, encode(clientDescription));
        }
        if (clientRedirecturl != null) {
            map.put(OAuthConstants.CLIENT_REDIRECT_URL, encode(clientRedirecturl));
        }
        if (clientIcon != null) {
            map.put(OAuthConstants.CLIENT_ICON, encode(clientIcon));
        }

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
        String AMPER = "&";
        String EQ = "=";

        // String clientName, clientURL, clientDescription, clientRedirectURI, icon, location;

        StringBuilder builder = new StringBuilder(OAuthConstants.CLIENT_NAME);
        builder.append(EQ).append(encode(clientName)).append(AMPER);
        if (clientUrl != null) {
            builder.append(OAuthConstants.CLIENT_URL).append(EQ).append(encode(clientUrl)).append(AMPER);
        }
        if (clientDescription != null) {
            builder.append(OAuthConstants.CLIENT_DESCRIPTION).append(EQ).append(encode(clientDescription)).append(AMPER);
        }
        if (clientRedirecturl != null) {
            builder.append(OAuthConstants.CLIENT_REDIRECT_URL).append(EQ).append(encode(clientRedirecturl)).append(AMPER);
        }
        if (clientIcon != null) {
            builder.append(OAuthConstants.CLIENT_ICON).append(EQ).append(encode(clientIcon)).append(AMPER);
        }
        return builder.toString();
    }
}