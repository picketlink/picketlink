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
package org.picketlink.identity.federation.bindings.tomcat.idp;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import javax.crypto.SecretKey;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.EncryptionType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * Valve at the Identity Provider that supports
 * SAML2 HTTP/Redirect binding with digital signature support
 * and xml encryption
 * @author Anil.Saldhana@redhat.com
 * @since Jan 14, 2009
 */
public class IDPRedirectWithSignatureValve extends IDPRedirectValve
{   
   private static Logger log = Logger.getLogger(IDPRedirectWithSignatureValve.class); 
   private boolean trace = log.isTraceEnabled();
   
   private boolean ignoreSignature = false;
   
   private TrustKeyManager keyManager; 
   
   public IDPRedirectWithSignatureValve()
   {
      super();  
   }
   
   /**
    * Indicate whether the signature parameter in the request
    * needs to be ignored
    * @param val
    */
   public void setIgnoreSignature(String val)
   {
     if(isNotNull(val))
        this.ignoreSignature = Boolean.valueOf(val); 
   } 
   
   @Override
   public void start() throws LifecycleException
   { 
      super.start();
      KeyProviderType keyProvider = this.idpConfiguration.getKeyProvider();
      try
      { 
         this.keyManager = CoreConfigUtil.getTrustKeyManager(keyProvider);

         List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
         keyManager.setAuthProperties( authProperties ); 
         
         keyManager.setValidatingAlias(keyProvider.getValidatingAlias()); 
      }
      catch(Exception e)
      {
         log.error("Exception reading configuration:",e);
         throw new LifecycleException(e.getLocalizedMessage());
      }
      if(trace) log.trace("Key Provider=" + keyProvider.getClassName());
   }   
   
   @Override
   protected boolean validate(Request request) throws IOException, GeneralSecurityException
   {
      boolean result = super.validate(request);
      if( result == false)
         return result;
      
      if(this.ignoreSignature)
      {
         if(trace) log.trace("Since signature is to be ignored, validation returns"); 
         return true;  
      }
      
      String queryString = request.getQueryString();
      //Check if there is a signature   
      byte[] sigValue = RedirectBindingSignatureUtil.getSignatureValueFromSignedURL(queryString);
      if(sigValue == null)
         return false;
      
      PublicKey validatingKey;
      try
      {
         validatingKey = keyManager.getValidatingKey(request.getRemoteAddr());
      }
      catch (TrustKeyConfigurationException e)
      {
         throw new GeneralSecurityException(e.getCause());
      }
      catch (TrustKeyProcessingException e)
      {
         throw new GeneralSecurityException(e.getCause());
      }
      
      return RedirectBindingSignatureUtil.validateSignature(queryString, validatingKey, sigValue); 
   }
   
   @Override
   protected String getDestinationQueryString(String urlEncodedResponse, String urlEncodedRelayState)
   {
      try
      {
         //Get the signing key  
         PrivateKey signingKey = keyManager.getSigningKey();
         String url = RedirectBindingSignatureUtil.getSAMLResponseURLWithSignature(urlEncodedResponse, urlEncodedRelayState, signingKey);
         return url;
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   protected ResponseType getResponse(Request request, Principal userPrincipal) 
   throws ParsingException, ConfigurationException, ProcessingException
   {
      SAML2Response saml2Response = new SAML2Response();
      
      ResponseType responseType =  super.getResponse(request, userPrincipal);
     
      //If there is a configuration to encrypt
      if(this.idpConfiguration.isEncrypt())
      {
         //Need to encrypt the assertion
         String sp = responseType.getDestination();
         if(sp == null)
            throw new IllegalStateException("Unable to handle encryption as SP url is null");
         try
         {
            URL spurl = new URL(sp); 
            PublicKey publicKey = keyManager.getValidatingKey(spurl.getHost());
            EncryptionType enc = idpConfiguration.getEncryption();
            if(enc == null)
               throw new IllegalStateException("EncryptionType not configured");
            String encAlgo = enc.getEncAlgo().value();
            int keyLength = enc.getKeySize();
            //Generate a key on the fly
            SecretKey sk = keyManager.getEncryptionKey(spurl.getHost(), encAlgo, keyLength);
            
            StringWriter sw = new StringWriter();
            saml2Response.marshall(responseType, sw);
            
            Document responseDoc = DocumentUtil.getDocument(new StringReader(sw.toString()));  
      
            String assertionNS = JBossSAMLURIConstants.ASSERTION_NSURI.get();
            
            QName assertionQName = new QName(assertionNS, "EncryptedAssertion", "saml");
            
            Element encAssertion = XMLEncryptionUtil.encryptElementInDocument(responseDoc,
                            publicKey, sk, keyLength, assertionQName, true);
            
            
            EncryptedElementType eet = saml2Response.getEncryptedAssertion(DocumentUtil.getNodeAsStream(encAssertion));
            responseType.getAssertionOrEncryptedAssertion().set(0, eet);
         }
         catch (MalformedURLException e)
         {
            throw new ParsingException(e);
         }
         catch (JAXBException e)
         {
            throw new ParsingException(e);
         }
         catch (SAXException e)
         {
            throw new ParsingException(e);
         } 
         catch (Exception e)
         {
            throw new ProcessingException(e);
         } 
      }
      //Lets see how the response looks like 
      if(log.isTraceEnabled())
      {
         StringWriter sw = new StringWriter();
         try
         {
            saml2Response.marshall(responseType, sw);
         }
         catch (JAXBException e)
         {
            if(trace) log.trace(e);
         }
         catch (SAXException e)
         {
            if(trace) log.trace(e);
         }
         log.trace("IDPRedirectValveWithSignature::Response="+sw.toString()); 
      }
      return responseType;
   } 
}