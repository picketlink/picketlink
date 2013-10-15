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

import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * Request for {@code SAML2Handler}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Sep 25, 2009
 */
public interface SAML2HandlerRequest {

    public enum GENERATE_REQUEST_TYPE {
        AUTH, LOGOUT;
    }

    ;

    /**
     * Holder of transport context such as HTTP
     *
     * @return
     */
    ProtocolContext getContext();

    /**
     * The SAML2 Request
     *
     * @return
     */
    SAML2Object getSAML2Object();

    /**
     * Get the request as a DOM
     *
     * @return
     */
    Document getRequestDocument();

    /**
     * Return the type of SAML request that needs to be generated at the handler
     *
     * @return
     */
    GENERATE_REQUEST_TYPE getTypeOfRequestToBeGenerated();

    /**
     * set the type of SAML request that needs to be generated at the handler
     *
     * @return
     */
    void setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE grt);

    /**
     * Get the Issuer (SP or IDP) where the handler chain is currently processing
     *
     * @return
     */
    NameIDType getIssuer();

    /**
     * Set the relay state that was part of the interaction
     *
     * @param relayState
     */
    void setRelayState(String relayState);

    /**
     * Get the RelayState that was part of the interaction
     *
     * @return
     */
    String getRelayState();

    /**
     * Add an option
     *
     * @param key
     * @param option
     */
    void addOption(String key, Object option);

    /**
     * Configure options
     *
     * @param options
     */
    void setOptions(Map<String, Object> options);

    /**
     * Get the configured options
     *
     * @return
     */
    Map<String, Object> getOptions();
}