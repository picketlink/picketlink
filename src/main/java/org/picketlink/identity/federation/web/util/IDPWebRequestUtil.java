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
package org.picketlink.identity.federation.web.util;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.w3c.dom.Document;

/**
 * Request Util
 * <b> Not thread safe</b>
 * @author Anil.Saldhana@redhat.com
 * @since May 18, 2009
 */
public class IDPWebRequestUtil
{
   private static Logger log = Logger.getLogger(IDPWebRequestUtil.class);

   private final boolean trace = log.isTraceEnabled();

   private boolean redirectProfile = false;

   private boolean postProfile = false;

   private final IDPType idpConfiguration;

   private final TrustKeyManager keyManager;


   protected String canonicalizationMethod = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

   public IDPWebRequestUtil(HttpServletRequest request, IDPType idp, TrustKeyManager keym)
   {
      this.idpConfiguration = idp;
      this.keyManager = keym;
      this.redirectProfile = "GET".equals(request.getMethod());
      this.postProfile = "POST".equals(request.getMethod());
   }

   public String getCanonicalizationMethod()
   {
      return canonicalizationMethod;
   }

   public void setCanonicalizationMethod(String canonicalizationMethod)
   {
      this.canonicalizationMethod = canonicalizationMethod;
   }

   public boolean hasSAMLRequestInRedirectProfile()
   {
      return redirectProfile;
   }

   public boolean hasSAMLRequestInPostProfile()
   {
      return postProfile;
   }

   public SAMLDocumentHolder getSAMLDocumentHolder(String samlMessage) throws ParsingException, ConfigurationException,
         ProcessingException
   {
      InputStream is = null;
      SAML2Request saml2Request = new SAML2Request();
      if (redirectProfile)
      {
         is = RedirectBindingUtil.base64DeflateDecode(samlMessage);
      }
      else
      {
         try
         {
            byte[] samlBytes = PostBindingUtil.base64Decode(samlMessage);
            if (trace)
               log.trace("SAMLRequest=" + new String(samlBytes));
            is = new ByteArrayInputStream(samlBytes);
         }
         catch (Exception rte)
         {
            if (trace)
               log.trace("Error in base64 decoding saml message: " + rte);
            throw new ParsingException(rte);
         }
      }
      saml2Request.getSAML2ObjectFromStream(is);
      return saml2Request.getSamlDocumentHolder();
   }

   public RequestAbstractType getSAMLRequest(String samlMessage) throws ParsingException, ConfigurationException,
         ProcessingException
   {
      InputStream is = null;
      SAML2Request saml2Request = new SAML2Request();
      if (redirectProfile)
      {
         try
         {
            is = RedirectBindingUtil.base64DeflateDecode(samlMessage);
         }
         catch (Exception e)
         {
            log.error("Exception in parsing saml message:", e);
            throw new ParsingException();
         }
      }
      else
      {
         byte[] samlBytes = PostBindingUtil.base64Decode(samlMessage);
         if (trace)
            log.trace("SAMLRequest=" + new String(samlBytes));
         is = new ByteArrayInputStream(samlBytes);
      }
      return saml2Request.getRequestType(is);
   }

   /**
    * Verify that the issuer is trusted
    * @param issuer
    * @throws IssuerNotTrustedException
    */
   public void isTrusted(String issuer) throws IssuerNotTrustedException
   {
      if (idpConfiguration == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "IDP Configuration");
      try
      {
         String issuerDomain = getDomain(issuer);
         TrustType idpTrust = idpConfiguration.getTrust();
         if (idpTrust != null)
         {
            String domainsTrusted = idpTrust.getDomains();
            if (trace)
               log.trace("Domains that IDP trusts=" + domainsTrusted + " and issuer domain=" + issuerDomain);
            if (domainsTrusted.indexOf(issuerDomain) < 0)
            {
               //Let us do string parts checking
               StringTokenizer st = new StringTokenizer(domainsTrusted, ",");
               while (st != null && st.hasMoreTokens())
               {
                  String uriBit = st.nextToken();
                  if (trace)
                     log.trace("Matching uri bit=" + uriBit);
                  if (issuerDomain.indexOf(uriBit) > 0)
                  {
                     if (trace)
                        log.trace("Matched " + uriBit + " trust for " + issuerDomain);
                     return;
                  }
               }
               throw new IssuerNotTrustedException(issuer);
            }
         }
      }
      catch (Exception e)
      {
         throw new IssuerNotTrustedException(e.getLocalizedMessage(), e);
      }
   }

   /** 
    * Send a response
    * @param responseDoc
    * @param relayState
    * @param response 
    * @throws GeneralSecurityException 
    * @throws IOException  
    */
   public void send(WebRequestUtilHolder holder) throws GeneralSecurityException, IOException
   {
      Document responseDoc = holder.getResponseDoc();

      if (responseDoc == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + "responseType is null");

      String destination = holder.getDestination();
      String relayState = holder.getRelayState();
      boolean supportSignature = holder.isSupportSignature();
      boolean sendRequest = holder.isAreWeSendingRequest();
      HttpServletResponse response = holder.getServletResponse();

      if (holder.isPostBindingRequested() == false && !holder.isStrictPostBinding())
      {
         byte[] responseBytes = DocumentUtil.getDocumentAsString(responseDoc).getBytes("UTF-8");

         String urlEncodedResponse = RedirectBindingUtil.deflateBase64URLEncode(responseBytes);

         if (trace)
            log.trace("IDP:Destination=" + destination);

         if (isNotNull(relayState))
            relayState = RedirectBindingUtil.urlEncode(relayState);

         String finalDest = destination + getDestination(urlEncodedResponse, relayState, supportSignature, sendRequest);
         if (trace)
            log.trace("Redirecting to=" + finalDest);
         HTTPRedirectUtil.sendRedirectForResponder(finalDest, response);
      }
      else
      {
         //If we support signature
         if (supportSignature)
         {
            //Sign the document
            SAML2Signature samlSignature = new SAML2Signature();

            KeyPair keypair = keyManager.getSigningKeyPair();
            samlSignature.signSAMLDocument(responseDoc, keypair);

            if (trace)
               log.trace("Sending over to SP:" + DocumentUtil.asString(responseDoc));
         }
         byte[] responseBytes = DocumentUtil.getDocumentAsString(responseDoc).getBytes("UTF-8");

         String samlResponse = PostBindingUtil.base64Encode(new String(responseBytes));

         PostBindingUtil.sendPost(new DestinationInfoHolder(destination, samlResponse, relayState), response,
               sendRequest);
      }
   }

   /**
    * Generate a Destination URL for the HTTPRedirect binding
    * with the saml response and relay state
    * @param urlEncodedResponse
    * @param urlEncodedRelayState
    * @return
    */
   public String getDestination(String urlEncodedResponse, String urlEncodedRelayState, boolean supportSignature,
         boolean sendRequest)
   {
      StringBuilder sb = new StringBuilder();

      if (supportSignature)
      {
         try
         {
            sb.append("?");
            sb.append(RedirectBindingSignatureUtil.getSAMLResponseURLWithSignature(urlEncodedResponse,
                  urlEncodedRelayState, keyManager.getSigningKey()));
         }
         catch (Exception e)
         {
            if (trace)
               log.trace(e);
         }
      }
      else
      {
         if (sendRequest)
            sb.append("?SAMLRequest=").append(urlEncodedResponse);
         else
            sb.append("?SAMLResponse=").append(urlEncodedResponse);
         if (isNotNull(urlEncodedRelayState))
            sb.append("&RelayState=").append(urlEncodedRelayState);
      }
      return sb.toString();
   }

   public WebRequestUtilHolder getHolder()
   {
      return new WebRequestUtilHolder();
   }

   /**
    * Create an Error Response
    * @param responseURL
    * @param status
    * @param identityURL
    * @param supportSignature
    * @return 
    * @throws ConfigurationException   
    */
   public Document getErrorResponse(String responseURL, String status, String identityURL, boolean supportSignature)
   {
      Document samlResponse = null;
      ResponseType responseType = null;

      SAML2Response saml2Response = new SAML2Response();

      //Create a response type
      String id = IDGenerator.create("ID_");

      IssuerInfoHolder issuerHolder = new IssuerInfoHolder(identityURL);
      issuerHolder.setStatusCode(status);

      IDPInfoHolder idp = new IDPInfoHolder();
      idp.setNameIDFormatValue(null);
      idp.setNameIDFormat(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());

      SPInfoHolder sp = new SPInfoHolder();
      sp.setResponseDestinationURI(responseURL);
      try
      {
         responseType = saml2Response.createResponseType(id, sp, idp, issuerHolder);
      }
      catch (ConfigurationException e1)
      {
         if (trace)
            log.trace(e1);
         responseType = saml2Response.createResponseType(id);
      }
      catch (ProcessingException e)
      {
         if (trace)
            log.trace(e);
         responseType = saml2Response.createResponseType(id);
      }

      //Lets see how the response looks like 
      if (log.isTraceEnabled())
      {
         log.trace("Error_ResponseType = ");
         StringWriter sw = new StringWriter();
         try
         {
            saml2Response.marshall(responseType, sw);
         }
         catch (ProcessingException e)
         {
            log.trace(e);
         }
         log.trace("Response=" + sw.toString());
      }

      if (supportSignature)
      {
         try
         {
            SAML2Signature ss = new SAML2Signature();
            samlResponse = ss.sign(responseType, keyManager.getSigningKeyPair());
         }
         catch (Exception e)
         {
            if (trace)
               log.trace(e);
         }
      }
      else
         try
         {
            samlResponse = saml2Response.convert(responseType);
         }
         catch (Exception e)
         {
            if (trace)
               log.trace(e);
         }

      return samlResponse;
   }

   /**
    * Given a SP or IDP issuer from the assertion, return the host
    * @param domainURL
    * @return
    * @throws IOException  
    */
   private static String getDomain(String domainURL) throws IOException
   {
      URL url = new URL(domainURL);
      return url.getHost();
   }

   public class WebRequestUtilHolder
   {
      private Document responseDoc;

      private String relayState;

      private String destination;

      private HttpServletResponse servletResponse;

      private PrivateKey privateKey;

      private boolean supportSignature;

      private boolean postBindingRequested;

      private boolean areWeSendingRequest;
      
      //Cater to SAML Web Browser SSO Profile demand that we do not reply in Redirect Binding
      private boolean strictPostBinding = false;
      
      public boolean isStrictPostBinding()
      {
         return strictPostBinding;
      }

      public void setStrictPostBinding(boolean strictPostBinding)
      {
         this.strictPostBinding = strictPostBinding;
      }
      
      public Document getResponseDoc()
      {
         return responseDoc;
      }

      public WebRequestUtilHolder setResponseDoc(Document responseDoc)
      {
         this.responseDoc = responseDoc;
         return this;
      }

      public String getRelayState()
      {
         return relayState;
      }

      public WebRequestUtilHolder setRelayState(String relayState)
      {
         this.relayState = relayState;
         return this;
      }

      public String getDestination()
      {
         return destination;
      }

      public WebRequestUtilHolder setDestination(String destination)
      {
         this.destination = destination;
         return this;
      }

      public HttpServletResponse getServletResponse()
      {
         return servletResponse;
      }

      public WebRequestUtilHolder setServletResponse(HttpServletResponse servletResponse)
      {
         this.servletResponse = servletResponse;
         return this;
      }

      public PrivateKey getPrivateKey()
      {
         return privateKey;
      }

      public WebRequestUtilHolder setPrivateKey(PrivateKey privateKey)
      {
         this.privateKey = privateKey;
         return this;
      }

      public boolean isSupportSignature()
      {
         return supportSignature;
      }

      public WebRequestUtilHolder setSupportSignature(boolean supportSignature)
      {
         this.supportSignature = supportSignature;
         return this;
      }

      public boolean isPostBindingRequested()
      {
         return postBindingRequested;
      }

      public WebRequestUtilHolder setPostBindingRequested(boolean postBindingRequested)
      {
         this.postBindingRequested = postBindingRequested;
         return this;
      }

      public boolean isAreWeSendingRequest()
      {
         return areWeSendingRequest;
      }

      public WebRequestUtilHolder setAreWeSendingRequest(boolean areWeSendingRequest)
      {
         this.areWeSendingRequest = areWeSendingRequest;
         return this;
      }
   }
}