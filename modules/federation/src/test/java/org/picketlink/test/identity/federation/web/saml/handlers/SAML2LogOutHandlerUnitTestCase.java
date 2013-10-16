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
package org.picketlink.test.identity.federation.web.saml.handlers;

import org.junit.Test;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2LogOutHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test the {@link SAML2LogoutHandler}
 *
 * @author Anil Saldhana
 * @since June 03, 2013
 */
public class SAML2LogOutHandlerUnitTestCase {

    @Test
    public void handleIDPResponseWithSAMLResponderStatus() throws Exception {
        SAML2LogOutHandler handler = new SAML2LogOutHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        ProviderType spType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        StatusResponseType srt = getIDPStatusResponseForSAMLResponder();
        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(srt, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");

        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        request.setTypeOfRequestToBeGenerated(SAML2HandlerRequest.GENERATE_REQUEST_TYPE.AUTH);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
        handler.handleStatusResponseType(request, response);
    }

    private StatusResponseType getIDPStatusResponseForSAMLResponder() throws Exception {
        String value = "<samlp:LogoutResponse xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"\n" +
                "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n" +
                " Destination=\"http:/xyz:8080/sales-metadata/logout.jsp\"\n" +
                "ID=\"idHK5-YHIb34t7v5PDMrbXPsFPyc8\"\n" +
                " InResponseTo=\"ID_5d5e5607-0d03-4b84-9c44-50a1744f144a\" IssueInstant=\"2013-05-31T13:10:05Z\" Version=\"2.0\">i\n" +
                "<saml:Issuer>https:/idp/saml2/metadata</saml:Issuer>\n" +
                "<samlp:Status><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Responder\"/></samlp:Status></samlp:LogoutResponse>";
        ByteArrayInputStream bis = new ByteArrayInputStream(value.getBytes());
        SAMLParser parser = new SAMLParser();
        return (StatusResponseType) parser.parse(bis);
    }
}