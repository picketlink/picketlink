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

import java.util.Map;

import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.saml.v2.SAML2Object; 
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.w3c.dom.Document;

/**
 * Request for {@code SAML2Handler}
 * @author Anil.Saldhana@redhat.com
 * @since Sep 25, 2009
 */
public interface SAML2HandlerRequest
{ 
   public enum GENERATE_REQUEST_TYPE
   {
      AUTH,LOGOUT;   
   };
   
   /**
    * Holder of transport context such
    * as HTTP
    * @return
    */
   ProtocolContext getContext();
   
   /**
    * The SAML2 Request
    * @return
    */
   SAML2Object getSAML2Object();
   
   /**
    * Get the request as a DOM
    * @return
    */
   Document getRequestDocument();
   
   /**
    * Return the type of SAML request
    * that needs to be generated at the handler
    * @return
    */
   GENERATE_REQUEST_TYPE getTypeOfRequestToBeGenerated();
   
   /**
    * set the type of SAML request
    * that needs to be generated at the handler
    * @return
    */
    void setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE grt);
   
   /**
    * Get the Issuer (SP or IDP) where
    * the handler chain is currently processing
    * @return
    */
   NameIDType getIssuer();
   
   /**
    * Set the relay state that was part of the interaction
    * @param relayState
    */
   void setRelayState(String relayState);
   
   /**
    * Get the RelayState that was part of the interaction
    * @return
    */
   String getRelayState();
   
   /**
    * Add an option
    * @param key
    * @param option
    */
   void addOption(String key, Object option);
   
   /**
    * Configure options
    * @param options
    */
   void setOptions(Map<String, Object> options);
   
   /**
    * Get the configured options
    * @return
    */
   Map<String, Object> getOptions(); 
}