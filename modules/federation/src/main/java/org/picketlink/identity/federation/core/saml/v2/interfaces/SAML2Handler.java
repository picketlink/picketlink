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
package org.picketlink.identity.federation.core.saml.v2.interfaces;

import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;

/**
 * Handle SAML2 Request types and status response types
 *
 * @author Anil.Saldhana@redhat.com
 * @since Sep 17, 2009
 */
public interface SAML2Handler {
    // Define some constants
    String ASSERTION_CONSUMER_URL = "ASSERTION_CONSUMER_URL";

    String CLOCK_SKEW_MILIS = "CLOCK_SKEW_MILIS";

    String DISABLE_AUTHN_STATEMENT = "DISABLE_AUTHN_STATEMENT";

    String DISABLE_SENDING_ROLES = "DISABLE_SENDING_ROLES";

    String DISABLE_ROLE_PICKING = "DISABLE_ROLE_PICKING";

    String ROLE_KEY = "ROLE_KEY";

    /**
     * Processing Point - idp side or service side
     */
    public enum HANDLER_TYPE {
        IDP, SP;
    };

    /**
     * Initialize the handler
     *
     * @param handlerConfig Handler Config
     */
    void initChainConfig(SAML2HandlerChainConfig handlerChainConfig) throws ConfigurationException;

    /**
     * Initialize the handler from configuration
     *
     * @param options
     */
    void initHandlerConfig(SAML2HandlerConfig handlerConfig) throws ConfigurationException;

    /**
     * Generate a SAML Request to be sent to the IDP if the handler is invoked at the SP and vice-versa
     *
     * @param request
     * @param response
     * @throws ProcessingException
     */
    void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException;

    /**
     * Get the type of handler - handler at IDP or SP
     *
     * @return
     */
    HANDLER_TYPE getType();

    /**
     * Handle a SAML2 RequestAbstractType
     *
     * @param requestAbstractType
     * @param resultingDocument
     * @return
     */
    void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException;

    /**
     * Handle a SAML2 Status Response Type
     *
     * @param statusResponseType
     * @param resultingDocument
     * @return
     */
    void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException;

    /**
     * Shed all state
     *
     * @throws ProcessingException
     */
    void reset() throws ProcessingException;
}