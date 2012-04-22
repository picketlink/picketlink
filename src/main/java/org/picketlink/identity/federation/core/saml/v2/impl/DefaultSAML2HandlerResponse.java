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

import java.util.ArrayList;
import java.util.List;

import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.w3c.dom.Document;

/**
 * Default implementation of the SAML2 Handler response
 * @author Anil.Saldhana@redhat.com
 * @since Oct 1, 2009
 */
public class DefaultSAML2HandlerResponse implements SAML2HandlerResponse
{
   private Document document;
   private String relayState;
   private List<String> roles = new ArrayList<String>();
   private String destination;
   private int errorCode;
   private String errorMessage;
   private boolean errorMode;
   private boolean sendRequest;
   
   private boolean postBinding = true;
   
   

   /**
    * @see SAML2HandlerResponse#getRelayState()
    */
   public String getRelayState()
   {
      return this.relayState;
   }
   
   /**
    * @see SAML2HandlerResponse#getResultingDocument()
    */
   public Document getResultingDocument()
   {
      return this.document;
   }

   /**
    * @see SAML2HandlerResponse#setRelayState(String)
    */
   public void setRelayState(String relayState)
   {
      this.relayState= relayState;
   }

   /**
    * @see SAML2HandlerResponse#setResultingDocument(Document)
    */
   public void setResultingDocument(Document doc)
   {
      this.document = doc;
   }

   /**
    * @see SAML2HandlerResponse#getRoles()
    */
   public List<String> getRoles()
   {
      return this.roles ;
   }
   
   /**
    * @see SAML2HandlerResponse#setRoles(List)
    */
   public void setRoles(List<String> roles)
   {
      this.roles.addAll(roles);
   }

   /**
    * @see SAML2HandlerResponse#getDestination()
    */
   public String getDestination()
   {
      return this.destination;
   }

   /**
    * @see SAML2HandlerResponse#setDestination(String)
    */
   public void setDestination(String destination)
   {
      this.destination = destination;  
   }

   /**
    * @see SAML2HandlerResponse#getErrorCode()
    */
   public int getErrorCode()
   {
      return this.errorCode;
   }

   /**
    * @see SAML2HandlerResponse#getErrorMessage()
    */
   public String getErrorMessage()
   {
      return this.errorMessage;
   }

   /**
    * @see SAML2HandlerResponse#setError(int, String)
    */
   public void setError(int errorCode, String errorMessage)
   {
      this.errorCode = errorCode;
      this.errorMessage = errorMessage;
      
      this.errorMode = true;
   }

   /**
    * @see SAML2HandlerResponse#isInError()
    */
   public boolean isInError()
   {
      return this.errorMode;
   }

   /**
    * @see SAML2HandlerResponse#getSendRequest()
    */
   public boolean getSendRequest()
   { 
      return this.sendRequest;
   }
   
   /**
    * @see SAML2HandlerResponse#setSendRequest(boolean)
    */
   public void setSendRequest(boolean request)
   { 
      this.sendRequest = request;
   }

   /**
    * @see SAML2HandlerResponse#setPostBindingForResponse(boolean)
    */
   public void setPostBindingForResponse(boolean postB)
   {
      this.postBinding = postB;
   }
 
   /**
    * @see SAML2HandlerResponse#isPostBindingForResponse()
    */
   public boolean isPostBindingForResponse()
   { 
      return this.postBinding;
   }
}