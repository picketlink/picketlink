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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
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
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.handlers.saml2.BaseSAML2Handler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2InResponseToVerificationHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;
import org.w3c.dom.Document;

/**
 * Unit test the {@link org.picketlink.identity.federation.web.handlers.saml2.SAML2InResponseToVerificationHandler}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SAML2InResponseToVerificationHandlerUnitTestCase extends TestCase
{

   public void testResponseIdVerification() throws Exception
   {
      // 1) CONFIGURATION AND INITIALIZATION OF TEST

      // Create handlers
      SAML2AuthenticationHandler authenticationHandler = new SAML2AuthenticationHandler();
      SAML2InResponseToVerificationHandler verificationHandler = new SAML2InResponseToVerificationHandler();

      // Create configuration for handlers
      SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
      SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
      handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
      handlerConfig.addParameter(SAML2Handler.DISABLE_SENDING_ROLES, "true");

      Map<String, Object> chainOptions = new HashMap<String, Object>();
      SPType spType = new SPType();
      chainOptions.put(GeneralConstants.CONFIGURATION, spType);
      chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
      chainConfig.set(chainOptions);

      // Initialize the handlers
      authenticationHandler.initChainConfig(chainConfig);
      authenticationHandler.initHandlerConfig(handlerConfig);
      verificationHandler.initChainConfig(chainConfig);
      verificationHandler.initHandlerConfig(handlerConfig);

      // Create a Protocol Context
      MockHttpSession session = new MockHttpSession();
      MockServletContext servletContext = new MockServletContext();
      MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
      MockHttpServletResponse servletResponse = new MockHttpServletResponse();
      HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

      // Create handler request and response
      IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/sales/");
      SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), null,
            SAML2Handler.HANDLER_TYPE.SP);
      request.setTypeOfRequestToBeGenerated(SAML2HandlerRequest.GENERATE_REQUEST_TYPE.AUTH);
      SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

      // 2) GENERATE SAML AUTHENTICATION REQUEST

      // Generate SAML AuthnRequest with handlers
      authenticationHandler.generateSAMLRequest(request, response);
      verificationHandler.generateSAMLRequest(request, response);

      // Parse document and verify that ID is saved in Http session
      Document samlReqDoc = response.getResultingDocument();
      SAMLParser parser = new SAMLParser();
      AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(DocumentUtil.getNodeAsStream(samlReqDoc));
      assertEquals(authnRequest.getID(), servletRequest.getSession().getAttribute(GeneralConstants.AUTH_REQUEST_ID));

      // 3) SEND SAML AUTHENTICATION REQUEST TO IDP

      // Generate request and response for IDP
      SAML2HandlerResponse handlerResponseFromIdp = sendRequestToIdp(authnRequest, samlReqDoc, httpContext,
            handlerConfig);

      // Parse SAML response from IDP
      Document doc2response = handlerResponseFromIdp.getResultingDocument();
      assertNotNull(doc2response);
      String responseString = DocumentUtil.asString(doc2response);

      // 4) PROCESS SAML RESPONSE FROM IDP. VERIFICATION OF InResponseId SHOULD BE SUCCESSFUL

      HandlerContext handlerContext = getHandlerRequestAndResponse(httpContext, issuerInfo, responseString);

      // Assert that ID from session is not null
      String inResponseIdFromSession = (String) servletRequest.getSession().getAttribute(
            GeneralConstants.AUTH_REQUEST_ID);
      assertNotNull(inResponseIdFromSession);

      // Handle response from IDP
      authenticationHandler.handleStatusResponseType(handlerContext.request, handlerContext.response);
      verificationHandler.handleStatusResponseType(handlerContext.request, handlerContext.response);

      // Verify that Id is not in session anymore. Becaue it was removed by SAML2ResponseIdVerificationHandler
      assertNull(servletRequest.getSession().getAttribute(GeneralConstants.AUTH_REQUEST_ID));

      // 5) CHANGE InResponseId IN SAML RESPONSE. VALIDATION MUST FAIL NOW.

      // Change InResponseId
      String responseStringChangedId = responseString.replaceAll("InResponseTo=\"" + inResponseIdFromSession + "\"",
            "InResponseTo=\"ID_101dcb5e-f432-4f45-87cb-47daff92edef\"");
      HandlerContext handlerContextChangedId = getHandlerRequestAndResponse(httpContext, issuerInfo,
            responseStringChangedId);

      // Set Id to session again as it was removed in previous processing
      servletRequest.getSession().setAttribute(GeneralConstants.AUTH_REQUEST_ID, inResponseIdFromSession);

      // Handle response with changed Id. This time it should fail
      try
      {
         authenticationHandler.handleStatusResponseType(handlerContextChangedId.request,
               handlerContextChangedId.response);
         verificationHandler
               .handleStatusResponseType(handlerContextChangedId.request, handlerContextChangedId.response);

         fail("Verification of InResponseTo should fail.");
      }
      catch (ProcessingException pe)
      {
         assertEquals(ErrorCodes.AUTHN_REQUEST_ID_VERIFICATION_FAILED, pe.getMessage());
      }

      // 6) REMOVE InResponseId FROM SAML RESPONSE. VALIDATION MUST FAIL NOW.

      // Remove inResponseId
      String responseStringRemovedId = responseString
            .replaceAll("InResponseTo=\"" + inResponseIdFromSession + "\"", "");
      HandlerContext handlerContextRemovedId = getHandlerRequestAndResponse(httpContext, issuerInfo,
            responseStringRemovedId);

      // Set Id to session again as it was removed in previous processing
      servletRequest.getSession().setAttribute(GeneralConstants.AUTH_REQUEST_ID, inResponseIdFromSession);

      // Now handle again response from IDP. This time it should also fail as InResponseTo is null
      try
      {
         authenticationHandler.handleStatusResponseType(handlerContextRemovedId.request,
               handlerContextRemovedId.response);
         verificationHandler
               .handleStatusResponseType(handlerContextRemovedId.request, handlerContextRemovedId.response);

         fail("Verification of InResponseTo should fail.");
      }
      catch (ProcessingException pe)
      {
         assertEquals(ErrorCodes.AUTHN_REQUEST_ID_VERIFICATION_FAILED, pe.getMessage());
      }
   }

   /**
    * Sending SAML Request to IDP and receiving SAML response.
    *
    * @param authnRequest Generated SAML Request object
    * @param samlReqDoc Document for generated SAML Request object
    * @param httpContext httpContext
    * @param handlerConfig handlerConfig
    * @return SAML2HandlerResponse after receiving response from IDP
    * @throws Exception
    */
   private SAML2HandlerResponse sendRequestToIdp(AuthnRequestType authnRequest, Document samlReqDoc,
         HTTPContext httpContext, SAML2HandlerConfig handlerConfig) throws Exception
   {
      // Generate handler request and handler response for IDP
      IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
      SAMLDocumentHolder docHolder = new SAMLDocumentHolder(authnRequest, samlReqDoc);
      SAML2HandlerRequest idpHandlerRequest = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(),
            docHolder, SAML2Handler.HANDLER_TYPE.IDP);
      idpHandlerRequest.addOption(GeneralConstants.ASSERTIONS_VALIDITY, 10000l);
      SAML2HandlerResponse idpHandlerResponse = new DefaultSAML2HandlerResponse();

      // Create chainConfig for IDP
      Map<String, Object> chainOptionsIdp = new HashMap<String, Object>();
      IDPType idpType = new IDPType();
      chainOptionsIdp.put(GeneralConstants.CONFIGURATION, idpType);
      chainOptionsIdp.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
      SAML2HandlerChainConfig chainConfigIdp = new DefaultSAML2HandlerChainConfig(chainOptionsIdp);

      // Create and init handlers for IDP
      SAML2AuthenticationHandler authenticationHandlerIdp = new SAML2AuthenticationHandler();
      SAML2InResponseToVerificationHandler verificationHandlerIdp = new SAML2InResponseToVerificationHandler();
      authenticationHandlerIdp.initChainConfig(chainConfigIdp);
      authenticationHandlerIdp.initHandlerConfig(handlerConfig);
      verificationHandlerIdp.initChainConfig(chainConfigIdp);
      verificationHandlerIdp.initHandlerConfig(handlerConfig);

      HttpSession session = BaseSAML2Handler.getHttpSession(idpHandlerRequest);
      session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal()
      {
         public String getName()
         {
            return "testPrincipal";
         }
      });

      // Init Picketlink Core STS
      PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
      sts.installDefaultConfiguration();

      // Init identityServer
      IdentityServer identityServer = new IdentityServer();
      httpContext.getServletContext().setAttribute(GeneralConstants.IDENTITY_SERVER, identityServer);

      // Handle request by IDP
      authenticationHandlerIdp.handleRequestType(idpHandlerRequest, idpHandlerResponse);
      verificationHandlerIdp.handleRequestType(idpHandlerRequest, idpHandlerResponse);

      return idpHandlerResponse;
   }

   private ResponseType getResponseTypeFromString(String responseString) throws Exception
   {
      InputStream is = new ByteArrayInputStream(responseString.getBytes());
      SAML2Response saml2Response = new SAML2Response();
      return saml2Response.getResponseType(is);
   }

   private HandlerContext getHandlerRequestAndResponse(HTTPContext httpContext, IssuerInfoHolder issuerInfo,
         String responseString) throws Exception
   {
      ResponseType responseType = getResponseTypeFromString(responseString);
      SAML2Response saml2Response = new SAML2Response();
      Document doc = saml2Response.convert(responseType);
      SAMLDocumentHolder docHolder = new SAMLDocumentHolder(responseType, doc);

      SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
            SAML2Handler.HANDLER_TYPE.SP);
      SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
      return new HandlerContext(request, response);
   }

   private class HandlerContext
   {
      private final SAML2HandlerRequest request;

      private final SAML2HandlerResponse response;

      private HandlerContext(SAML2HandlerRequest request, SAML2HandlerResponse response)
      {
         this.request = request;
         this.response = response;
      }
   }

}
