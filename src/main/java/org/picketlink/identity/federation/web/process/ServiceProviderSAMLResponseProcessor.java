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
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.w3c.dom.Document;

/**
 * Utility Class to handle processing of
 * an SAML Request Message
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */
public class ServiceProviderSAMLResponseProcessor extends ServiceProviderBaseProcessor
{
   private boolean validateSignature = false;
   
   private boolean idpPostBinding = false;
   
   public void setIdpPostBinding(boolean idpPostBinding)
   {
      this.idpPostBinding = idpPostBinding;
   }

   /**
    * Construct
    * @param postBinding Whether it is the Post Binding
    * @param serviceURL Service URL of the SP
    */
   public ServiceProviderSAMLResponseProcessor(boolean postBinding, String serviceURL)
   {
      super(postBinding, serviceURL);
   }

   /**
    * Flag to indicate whether the response should be validated for signature
    * @param validateSignature
    */
   public void setValidateSignature(boolean validateSignature)
   {
      this.validateSignature = validateSignature;
   }

   /**
    * Process the message
    * @param samlResponse
    * @param httpContext
    * @param handlers
    * @param chainLock a lock that needs to be used to process the chain of handlers
    * @return
    * @throws ProcessingException
    * @throws IOException
    * @throws ParsingException
    * @throws ConfigurationException
    */
   public SAML2HandlerResponse process(String samlResponse, HTTPContext httpContext, Set<SAML2Handler> handlers,
         Lock chainLock) throws ProcessingException, IOException, ParsingException, ConfigurationException
   {
      SAML2Response saml2Response = new SAML2Response();
      SAMLDocumentHolder documentHolder = null;
      SAML2Object samlObject = null;

      InputStream dataStream = null;
      
      if (this.postBinding || idpPostBinding )
      {  
         //deal with SAML response from IDP
         dataStream = PostBindingUtil.base64DecodeAsStream(samlResponse);
      }
      else
      {
         //deal with SAML response from IDP
         dataStream = RedirectBindingUtil.base64DeflateDecode(samlResponse);
      }

      samlObject = saml2Response.getSAML2ObjectFromStream(dataStream);
      documentHolder = saml2Response.getSamlDocumentHolder();

      if (this.validateSignature)
         try
         {
            if (!this.verifySignature(documentHolder))
               throw new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation failed");
         }
         catch (IssuerNotTrustedException e)
         {
            throw new ProcessingException(e);
         }

      //Create the request/response
      SAML2HandlerRequest saml2HandlerRequest = getSAML2HandlerRequest(documentHolder, httpContext);
      SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

      SAMLHandlerChainProcessor chainProcessor = new SAMLHandlerChainProcessor(handlers);

      //Set some request options
      if (spConfiguration != null)
      {
         Map<String, Object> requestOptions = new HashMap<String, Object>();
         requestOptions.put(GeneralConstants.CONFIGURATION, spConfiguration);
         if (keyManager != null)
         {
            String remoteHost = httpContext.getRequest().getRemoteAddr();
            if (trace)
            {
               log.trace("ServiceProviderSAMLResponseProcessor::Remote Host=" + remoteHost);
            }
            String idpKey = (String) keyManager.getAdditionalOption(ServiceProviderBaseProcessor.IDP_KEY);
            if (StringUtil.isNullOrEmpty(idpKey))
            {
               idpKey = remoteHost;
            }
            PublicKey validatingKey = CoreConfigUtil.getValidatingKey(keyManager, idpKey);
            requestOptions.put(GeneralConstants.SENDER_PUBLIC_KEY, validatingKey);
            requestOptions.put(GeneralConstants.DECRYPTING_KEY, keyManager.getSigningKey());
         }

         saml2HandlerRequest.setOptions(requestOptions);
      }

      chainProcessor.callHandlerChain(samlObject, saml2HandlerRequest, saml2HandlerResponse, httpContext, chainLock);

      return saml2HandlerResponse;
   }

   /**
    * Validate the signature of the IDP response
    * @param samlDocumentHolder
    * @return
    * @throws IssuerNotTrustedException
    */
   private boolean verifySignature(SAMLDocumentHolder samlDocumentHolder) throws IssuerNotTrustedException
   {
      if (keyManager == null)
         throw new IllegalStateException(ErrorCodes.NULL_ARGUMENT + "Key Manager");
      Document samlResponse = samlDocumentHolder.getSamlDocument();
      StatusResponseType response = (StatusResponseType) samlDocumentHolder.getSamlObject();

      String issuerID = response.getIssuer().getValue();

      if (issuerID == null)
         throw new IssuerNotTrustedException(ErrorCodes.NULL_VALUE + "Issue missing");

      URL issuerURL;
      try
      {
         issuerURL = new URL(issuerID);
      }
      catch (MalformedURLException e1)
      {
         throw new IssuerNotTrustedException(e1);
      }

      try
      {
         PublicKey publicKey = keyManager.getValidatingKey(issuerURL.getHost());
         if (trace)
            log.trace("Going to verify signature in the saml response from IDP");
         boolean sigResult = XMLSignatureUtil.validate(samlResponse, publicKey);
         if (trace)
            log.trace("Signature verification=" + sigResult);
         return sigResult;
      }
      catch (TrustKeyConfigurationException e)
      {
         log.error("Unable to verify signature", e);
      }
      catch (TrustKeyProcessingException e)
      {
         log.error("Unable to verify signature", e);
      }
      catch (MarshalException e)
      {
         log.error("Unable to verify signature", e);
      }
      catch (XMLSignatureException e)
      {
         log.error("Unable to verify signature", e);
      }
      return false;
   }
}