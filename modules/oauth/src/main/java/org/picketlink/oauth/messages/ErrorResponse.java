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

import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Error Response
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public class ErrorResponse extends OAuthResponse {
    private static final long serialVersionUID = -225455700169771043L;

    private ErrorResponseCode error;

    private String errorDescription, errorURI, state;

    public ErrorResponseCode getError() {
        return error;
    }

    public ErrorResponse setError(ErrorResponseCode error) {
        this.error = error;
        return this;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public ErrorResponse setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }

    public String getErrorURI() {
        return errorURI;
    }

    public ErrorResponse setErrorURI(String errorURI) {
        this.errorURI = errorURI;
        return this;
    }

    public String getState() {
        return state;
    }

    public ErrorResponse setState(String state) {
        this.state = state;
        return this;
    }

    public enum ErrorResponseCode {
        invalid_request, unauthorized_client, access_denied, unsupported_response_type, invalid_scope, server_error, temporarily_unavailable, invalid_client, invalid_grant, unsupported_grant_type;
    }

    public String asJSON() {
        StringWriter sw = new StringWriter();
        try {
            ObjectMapper mapper = getObjectMapper();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("error", error.name());
            if (errorDescription != null) {
                map.put("error_description", URLEncoder.encode(errorDescription, "UTF-8"));
            }
            if (errorURI != null) {
                map.put("error_uri", URLEncoder.encode(errorURI, "UTF-8"));
            }

            if (state != null) {
                map.put("state", URLEncoder.encode(state, "UTF-8"));
            }

            mapper.writeValue(sw, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    @Override
    public String asQueryParams() {
        return null;
    }
}