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

import static org.jboss.identity.federation.core.util.StringUtil.isNotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.xml.bind.JAXBException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.log4j.Logger;
import org.jboss.identity.federation.api.saml.v2.response.SAML2Response;
import org.jboss.identity.federation.core.config.KeyProviderType;
import org.jboss.identity.federation.core.exceptions.ConfigurationException;
import org.jboss.identity.federation.core.exceptions.ParsingException;
import org.jboss.identity.federation.core.exceptions.ProcessingException;
import org.jboss.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.jboss.identity.federation.core.interfaces.TrustKeyManager;
import org.jboss.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.jboss.identity.federation.core.saml.v2.util.DocumentUtil;
import org.jboss.identity.federation.core.saml.v2.util.SignatureUtil;
import org.jboss.identity.federation.core.util.XMLEncryptionUtil;
import org.jboss.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.jboss.identity.federation.saml.v2.protocol.ResponseType;
import org.jboss.identity.federation.web.constants.GeneralConstants;
import org.jboss.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tomcat Authenticator for the HTTP/Redirect binding with Signature support
 * @author Anil.Saldhana@redhat.com
 * @since Jan 12, 2009
 */
public class SPRedirectSignatureFormAuthenticator extends SPRedirectFormAuthenticator
{ 
   private static Logger log = Logger.getLogger(SPRedirectSignatureFormAuthenticator.class);
   private boolean trace = log.isTraceEnabled();
   
   private TrustKeyManager keyManager; 

   public SPRedirectSignatureFormAuthenticator()
   {
      super(); 
   }
   
   @Override
   public void start() throws LifecycleException
   {
      super.start();
      Context context = (Context) getContainer();
      
      KeyProviderType keyProvider = this.spConfiguration.getKeyProvider();
      if(keyProvider == null)
         throw new LifecycleException("KeyProvider is null for context="+ context.getName());
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
      
      //Initialize the handler chain again, mainly for the signing pair
      try
      {
         populateChainConfig();
         super.initializeHandlerChain();
      }
      catch (Exception e)
      {  
         log.error("Exception reading configuration:",e);
         throw new LifecycleException(e.getLocalizedMessage()); 
      } 
   }
   
   protected boolean validate(Request request) throws IOException, GeneralSecurityException
   {
      boolean result = super.validate(request);
      if( result == false)
         return result;
      
      String queryString = request.getQueryString();
      //Check if there is a signature   
      byte[] sigValue = RedirectBindingSignatureUtil.getSignatureValueFromSignedURL(queryString);
      if(sigValue == null)
         return false;
      
      //Construct the url again
      String reqFromURL = RedirectBindingSignatureUtil.getTokenValue(queryString, "SAMLResponse"); 
      String relayStateFromURL = RedirectBindingSignatureUtil.getTokenValue(queryString, 
            GeneralConstants.RELAY_STATE);
      String sigAlgFromURL = RedirectBindingSignatureUtil.getTokenValue(queryString, "SigAlg"); 

      StringBuilder sb = new StringBuilder();
      sb.append("SAMLResponse=").append(reqFromURL);
       
      if(isNotNull(relayStateFromURL))
      {
         sb.append("&RelayState=").append(relayStateFromURL);
      }
      sb.append("&SigAlg=").append(sigAlgFromURL);
      
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
      boolean isValid = SignatureUtil.validate(sb.toString().getBytes("UTF-8"), sigValue, validatingKey);
      return isValid;     
   }

   @Override
   protected String getDestination(String urlEncodedRequest, String urlEncodedRelayState, boolean sendRequest)
   {
      try
      {
         //Get the signing key  
         PrivateKey signingKey = keyManager.getSigningKey();
         StringBuffer sb = new StringBuffer();
         String url = RedirectBindingSignatureUtil.getSAMLRequestURLWithSignature(urlEncodedRequest, urlEncodedRelayState, signingKey);
         sb.append("?").append(url);
         return sb.toString();
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   protected ResponseType decryptAssertion(ResponseType responseType) 
   throws IOException, GeneralSecurityException, ConfigurationException, ParsingException
   {
      try
      {
         SAML2Response saml2Response = new SAML2Response();
         PrivateKey privateKey = keyManager.getSigningKey(); 
         
         EncryptedElementType myEET = (EncryptedElementType) responseType.getAssertionOrEncryptedAssertion().get(0);
         Document eetDoc = saml2Response.convert(myEET); 
         
         Element decryptedDocumentElement = XMLEncryptionUtil.decryptElementInDocument(eetDoc,privateKey);
         
         //Let us use the encrypted doc element to decrypt it
         return  saml2Response.getResponseType(DocumentUtil.getNodeAsStream(decryptedDocumentElement));    
      } 
      catch (JAXBException e)
      {
         throw new ConfigurationException(e);
      } 
      catch (Exception e)
      {
         throw new GeneralSecurityException(e);
      } 
   }   
   
   @Override
   protected void populateChainConfig()
   throws ConfigurationException, ProcessingException
   {   
      super.populateChainConfig();
      if(this.keyManager != null)
      {
         if(trace)
            log.trace("Adding Keypair to the chain config");
         chainConfigOptions.put(GeneralConstants.KEYPAIR, keyManager.getSigningKeyPair());
      }  
   }
}