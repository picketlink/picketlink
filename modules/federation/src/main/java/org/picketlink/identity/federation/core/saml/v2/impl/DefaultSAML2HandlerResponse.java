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
package org.picketlink.identity.federation.core.saml.v2.impl;

import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the SAML2 Handler response
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 1, 2009
 */
public class DefaultSAML2HandlerResponse implements SAML2HandlerResponse {

    private Document document;
    private String relayState;
    private List<String> roles = new ArrayList<String>();
    private String destination;
    private int errorCode;
    private String errorMessage;
    private boolean errorMode;
    private boolean sendRequest;

    private boolean postBinding = true;

    private String destinationQueryStringWithSignature;

    /**
     * @see SAML2HandlerResponse#getRelayState()
     */
    public String getRelayState() {
        return this.relayState;
    }

    /**
     * @see SAML2HandlerResponse#getResultingDocument()
     */
    public Document getResultingDocument() {
        return this.document;
    }

    /**
     * @see SAML2HandlerResponse#setRelayState(String)
     */
    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }

    /**
     * @see SAML2HandlerResponse#setResultingDocument(Document)
     */
    public void setResultingDocument(Document doc) {
        this.document = doc;
    }

    /**
     * @see SAML2HandlerResponse#getRoles()
     */
    public List<String> getRoles() {
        return this.roles;
    }

    /**
     * @see SAML2HandlerResponse#setRoles(List)
     */
    public void setRoles(List<String> roles) {
        this.roles.addAll(roles);
    }

    /**
     * @see SAML2HandlerResponse#getDestination()
     */
    public String getDestination() {
        return this.destination;
    }

    /**
     * @see SAML2HandlerResponse#setDestination(String)
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @see SAML2HandlerResponse#getErrorCode()
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * @see SAML2HandlerResponse#getErrorMessage()
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * @see SAML2HandlerResponse#setError(int, String)
     */
    public void setError(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

        this.errorMode = true;
    }

    /**
     * @see SAML2HandlerResponse#isInError()
     */
    public boolean isInError() {
        return this.errorMode;
    }

    /**
     * @see SAML2HandlerResponse#getSendRequest()
     */
    public boolean getSendRequest() {
        return this.sendRequest;
    }

    /**
     * @see SAML2HandlerResponse#setSendRequest(boolean)
     */
    public void setSendRequest(boolean request) {
        this.sendRequest = request;
    }

    /**
     * @see SAML2HandlerResponse#setPostBindingForResponse(boolean)
     */
    public void setPostBindingForResponse(boolean postB) {
        this.postBinding = postB;
    }

    /**
     * @see SAML2HandlerResponse#isPostBindingForResponse()
     */
    public boolean isPostBindingForResponse() {
        return this.postBinding;
    }

    /**
     * @see SAML2HandlerResponse#setDestinationQueryStringWithSignature(String)
     */
    public void setDestinationQueryStringWithSignature(String destinationQueryStringWithSignature) {
        this.destinationQueryStringWithSignature = destinationQueryStringWithSignature;
    }

    /**
     * @see SAML2HandlerResponse#getDestinationQueryStringWithSignature()
     */
    public String getDestinationQueryStringWithSignature() {
        return this.destinationQueryStringWithSignature;
    }
}