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
package org.picketlink.identity.federation.core.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.ClaimsProcessorType;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.config.KeyValueType;
import org.picketlink.identity.federation.core.config.ProviderType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.config.TokenProviderType;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IndexedEndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;

/**
 * Utility for configuration
 * @author Anil.Saldhana@redhat.com
 * @since Nov 13, 2009
 */
public class CoreConfigUtil
{
   private static Logger log = Logger.getLogger(CoreConfigUtil.class);

   /**
    * Given either the IDP Configuration or the SP Configuration, derive
    * the TrustKeyManager
    * @param idpOrSPConfiguration
    * @return
    */
   public static TrustKeyManager getTrustKeyManager(ProviderType idpOrSPConfiguration)
   {
      KeyProviderType keyProvider = idpOrSPConfiguration.getKeyProvider();
      return getTrustKeyManager(keyProvider);
   }

   /**
    * Once the {@code KeyProviderType} is derived, get
    * the {@code TrustKeyManager}
    * @param keyProvider
    * @return
    */
   public static TrustKeyManager getTrustKeyManager(KeyProviderType keyProvider)
   {
      TrustKeyManager trustKeyManager = null;
      try
      {
         String keyManagerClassName = keyProvider.getClassName();
         if (keyManagerClassName == null)
            throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyManager class name");

         Class<?> clazz = SecurityActions.loadClass(CoreConfigUtil.class, keyManagerClassName);
         if (clazz == null)
            throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + keyManagerClassName);
         trustKeyManager = (TrustKeyManager) clazz.newInstance();
      }
      catch (Exception e)
      {
         log.error("Exception in getting TrustKeyManager:", e);
      }
      return trustKeyManager;
   }

   /**
    * Get the validating key
    * @param idpSpConfiguration
    * @param domain
    * @return
    * @throws ConfigurationException
    * @throws ProcessingException
    */
   public static PublicKey getValidatingKey(ProviderType idpSpConfiguration, String domain)
         throws ConfigurationException, ProcessingException
   {
      TrustKeyManager trustKeyManager = getTrustKeyManager(idpSpConfiguration);

      return getValidatingKey(trustKeyManager, domain);
   }

   /**
    * Get the validating key given the trust key manager
    * @param trustKeyManager
    * @param domain
    * @return
    * @throws ConfigurationException
    * @throws ProcessingException
    */
   public static PublicKey getValidatingKey(TrustKeyManager trustKeyManager, String domain)
         throws ConfigurationException, ProcessingException
   {
      if (trustKeyManager == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + "Trust Key Manager");

      return trustKeyManager.getValidatingKey(domain);
   }

   /**
    * Given a {@code KeyProviderType}, return the list of auth properties that have been decrypted for any
    * masked password
    * @param keyProviderType
    * @return
    * @throws GeneralSecurityException
    */
   @SuppressWarnings("unchecked")
   public static List<AuthPropertyType> getKeyProviderProperties(KeyProviderType keyProviderType)
         throws GeneralSecurityException
   {
      List<AuthPropertyType> authProperties = keyProviderType.getAuth();
      if (decryptionNeeded(authProperties))
         authProperties = decryptPasswords(authProperties);

      return authProperties;
   }

   /**
    * Given a {@code TokenProviderType}, return the list of properties that have been decrypted for
    * any masked property value
    * @param tokenProviderType
    * @return
    * @throws GeneralSecurityException
    */
   @SuppressWarnings("unchecked")
   public static List<KeyValueType> getProperties(TokenProviderType tokenProviderType) throws GeneralSecurityException
   {
      List<KeyValueType> keyValueTypeList = tokenProviderType.getProperty();
      if (decryptionNeeded(keyValueTypeList))
         keyValueTypeList = decryptPasswords(keyValueTypeList);

      return keyValueTypeList;
   }

   /**
    * Given a {@code ClaimsProcessorType}, return the list of properties that have been decrypted for
    * any masked property value
    * @param claimsProcessorType
    * @return
    * @throws GeneralSecurityException
    */
   @SuppressWarnings("unchecked")
   public static List<KeyValueType> getProperties(ClaimsProcessorType claimsProcessorType)
         throws GeneralSecurityException
   {
      List<KeyValueType> keyValueTypeList = claimsProcessorType.getProperty();
      if (decryptionNeeded(keyValueTypeList))
         keyValueTypeList = decryptPasswords(keyValueTypeList);

      return keyValueTypeList;
   }

   /**
    * Given a key value list, check if decrypt of any properties is needed. 
    * Unless one of the keys is "salt", we cannot figure out is decrypt is needed
    * @param keyValueList
    * @return
    */
   public static boolean decryptionNeeded(List<? extends KeyValueType> keyValueList)
   {
      int length = keyValueList.size();

      //Let us run through the list to see if there is any salt
      for (int i = 0; i < length; i++)
      {
         KeyValueType kvt = keyValueList.get(i);

         String key = kvt.getKey();
         if (PicketLinkFederationConstants.SALT.equalsIgnoreCase(key))
            return true;
      }
      return false;
   }

   /**
    * Given a key value pair read from PicketLink configuration, ensure
    * that we replace the masked passwords with the decoded passwords
    * and pass it back
    * 
    * @param keyValueList
    * @return
    * @throws GeneralSecurityException 
    * @throws Exception
    */
   @SuppressWarnings("rawtypes")
   private static List decryptPasswords(List keyValueList) throws GeneralSecurityException
   {
      String pbeAlgo = PicketLinkFederationConstants.PBE_ALGORITHM;

      String salt = null;
      int iterationCount = 0;

      int length = keyValueList.size();

      //Let us run through the list to see if there is any salt
      for (int i = 0; i < length; i++)
      {
         KeyValueType kvt = (KeyValueType) keyValueList.get(i);

         String key = kvt.getKey();
         if (PicketLinkFederationConstants.SALT.equalsIgnoreCase(key))
            salt = kvt.getValue();
         if (PicketLinkFederationConstants.ITERATION_COUNT.equalsIgnoreCase(key))
            iterationCount = Integer.parseInt(kvt.getValue());
      }

      if (salt == null)
         return keyValueList;

      //Ok. there is a salt configured. So we have some properties with masked values
      List<KeyValueType> returningList = new ArrayList<KeyValueType>();

      // Create the PBE secret key 
      SecretKeyFactory factory = SecretKeyFactory.getInstance(pbeAlgo);

      char[] password = "somearbitrarycrazystringthatdoesnotmatter".toCharArray();
      PBEParameterSpec cipherSpec = new PBEParameterSpec(salt.getBytes(), iterationCount);
      PBEKeySpec keySpec = new PBEKeySpec(password);
      SecretKey cipherKey = factory.generateSecret(keySpec);

      for (int i = 0; i < length; i++)
      {
         KeyValueType kvt = (KeyValueType) keyValueList.get(i);

         String val = kvt.getValue();
         if (val.startsWith(PicketLinkFederationConstants.PASS_MASK_PREFIX))
         {
            val = val.substring(PicketLinkFederationConstants.PASS_MASK_PREFIX.length());
            String decodedValue;
            try
            {
               decodedValue = PBEUtils.decode64(val, pbeAlgo, cipherKey, cipherSpec);
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException(e);
            }

            KeyValueType newKVT = new KeyValueType();
            if (keyValueList.get(0) instanceof AuthPropertyType)
               newKVT = new AuthPropertyType();
            newKVT.setKey(kvt.getKey());
            newKVT.setValue(new String(decodedValue));
            returningList.add(newKVT);
         }
         else
         {
            returningList.add(kvt);
         }
      }

      return returningList;
   }

   /**
    * Given a metadata {@link EntityDescriptorType}, construct the Service provider configuration
    * @param entityDescriptor
    * @param bindingURI
    * @return
    */
   public static SPType getSPConfiguration(EntityDescriptorType entityDescriptor, String bindingURI)
   {
      SPType spType = new SPType();
      String identityURL = null;
      String serviceURL = null;

      if (identityURL == null)
      {
         IDPSSODescriptorType idpSSO = getIDPDescriptor(entityDescriptor);
         if (idpSSO != null)
         {
            identityURL = getIdentityURL(idpSSO, bindingURI);
         }
         spType.setIdentityURL(identityURL);
      }
      if (serviceURL == null)
      {
         SPSSODescriptorType spSSO = getSPDescriptor(entityDescriptor);
         if (spSSO != null)
         {
            serviceURL = getServiceURL(spSSO, bindingURI);
         }
         spType.setServiceURL(serviceURL);
      }
      return spType;
   }

   /**
    * Given a metadata {@link EntityDescriptorType}, construct the Service provider configuration
    * @param entityDescriptor
    * @param bindingURI
    * @return
    */
   public static SPType getSPConfiguration(EntitiesDescriptorType entitiesDescriptor, String bindingURI)
   {
      SPType spType = null;
      String identityURL = null;
      String serviceURL = null;

      List<Object> list = entitiesDescriptor.getEntityDescriptor();
      if (list != null)
      {
         for (Object theObject : list)
         {
            if (theObject instanceof EntitiesDescriptorType)
            {
               spType = getSPConfiguration((EntitiesDescriptorType) theObject, bindingURI);
            }
            else if (theObject instanceof EntityDescriptorType)
            {
               if (identityURL == null)
               {
                  IDPSSODescriptorType idpSSO = getIDPDescriptor((EntityDescriptorType) theObject);
                  if (idpSSO != null)
                  {
                     identityURL = getIdentityURL(idpSSO, bindingURI);
                  }
                  if (identityURL != null && spType != null)
                  {
                     spType.setIdentityURL(identityURL);
                  }
                  else if (identityURL != null && spType == null)
                  {
                     spType = new SPType();
                     spType.setIdentityURL(identityURL);
                  }
               }
               if (serviceURL == null)
               {
                  SPSSODescriptorType spSSO = getSPDescriptor((EntityDescriptorType) theObject);
                  if (spSSO != null)
                  {
                     serviceURL = getServiceURL(spSSO, bindingURI);
                  }
                  if (serviceURL != null && spType != null)
                  {
                     spType.setServiceURL(serviceURL);
                  }
                  else if (serviceURL != null && spType == null)
                  {
                     spType = new SPType();
                     spType.setServiceURL(serviceURL);
                  }
               }
            }
            if (spType != null && !StringUtil.isNullOrEmpty(spType.getIdentityURL())
                  && !StringUtil.isNullOrEmpty(spType.getServiceURL()))
               break;
         }
      }
      return spType;
   }

   /**
    * Get the first metadata descriptor for an IDP
    * @param entitiesDescriptor
    * @return
    */
   public static IDPSSODescriptorType getIDPDescriptor(EntitiesDescriptorType entitiesDescriptor)
   {
      IDPSSODescriptorType idp = null;
      List<Object> entitiesList = entitiesDescriptor.getEntityDescriptor();
      for (Object theObject : entitiesList)
      {
         if (theObject instanceof EntitiesDescriptorType)
         {
            idp = getIDPDescriptor((EntitiesDescriptorType) theObject);
         }
         else if (theObject instanceof EntityDescriptorType)
         {
            idp = getIDPDescriptor((EntityDescriptorType) theObject);
         }
         if (idp != null)
         {
            break;
         }
      }
      return idp;
   }

   /**
    * Get the IDP metadata descriptor from an entity descriptor
    * @param entityDescriptor
    * @return
    */
   public static IDPSSODescriptorType getIDPDescriptor(EntityDescriptorType entityDescriptor)
   {
      List<EDTChoiceType> edtChoices = entityDescriptor.getChoiceType();
      for (EDTChoiceType edt : edtChoices)
      {
         List<EDTDescriptorChoiceType> edtDescriptors = edt.getDescriptors();
         for (EDTDescriptorChoiceType edtDesc : edtDescriptors)
         {
            IDPSSODescriptorType idpSSO = edtDesc.getIdpDescriptor();
            if (idpSSO != null)
            {
               return idpSSO;
            }
         }
      }
      return null;
   }

   /**
    * Get the SP Descriptor from an entity descriptor
    * @param entityDescriptor
    * @return
    */
   public static SPSSODescriptorType getSPDescriptor(EntityDescriptorType entityDescriptor)
   {
      List<EDTChoiceType> edtChoices = entityDescriptor.getChoiceType();
      for (EDTChoiceType edt : edtChoices)
      {
         List<EDTDescriptorChoiceType> edtDescriptors = edt.getDescriptors();
         for (EDTDescriptorChoiceType edtDesc : edtDescriptors)
         {
            SPSSODescriptorType spSSO = edtDesc.getSpDescriptor();
            if (spSSO != null)
            {
               return spSSO;
            }
         }
      }
      return null;
   }

   /**
    * Given a binding uri, get the IDP identity url
    * @param idp
    * @param bindingURI
    * @return
    */
   public static String getIdentityURL(IDPSSODescriptorType idp, String bindingURI)
   {
      String identityURL = null;

      List<EndpointType> endpoints = idp.getSingleSignOnService();
      for (EndpointType endpoint : endpoints)
      {
         if (endpoint.getBinding().toString().equals(bindingURI))
         {
            identityURL = endpoint.getLocation().toString();
            break;
         }

      }
      return identityURL;
   }

   /**
    * Get the service url for the SP
    * @param sp
    * @param bindingURI
    * @return
    */
   public static String getServiceURL(SPSSODescriptorType sp, String bindingURI)
   {
      String serviceURL = null;

      List<IndexedEndpointType> endpoints = sp.getAssertionConsumerService();
      for (IndexedEndpointType endpoint : endpoints)
      {
         if (endpoint.getBinding().toString().equals(bindingURI))
         {
            serviceURL = endpoint.getLocation().toString();
            break;
         }

      }
      return serviceURL;
   }

   /**
    * Get the IDP Type
    * @param idpSSODescriptor
    * @return
    */
   public static IDPType getIDPType(IDPSSODescriptorType idpSSODescriptor)
   {
      IDPType idp = new IDPType();

      List<EndpointType> endpoints = idpSSODescriptor.getSingleSignOnService();

      if (endpoints != null)
      {
         for (EndpointType endpoint : endpoints)
         {
            if (endpoint.getBinding().toString().equals(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get()))
            {
               idp.setIdentityURL(endpoint.getLocation().toString());
               break;
            }
         }
      }

      if (StringUtil.isNullOrEmpty(idp.getIdentityURL()))
      {
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "identity url");
      }
      return idp;
   }
}