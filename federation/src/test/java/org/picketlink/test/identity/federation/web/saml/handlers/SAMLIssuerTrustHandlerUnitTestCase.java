/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.test.identity.federation.web.saml.handlers;

import junit.framework.TestCase;
import org.junit.Assert;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
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
        }
        catch (ProcessingException pe) {
            Assert.assertEquals(pe.getCause().getClass(), IssuerNotTrustedException.class);
        }

        // Test google.com
        issuer.setValue("google.com");
        issuerTrustHandler.handleRequestType(request, response);

        issuer.setValue("google.com/a/mposolda1.com");
        issuerTrustHandler.handleRequestType(request, response);
    }

}
