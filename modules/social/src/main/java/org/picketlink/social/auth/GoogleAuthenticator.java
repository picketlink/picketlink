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

package org.picketlink.social.auth;

import com.google.api.services.oauth2.model.Userinfo;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.idm.model.basic.User;
import org.picketlink.social.auth.conf.GoogleConfiguration;
import org.picketlink.social.standalone.google.GoogleAccessTokenContext;
import org.picketlink.social.standalone.google.GoogleConstants;
import org.picketlink.social.standalone.google.GoogleProcessor;
import org.picketlink.social.standalone.google.InteractionState;

/**
 * An implementation of {@link org.picketlink.authentication.Authenticator} for Google+ login
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GoogleAuthenticator extends AbstractSocialAuthenticator {

    private GoogleConfiguration configuration;
    private GoogleProcessor googleProcessor;

    public void setConfiguration(GoogleConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void authenticate() {
        if(httpServletRequest == null){
            throw new IllegalStateException("http request not available");
        }
        if(httpServletResponse == null){
            throw new IllegalStateException("http response not available");
        }
        if(configuration == null){
            throw new IllegalStateException("configuration not available");
        }

        InteractionState interactionState;
        Userinfo userInfo = null;

        try {
            interactionState = getGoogleProcessor().processOAuthInteraction(httpServletRequest, httpServletResponse);
        } catch (Exception e) {
            // Cleanup state of OAuth interaction if error occured
            httpServletRequest.getSession().removeAttribute(GoogleConstants.ATTRIBUTE_AUTH_STATE);

            throw new AuthenticationException("Google+ login failed due to error", e);
        }

        // Authentication is finished. Let's obtain user info
        if (interactionState.getState().equals(InteractionState.State.FINISH)) {
            GoogleAccessTokenContext accessTokenContext = interactionState.getAccessTokenContext();
            userInfo = getGoogleProcessor().obtainUserInfo(accessTokenContext);

            // Establish security context
            setStatus(AuthenticationStatus.SUCCESS);
            setAccount(new User(userInfo.getEmail()));
        }
    }

    protected GoogleProcessor getGoogleProcessor() {
        if (this.googleProcessor == null) {
            this.googleProcessor = new GoogleProcessor(configuration.getClientID(),
                    configuration.getClientSecret(),
                    configuration.getReturnURL(),
                    configuration.getAccessType(),
                    configuration.getApplicationName(),
                    configuration.getRandomAlgorithm(),
                    configuration.getScope());
        }
        return this.googleProcessor;
    }
}
