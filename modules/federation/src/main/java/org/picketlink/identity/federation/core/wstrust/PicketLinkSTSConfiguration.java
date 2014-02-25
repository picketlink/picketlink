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
package org.picketlink.identity.federation.core.wstrust;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.ClaimsProcessorType;
import org.picketlink.config.federation.ClaimsProcessorsType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.STSType;
import org.picketlink.config.federation.ServiceProviderType;
import org.picketlink.config.federation.ServiceProvidersType;
import org.picketlink.config.federation.TokenProviderType;
import org.picketlink.config.federation.TokenProvidersType;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.core.sts.STSCoreConfig;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;

import javax.xml.namespace.QName;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Standard JBoss STS configuration implementation.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author <a href="mailto:asaldhan@redhat.com">Anil Saldhana</a>
 */
public class PicketLinkSTSConfiguration implements STSConfiguration {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    // the delegate contains all the information extracted from the picketlink-sts.xml configuration file.
    private final STSType delegate;

    private final Map<String, SecurityTokenProvider> tokenProviders = new HashMap<String, SecurityTokenProvider>();

    private final Map<String, ServiceProviderType> spMetadata = new HashMap<String, ServiceProviderType>();

    private final Map<String, ServiceProviderType> spRegExMetadata = new HashMap<String, ServiceProviderType>();

    private final Map<String, ClaimsProcessor> claimsProcessors = new HashMap<String, ClaimsProcessor>();

    private TrustKeyManager trustManager;
    private WSTrustRequestHandler handler;
    private String certificateAlias;

    /**
     * <p>
     * Creates an instance of {@code PicketLinkSTSConfiguration} with default configuration values.
     * </p>
     */
    public PicketLinkSTSConfiguration() {
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
    public PicketLinkSTSConfiguration(STSType config) {
        this.delegate = config;
        // set the default request handler if one hasn't been specified.
        if (this.delegate.getRequestHandler() == null)
            this.delegate.setRequestHandler(StandardRequestHandler.class.getCanonicalName());

        // build the token-provider maps.
        TokenProvidersType providers = this.delegate.getTokenProviders();
        if (providers != null) {
            for (TokenProviderType provider : providers.getTokenProvider()) {
                // get the properties that have been configured for the token provider.
                Map<String, String> properties = new HashMap<String, String>();

                List<KeyValueType> providerPropertiesList;
                try {
                    providerPropertiesList = CoreConfigUtil.getProperties(provider);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }

                properties.put(GeneralConstants.ASSERTIONS_VALIDITY, String.valueOf(config.getTokenTimeout()));
                properties.put(GeneralConstants.CLOCK_SKEW, String.valueOf(config.getClockSkew()));

                for (KeyValueType propertyType : providerPropertiesList) {
                    properties.put(propertyType.getKey(), propertyType.getValue());
                }

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
        if (processors != null) {
            for (ClaimsProcessorType processor : processors.getClaimsProcessor()) {
                // get the properties that have been configured for the claims processor.
                Map<String, String> properties = new HashMap<String, String>();
                List<KeyValueType> processorPropertiesList;
                try {
                    processorPropertiesList = CoreConfigUtil.getProperties(processor);
                } catch (GeneralSecurityException e) {
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
        //if the provider.getEndpoint() not null the provider will be added to spMetaData
        //if the provider.getEndpointRegEx() not null the provider will be added to spRegExMetadata
        ServiceProvidersType serviceProviders = this.delegate.getServiceProviders();
        if (serviceProviders != null) {
            for (ServiceProviderType provider : serviceProviders.getServiceProvider()) {
                if (provider.getEndpoint() != null) {
                    this.spMetadata.put(provider.getEndpoint(), provider);
                }
                if (provider.getEndpointRegEx() != null) {
                    this.spRegExMetadata.put(provider.getEndpointRegEx(), provider);
                }

            }
        }



        // setup the key store.
        KeyProviderType keyProviderType = config.getKeyProvider();
        if (keyProviderType != null) {
            String keyManagerClassName = keyProviderType.getClassName();
            try {
                // Decrypt/de-mask the passwords if any
                List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProviderType);

                Class<?> clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);
                if (clazz == null)
                    throw logger.classNotLoadedError(keyManagerClassName);
                this.trustManager = (TrustKeyManager) clazz.newInstance();
                this.trustManager.setAuthProperties(authProperties);
                this.trustManager.setValidatingAlias(keyProviderType.getValidatingAlias());

                //Special case when you need X509Data in SignedInfo
                if (authProperties != null) {
                    for (AuthPropertyType authPropertyType : authProperties) {
                        String key = authPropertyType.getKey();
                        if (GeneralConstants.X509CERTIFICATE.equals(key)) {
                            //we need X509Certificate in SignedInfo. The value is the alias name
                            trustManager.addAdditionalOption(GeneralConstants.X509CERTIFICATE, authPropertyType.getValue());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                throw logger.stsUnableToConstructKeyManagerError(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getSTSName()
     */
    public String getSTSName() {
        return this.delegate.getSTSName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getEncryptIssuedToken()
     */
    public boolean encryptIssuedToken() {
        return this.delegate.isEncryptToken();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#signIssuedToken()
     */
    public boolean signIssuedToken() {
        return this.delegate.isSignToken();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getIssuedTokenTimeout()
     */
    public long getIssuedTokenTimeout() {
        // return the timeout value in milliseconds.
        return this.delegate.getTokenTimeout() * 1000;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getRequestHandlerClass()
     */
    public WSTrustRequestHandler getRequestHandler() {
        if (this.handler == null)
            this.handler = WSTrustServiceFactory.getInstance().createRequestHandler(this.delegate.getRequestHandler(), this);
        return this.handler;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getProviderForService(java.lang.String)
     */
    public SecurityTokenProvider getProviderForService(String serviceName) {
        if (serviceName == null)
            throw logger.nullArgumentError("serviceName");

        ServiceProviderType provider = this.spMetadata.get(serviceName);
        if (provider != null) {
            return this.tokenProviders.get(provider.getTokenType());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getProviderForTokenType(java.lang.String)
     */
    public SecurityTokenProvider getProviderForTokenType(String tokenType) {
        if (tokenType == null)
            throw logger.nullArgumentError("tokenType");
        return this.tokenProviders.get(tokenType);
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.STSCoreConfig#getProviderForTokenElementNS(java.lang.String,
     *      javax.xml.namespace.QName)
     */
    public SecurityTokenProvider getProviderForTokenElementNS(String family, QName tokenQName) {
        return this.tokenProviders.get(family + "$" + tokenQName.getLocalPart() + "$" + tokenQName.getNamespaceURI());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getClaimsProcessor(java.lang.String)
     */
    public ClaimsProcessor getClaimsProcessor(String claimsDialect) {
        return this.claimsProcessors.get(claimsDialect);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getTokenTypeForService(java.lang.String)
     */
    public String getTokenTypeForService(String serviceName) {
        ServiceProviderType provider = this.spMetadata.get(serviceName);
        if (provider != null)
            return provider.getTokenType();

        Set<String> keys = this.spRegExMetadata.keySet();

        for (String next : keys) {
            if (Pattern.matches(next, serviceName)) {
                return this.spRegExMetadata.get(next).getTokenType();
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getServiceProviderPublicKey(java.lang.String)
     */
    public PublicKey getServiceProviderPublicKey(String serviceName) {
        PublicKey key = null;
        if (this.trustManager != null) {
            try {
                // try using the truststore alias from the service provider metadata.
                ServiceProviderType provider = this.spMetadata.get(serviceName);
                if (provider != null && provider.getTruststoreAlias() != null) {
                    key = this.trustManager.getPublicKey(provider.getTruststoreAlias());
                }
                // if there was no truststore alias or no PKC under that alias, use the KeyProvider mapping.
                if (key == null) {
                    key = this.trustManager.getValidatingKey(serviceName);
                }
            } catch (Exception e) {
                throw logger.stsPublicKeyError(serviceName, e);
            }
        }
        return key;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getServiceProviderPublicKey(java.lang.String)
     */
    public X509Certificate getServiceProviderCertificate(String serviceName) {
        X509Certificate x509 = null;

        if (this.trustManager != null) {
            try {
                // try using the truststore alias from the service provider metadata.
                ServiceProviderType provider = this.spMetadata.get(serviceName);
                if (provider != null && provider.getTruststoreAlias() != null) {
                    Certificate cer = this.trustManager.getCertificate(provider.getTruststoreAlias());

                    if (cer instanceof X509Certificate) {
                        x509 = (X509Certificate) cer;
                    }
                }

                // try using the truststore alias from the service provider with regex metadata.

                Set<String> keys = this.spRegExMetadata.keySet();

                for (String next : keys) {
                    if (Pattern.matches(next, serviceName)) {
                        ServiceProviderType providerRegEx = this.spRegExMetadata.get(next);
                        if (providerRegEx != null && providerRegEx.getTruststoreAlias() != null) {
                            Certificate cer = this.trustManager.getCertificate(providerRegEx.getTruststoreAlias());

                            if (cer instanceof X509Certificate) {
                                x509 = (X509Certificate) cer;
                            }
                        }
                    }
                }


                // if there was no truststore alias or no PKC under that alias, use the KeyProvider mapping.
                if (x509 == null) {
                    Certificate cer = this.trustManager.getCertificate(serviceName);

                    if (cer instanceof X509Certificate) {
                        x509 = (X509Certificate) cer;
                    }
                }
            } catch (Exception e) {
                throw logger.stsPublicKeyError(serviceName, e);
            }
        }
        return x509;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getSTSKeyPair()
     */
    public KeyPair getSTSKeyPair() {
        KeyPair keyPair = null;
        if (this.trustManager != null) {
            try {
                keyPair = this.trustManager.getSigningKeyPair();
            } catch (Exception e) {
                throw logger.stsSigningKeyPairError(e);
            }
        }
        return keyPair;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.STSConfiguration#getCertificate(java.lang.String)
     */
    public Certificate getCertificate(String alias) {
        Certificate certificate = null;
        if (this.trustManager != null) {
            try {
                certificate = trustManager.getCertificate(alias);
            } catch (Exception e) {
                throw logger.stsPublicKeyCertError(e);
            }
        }
        return certificate;
    }

    /**
     * @see STSConfiguration#getXMLDSigCanonicalizationMethod()
     */
    public String getXMLDSigCanonicalizationMethod() {
        return delegate.getCanonicalizationMethod();
    }

    /**
     * @see {@code STSCoreConfig#addTokenProvider(String, SecurityTokenProvider)}
     */
    public void addTokenProvider(String key, SecurityTokenProvider provider) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        tokenProviders.put(key, provider);

        QName tokenQName = provider.getSupportedQName();
        if (tokenQName != null) {
            String tokenElementAndNS = provider.family() + "$" + tokenQName.getLocalPart() + "$" + tokenQName.getNamespaceURI();

            this.tokenProviders.put(tokenElementAndNS, provider);
        }
    }

    /**
     * @see {@code STSCoreConfig#removeTokenProvider(String)}
     */
    public void removeTokenProvider(String key) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        tokenProviders.remove(key);
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.STSCoreConfig#getTokenProviders()
     */
    public List<SecurityTokenProvider> getTokenProviders() {
        List<SecurityTokenProvider> list = new ArrayList<SecurityTokenProvider>();
        list.addAll(tokenProviders.values());
        return Collections.unmodifiableList(list);
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.STSCoreConfig#getProvidersByFamily(java.lang.String)
     */
    public List<SecurityTokenProvider> getProvidersByFamily(String familyName) {
        List<SecurityTokenProvider> result = new ArrayList<SecurityTokenProvider>();
        for (SecurityTokenProvider provider : tokenProviders.values()) {
            if (provider.family().equals(familyName))
                result.add(provider);
        }
        return result;
    }

    @Override
    public String getSigningCertificateAlias() {
        //Check keymanager
        if (certificateAlias == null) {
            certificateAlias = (String) trustManager.getAdditionalOption(GeneralConstants.X509CERTIFICATE);
        }
        return certificateAlias;
    }

    public void setSigningCertificateAlias(String alias) {
        this.certificateAlias = alias;
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.STSCoreConfig#copy(org.picketlink.identity.federation.core.sts.STSCoreConfig)
     */
    public void copy(STSCoreConfig thatConfig) {
        if (thatConfig instanceof PicketLinkSTSConfiguration) {
            PicketLinkSTSConfiguration pc = (PicketLinkSTSConfiguration) thatConfig;
            this.tokenProviders.putAll(pc.tokenProviders);
            this.claimsProcessors.putAll(pc.claimsProcessors);
        } else
            throw new RuntimeException("Unknown config :" + thatConfig); // TODO: Handle other configuration
    }

    @Override
    public String toString() {
        return "PicketLinkSTSConfiguration [delegate=" + delegate + ", tokenProviders=" + tokenProviders + ", spMetadata="
                + spMetadata + ", claimsProcessors=" + claimsProcessors + ", trustManager=" + trustManager + ", handler="
                + handler + "]";
    }
}