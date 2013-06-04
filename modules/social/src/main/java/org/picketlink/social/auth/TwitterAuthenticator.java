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

import org.picketlink.authentication.AuthenticationException;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.social.auth.conf.TwitterConfiguration;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * Implementation of {@link org.picketlink.authentication.Authenticator} for Twitter login
 * @author Anil Saldhana
 * @since June 03, 2013
 */
public class TwitterAuthenticator extends AbstractSocialAuthenticator{
    protected static final String TWIT_REQUEST_TOKEN_SESSION_ATTRIBUTE = "TWIT_REQUEST_TOKEN_SESSION_ATTRIBUTE";
    protected TwitterConfiguration configuration;

    public void setConfiguration(TwitterConfiguration configuration) {
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

        String clientID = configuration.getClientID();
        String clientSecret = configuration.getClientSecret();
        String returnURL = configuration.getReturnURL();

        Principal principal = null;
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(clientID, clientSecret);

        //See if we are a callback
        String verifier = httpServletRequest.getParameter("oauth_verifier");
        RequestToken requestToken = (RequestToken) session.getAttribute(TWIT_REQUEST_TOKEN_SESSION_ATTRIBUTE);
        if(requestToken != null || verifier != null){
            try {
                if(verifier == null){
                    throw new IllegalStateException("OAuth Verifier is null");
                }
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                session.setAttribute("accessToken", accessToken);
                session.removeAttribute("requestToken");
            } catch (TwitterException e) {
                throw new AuthenticationException("Twitter Login:",e);
            }

            try {
                principal = new TwitterPrincipal(twitter.verifyCredentials());

                setStatus(AuthenticationStatus.SUCCESS);
                setAgent(new SimpleUser(principal.getName()));
                return;
            } catch (TwitterException e) {
                throw new AuthenticationException("Twitter Login:",e);
            }
        }
        try {
            requestToken = twitter.getOAuthRequestToken(returnURL);
            session.setAttribute(TWIT_REQUEST_TOKEN_SESSION_ATTRIBUTE, requestToken);
            httpServletResponse.sendRedirect(requestToken.getAuthenticationURL());

        } catch (Exception e) {
            throw new AuthenticationException("Twitter Login:", e);
        }
        if(principal != null){
            setStatus(AuthenticationStatus.SUCCESS);
            setAgent(new SimpleUser(principal.getName()));
        }
    }
}