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
package org.picketlink.identity.federation.bindings.tomcat.sp;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.SignatureUtil;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.process.ServiceProviderBaseProcessor;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
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

   private final boolean trace = log.isTraceEnabled();

   private TrustKeyManager keyManager;

   protected String idpAddress = null;

   public SPRedirectSignatureFormAuthenticator()
   {
      super();
   }

   /**
    * If the request.getRemoteAddr is not exactly the IDP address that you have keyed
    * in your deployment descriptor for keystore alias, you can set it here explicitly
    */
   public void setIdpAddress(String idpAddress)
   {
      this.idpAddress = idpAddress;
   }
   
   @Override
   public void testStart() throws LifecycleException
   {
      super.testStart();
      this.init();
   }
   
   @Override
   public void start() throws LifecycleException
   {
      super.start();
      this.init();
   }

   private void init() throws LifecycleException
   {
      Context context = (Context) getContainer();

      KeyProviderType keyProvider = this.spConfiguration.getKeyProvider();
      if (keyProvider == null)
         throw new LifecycleException(ErrorCodes.NULL_VALUE + "KeyProvider is null for context=" + context.getName());
      try
      {
         String keyManagerClassName = keyProvider.getClassName();
         if (keyManagerClassName == null)
            throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyManager class name");

         Class<?> clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);
         if (clazz == null)
            throw new ClassNotFoundException(ErrorCodes.CLASS_NOT_LOADED + keyManagerClassName);
         this.keyManager = (TrustKeyManager) clazz.newInstance();

         List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
         keyManager.setAuthProperties(authProperties);
         keyManager.setValidatingAlias(keyProvider.getValidatingAlias());

         /**
          * Since the user has explicitly configured the idp address, we need
          * to add an option on the keymanager such that users of keymanager
          * can choose the proper idp key for validation
          */
         if (StringUtil.isNotNull(idpAddress))
         {
            keyManager.addAdditionalOption(ServiceProviderBaseProcessor.IDP_KEY, this.idpAddress);
         }
      }
      catch (Exception e)
      {
         log.error("Exception reading configuration:", e);
         throw new LifecycleException(e.getLocalizedMessage());
      }
      if (trace)
         log.trace("Key Provider=" + keyProvider.getClassName());

      //Initialize the handler chain again, mainly for the signing pair
      try
      {
         populateChainConfig();
         super.initializeHandlerChain();
      }
      catch (Exception e)
      {
         log.error("Exception reading configuration:", e);
         throw new LifecycleException(e.getLocalizedMessage());
      }
   }

   protected boolean validate(Request request) throws IOException, GeneralSecurityException
   {
      boolean result = super.validate(request);
      if (result == false)
         return result;

      String queryString = request.getQueryString();
      //Check if there is a signature   
      byte[] sigValue = RedirectBindingSignatureUtil.getSignatureValueFromSignedURL(queryString);
      if (sigValue == null)
         return false;

      //Construct the url again
      String reqFromURL = RedirectBindingSignatureUtil.getTokenValue(queryString, "SAMLResponse");
      String relayStateFromURL = RedirectBindingSignatureUtil.getTokenValue(queryString, GeneralConstants.RELAY_STATE);
      String sigAlgFromURL = RedirectBindingSignatureUtil.getTokenValue(queryString, "SigAlg");

      StringBuilder sb = new StringBuilder();
      sb.append("SAMLResponse=").append(reqFromURL);

      if (isNotNull(relayStateFromURL))
      {
         sb.append("&RelayState=").append(relayStateFromURL);
      }
      sb.append("&SigAlg=").append(sigAlgFromURL);

      PublicKey validatingKey;
      try
      {
         if (StringUtil.isNullOrEmpty(idpAddress))
         {
            idpAddress = request.getRemoteAddr();
         }
         validatingKey = keyManager.getValidatingKey(idpAddress);
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
   protected String getDestinationQueryString(String urlEncodedRequest, String urlEncodedRelayState, boolean sendRequest)
   {
      try
      {
         //Get the signing key  
         PrivateKey signingKey = keyManager.getSigningKey();
         
         if (signingKey == null) {
            log.error("Signing key is null. Check your KeyStore configuration.");
            throw new RuntimeException(ErrorCodes.SIGNING_PROCESS_FAILURE);
         }
         
         String url = null;

         if (sendRequest)
         {
            url = RedirectBindingSignatureUtil.getSAMLRequestURLWithSignature(urlEncodedRequest, urlEncodedRelayState,
                  signingKey);
         }
         else
         {
            url = RedirectBindingSignatureUtil.getSAMLResponseURLWithSignature(urlEncodedRequest, urlEncodedRelayState,
                  signingKey);
         }

         return url;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   protected void initializeSAMLProcessor(ServiceProviderBaseProcessor processor)
   {
      super.initializeSAMLProcessor(processor);
      processor.setTrustKeyManager(keyManager);
   }

   @Override
   protected ResponseType decryptAssertion(ResponseType responseType) throws IOException, GeneralSecurityException,
         ConfigurationException, ParsingException
   {
      try
      {
         SAML2Response saml2Response = new SAML2Response();
         PrivateKey privateKey = keyManager.getSigningKey();

         EncryptedElementType myEET = responseType.getAssertions().get(0).getEncryptedAssertion();
         Document eetDoc = saml2Response.convert(myEET);

         Element decryptedDocumentElement = XMLEncryptionUtil.decryptElementInDocument(eetDoc, privateKey);
         return saml2Response.getResponseType(DocumentUtil.getNodeAsStream(decryptedDocumentElement));
      }
      catch (Exception e)
      {
         throw new GeneralSecurityException(e);
      }
   }

   @Override
   protected void populateChainConfig() throws ConfigurationException, ProcessingException
   {
      super.populateChainConfig();
      if (this.keyManager != null)
      {
         if (trace)
            log.trace("Adding Keypair to the chain config");
         chainConfigOptions.put(GeneralConstants.KEYPAIR, keyManager.getSigningKeyPair());
      }
   }
}