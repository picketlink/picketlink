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

package org.picketlink.authentication.web.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <p>
 * This class is a representation of the state of a previous {@link HttpServletRequest} instance.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class SavedRequest {

    private List<Cookie> cookies = new ArrayList<Cookie>();
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String[]> parameters = new HashMap<String, String[]>();
    private String method;
    private String queryString;
    private String requestURI;
    private String scheme;
    private String contextPath;

    /**
     * <p>
     * Create a new instance copying the state from the request passed as argument.
     * </p>
     */
    public SavedRequest(HttpServletRequest request) {
        copyCookies(request);
        copyHeaders(request);
        copyParameters(request);

        // copy general properties from the original request
        this.method = request.getMethod();
        this.queryString = request.getQueryString();
        this.requestURI = request.getRequestURI();
        this.scheme = request.getScheme();
        this.contextPath = request.getContextPath();
    }

    /**
     * <p>
     * Returns the parameters copied from the original request.
     * </p>
     */
    public Map<String, String[]> getParameters() {
        return this.parameters;
    }

    /**
     * <p>
     * Returns the headers copied from the original request.
     * </p>
     */
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * <p>
     * Returns the cookies copied from the original request.
     * </p>
     */
    public List<Cookie> getCookies() {
        return this.cookies;
    }

    /**
     * <p>
     * Returns the original HTTP method used by the original request.
     * </p>
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * <p>
     * Returns the querystring used by the original request.
     * </p>
     */
    public String getQueryString() {
        return this.queryString;
    }

    /**
     * <p>
     * Returns the requestURI used by the original request.
     * </p>
     */
    public String getRequestURI() {
        return this.requestURI;
    }

    /**
     * <p>
     * Returns the original scheme used by the original request.
     * </p>
     */
    public String getScheme() {
        return this.scheme;
    }

    /**
     * <p>
     * Returns the original context path used by the original request.
     * </p>
     */
    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * <p>
     * Copy the parameters from the original {@link HttpServletRequest}.
     * </p>
     */
    private void copyParameters(HttpServletRequest request) {
        Set<Entry<String, String[]>> parametersEntries = request.getParameterMap().entrySet();

        for (Entry<String, String[]> parameter : parametersEntries) {
            this.getParameters().put(parameter.getKey(), (String[]) parameter.getValue());
        }
    }

    /**
     * <p>
     * Copy the headers from the original {@link HttpServletRequest}.
     * </p>
     */
    private void copyHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            this.getHeaders().put(headerName, headerValue);
        }
    }

    /**
     * <p>
     * Copy the cookies from the original {@link HttpServletRequest}.
     * </p>
     */
    private void copyCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            this.getCookies().add(cookie);
        }
    }
}