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
package org.picketlink.social.standalone.fb;

/**
 * Constants for facebook
 *
 * @author Marcel Kolsteren
 * @author anil saldhana
 * @since Sep 26, 2010
 */
public class FacebookConstants {
    public static final String AUTHENTICATION_ENDPOINT_URL = "https://graph.facebook.com/oauth/authorize";

    public static final String ACCESS_TOKEN_ENDPOINT_URL = "https://graph.facebook.com/oauth/access_token";

    public static final String PROFILE_ENDPOINT_URL = "https://graph.facebook.com/me";

    public static final String RETURN_URL_PARAMETER = "returnUrl";

    public static final String SERVICE_URL = "https://www.facebook.com/dialog/oauth";

    public static final String TYPE = "type";

    public static final String WEB_SERVER = "web_server";

    public static final String EXPIRES = "expires";
}
