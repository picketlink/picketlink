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
package org.jboss.identity.federation.bindings.tomcat.sp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Response;
import org.apache.log4j.Logger;
import org.jboss.identity.federation.api.saml.v2.request.SAML2Request;
import org.jboss.identity.federation.core.config.KeyProviderType;
import org.jboss.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.jboss.identity.federation.core.interfaces.TrustKeyManager;
import org.jboss.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.jboss.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.jboss.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.jboss.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.jboss.identity.federation.core.util.XMLSignatureUtil;
import org.jboss.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.jboss.identity.federation.saml.v2.protocol.ResponseType;
import org.jboss.identity.federation.web.util.PostBindingUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * JBID-142: POST form authenticator that can 
 * handle signatures at the SP side
 * @author Anil.Saldhana@redhat.com
 * @since Jul 24, 2009
 */
public class SPPostSignatureFormAuthenticator extends SPPostFormAuthenticator
{
   private static Logger log = Logger.getLogger(SPPostSignatureFormAuthenticator.class);
   private boolean trace = log.isTraceEnabled();
   
   private TrustKeyManager keyManager; 
   
   private boolean signAssertions = false;
   
   public boolean isSignAssertions()
   {
      return signAssertions;
   }

   public void setSignAssertions(boolean signAssertions)
   {
      this.signAssertions = signAssertions;
   }  

   @Override
   public void start() throws LifecycleException
   {
      super.start();
      KeyProviderType keyProvider = this.spConfiguration.getKeyProvider();
      if(keyProvider == null)
         throw new LifecycleException("KeyProvider is null");
      try
      {
         ClassLoader tcl = SecurityActions.getContextClassLoader();
         String keyManagerClassName = keyProvider.getClassName();
         if(keyManagerClassName == null)
            throw new RuntimeException("KeyManager class name is null");
         
         Class<?> clazz = tcl.loadClass(keyManagerClassName);
         this.keyManager = (TrustKeyManager) clazz.newInstance();
         keyManager.setAuthProperties(keyProvider.getAuth());
         keyManager.setValidatingAlias(keyProvider.getValidatingAlias());
      }
      catch(Exception e)
      {
         log.error("Exception reading configuration:",e);
         throw new LifecycleException(e.getLocalizedMessage());
      }
      if(trace) log.trace("Key Provider=" + keyProvider.getClassName());
   }
   
   protected void sendRequestToIDP(AuthnRequestType authnRequest, String relayState, Response response)
   throws IOException, SAXException, JAXBException, GeneralSecurityException
   {
      SAML2Request saml2Request = new SAML2Request();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      saml2Request.marshall(authnRequest, baos);
 
      String samlMessage = PostBindingUtil.base64Encode(baos.toString());  
      String destination = authnRequest.getDestination();
      
      PostBindingUtil.sendPost(new DestinationInfoHolder(destination, samlMessage, relayState),
            response, true);
   } 

   @Override
   protected boolean verifySignature(SAMLDocumentHolder samlDocumentHolder) throws IssuerNotTrustedException
   {   
      Document samlResponse = samlDocumentHolder.getSamlDocument();
      ResponseType response = (ResponseType) samlDocumentHolder.getSamlObject();
      
      String issuerID = response.getIssuer().getValue();
      
      if(issuerID == null)
         throw new IssuerNotTrustedException("Issue missing");
      
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
         if(trace) log.trace("Going to verify signature in the saml response from IDP"); 
         boolean sigResult =  XMLSignatureUtil.validate(samlResponse, publicKey);
         if(trace) log.trace("Signature verification="+sigResult);
         return sigResult;
      }
      catch (TrustKeyConfigurationException e)
      {
         log.error("Unable to verify signature",e);
      }
      catch (TrustKeyProcessingException e)
      {
         log.error("Unable to verify signature",e);
      }
      catch (MarshalException e)
      {
         log.error("Unable to verify signature",e);
      }
      catch (XMLSignatureException e)
      {
         log.error("Unable to verify signature",e);
      }
      return false;
   }  
}