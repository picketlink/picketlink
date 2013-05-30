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
 * A request for resource
 *
 * @author anil saldhana
 * @since Mar 6, 2013
 */
public class ResourceAccessRequest extends OAuthRequest {
    private static final long serialVersionUID = 7621868996526669958L;
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public ResourceAccessRequest setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    @Override
    public String asJSON() {
        StringWriter sw = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        if (accessToken != null) {
            map.put(OAuthConstants.ACCESS_TOKEN, encode(accessToken));
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

        StringBuilder builder = new StringBuilder();
        if (accessToken != null) {
            builder.append(OAuthConstants.ACCESS_TOKEN).append(EQ).append(encode(accessToken)).append(AMPER);
        }
        return builder.toString();
    }
}