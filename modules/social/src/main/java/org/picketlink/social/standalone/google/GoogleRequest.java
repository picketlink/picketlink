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

import java.io.IOException;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpResponseException;
import org.apache.log4j.Logger;
import org.picketlink.social.standalone.oauth.SocialException;

/**
 * Wrap Google operation within block of code to handle errors (and possibly restore access token and invoke operation again)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
abstract class GoogleRequest<T> {

    protected static Logger log = Logger.getLogger(GoogleRequest.class);

    protected abstract T invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException;

    protected abstract SocialException createException(IOException cause);

    public T executeRequest(GoogleAccessTokenContext accessTokenContext, GoogleProcessor googleProcessor) {
        GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
        try {
            return invokeRequest(accessTokenContext);
        } catch (IOException ioe) {
            if (ioe instanceof HttpResponseException) {
                HttpResponseException googleException = (HttpResponseException)ioe;
                if (googleException.getStatusCode() == 400 && tokenData.getRefreshToken() != null) {
                    try {
                        // Refresh token and retry revocation with refreshed token
                        googleProcessor.refreshToken(accessTokenContext);
                        return invokeRequest(accessTokenContext);
                    } catch (SocialException refreshException) {
                        // Log this one with trace level. We will rethrow original exception
                        if (log.isTraceEnabled()) {
                            log.trace("Refreshing token failed", refreshException);
                        }
                    } catch (IOException ioe2) {
                        ioe = ioe2;
                    }
                }
            }
            log.warn("Error when calling Google operation. Details: " + ioe.getMessage());
            throw createException(ioe);
        }
    }
}
