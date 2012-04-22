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
package org.picketlink.identity.federation.web.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.servlet.http.HttpServletResponse;

import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil.RedirectBindingUtilDestHolder;
import org.w3c.dom.Document;

/**
 * Utility Class to handle processing of
 * an SAML Request Message
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */
public class ServiceProviderSAMLRequestProcessor extends ServiceProviderBaseProcessor
{ 
   /**
    * Construct
    * @param postBinding Whether it is the Post Binding
    * @param serviceURL Service URL of the SP
    */
   public ServiceProviderSAMLRequestProcessor(boolean postBinding, String serviceURL)
   {
      super(postBinding,serviceURL);
   }

   /**
    * Process the message
    * @param samlRequest
    * @param httpContext
    * @param handlers
    * @param chainLock A Lock on the chain of handlers that needs to be used for locking
    * @return
    * @throws ProcessingException
    * @throws IOException
    * @throws ParsingException
    * @throws ConfigurationException
    */
   public boolean process(String samlRequest, HTTPContext httpContext,
         Set<SAML2Handler> handlers, Lock chainLock) 
   throws ProcessingException, IOException, ParsingException, ConfigurationException
   {
      SAML2Request saml2Request = new SAML2Request();
      SAML2HandlerResponse saml2HandlerResponse = null;
      SAML2Object samlObject = null;
      SAMLDocumentHolder documentHolder = null;
      
      if(this.postBinding)
      {         
         //we got a logout request from IDP
         InputStream is = PostBindingUtil.base64DecodeAsStream(samlRequest); 
         samlObject = saml2Request.getSAML2ObjectFromStream(is);
      }
      else
      {
         InputStream is = RedirectBindingUtil.base64DeflateDecode(samlRequest);
         samlObject = saml2Request.getSAML2ObjectFromStream(is);  
      }

      documentHolder = saml2Request.getSamlDocumentHolder(); 
      
      //Create the request/response
      SAML2HandlerRequest saml2HandlerRequest = getSAML2HandlerRequest(documentHolder, httpContext); 
      saml2HandlerResponse = new DefaultSAML2HandlerResponse(); 

      SAMLHandlerChainProcessor chainProcessor = new SAMLHandlerChainProcessor(handlers);

      chainProcessor.callHandlerChain(samlObject, saml2HandlerRequest, 
            saml2HandlerResponse, httpContext, chainLock); 

      Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
      String relayState = saml2HandlerResponse.getRelayState();

      String destination = saml2HandlerResponse.getDestination();

      boolean willSendRequest = saml2HandlerResponse.getSendRequest(); 

      if(destination != null && 
            samlResponseDocument != null)
      {
         if(postBinding)
         {
            sendRequestToIDP(destination, samlResponseDocument, relayState, 
                  httpContext.getResponse(), willSendRequest); 
         }
         else
         {
            boolean areWeSendingRequest = saml2HandlerResponse.getSendRequest();
            String samlMsg = DocumentUtil.getDocumentAsString(samlResponseDocument);

            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMsg.getBytes("UTF-8"));
            
            String destinationQuery = RedirectBindingUtil.getDestinationQueryString(base64Request, relayState, areWeSendingRequest);
            
            RedirectBindingUtilDestHolder holder = new RedirectBindingUtilDestHolder();
            holder.setDestination(destination).setDestinationQueryString(destinationQuery);
            
            String destinationURL = RedirectBindingUtil.getDestinationURL(holder); 

            HTTPRedirectUtil.sendRedirectForRequestor(destinationURL, httpContext.getResponse());
         }
         return true;
      } 

      return false;
   }
   
   /**
    * Send the request to the IDP
    * @param destination idp url
    * @param samlDocument request or response document
    * @param relayState
    * @param response
    * @param willSendRequest are we sending Request or Response to IDP
    * @throws ProcessingException
    * @throws ConfigurationException
    * @throws IOException 
    */
   protected void sendRequestToIDP( 
         String destination, Document samlDocument,String relayState, 
         HttpServletResponse response,
         boolean willSendRequest)
   throws ProcessingException, ConfigurationException, IOException
   {
      if(this.supportSignatures)
      {
         SAML2Signature ss = new SAML2Signature();
         ss.signSAMLDocument(samlDocument, keyManager.getSigningKeyPair());
      }
      
      String samlMessage = DocumentUtil.getDocumentAsString(samlDocument); 
      samlMessage = PostBindingUtil.base64Encode(samlMessage);
      PostBindingUtil.sendPost(new DestinationInfoHolder(destination, samlMessage, relayState),
            response, willSendRequest);
   }
}