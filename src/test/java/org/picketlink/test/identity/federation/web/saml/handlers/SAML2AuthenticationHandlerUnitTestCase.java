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

import static org.junit.Assert.assertEquals;

import java.security.KeyPair;
import java.security.Principal;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;

import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
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
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;
import org.w3c.dom.Document;

/**
 * Unit test the {@link SAML2AuthenticationHandler}
 * @author Anil.Saldhana@redhat.com
 * @since Feb 17, 2011
 */
public class SAML2AuthenticationHandlerUnitTestCase
{
   @Test
   public void handleNameIDCustomization() throws Exception
   {
      SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

      SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
      SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
      handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());

      Map<String, Object> chainOptions = new HashMap<String, Object>();
      SPType spType = new SPType();
      chainOptions.put(GeneralConstants.CONFIGURATION, spType);
      chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
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

      SAML2Object saml2Object = new SAML2Object()
      {
      };

      SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
      IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");

      SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
            SAML2Handler.HANDLER_TYPE.SP);
      request.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

      SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
      handler.generateSAMLRequest(request, response);

      Document samlReq = response.getResultingDocument();
      SAMLParser parser = new SAMLParser();
      AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(DocumentUtil.getNodeAsStream(samlReq));
      NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
      assertEquals(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get(), nameIDPolicy.getFormat().toString());
   }

   @Ignore
   @Test
   public void handleEncryptedAssertion() throws Exception
   {
      SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

      SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
      SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

      Map<String, Object> chainOptions = new HashMap<String, Object>();
      SPType spType = new SPType();
      chainOptions.put(GeneralConstants.CONFIGURATION, spType);
      chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
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

      SAML2Object saml2Object = new SAML2Object()
      {
      };

      KeyPair keypair = KeyStoreUtil.generateKeyPair("RSA");

      SAML2Response saml2Response = new SAML2Response();
      IssuerInfoHolder issuerInfoholder = new IssuerInfoHolder("testIssuer");

      AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), new NameIDType());
      SubjectType assertionSubject = new SubjectType();
      STSubType subType = new STSubType();
      NameIDType anil = new NameIDType();
      anil.setValue("anil");
      subType.addBaseID(anil);
      assertionSubject.setSubType(subType);
      assertion.setSubject(assertionSubject);

      ResponseType responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder,
            assertion);

      String assertionNS = JBossSAMLURIConstants.ASSERTION_NSURI.get();

      QName assertionQName = new QName(assertionNS, "EncryptedAssertion", "saml");
      Document responseDoc = saml2Response.convert(responseType);

      byte[] secret = WSTrustUtil.createRandomSecret(128 / 8);
      SecretKey secretKey = new SecretKeySpec(secret, "AES");

      PublicKey publicKey = keypair.getPublic();
      XMLEncryptionUtil.encryptElement(new QName(assertionNS, "Assertion", "saml"), responseDoc, publicKey, secretKey,
            128, assertionQName, true);

      SAMLParser parser = new SAMLParser();
      saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

      SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
      IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
      SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
            SAML2Handler.HANDLER_TYPE.SP);
      request.addOption(GeneralConstants.DECRYPTING_KEY, keypair.getPrivate());

      SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

      session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal()
      {
         public String getName()
         {
            return "Hi";
         }
      });

      handler.handleStatusResponseType(request, response);
   }
}