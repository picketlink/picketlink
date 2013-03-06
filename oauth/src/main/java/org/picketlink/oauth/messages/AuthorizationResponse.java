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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * OAuth2 Authorization Response
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class AuthorizationResponse extends OAuthResponse {
    private static final long serialVersionUID = 53914271397926560L;
    private String code, state, responseMessage;

    public String getCode() {
        return code;
    }

    public AuthorizationResponse setCode(String code) {
        this.code = code;
        return this;
    }

    public String getState() {
        return state;
    }

    public AuthorizationResponse setState(String state) {
        this.state = state;
        return this;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public AuthorizationResponse setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
        return this;
    }

    public String asQueryParams() {
        StringBuilder builder = new StringBuilder();
        try {
            if (code != null) {
                builder.append("code=");
                builder.append(URLEncoder.encode(code, "UTF-8")).append("&");
            }
            if (state != null) {
                builder.append("state=");
                builder.append(URLEncoder.encode(state, "UTF-8")).append("&");
            }

        } catch (UnsupportedEncodingException ue) {

        }
        return builder.toString();
    }

    @Override
    public String asJSON() {
        return null;
    }
}