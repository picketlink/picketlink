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
package org.picketlink.identity.federation.core.saml.v2.interfaces;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;

/**
 * Handle SAML2 Request types and status response types
 * @author Anil.Saldhana@redhat.com
 * @since Sep 17, 2009
 */
public interface SAML2Handler
{
   //Define some constants
   String ASSERTION_CONSUMER_URL = "ASSERTION_CONSUMER_URL";

   String CLOCK_SKEW_MILIS = "CLOCK_SKEW_MILIS";

   String DISABLE_AUTHN_STATEMENT = "DISABLE_AUTHN_STATEMENT";

   String DISABLE_SENDING_ROLES = "DISABLE_SENDING_ROLES";

   String DISABLE_ROLE_PICKING = "DISABLE_ROLE_PICKING";

   String ROLE_KEY = "ROLE_KEY";

   /**
    * Processing Point - idp side 
    * or service side
    */
   public enum HANDLER_TYPE {
      IDP, SP;
   };

   /**
    * Initialize the handler
    * @param handlerConfig Handler Config
    */
   void initChainConfig(SAML2HandlerChainConfig handlerChainConfig) throws ConfigurationException;

   /**
    * Initialize the handler from configuration
    * @param options
    */
   void initHandlerConfig(SAML2HandlerConfig handlerConfig) throws ConfigurationException;

   /**
    * Generate a SAML Request to be sent to the IDP
    * if the handler is invoked at the SP and vice-versa
    * @param request
    * @param response
    * @throws ProcessingException
    */
   void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException;

   /**
    * Get the type of handler 
    * - handler at IDP or SP
    * @return
    */
   HANDLER_TYPE getType();

   /**
    * Handle a SAML2 RequestAbstractType
    * @param requestAbstractType
    * @param resultingDocument
    * @return
    */
   void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException;

   /**
    * Handle a SAML2 Status Response Type
    * @param statusResponseType
    * @param resultingDocument
    * @return
    */
   void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException;

   /**
    * Shed all state
    * @throws ProcessingException
    */
   void reset() throws ProcessingException;
}