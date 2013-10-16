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
import org.junit.Assert;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.fed.IssuerNotTrustedException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.TrustType;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2IssuerTrustHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SAMLIssuerTrustHandlerUnitTestCase extends TestCase {

    public void testIssuer() throws Exception {
        SAML2IssuerTrustHandler issuerTrustHandler = new SAML2IssuerTrustHandler();

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        // Create chainConfig for IDP
        TrustType trustType = new TrustType();
        Map<String, Object> chainOptionsIdp = new HashMap<String, Object>();
        IDPType idpType = new IDPType();
        idpType.setTrust(trustType);
        chainOptionsIdp.put(GeneralConstants.CONFIGURATION, idpType);
        SAML2HandlerChainConfig chainConfigIdp = new DefaultSAML2HandlerChainConfig(chainOptionsIdp);
        issuerTrustHandler.initChainConfig(chainConfigIdp);

        // Create documentHolder
        NameIDType issuer = new NameIDType();
        AuthnRequestType authnRequestType = new AuthnRequestType("ID_123456789", null);
        authnRequestType.setIssuer(issuer);
        SAMLDocumentHolder documentHolder = new SAMLDocumentHolder(authnRequestType);

        // Create request and response
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, null, documentHolder,
                SAML2Handler.HANDLER_TYPE.IDP);
        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        // Test localhost
        issuer.setValue("http://localhost:8080/sales");
        trustType.setDomains("localhost,google.com,somedomain.com");
        issuerTrustHandler.handleRequestType(request, response);

        // Test somedomain
        issuer.setValue("http://www.somedomain.com:8080/sales/");
        issuerTrustHandler.handleRequestType(request, response);

        // Test non-trusted domain
        try {
            issuer.setValue("http://www.evil.com:8080/sales/");
            issuerTrustHandler.handleRequestType(request, response);

            fail("www.evil.com is non-trusted domain");
        } catch (ProcessingException pe) {
            Assert.assertEquals(pe.getCause().getClass(), IssuerNotTrustedException.class);
        }

        // Test google.com
        issuer.setValue("google.com");
        issuerTrustHandler.handleRequestType(request, response);

        issuer.setValue("google.com/a/mposolda1.com");
        issuerTrustHandler.handleRequestType(request, response);
    }

}
