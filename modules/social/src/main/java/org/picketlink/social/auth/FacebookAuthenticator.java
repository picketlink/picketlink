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

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import javax.servlet.http.HttpSession;
import org.picketlink.idm.model.basic.User;
import org.picketlink.social.auth.conf.FacebookConfiguration;
import org.picketlink.social.standalone.fb.FacebookProcessor;

/**
 * An implementation of {@link org.picketlink.authentication.Authenticator} for Facebook login
 * @author Anil Saldhana
 * @since May 30, 2013
 */
public class FacebookAuthenticator extends AbstractSocialAuthenticator {

    protected FacebookConfiguration configuration;

    private enum STATES {
        AUTH, AUTHZ, FINISH
    };

    protected static final String FB_AUTH_STATE_SESSION_ATTRIBUTE = "FB_AUTH_STATE_SESSION_ATTRIBUTE";
    protected String returnURL;
    protected String clientID;
    protected String clientSecret;
    protected String scope;

    protected FacebookProcessor processor;

    public FacebookAuthenticator(){
    }

    public void setConfiguration(FacebookConfiguration configuration){
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
        HttpSession session = httpServletRequest.getSession();

        Principal principal = null;

        if (isFirstInteraction(session)) {
            try {
                getFacebookProcessor().initialInteraction(httpServletRequest, httpServletResponse);
            } catch (IOException e) {
                throw new RuntimeException("Error while initiating Facebook authentication interaction.", e);
            }
        } else if (isAuthenticationInteraction(session)) {
            if (!httpServletResponse.isCommitted())
                getFacebookProcessor().handleAuthStage(httpServletRequest, httpServletResponse);
        } else if (isAuthorizationInteraction(session)) {
            session.removeAttribute(FB_AUTH_STATE_SESSION_ATTRIBUTE);
            principal = getFacebookProcessor().getPrincipal(httpServletRequest, httpServletResponse);
            //provisionNewUser((FacebookPrincipal) principal);
        }
        if(principal != null){
            setStatus(AuthenticationStatus.SUCCESS);
            setAccount(new User(principal.getName()));
        }
    }


    private boolean isAuthorizationInteraction(HttpSession session) {
        return getCurrentAuthenticationState(session).equals(STATES.AUTHZ.name());
    }

    private boolean isAuthenticationInteraction(HttpSession session) {
        return getCurrentAuthenticationState(session).equals(STATES.AUTH.name());
    }

    private boolean isFirstInteraction(HttpSession session) {
        return getCurrentAuthenticationState(session) == null || getCurrentAuthenticationState(session).isEmpty();
    }

    private String getCurrentAuthenticationState(HttpSession session) {
        return (String) session.getAttribute(FB_AUTH_STATE_SESSION_ATTRIBUTE);
    }
    private FacebookProcessor getFacebookProcessor() {
        if (this.processor == null) {
            if(clientID == null){
                clientID = configuration.getClientID();
            }
            if(clientSecret == null){
                clientSecret = configuration.getClientSecret();
            }
            if(scope == null){
                scope = configuration.getScope();
            }
            if(returnURL == null){
                returnURL = configuration.getReturnURL();
            }
            this.processor = new FacebookProcessor(clientID, clientSecret, scope, returnURL, Collections.EMPTY_LIST);
        }
        return this.processor;
    }
}