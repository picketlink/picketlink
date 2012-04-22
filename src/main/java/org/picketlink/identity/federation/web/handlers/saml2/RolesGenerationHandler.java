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
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.impl.EmptyRoleGenerator;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;

/**
 * Handles the generation of roles on the IDP Side
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
public class RolesGenerationHandler extends BaseSAML2Handler
{
   private static Logger log = Logger.getLogger(RolesGenerationHandler.class);

   private final boolean trace = log.isTraceEnabled();

   private transient RoleGenerator roleGenerator = new EmptyRoleGenerator();

   @Override
   public void initChainConfig(SAML2HandlerChainConfig handlerChainConfig) throws ConfigurationException
   {
      super.initChainConfig(handlerChainConfig);
      Object config = this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION);
      if (config instanceof IDPType)
      {
         IDPType idpType = (IDPType) config;
         String roleGeneratorString = idpType.getRoleGenerator();
         this.insantiateRoleValidator(roleGeneratorString);
      }
   }

   @Override
   public void initHandlerConfig(SAML2HandlerConfig handlerConfig) throws ConfigurationException
   {
      super.initHandlerConfig(handlerConfig);
      String roleGeneratorString = (String) this.handlerConfig.getParameter(GeneralConstants.ATTIBUTE_MANAGER);
      this.insantiateRoleValidator(roleGeneratorString);
   }

   /**
    * @see {@code SAML2Handler#handleRequestType(SAML2HandlerRequest, SAML2HandlerResponse)}
    */
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
      List<String> roles = (List<String>) session.getAttribute(GeneralConstants.ROLES_ID);

      if (roles == null)
      {
         roles = roleGenerator.generateRoles(userPrincipal);
         session.setAttribute(GeneralConstants.ROLES_ID, roles);
      }
      response.setRoles(roles);
   }

   private void insantiateRoleValidator(String attribStr) throws ConfigurationException
   {
      if (attribStr != null && !"".equals(attribStr))
      {
         try
         {
            Class<?> clazz = SecurityActions.loadClass(getClass(), attribStr);
            roleGenerator = (RoleGenerator) clazz.newInstance();
            if (trace)
               log.trace("RoleGenerator set to " + this.roleGenerator);
         }
         catch (Exception e)
         {
            log.error("Exception initializing role generator:", e);
            throw new ConfigurationException();
         }
      }
   }
}