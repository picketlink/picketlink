/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.wstrust;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.ClaimsProcessorType;
import org.picketlink.identity.federation.core.config.ClaimsProcessorsType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.config.KeyValueType;
import org.picketlink.identity.federation.core.config.STSType;
import org.picketlink.identity.federation.core.config.ServiceProviderType;
import org.picketlink.identity.federation.core.config.ServiceProvidersType;
import org.picketlink.identity.federation.core.config.TokenProviderType;
import org.picketlink.identity.federation.core.config.TokenProvidersType;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.core.sts.STSCoreConfig;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;

/**
 * <p>
 * Standard JBoss STS configuration implementation.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author <a href="mailto:asaldhan@redhat.com">Anil Saldhana</a>
 */
public class PicketLinkSTSConfiguration implements STSConfiguration
{

   // the delegate contains all the information extracted from the picketlink-sts.xml configuration file.
   private final STSType delegate;

   private final Map<String, SecurityTokenProvider> tokenProviders = new HashMap<String, SecurityTokenProvider>();

   private final Map<String, ServiceProviderType> spMetadata = new HashMap<String, ServiceProviderType>();

   private final Map<String, ClaimsProcessor> claimsProcessors = new HashMap<String, ClaimsProcessor>();

   private TrustKeyManager trustManager;

   private WSTrustRequestHandler handler;

   /**
    * <p>
    * Creates an instance of {@code PicketLinkSTSConfiguration} with default configuration values.
    * </p>
    */
   public PicketLinkSTSConfiguration()
   {
      this.delegate = new STSType();
      this.delegate.setRequestHandler(StandardRequestHandler.class.getCanonicalName());
      // TODO: add default token provider classes.
   }

   /**
    * <p>
    * Creates an instance of {@code PicketLinkSTSConfiguration} with the specified configuration.
    * </p>
    * 
    * @param config a reference to the object that holds the configuration of the STS.
    */
   public PicketLinkSTSConfiguration(STSType config)
   {
      this.delegate = config;
      // set the default request handler if one hasn't been specified.
      if (this.delegate.getRequestHandler() == null)
         this.delegate.setRequestHandler(StandardRequestHandler.class.getCanonicalName());

      // build the token-provider maps.
      TokenProvidersType providers = this.delegate.getTokenProviders();
      if (providers != null)
      {
         for (TokenProviderType provider : providers.getTokenProvider())
         {
            // get the properties that have been configured for the token provider.
            Map<String, String> properties = new HashMap<String, String>();

            List<KeyValueType> providerPropertiesList;
            try
            {
               providerPropertiesList = CoreConfigUtil.getProperties(provider);
            }
            catch (GeneralSecurityException e)
            {
               throw new RuntimeException(e);
            }

            for (KeyValueType propertyType : providerPropertiesList)
               properties.put(propertyType.getKey(), propertyType.getValue());

            // create and initialize the token provider.
            SecurityTokenProvider tokenProvider = WSTrustServiceFactory.getInstance().createTokenProvider(
                  provider.getProviderClass(), properties);
            // token providers can be keyed by the token type and by token element + namespace.
            this.tokenProviders.put(provider.getTokenType(), tokenProvider);
            String tokenElementAndNS = tokenProvider.family() + "$" + provider.getTokenElement() + "$"
                  + provider.getTokenElementNS();
            this.tokenProviders.put(tokenElementAndNS, tokenProvider);
         }
      }

      // build the claims processors map.
      ClaimsProcessorsType processors = this.delegate.getClaimsProcessors();
      if (processors != null)
      {
         for (ClaimsProcessorType processor : processors.getClaimsProcessor())
         {
            // get the properties that have been configured for the claims processor.
            Map<String, String> properties = new HashMap<String, String>();
            List<KeyValueType> processorPropertiesList;
            try
            {
               processorPropertiesList = CoreConfigUtil.getProperties(processor);
            }
            catch (GeneralSecurityException e)
            {
               throw new RuntimeException(e);
            }

            for (KeyValueType propertyType : processorPropertiesList)
               properties.put(propertyType.getKey(), propertyType.getValue());

            // create and initialize the claims processor.
            ClaimsProcessor claimsProcessor = WSTrustServiceFactory.getInstance().createClaimsProcessor(
                  processor.getProcessorClass(), properties);
            // store the processor using the dialect as the key.
            this.claimsProcessors.put(processor.getDialect(), claimsProcessor);
         }
      }

      // setup the service providers metadata.
      ServiceProvidersType serviceProviders = this.delegate.getServiceProviders();
      if (serviceProviders != null)
      {
         for (ServiceProviderType provider : serviceProviders.getServiceProvider())
            this.spMetadata.put(provider.getEndpoint(), provider);
      }

      // setup the key store.
      KeyProviderType keyProviderType = config.getKeyProvider();
      if (keyProviderType != null)
      {
         String keyManagerClassName = keyProviderType.getClassName();
         try
         {
            //Decrypt/de-mask the passwords if any
            List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProviderType);

            Class<?> clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);
            if (clazz == null)
               throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + keyManagerClassName);
            this.trustManager = (TrustKeyManager) clazz.newInstance();
            this.trustManager.setAuthProperties(authProperties);
            this.trustManager.setValidatingAlias(keyProviderType.getValidatingAlias());
         }
         catch (Exception e)
         {
            throw new RuntimeException(ErrorCodes.STS_UNABLE_TO_CONSTRUCT_KEYMGR, e);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getSTSName()
    */
   public String getSTSName()
   {
      return this.delegate.getSTSName();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getEncryptIssuedToken()
    */
   public boolean encryptIssuedToken()
   {
      return this.delegate.isEncryptToken();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#signIssuedToken()
    */
   public boolean signIssuedToken()
   {
      return this.delegate.isSignToken();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getIssuedTokenTimeout()
    */
   public long getIssuedTokenTimeout()
   {
      // return the timeout value in milliseconds.
      return this.delegate.getTokenTimeout() * 1000;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getRequestHandlerClass()
    */
   public WSTrustRequestHandler getRequestHandler()
   {
      if (this.handler == null)
         this.handler = WSTrustServiceFactory.getInstance().createRequestHandler(this.delegate.getRequestHandler(),
               this);
      return this.handler;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getProviderForService(java.lang.String)
    */
   public SecurityTokenProvider getProviderForService(String serviceName)
   {
      if (serviceName == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "serviceName");

      ServiceProviderType provider = this.spMetadata.get(serviceName);
      if (provider != null)
      {
         return this.tokenProviders.get(provider.getTokenType());
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getProviderForTokenType(java.lang.String)
    */
   public SecurityTokenProvider getProviderForTokenType(String tokenType)
   {
      if (tokenType == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "tokenType");
      return this.tokenProviders.get(tokenType);
   }

   /**
    * @see org.picketlink.identity.federation.core.sts.STSCoreConfig#getProviderForTokenElementNS(java.lang.String, javax.xml.namespace.QName)
    */
   public SecurityTokenProvider getProviderForTokenElementNS(String family, QName tokenQName)
   {
      return this.tokenProviders.get(family + "$" + tokenQName.getLocalPart() + "$" + tokenQName.getNamespaceURI());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getClaimsProcessor(java.lang.String)
    */
   public ClaimsProcessor getClaimsProcessor(String claimsDialect)
   {
      return this.claimsProcessors.get(claimsDialect);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getTokenTypeForService(java.lang.String)
    */
   public String getTokenTypeForService(String serviceName)
   {
      ServiceProviderType provider = this.spMetadata.get(serviceName);
      if (provider != null)
         return provider.getTokenType();
      return null;
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getServiceProviderPublicKey(java.lang.String)
    */
   public PublicKey getServiceProviderPublicKey(String serviceName)
   {
      PublicKey key = null;
      if (this.trustManager != null)
      {
         try
         {
            // try using the truststore alias from the service provider metadata.
            ServiceProviderType provider = this.spMetadata.get(serviceName);
            if (provider != null && provider.getTruststoreAlias() != null)
            {
               key = this.trustManager.getPublicKey(provider.getTruststoreAlias());
            }
            // if there was no truststore alias or no PKC under that alias, use the KeyProvider mapping.
            if (key == null)
            {
               key = this.trustManager.getValidatingKey(serviceName);
            }
         }
         catch (Exception e)
         {
            throw new RuntimeException(ErrorCodes.STS_PUBLIC_KEY_ERROR + serviceName, e);
         }
      }
      return key;
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getSTSKeyPair()
    */
   public KeyPair getSTSKeyPair()
   {
      KeyPair keyPair = null;
      if (this.trustManager != null)
      {
         try
         {
            keyPair = this.trustManager.getSigningKeyPair();
         }
         catch (Exception e)
         {
            throw new RuntimeException(ErrorCodes.STS_SIGNING_KEYPAIR_ERROR, e);
         }
      }
      return keyPair;
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getCertificate(java.lang.String)
    */
   public Certificate getCertificate(String alias)
   {
      Certificate certificate = null;
      if (this.trustManager != null)
      {
         try
         {
            certificate = trustManager.getCertificate(alias);
         }
         catch (Exception e)
         {
            throw new RuntimeException(ErrorCodes.STS_PUBLIC_KEY_CERT, e);
         }
      }
      return certificate;
   }

   /**
    * @see STSConfiguration#getXMLDSigCanonicalizationMethod()
    */
   public String getXMLDSigCanonicalizationMethod()
   {
      return delegate.getCanonicalizationMethod();
   }

   /**
    * @see {@code STSCoreConfig#addTokenProvider(String, SecurityTokenProvider)}
    */
   public void addTokenProvider(String key, SecurityTokenProvider provider)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(PicketLinkCoreSTS.rte);

      tokenProviders.put(key, provider);

      QName tokenQName = provider.getSupportedQName();
      if (tokenQName != null)
      {
         String tokenElementAndNS = provider.family() + "$" + tokenQName.getLocalPart() + "$"
               + tokenQName.getNamespaceURI();

         this.tokenProviders.put(tokenElementAndNS, provider);
      }
   }

   /**
    * @see {@code STSCoreConfig#removeTokenProvider(String)}
    */
   public void removeTokenProvider(String key)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(PicketLinkCoreSTS.rte);

      tokenProviders.remove(key);
   }

   /**
    * @see org.picketlink.identity.federation.core.sts.STSCoreConfig#getTokenProviders()
    */
   public List<SecurityTokenProvider> getTokenProviders()
   {
      List<SecurityTokenProvider> list = new ArrayList<SecurityTokenProvider>();
      list.addAll(tokenProviders.values());
      return Collections.unmodifiableList(list);
   }

   /**
    * @see org.picketlink.identity.federation.core.sts.STSCoreConfig#getProvidersByFamily(java.lang.String)
    */
   public List<SecurityTokenProvider> getProvidersByFamily(String familyName)
   {
      List<SecurityTokenProvider> result = new ArrayList<SecurityTokenProvider>();
      for (SecurityTokenProvider provider : tokenProviders.values())
      {
         if (provider.family().equals(familyName))
            result.add(provider);
      }
      return result;
   }

   /**
    * @see org.picketlink.identity.federation.core.sts.STSCoreConfig#copy(org.picketlink.identity.federation.core.sts.STSCoreConfig)
    */
   public void copy(STSCoreConfig thatConfig)
   {
      if (thatConfig instanceof PicketLinkSTSConfiguration)
      {
         PicketLinkSTSConfiguration pc = (PicketLinkSTSConfiguration) thatConfig;
         this.tokenProviders.putAll(pc.tokenProviders);
         this.claimsProcessors.putAll(pc.claimsProcessors);
      }
      else
         throw new RuntimeException("Unknown config :" + thatConfig); //TODO: Handle other configuration
   }

   @Override
   public String toString()
   {
      return "PicketLinkSTSConfiguration [delegate=" + delegate + ", tokenProviders=" + tokenProviders
            + ", spMetadata=" + spMetadata + ", claimsProcessors=" + claimsProcessors + ", trustManager="
            + trustManager + ", handler=" + handler + "]";
   }
}