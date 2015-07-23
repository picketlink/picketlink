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
package org.picketlink.http.internal.authentication.schemes;

import org.picketlink.Identity;
import org.picketlink.config.http.FormAuthenticationConfiguration;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.http.authentication.HttpAuthenticationScheme;
import org.picketlink.http.internal.authentication.schemes.support.RequestCache;
import org.picketlink.http.internal.authentication.schemes.support.SavedRequest;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An implementation of {@link org.picketlink.http.authentication.HttpAuthenticationScheme} that supports the Servlet Specification FORM Authentication Scheme
 *
 * @author Anil Saldhana
 * @since June 06, 2013
 */
public class FormAuthenticationScheme implements HttpAuthenticationScheme<FormAuthenticationConfiguration> {

    public static final String J_USERNAME = "j_username";
    public static final String J_PASSWORD = "j_password";

    private final RequestCache requestCache = new RequestCache();
    private FormAuthenticationConfiguration configuration;

    @Inject
    private Instance<Identity> identity;

    @Override
    public void initialize(FormAuthenticationConfiguration config) {
        this.configuration = config;
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        if (isFormSubmitted(request)) {
            String username = request.getParameter(J_USERNAME);

            if (username != null) {
                creds.setUserId(username);
            }

            String password = request.getParameter(J_PASSWORD);

            if (password != null) {
                creds.setPassword(password);
            }
        }
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) {
        String requestedUri = request.getRequestURI();

        if (!isFormSubmitted(request) && this.configuration.isRestoreOriginalRequest() && !requestedUri.contains(this.configuration.getLoginPageUrl())) {
            requestCache.saveRequest(request);
        }

        if (!requestedUri.contains(this.configuration.getLoginPageUrl())
                && !requestedUri.contains(this.configuration.getErrorPageUrl())) {

            if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
                sendRedirectXml(request, response);
                return;
            }

            forwardToLoginPage(request, response);
        }
    }

    @Override
    public void onPostAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (this.identity.get().isLoggedIn()) {
                SavedRequest savedRequest = requestCache.removeAndStoreSavedRequestInSession(request);

                if (savedRequest != null) {
                    response.sendRedirect(savedRequest.getRequestURI());
                }

                if (!this.configuration.isRestoreOriginalRequest() || savedRequest == null) {
                    response.sendRedirect(request.getContextPath());
                }
            } else if (isFormSubmitted(request)) {
                forwardToErrorPage(request, response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not perform post authentication tasks after a form-based authentication.", e);
        }
    }

    private void sendRedirectXml(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");

        try {
            response.getWriter()
                    .printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\"%s\"></redirect></partial-response>",
                            request.getContextPath() + this.configuration.getLoginPageUrl()).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void forwardToLoginPage(HttpServletRequest request, HttpServletResponse response) {
        forwardToPage(this.configuration.getLoginPageUrl(), request, response);
    }

    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response) {
        forwardToPage(this.configuration.getErrorPageUrl(), request, response);
    }

    private void forwardToPage(String page, HttpServletRequest request, HttpServletResponse response) {
        try {
            response.sendRedirect(request.getContextPath() + page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isFormSubmitted(HttpServletRequest request) {
        return request.getRequestURI().contains(this.configuration.getAuthenticationUri());
    }
}