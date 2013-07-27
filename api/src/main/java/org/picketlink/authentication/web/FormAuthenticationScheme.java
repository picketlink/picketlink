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
package org.picketlink.authentication.web;

import java.io.IOException;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.picketlink.authentication.web.support.RequestCache;
import org.picketlink.authentication.web.support.SavedRequest;
import org.picketlink.credential.DefaultLoginCredentials;

/**
 * An implementation of {@link HTTPAuthenticationScheme} that supports the Servlet Specification
 * FORM Authentication Scheme
 * @author Anil Saldhana
 * @since June 06, 2013
 */
public class FormAuthenticationScheme implements HTTPAuthenticationScheme{

    public static final String FORM_LOGIN_PAGE_INIT_PARAM = "form-login-page";
    public static final String FORM_ERROR_PAGE_INIT_PARAM = "form-error-page";

    private RequestCache requestCache = new RequestCache();

    private final String formLoginPage;
    private final String formErrorPage;

    public static final String J_SECURITY_CHECK ="j_security_check";
    public static final String J_USERNAME = "j_username";
    public static final String J_PASSWORD = "j_password";

    public static final String SAVED_REQUEST = "SAVED_REQUEST";

    private enum STATES {BEFORE_LOGIN,SHOW_LOGIN_PAGE,AFTER_LOGIN};

    public static final String STATE = "STATE";

    public FormAuthenticationScheme(FilterConfig config) {
        String formLoginPage = config.getInitParameter(FORM_LOGIN_PAGE_INIT_PARAM);

        if (formLoginPage == null) {
            formLoginPage = "/login.jsp";
        }

        this.formLoginPage = formLoginPage;

        String formErrorPage = config.getInitParameter(FORM_ERROR_PAGE_INIT_PARAM);

        if (formErrorPage == null) {
            formErrorPage = "/loginError.jsp";
        }

        this.formErrorPage = formErrorPage;
    }
    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        if(isFormSubmitted(request)){
            creds.setUserId(request.getParameter(J_USERNAME));
            creds.setPassword(request.getParameter(J_PASSWORD));
        }
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String sessionState = (String) session.getAttribute(STATE);
        if(sessionState == null || STATES.BEFORE_LOGIN.toString().equals(sessionState)){
            //Save current request
            requestCache.saveRequest(request);
            session.setAttribute(STATE,STATES.SHOW_LOGIN_PAGE.toString());
        }

        forwardToLoginPage(request,response);
    }

    @Override
    public boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String state = (String) session.getAttribute(STATE);

        if(state != null && STATES.SHOW_LOGIN_PAGE.toString().equals(state)){
            requestCache.removeAndStoreSavedRequestInSession(request);
            SavedRequest savedRequest = (SavedRequest) session.getAttribute(FormAuthenticationScheme.SAVED_REQUEST);
            String requestedURI = savedRequest.getRequestURI();
            session.setAttribute(STATE,STATES.AFTER_LOGIN.toString());

            response.sendRedirect(requestedURI);
        }

        return false;
    }

    private void forwardToLoginPage(HttpServletRequest request, HttpServletResponse response){
        RequestDispatcher rd = request.getRequestDispatcher(formLoginPage);
        try {
            rd.forward(request,response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response){
        RequestDispatcher rd = request.getRequestDispatcher(formErrorPage);
        try {
            rd.forward(request,response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isFormSubmitted(HttpServletRequest request){
        return request.getRequestURI().contains(J_SECURITY_CHECK);
    }
}