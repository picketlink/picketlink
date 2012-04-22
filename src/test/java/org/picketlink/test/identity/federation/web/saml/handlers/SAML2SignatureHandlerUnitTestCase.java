/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.config.IDPType;
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
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureGenerationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureValidationHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;
import org.w3c.dom.Document;

/**
 * Unit test the {@code SAML2SignatureHandler}
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2SignatureHandlerUnitTestCase extends TestCase
{
   public void testSignatures() throws Exception
   {
      SAML2Request saml2Request = new SAML2Request();
      String id = IDGenerator.create("ID_");
      String assertionConsumerURL = "http://sp";
      String destination = "http://idp";
      String issuerValue = "http://sp";
      AuthnRequestType authnRequest = saml2Request.createAuthnRequestType(id, assertionConsumerURL, destination,
            issuerValue);

      Document authDoc = saml2Request.convert(authnRequest);

      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      KeyPair keypair = kpg.genKeyPair();

      SAML2SignatureGenerationHandler handler = new SAML2SignatureGenerationHandler();

      SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
      SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

      Map<String, Object> chainOptions = new HashMap<String, Object>();
      IDPType idpType = new IDPType();
      chainOptions.put(GeneralConstants.CONFIGURATION, idpType);
      chainOptions.put(GeneralConstants.KEYPAIR, keypair);
      chainConfig.set(chainOptions);

      //Initialize the handler
      handler.initChainConfig(chainConfig);
      handler.initHandlerConfig(handlerConfig);

      //Create a Protocol Context
      MockHttpSession session = new MockHttpSession();
      MockServletContext servletContext = new MockServletContext();
      MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
      MockHttpServletResponse servletResponse = new MockHttpServletResponse();
      HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

      SAMLDocumentHolder docHolder = new SAMLDocumentHolder(authnRequest, authDoc);
      IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
      SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
            SAML2Handler.HANDLER_TYPE.IDP);
      request.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

      SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

      request.addOption(GeneralConstants.SENDER_PUBLIC_KEY, keypair.getPublic());

      SAML2AuthenticationHandler authHandler = new SAML2AuthenticationHandler();
      authHandler.initHandlerConfig(handlerConfig);
      authHandler.generateSAMLRequest(request, response);

      handler.generateSAMLRequest(request, response);
      Document signedDoc = response.getResultingDocument();

      assertNotNull("Signed Doc is not null", signedDoc);
      SAMLDocumentHolder signedHolder = new SAMLDocumentHolder(signedDoc);
      request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), signedHolder,
            SAML2Handler.HANDLER_TYPE.SP);

      request.addOption(GeneralConstants.SENDER_PUBLIC_KEY, keypair.getPublic());

      SAML2SignatureValidationHandler validHandler = new SAML2SignatureValidationHandler();
      validHandler.initChainConfig(chainConfig);
      validHandler.initHandlerConfig(handlerConfig);

      validHandler.handleStatusResponseType(request, response);
   }
}