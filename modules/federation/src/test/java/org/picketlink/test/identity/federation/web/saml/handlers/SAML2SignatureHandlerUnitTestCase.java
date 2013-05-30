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

import junit.framework.TestCase;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
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
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest.GENERATE_REQUEST_TYPE;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureGenerationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureValidationHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;
import org.w3c.dom.Document;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test the {@code SAML2SignatureHandler}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2SignatureHandlerUnitTestCase extends TestCase {

    public void testSignaturesPostBinding() throws Exception {
        doSignatureTest(true);
    }

    public void testSignaturesRedirectBinding() throws Exception {
        doSignatureTest(false);
    }

    private void doSignatureTest(boolean isPostBinding) throws Exception {
        SAML2Request saml2Request = new SAML2Request();
        String id = IDGenerator.create("ID_");
        String assertionConsumerURL = "http://sp";
        String destination = "http://idp";
        String issuerValue = "http://sp";
        AuthnRequestType authnRequest = saml2Request.createAuthnRequestType(id, assertionConsumerURL, destination, issuerValue);

        Document authDoc = saml2Request.convert(authnRequest);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPair keypair = kpg.genKeyPair();

        SAML2SignatureGenerationHandler handler = new SAML2SignatureGenerationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        SPType idpType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, idpType);
        chainOptions.put(GeneralConstants.KEYPAIR, keypair);
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = new MockServletContext();
        String httpMethod = isPostBinding ? "POST" : "GET";
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, httpMethod);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(authnRequest, authDoc);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.IDP);
        request.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
        response.setPostBindingForResponse(isPostBinding);

        request.addOption(GeneralConstants.SENDER_PUBLIC_KEY, keypair.getPublic());

        SAML2AuthenticationHandler authHandler = new SAML2AuthenticationHandler();
        authHandler.initChainConfig(chainConfig);
        authHandler.initHandlerConfig(handlerConfig);
        authHandler.generateSAMLRequest(request, response);

        handler.generateSAMLRequest(request, response);
        Document signedDoc = response.getResultingDocument();

        assertNotNull("Signed Doc is not null", signedDoc);
        SAMLDocumentHolder signedHolder = new SAMLDocumentHolder(signedDoc);
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), signedHolder,
                SAML2Handler.HANDLER_TYPE.SP);

        request.addOption(GeneralConstants.SENDER_PUBLIC_KEY, keypair.getPublic());

        if (!isPostBinding) {
            servletRequest.setQueryString(response.getDestinationQueryStringWithSignature());
        }

        SAML2SignatureValidationHandler validHandler = new SAML2SignatureValidationHandler();
        validHandler.initChainConfig(chainConfig);
        validHandler.initHandlerConfig(handlerConfig);

        validHandler.handleStatusResponseType(request, response);
    }
}