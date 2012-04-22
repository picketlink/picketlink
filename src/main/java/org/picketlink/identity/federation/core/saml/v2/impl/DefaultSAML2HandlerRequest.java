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
package org.picketlink.identity.federation.core.saml.v2.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler.HANDLER_TYPE;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.w3c.dom.Document;

/**
 * Default SAML2HandlerRequest
 * @author Anil.Saldhana@redhat.com
 * @since Oct 1, 2009
 */
public class DefaultSAML2HandlerRequest implements SAML2HandlerRequest
{
   private ProtocolContext protocolContext = null;
   private NameIDType issuer;
   private SAMLDocumentHolder documentHolder; ;
   private HANDLER_TYPE handlerType;
   private Map<String,Object> options = new HashMap<String,Object>();
   private GENERATE_REQUEST_TYPE generateRequestType;
   private String relayState;
   
   public DefaultSAML2HandlerRequest(ProtocolContext protocolContext,
         NameIDType issuer, SAMLDocumentHolder samlDocumentHolder,
         HANDLER_TYPE handlerType)
   {
      this.protocolContext = protocolContext;
      this.issuer = issuer;
      this.documentHolder = samlDocumentHolder; 
      this.handlerType = handlerType;
   }
   
   public void setOptions(Map<String,Object> options)
   {
      this.options = options;
   }

   /**
    * @see SAML2HandlerRequest#getContext()
    */
   public ProtocolContext getContext()
   {
      return this.protocolContext;
   }
   /**
    * @see SAML2HandlerRequest#getIssuer()
    */
   public NameIDType getIssuer()
   {
      return this.issuer;
   }
   /**
    * @see SAML2HandlerRequest#getSAML2Object()
    */
   public SAML2Object getSAML2Object()
   {
      return (SAML2Object) this.documentHolder.getSamlObject();
   }
   /**
    * @see SAML2HandlerRequest#getType()
    */
   public HANDLER_TYPE getType()
   {
      return handlerType;
   }


   /**
    * @see {@code SAML2HandlerRequest#addOption(String, Object)}
    */
   public void addOption(String key, Object option)
   {
      this.options.put(key, option); 
   }
   
   /**
    * @see SAML2HandlerRequest#getOptions()
    */
   public Map<String, Object> getOptions()
   {
      return Collections.unmodifiableMap(this.options);
   }

   /**
    * Set the type of saml2 request that need to be generated
    * by the handler
    * @param grt
    */
   public void setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE grt)
   {
      this.generateRequestType = grt;
   }
   
   /**
    * @see SAML2HandlerRequest#getTypeOfRequestToBeGenerated()
    */
   public GENERATE_REQUEST_TYPE getTypeOfRequestToBeGenerated()
   {
      return this.generateRequestType;
   }

   /**
    * @see SAML2HandlerRequest#getRelayState()
    */
   public String getRelayState()
   {
      return this.relayState;
   }
   
   public void setRelayState(String relay)
   {
      this.relayState = relay;
   }

   public Document getRequestDocument()
   {
      return this.documentHolder.getSamlDocument();
   }

}