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
package org.picketlink.identity.federation.web.core;

import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/**
 * Protocol Context based on HTTP
 *
 * @author Anil.Saldhana@redhat.com
 * @since Sep 17, 2009
 */
public class HTTPContext implements ProtocolContext {

    protected HttpServletRequest request;
    protected HttpServletResponse response;

    protected ServletContext servletContext;

    public HTTPContext(HttpServletRequest httpReq, HttpServletResponse httpResp, ServletContext sctx) {
        this.request = httpReq;
        this.response = httpResp;
        this.servletContext = sctx;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    // Setters

    public HTTPContext setRequest(HttpServletRequest req) {
        this.request = req;
        return this;
    }

    public HTTPContext setResponse(HttpServletResponse resp) {
        this.response = resp;
        return this;
    }

    public HTTPContext setServletContext(ServletContext sctx) {
        this.servletContext = sctx;
        return this;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#serviceName()
     */
    public String serviceName() {
        return null;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#tokenType()
     */
    public String tokenType() {
        return null;
    }

    public QName getQName() {
        return null;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#family()
     */
    public String family() {
        return SecurityTokenProvider.FAMILY_TYPE.OPENID.toString();
    }
}