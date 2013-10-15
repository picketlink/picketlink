/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.core.util;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.PBEUtils;
import org.picketlink.common.util.StringUtil;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.ClaimsProcessorType;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.MetadataProviderType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.config.federation.TokenProviderType;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;
import org.picketlink.identity.federation.core.interfaces.IMetadataProvider;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IndexedEndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.servlet.ServletContext;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.picketlink.common.util.StringUtil.isNotNull;

/**
 * Utility for configuration
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 13, 2009
 */
public class CoreConfigUtil {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Given either the IDP Configuration or the SP Configuration, derive the TrustKeyManager
     *
     * @param idpOrSPConfiguration
     *
     * @return
     */
    public static TrustKeyManager getTrustKeyManager(ProviderType idpOrSPConfiguration) {
        KeyProviderType keyProvider = idpOrSPConfiguration.getKeyProvider();
        return getTrustKeyManager(keyProvider);
    }

    /**
     * Once the {@code KeyProviderType} is derived, get the {@code TrustKeyManager}
     *
     * @param keyProvider
     *
     * @return
     */
    public static TrustKeyManager getTrustKeyManager(KeyProviderType keyProvider) {
        TrustKeyManager trustKeyManager = null;
        try {
            String keyManagerClassName = keyProvider.getClassName();
            if (keyManagerClassName == null)
                throw logger.nullValueError("KeyManager class name");

            Class<?> clazz = SecurityActions.loadClass(CoreConfigUtil.class, keyManagerClassName);
            if (clazz == null)
                throw logger.classNotLoadedError(keyManagerClassName);
            trustKeyManager = (TrustKeyManager) clazz.newInstance();
        } catch (Exception e) {
            logger.trustKeyManagerCreationError(e);
        }
        return trustKeyManager;
    }

    /**
     * Get the validating key
     *
     * @param idpSpConfiguration
     * @param domain
     *
     * @return
     *
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    public static PublicKey getValidatingKey(ProviderType idpSpConfiguration, String domain) throws ConfigurationException,
            ProcessingException {
        TrustKeyManager trustKeyManager = getTrustKeyManager(idpSpConfiguration);

        return getValidatingKey(trustKeyManager, domain);
    }

    /**
     * Get the validating key given the trust key manager
     *
     * @param trustKeyManager
     * @param domain
     *
     * @return
     *
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    public static PublicKey getValidatingKey(TrustKeyManager trustKeyManager, String domain) throws ConfigurationException,
            ProcessingException {
        if (trustKeyManager == null)
            throw logger.nullValueError("Trust Key Manager");

        return trustKeyManager.getValidatingKey(domain);
    }

    /**
     * Given a {@code KeyProviderType}, return the list of auth properties that have been decrypted for any masked
     * password
     *
     * @param keyProviderType
     *
     * @return
     *
     * @throws GeneralSecurityException
     */
    @SuppressWarnings("unchecked")
    public static List<AuthPropertyType> getKeyProviderProperties(KeyProviderType keyProviderType)
            throws GeneralSecurityException {
        List<AuthPropertyType> authProperties = keyProviderType.getAuth();
        if (decryptionNeeded(authProperties))
            authProperties = decryptPasswords(authProperties);

        return authProperties;
    }

    /**
     * Given a {@code TokenProviderType}, return the list of properties that have been decrypted for any masked
     * property
     * value
     *
     * @param tokenProviderType
     *
     * @return
     *
     * @throws GeneralSecurityException
     */
    @SuppressWarnings("unchecked")
    public static List<KeyValueType> getProperties(TokenProviderType tokenProviderType) throws GeneralSecurityException {
        List<KeyValueType> keyValueTypeList = tokenProviderType.getProperty();
        if (decryptionNeeded(keyValueTypeList))
            keyValueTypeList = decryptPasswords(keyValueTypeList);

        return keyValueTypeList;
    }

    /**
     * Given a {@code ClaimsProcessorType}, return the list of properties that have been decrypted for any masked
     * property value
     *
     * @param claimsProcessorType
     *
     * @return
     *
     * @throws GeneralSecurityException
     */
    @SuppressWarnings("unchecked")
    public static List<KeyValueType> getProperties(ClaimsProcessorType claimsProcessorType) throws GeneralSecurityException {
        List<KeyValueType> keyValueTypeList = claimsProcessorType.getProperty();
        if (decryptionNeeded(keyValueTypeList))
            keyValueTypeList = decryptPasswords(keyValueTypeList);

        return keyValueTypeList;
    }

    /**
     * Given a key value list, check if decrypt of any properties is needed. Unless one of the keys is "salt", we
     * cannot
     * figure out is decrypt is needed
     *
     * @param keyValueList
     *
     * @return
     */
    public static boolean decryptionNeeded(List<? extends KeyValueType> keyValueList) {
        int length = keyValueList.size();

        // Let us run through the list to see if there is any salt
        for (int i = 0; i < length; i++) {
            KeyValueType kvt = keyValueList.get(i);

            String key = kvt.getKey();
            if (PicketLinkFederationConstants.SALT.equalsIgnoreCase(key))
                return true;
        }
        return false;
    }

    /**
     * Given a key value pair read from PicketLink configuration, ensure that we replace the masked passwords with the
     * decoded passwords and pass it back
     *
     * @param keyValueList
     *
     * @return
     *
     * @throws GeneralSecurityException
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private static List decryptPasswords(List keyValueList) throws GeneralSecurityException {
        String pbeAlgo = PicketLinkFederationConstants.PBE_ALGORITHM;

        String salt = null;
        int iterationCount = 0;

        int length = keyValueList.size();

        // Let us run through the list to see if there is any salt
        for (int i = 0; i < length; i++) {
            KeyValueType kvt = (KeyValueType) keyValueList.get(i);

            String key = kvt.getKey();
            if (PicketLinkFederationConstants.SALT.equalsIgnoreCase(key))
                salt = kvt.getValue();
            if (PicketLinkFederationConstants.ITERATION_COUNT.equalsIgnoreCase(key))
                iterationCount = Integer.parseInt(kvt.getValue());
        }

        if (salt == null)
            return keyValueList;

        // Ok. there is a salt configured. So we have some properties with masked values
        List<KeyValueType> returningList = new ArrayList<KeyValueType>();

        // Create the PBE secret key
        SecretKeyFactory factory = SecretKeyFactory.getInstance(pbeAlgo);

        char[] password = "somearbitrarycrazystringthatdoesnotmatter".toCharArray();
        PBEParameterSpec cipherSpec = new PBEParameterSpec(salt.getBytes(), iterationCount);
        PBEKeySpec keySpec = new PBEKeySpec(password);
        SecretKey cipherKey = factory.generateSecret(keySpec);

        for (int i = 0; i < length; i++) {
            KeyValueType kvt = (KeyValueType) keyValueList.get(i);

            String val = kvt.getValue();
            if (val.startsWith(PicketLinkFederationConstants.PASS_MASK_PREFIX)) {
                val = val.substring(PicketLinkFederationConstants.PASS_MASK_PREFIX.length());
                String decodedValue;
                try {
                    decodedValue = PBEUtils.decode64(val, pbeAlgo, cipherKey, cipherSpec);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

                KeyValueType newKVT = new KeyValueType();
                if (keyValueList.get(0) instanceof AuthPropertyType)
                    newKVT = new AuthPropertyType();
                newKVT.setKey(kvt.getKey());
                newKVT.setValue(new String(decodedValue));
                returningList.add(newKVT);
            } else {
                returningList.add(kvt);
            }
        }

        return returningList;
    }

    /**
     * Given a metadata {@link EntityDescriptorType}, construct the Service provider configuration
     *
     * @param entityDescriptor
     * @param bindingURI
     *
     * @return
     */
    public static ProviderType getSPConfiguration(EntityDescriptorType entityDescriptor, String bindingURI) {
        SPType spType = new SPType();
        String identityURL = null;
        String serviceURL = null;

        if (identityURL == null) {
            IDPSSODescriptorType idpSSO = getIDPDescriptor(entityDescriptor);
            if (idpSSO != null) {
                identityURL = getIdentityURL(idpSSO, bindingURI);
            }
            spType.setIdentityURL(identityURL);
        }
        if (serviceURL == null) {
            SPSSODescriptorType spSSO = getSPDescriptor(entityDescriptor);
            if (spSSO != null) {
                serviceURL = getServiceURL(spSSO, bindingURI);
            }
            spType.setServiceURL(serviceURL);
        }
        return spType;
    }

    /**
     * Given a metadata {@link EntityDescriptorType}, construct the Service provider configuration
     *
     * @param entityDescriptor
     * @param bindingURI
     *
     * @return
     *
     * @throws ConfigurationException
     */
    public static SPType getSPConfiguration(EntitiesDescriptorType entitiesDescriptor, String bindingURI)
            throws ConfigurationException {
        SPType spType = new SPType();

        List<Object> list = entitiesDescriptor.getEntityDescriptor();

        IDPSSODescriptorType idpSSO = null;
        SPSSODescriptorType spSSO = null;

        if (list != null) {
            for (Object theObject : list) {
                if (theObject instanceof EntitiesDescriptorType) {
                    spType = getSPConfiguration((EntitiesDescriptorType) theObject, bindingURI);
                } else if (theObject instanceof EntityDescriptorType) {
                    if (idpSSO == null) {
                        // Ideally we should lookup the IDP metadata considering the specs. For now the IDP metadata must be
                        // defined within the SP metadata file.
                        idpSSO = getIDPDescriptor((EntityDescriptorType) theObject);
                    }

                    if (spSSO == null) {
                        spSSO = getSPDescriptor((EntityDescriptorType) theObject);
                    }
                }
            }

            if (idpSSO == null) {
                throw logger.samlMetaDataNoIdentityProviderDefined();
            }

            if (spSSO == null) {
                throw logger.samlMetaDataNoServiceProviderDefined();
            }

            String identityURL = getIdentityURL(idpSSO, bindingURI);

            if (identityURL == null) {
                throw logger.samlMetaDataNoIdentityProviderDefined();
            }

            spType.setIdentityURL(identityURL);
            spType.setLogoutUrl(getLogoutURL(idpSSO, bindingURI));
            spType.setLogoutResponseLocation(getLogoutResponseLocation(idpSSO, bindingURI));

            String serviceURL = getServiceURL(spSSO, bindingURI);

            if (serviceURL == null) {
                throw logger.samlMetaDataNoServiceProviderDefined();
            }

            spType.setServiceURL(serviceURL);
        }

        return spType;
    }

    /**
     * Get the first metadata descriptor for an IDP
     *
     * @param entitiesDescriptor
     *
     * @return
     */
    public static IDPSSODescriptorType getIDPDescriptor(EntitiesDescriptorType entitiesDescriptor) {
        IDPSSODescriptorType idp = null;
        List<Object> entitiesList = entitiesDescriptor.getEntityDescriptor();
        for (Object theObject : entitiesList) {
            if (theObject instanceof EntitiesDescriptorType) {
                idp = getIDPDescriptor((EntitiesDescriptorType) theObject);
            } else if (theObject instanceof EntityDescriptorType) {
                idp = getIDPDescriptor((EntityDescriptorType) theObject);
            }
            if (idp != null) {
                break;
            }
        }
        return idp;
    }

    /**
     * Get the IDP metadata descriptor from an entity descriptor
     *
     * @param entityDescriptor
     *
     * @return
     */
    public static IDPSSODescriptorType getIDPDescriptor(EntityDescriptorType entityDescriptor) {
        List<EDTChoiceType> edtChoices = entityDescriptor.getChoiceType();
        for (EDTChoiceType edt : edtChoices) {
            List<EDTDescriptorChoiceType> edtDescriptors = edt.getDescriptors();
            for (EDTDescriptorChoiceType edtDesc : edtDescriptors) {
                IDPSSODescriptorType idpSSO = edtDesc.getIdpDescriptor();
                if (idpSSO != null) {
                    return idpSSO;
                }
            }
        }
        return null;
    }

    /**
     * Get the SP Descriptor from an entity descriptor
     *
     * @param entityDescriptor
     *
     * @return
     */
    public static SPSSODescriptorType getSPDescriptor(EntityDescriptorType entityDescriptor) {
        List<EDTChoiceType> edtChoices = entityDescriptor.getChoiceType();
        for (EDTChoiceType edt : edtChoices) {
            List<EDTDescriptorChoiceType> edtDescriptors = edt.getDescriptors();
            for (EDTDescriptorChoiceType edtDesc : edtDescriptors) {
                SPSSODescriptorType spSSO = edtDesc.getSpDescriptor();
                if (spSSO != null) {
                    return spSSO;
                }
            }
        }
        return null;
    }

    /**
     * Given a binding uri, get the IDP identity url
     *
     * @param idp
     * @param bindingURI
     *
     * @return
     */
    public static String getIdentityURL(IDPSSODescriptorType idp, String bindingURI) {
        String identityURL = null;

        List<EndpointType> endpoints = idp.getSingleSignOnService();
        for (EndpointType endpoint : endpoints) {
            if (endpoint.getBinding().toString().equals(bindingURI)) {
                identityURL = endpoint.getLocation().toString();
                break;
            }

        }
        return identityURL;
    }

    /**
     * Given a binding uri, get the IDP identity url
     *
     * @param idp
     * @param bindingURI
     *
     * @return
     */
    public static String getLogoutURL(IDPSSODescriptorType idp, String bindingURI) {
        String logoutURL = null;

        List<EndpointType> endpoints = idp.getSingleLogoutService();
        for (EndpointType endpoint : endpoints) {
            if (endpoint.getBinding().toString().equals(bindingURI)) {
                logoutURL = endpoint.getLocation().toString();
                break;
            }

        }
        return logoutURL;
    }

    /**
     * Given a binding uri, get the IDP logout response url (used for global logouts)
     */
    public static String getLogoutResponseLocation(IDPSSODescriptorType idp, String bindingURI) {
        String logoutResponseLocation = null;

        List<EndpointType> endpoints = idp.getSingleLogoutService();
        for (EndpointType endpoint : endpoints) {
            if (endpoint.getBinding().toString().equals(bindingURI)) {
                if (endpoint.getResponseLocation() != null) {
                    logoutResponseLocation = endpoint.getResponseLocation().toString();
                } else {
                    logoutResponseLocation = null;
                }

                break;
            }

        }
        return logoutResponseLocation;
    }

    /**
     * Get the service url for the SP
     *
     * @param sp
     * @param bindingURI
     *
     * @return
     */
    public static String getServiceURL(SPSSODescriptorType sp, String bindingURI) {
        String serviceURL = null;

        List<IndexedEndpointType> endpoints = sp.getAssertionConsumerService();
        for (IndexedEndpointType endpoint : endpoints) {
            if (endpoint.getBinding().toString().equals(bindingURI)) {
                serviceURL = endpoint.getLocation().toString();
                break;
            }

        }
        return serviceURL;
    }

    /**
     * Get the IDP Type
     *
     * @param idpSSODescriptor
     *
     * @return
     */
    public static IDPType getIDPType(IDPSSODescriptorType idpSSODescriptor) {
        IDPType idp = new IDPType();

        List<EndpointType> endpoints = idpSSODescriptor.getSingleSignOnService();

        if (endpoints != null) {
            for (EndpointType endpoint : endpoints) {
                if (endpoint.getBinding().toString().equals(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get())) {
                    idp.setIdentityURL(endpoint.getLocation().toString());
                    break;
                }
            }
        }

        if (StringUtil.isNullOrEmpty(idp.getIdentityURL())) {
            throw logger.nullValueError("identity url");
        }
        return idp;
    }

    /**
     * Read metadata from ProviderType
     *
     * @param providerType
     * @param servletContext
     *
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<EntityDescriptorType> getMetadataConfiguration(ProviderType providerType, ServletContext servletContext) {
        MetadataProviderType metadataProviderType = providerType.getMetaDataProvider();

        if (metadataProviderType == null) {
            return null;
        }

        String fqn = metadataProviderType.getClassName();
        Class<?> clazz = SecurityActions.loadClass(CoreConfigUtil.class, fqn);
        IMetadataProvider metadataProvider;
        try {
            metadataProvider = (IMetadataProvider) clazz.newInstance();
        } catch (Exception iae) {
            throw new RuntimeException(iae);
        }

        List<KeyValueType> keyValues = metadataProviderType.getOption();
        Map<String, String> options = new HashMap<String, String>();
        if (keyValues != null) {
            for (KeyValueType kvt : keyValues)
                options.put(kvt.getKey(), kvt.getValue());
        }
        metadataProvider.init(options);

        String fileInjectionStr = metadataProvider.requireFileInjection();
        if (isNotNull(fileInjectionStr)) {
            metadataProvider.injectFileStream(servletContext.getResourceAsStream(fileInjectionStr));
        }

        List<EntityDescriptorType> resultList = new ArrayList<EntityDescriptorType>();
        if (metadataProvider.isMultiple()) {
            EntitiesDescriptorType metadatas = (EntitiesDescriptorType) metadataProvider.getMetaData();
            addAllEntityDescriptorsRecursively(resultList, metadatas);
        } else {
            EntityDescriptorType metadata = (EntityDescriptorType) metadataProvider.getMetaData();
            resultList.add(metadata);
        }
        return resultList;
    }

    private static void addAllEntityDescriptorsRecursively(List<EntityDescriptorType> resultList,
                                                           EntitiesDescriptorType entitiesDescriptorType) {
        List<Object> entities = entitiesDescriptorType.getEntityDescriptor();
        for (Object o : entities) {
            if (o instanceof EntitiesDescriptorType) {
                addAllEntityDescriptorsRecursively(resultList, (EntitiesDescriptorType) o);
            } else if (o instanceof EntityDescriptorType) {
                resultList.add((EntityDescriptorType) o);
            } else {
                throw new IllegalArgumentException("Wrong type: " + o.getClass());
            }
        }
    }
}