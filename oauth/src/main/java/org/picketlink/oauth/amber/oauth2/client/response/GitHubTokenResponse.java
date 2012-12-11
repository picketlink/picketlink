/**
 *       Copyright 2010 Newcastle University
 *
 *          http://research.ncl.ac.uk/smart/
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.oauth.amber.oauth2.client.response;

import org.picketlink.oauth.amber.oauth2.common.OAuth;
import org.picketlink.oauth.amber.oauth2.common.token.BasicOAuthToken;
import org.picketlink.oauth.amber.oauth2.common.token.OAuthToken;
import org.picketlink.oauth.amber.oauth2.common.utils.OAuthUtils;

/**
 *
 *
 *
 */
public class GitHubTokenResponse extends OAuthAccessTokenResponse {

    public String getAccessToken() {
        return getParam(OAuth.OAUTH_ACCESS_TOKEN);
    }

    public Long getExpiresIn() {
        String value = getParam(OAuth.OAUTH_EXPIRES_IN);
        return value == null ? null : Long.valueOf(value);
    }

    public String getRefreshToken() {
        return getParam(OAuth.OAUTH_EXPIRES_IN);
    }

    public String getScope() {
        return getParam(OAuth.OAUTH_SCOPE);
    }

    public OAuthToken getOAuthToken() {
        return new BasicOAuthToken(getAccessToken(), getExpiresIn(), getRefreshToken(), getScope());
    }

    protected void setBody(String body) {
        this.body = body;
        parameters = OAuthUtils.decodeForm(body);
    }

    protected void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected void setResponseCode(int code) {
        this.responseCode = code;
    }

}
