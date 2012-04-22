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
package org.picketlink.identity.federation.web.handlers.saml2;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.impl.EmptyAttributeManager;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;

/**
 * <p>Handler dealing with attributes for SAML2</p>
 * <p>
 * <b>Configuration for handler:</b>
 * </p>
 * <p>
 * <ul>
 * <li>ATTRIBUTE_MANAGER:  a fqn of the attribute manager class. This is an IDP setting.</li>
 * <li>ATTRIBUTE_KEYS:  a comma separated list of string values representing attributes to be sent.  IDP setting.</li>
 * <li>ATTRIBUTE_CHOOSE_FRIENDLY_NAME : set to true if you require attributes to be keyed by friendly name rather than default name. SP Setting.</li>
 * </ul>
 * </p>
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2AttributeHandler extends BaseSAML2Handler
{
   private static Logger log = Logger.getLogger(SAML2AttributeHandler.class);

   private final boolean trace = log.isTraceEnabled();

   protected AttributeManager attribManager = new EmptyAttributeManager();

   protected List<String> attributeKeys = new ArrayList<String>();

   protected boolean chooseFriendlyName = false;

   @Override
   public void initChainConfig(SAML2HandlerChainConfig handlerChainConfig) throws ConfigurationException
   {
      super.initChainConfig(handlerChainConfig);
      Object config = this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION);
      if (config instanceof IDPType)
      {
         IDPType idpType = (IDPType) config;
         String attribStr = idpType.getAttributeManager();
         insantiateAttributeManager(attribStr);
      }
   }

   @Override
   public void initHandlerConfig(SAML2HandlerConfig handlerConfig) throws ConfigurationException
   {
      super.initHandlerConfig(handlerConfig);

      String attribStr = (String) this.handlerConfig.getParameter(GeneralConstants.ATTIBUTE_MANAGER);
      this.insantiateAttributeManager(attribStr);
      //Get a list of attributes we are interested in
      String attribList = (String) this.handlerConfig.getParameter(GeneralConstants.ATTRIBUTE_KEYS);
      if (StringUtil.isNotNull(attribList))
      {
         this.attributeKeys.addAll(StringUtil.tokenize(attribList));
      }

      String chooseFriendlyNameStr = (String) handlerConfig
            .getParameter(GeneralConstants.ATTRIBUTE_CHOOSE_FRIENDLY_NAME);
      if (StringUtil.isNotNull(chooseFriendlyNameStr))
      {
         chooseFriendlyName = Boolean.parseBoolean(chooseFriendlyNameStr);
      }
   }

   @SuppressWarnings("unchecked")
   public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException
   {
      //Do not handle log out request interaction
      if (request.getSAML2Object() instanceof LogoutRequestType)
         return;

      //only handle IDP side
      if (getType() == HANDLER_TYPE.SP)
         return;

      HTTPContext httpContext = (HTTPContext) request.getContext();
      HttpSession session = httpContext.getRequest().getSession(false);

      Principal userPrincipal = (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);
      Map<String, Object> attribs = (Map<String, Object>) session.getAttribute(GeneralConstants.ATTRIBUTES);
      if (attribs == null)
      {
         attribs = this.attribManager.getAttributes(userPrincipal, attributeKeys);
         session.setAttribute(GeneralConstants.ATTRIBUTES, attribs);
      }
   }

   @Override
   public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response)
         throws ProcessingException
   {
      //only handle SP side
      if (getType() == HANDLER_TYPE.IDP)
         return;
      handleIDPResponse(request);
   }

   private void insantiateAttributeManager(String attribStr) throws ConfigurationException
   {
      if (attribStr != null && !"".equals(attribStr))
      {
         try
         {
            attribManager = (AttributeManager) SecurityActions.loadClass(getClass(), attribStr).newInstance();
            if (trace)
               log.trace("AttributeManager set to " + this.attribManager);
         }
         catch (Exception e)
         {
            log.error("Exception initializing attribute manager:", e);
            throw new ConfigurationException();
         }
      }
   }

   @SuppressWarnings("unchecked")
   protected void handleIDPResponse(SAML2HandlerRequest request)
   {
      HTTPContext httpContext = (HTTPContext) request.getContext();
      HttpSession session = httpContext.getRequest().getSession(false);

      AssertionType assertion = (AssertionType) request.getOptions().get(GeneralConstants.ASSERTION);
      if (assertion == null)
         throw new RuntimeException(ErrorCodes.NULL_VALUE + "Assertion not found in the handler request:"
               + request.getOptions());
      Set<StatementAbstractType> statements = assertion.getStatements();
      for (StatementAbstractType statement : statements)
      {
         if (statement instanceof AttributeStatementType)
         {
            AttributeStatementType attrStat = (AttributeStatementType) statement;
            List<ASTChoiceType> attrs = attrStat.getAttributes();
            for (ASTChoiceType attrChoice : attrs)
            {
               AttributeType attr = attrChoice.getAttribute();
               Map<String, List<Object>> attrMap = (Map<String, List<Object>>) session
                     .getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP);
               if (attrMap == null)
               {
                  attrMap = new HashMap<String, List<Object>>();
                  session.setAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP, attrMap);
               }
               if (chooseFriendlyName)
               {
                  attrMap.put(attr.getFriendlyName(), attr.getAttributeValue());
               }
               else
               {
                  attrMap.put(attr.getName(), attr.getAttributeValue());
               }
            }
         }
      }
   }
}