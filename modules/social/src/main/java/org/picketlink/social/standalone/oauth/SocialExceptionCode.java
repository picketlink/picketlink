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

package org.picketlink.social.standalone.oauth;

/**
 * Enum with various exception codes
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum SocialExceptionCode {


    /**
     * Unspecified GateIn+OAuth error
     */
    UNKNOWN_ERROR,

    /**
     * Some error during Google processing
     */
    GOOGLE_ERROR,

    /**
     * Error when we have invalid or revoked access token
     */
    ACCESS_TOKEN_ERROR,

    /**
     * Generic IO error (for example network error)
     */
    IO_ERROR,

    /**
     * Error when state parameter from request parameter, which is sent from Social network, is not equals to previously sent state
     */
    INVALID_STATE,

    /**
     * Error when revoking of accessToken of any social netowrk failed
     */
    TOKEN_REVOCATION_FAILED,

    /**
     * Error when OAuth2 flow failed because user denied to permit privileges (scope) for Social network
     */
    USER_DENIED_SCOPE,

    /**
     * Error when invalid clientID or clientSecret is used
     */
    INVALID_CLIENT,
}
