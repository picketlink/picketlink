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
import static org.junit.Assert.assertNotNull;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.constants.X500SAMLProfileConstants;
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
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AttributeHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;

/**
 * Unit test the {@code SAML2AttributeHandler} 
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2AttributeHandlerUnitTestCase
{
   private static String name = "anil";

   private static String email = "anil@test";

   @SuppressWarnings("unchecked")
   @Test
   public void testAttributes() throws Exception
   {
      SAML2AttributeHandler handler = new SAML2AttributeHandler();

      SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
      SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

      Map<String, Object> chainOptions = new HashMap<String, Object>();
      IDPType idpType = new IDPType();
      idpType.setAttributeManager(TestAttributeManager.class.getName());
      chainOptions.put(GeneralConstants.CONFIGURATION, idpType);
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
            SAML2Handler.HANDLER_TYPE.IDP);
      SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

      session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal()
      {
         public String getName()
         {
            return name;
         }
      });
      handler.handleRequestType(request, response);

      Map<String, Object> attribs = (Map<String, Object>) session.getAttribute(GeneralConstants.ATTRIBUTES);
      assertNotNull("Attributes are not null", attribs);
      assertEquals(email, attribs.get(X500SAMLProfileConstants.EMAIL.getFriendlyName()));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testAttribsOnSP() throws Exception
   {
      SAML2AttributeHandler handler = new SAML2AttributeHandler();

      SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
      SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

      Map<String, Object> chainOptions = new HashMap<String, Object>();
      SPType spType = new SPType();
      chainOptions.put(GeneralConstants.CONFIGURATION, spType);
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
            SAML2Handler.HANDLER_TYPE.IDP);
      SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

      AssertionType assertion = new AssertionType(IDGenerator.create("ID_"), XMLTimeUtil.getIssueInstant());

      Map<String, Object> myattr = new HashMap<String, Object>();
      myattr.put("testKey", "hello");
      AttributeStatementType attState = StatementUtil.createAttributeStatement(myattr);
      assertion.addStatement(attState);

      request.addOption(GeneralConstants.ASSERTION, assertion);
      handler.handleStatusResponseType(request, response);

      Map<String, List<Object>> sessionMap = (Map<String, List<Object>>) session
            .getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP);
      assertNotNull(sessionMap);
      List<Object> values = sessionMap.get("testKey");
      assertEquals("hello", values.get(0));
   }

   public static class TestAttributeManager implements AttributeManager
   {
      public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys)
      {
         Map<String, Object> attribs = new HashMap<String, Object>();

         if (name.equals(userPrincipal.getName()))
         {
            attribs.put(X500SAMLProfileConstants.EMAIL.getFriendlyName(), email);
         }
         return attribs;
      }
   }
}