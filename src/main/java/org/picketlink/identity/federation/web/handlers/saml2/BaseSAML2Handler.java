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
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;

/**
 * Base Class for SAML2 handlers
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
public abstract class BaseSAML2Handler implements SAML2Handler
{
   protected SAML2HandlerConfig handlerConfig = null;
   protected SAML2HandlerChainConfig handlerChainConfig = null;
   protected HANDLER_TYPE handlerType;
   
   /**
    * Initialize the handler
    * @param options
    */
   public void initHandlerConfig(SAML2HandlerConfig handlerConfig)
   throws ConfigurationException
   {
      this.handlerConfig = handlerConfig;
   }
   
   public void initChainConfig(SAML2HandlerChainConfig handlerChainConfig)
   throws ConfigurationException
   {
      this.handlerChainConfig = handlerChainConfig;
      Object config = this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION);
      if(config instanceof IDPType)
         this.handlerType = HANDLER_TYPE.IDP;
      else
         this.handlerType = HANDLER_TYPE.SP;
   }
   

   /**
    * Get the type of handler 
    * - handler at IDP or SP
    * @return
    */
   public HANDLER_TYPE getType()
   {
      return this.handlerType; 
   }
   
   public void reset() throws ProcessingException
   { 
   }
   
   /**
    * @see SAML2Handler#generateSAMLRequest(SAML2HandlerRequest, SAML2HandlerResponse)
    */
   public void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response)
   throws ProcessingException
   {
   }
   
   /**
    * @see {@code SAML2Handler#handleStatusResponseType(SAML2HandlerRequest, SAML2HandlerResponse)}
    */
   public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response)
         throws ProcessingException
   { 
   }
   
   
   public static HttpServletRequest getHttpRequest(SAML2HandlerRequest request)
   {
      HTTPContext context = (HTTPContext) request.getContext();
      return context.getRequest(); 
   }
   
   public static HttpSession getHttpSession(SAML2HandlerRequest request)
   {
      HTTPContext context = (HTTPContext) request.getContext();
      return context.getRequest().getSession(false); 
   }
}