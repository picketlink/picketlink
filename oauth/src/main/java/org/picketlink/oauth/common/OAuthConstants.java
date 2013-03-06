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
package org.picketlink.oauth.common;

/**
 * Constants
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public interface OAuthConstants {

    interface HttpMethod {
        String POST = "POST";
        String GET = "GET";
        String DELETE = "DELETE";
        String PUT = "PUT";
    }

    interface HeaderType {
        String CONTENT_TYPE = "Content-Type";
        String WWW_AUTHENTICATE = "WWW-Authenticate";
        String AUTHORIZATION = "Authorization";
    }

    interface WWWAuthHeader {
        String REALM = "realm";
    }

    interface ContentType {
        String URL_ENCODED = "application/x-www-form-urlencoded";
        String JSON = "application/json";
    }

    // Registration
    String CLIENT_NAME = "client_name";
    String CLIENT_DESCRIPTION = "client_description";
    String CLIENT_ICON = "client_icon";
    String CLIENT_URL = "client_url";
    String CLIENT_REDIRECT_URL = "client_redirecturl";

    String AUTHORIZATION_CODE = "authorization_code";
    String RESPONSE_TYPE = "response_type";
    String CLIENT_ID = "client_id";
    String CLIENT_SECRET = "client_secret";
    String REDIRECT_URI = "redirect_uri";
    String USERNAME = "username";
    String PASSWORD = "password";
    String ASSERTION_TYPE = "assertion_type";
    String ASSERTION = "assertion";
    String SCOPE = "scope";
    String STATE = "state";
    String GRANT_TYPE = "grant_type";

    String HEADER_NAME = "Bearer";

    String CODE = "code";
    String ACCESS_TOKEN = "access_token";
    String EXPIRES_IN = "expires_in";
    String REFRESH_TOKEN = "refresh_token";

    String TOKEN_TYPE = "token_type";

    String TOKEN = "oauth_token";

    String BEARER_TOKEN = "access_token";
}
