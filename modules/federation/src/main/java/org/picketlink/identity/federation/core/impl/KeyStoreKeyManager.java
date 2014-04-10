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
package org.picketlink.identity.federation.core.impl;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.TrustKeyConfigurationException;
import org.picketlink.common.exceptions.TrustKeyProcessingException;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.util.EncryptionKeyUtil;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KeyStore based Trust Key Manager
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 22, 2009
 */
public class KeyStoreKeyManager implements TrustKeyManager {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * An map of secret keys alive only for the duration of the program. The keys are generated on the fly. If you need
     * sophisticated key storage, then a custom version of the {@code TrustKeyManager} needs to be written that either
     * uses a
     * secure thumb drive or a TPM module or a HSM module. Also see JBoss XMLKey.
     */
    private final Map<String, SecretKey> keys = new HashMap<String, SecretKey>();

    private final Map<String, Object> options = new HashMap<String, Object>();

    private final HashMap<String, String> domainAliasMap = new HashMap<String, String>();

    private final HashMap<String, String> authPropsMap = new HashMap<String, String>();

    private KeyStore ks = null;

    private String keyStoreURL;

    private char[] signingKeyPass;

    private String signingAlias;

    private String keyStorePass;

    public static final String KEYSTORE_URL = "KeyStoreURL";

    public static final String KEYSTORE_PASS = "KeyStorePass";

    public static final String SIGNING_KEY_PASS = "SigningKeyPass";

    public static final String SIGNING_KEY_ALIAS = "SigningKeyAlias";

    /**
     * @see TrustKeyManager#getSigningKey()
     */
    public PrivateKey getSigningKey() throws TrustKeyConfigurationException, TrustKeyProcessingException {
        try {
            initKeyStore();
            return (PrivateKey) ks.getKey(this.signingAlias, this.signingKeyPass);
        } catch (KeyStoreException e) {
            throw logger.keyStoreConfigurationError(e);
        } catch (NoSuchAlgorithmException e) {
            throw logger.keyStoreProcessingError(e);
        } catch (UnrecoverableKeyException e) {
            throw logger.keyStoreProcessingError(e);
        } catch (GeneralSecurityException e) {
            throw logger.keyStoreProcessingError(e);
        } catch (IOException e) {
            throw logger.keyStoreProcessingError(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.bindings.interfaces.TrustKeyManager#getSigningKeyPair()
     */
    public KeyPair getSigningKeyPair() throws TrustKeyConfigurationException, TrustKeyProcessingException {
        try {
            initKeyStore();
            PrivateKey privateKey = this.getSigningKey();
            PublicKey publicKey = KeyStoreUtil.getPublicKey(this.ks, this.signingAlias, this.signingKeyPass);
            return new KeyPair(publicKey, privateKey);
        } catch (KeyStoreException e) {
            throw logger.keyStoreConfigurationError(e);
        } catch (GeneralSecurityException e) {
            throw logger.keyStoreProcessingError(e);
        } catch (IOException e) {
            throw logger.keyStoreProcessingError(e);
        }
    }

    /**
     * @see TrustKeyManager#getCertificate(String)
     */
    public Certificate getCertificate(String alias) throws TrustKeyConfigurationException, TrustKeyProcessingException {
        try {
            initKeyStore();

            if (alias == null || alias.length() == 0)
                throw logger.keyStoreNullAlias();

            return ks.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw logger.keyStoreConfigurationError(e);
        } catch (GeneralSecurityException e) {
            throw logger.keyStoreProcessingError(e);
        } catch (IOException e) {
            throw logger.keyStoreProcessingError(e);
        }
    }

    /**
     * @see TrustKeyManager#getPublicKey(String)
     */
    public PublicKey getPublicKey(String alias) throws TrustKeyConfigurationException, TrustKeyProcessingException {
        PublicKey publicKey = null;

        try {
            initKeyStore();

            Certificate cert = ks.getCertificate(alias);
            if (cert != null)
                publicKey = cert.getPublicKey();
            else
                logger.keyStoreNullPublicKeyForAlias(alias);

            return publicKey;
        } catch (KeyStoreException e) {
            throw logger.keyStoreConfigurationError(e);
        } catch (GeneralSecurityException e) {
            throw logger.keyStoreProcessingError(e);
        } catch (IOException e) {
            throw logger.keyStoreProcessingError(e);
        }
    }

    /**
     * Get the validating public key <b>Note:</b>: The domain is mapped to an alias in the keystore
     *
     * @throws IOException
     * @see TrustKeyManager#getValidatingKey(String)
     * @see TrustKeyManager#getPublicKey(String)
     */
    public PublicKey getValidatingKey(String domain) throws TrustKeyConfigurationException, TrustKeyProcessingException {
        PublicKey publicKey = null;
        try {
            initKeyStore();

            String domainAlias = this.domainAliasMap.get(domain);
            if (domainAlias == null && domain.startsWith("[") && domain.endsWith("]")) {
                // in case of IPv6 URL we need to strip [ ], because JBoss DMR is not storing attribute enclosed using [] (treating it as list)
                domainAlias = this.domainAliasMap.get(domain.substring(1, domain.length() - 1));
            }

            if (domainAlias == null)
                throw logger.keyStoreMissingDomainAlias(domain);

            try {
                publicKey = KeyStoreUtil.getPublicKey(ks, domainAlias, this.keyStorePass.toCharArray());
            } catch (UnrecoverableKeyException urke) {
                // Try with the signing key pass
                publicKey = KeyStoreUtil.getPublicKey(ks, domainAlias, this.signingKeyPass);
            }
        } catch (KeyStoreException e) {
            throw logger.keyStoreConfigurationError(e);
        } catch (NoSuchAlgorithmException e) {
            throw logger.keyStoreProcessingError(e);
        } catch (GeneralSecurityException e) {
            throw logger.keyStoreProcessingError(e);
        } catch (IOException e) {
            throw logger.keyStoreProcessingError(e);
        }
        return publicKey;
    }

    private void initKeyStore() throws GeneralSecurityException, IOException {
        if (ks == null) {
            logger.keyStoreSetup();
            this.setUpKeyStore();
        }

        if (ks == null)
            throw logger.keyStoreNullStore();
    }

    /**
     * @see TrustKeyManager#setAuthProperties(List)
     */
    public void setAuthProperties(List<AuthPropertyType> authList) throws TrustKeyConfigurationException,
            TrustKeyProcessingException {
        for (AuthPropertyType auth : authList) {
            this.authPropsMap.put(auth.getKey(), auth.getValue());
        }

        this.keyStoreURL = this.authPropsMap.get(KEYSTORE_URL);
        this.keyStorePass = this.authPropsMap.get(KEYSTORE_PASS);

        this.signingAlias = this.authPropsMap.get(SIGNING_KEY_ALIAS);

        String keypass = this.authPropsMap.get(SIGNING_KEY_PASS);
        if (keypass == null || keypass.length() == 0)
            throw logger.keyStoreNullSigningKeyPass();
        this.signingKeyPass = keypass.toCharArray();
    }

    /**
     * @see TrustKeyManager#setValidatingAlias(List)
     */
    public void setValidatingAlias(List<KeyValueType> aliases) throws TrustKeyConfigurationException,
            TrustKeyProcessingException {
        for (KeyValueType alias : aliases) {
            domainAliasMap.put(alias.getKey(), alias.getValue());
        }
    }

    /**
     * @throws GeneralSecurityException
     * @see TrustKeyManager#getEncryptionKey(String)
     */
    public SecretKey getEncryptionKey(String domain, String encryptionAlgorithm, int keyLength)
            throws TrustKeyConfigurationException, TrustKeyProcessingException {
        SecretKey key = keys.get(domain);
        if (key == null) {
            try {
                key = EncryptionKeyUtil.getSecretKey(encryptionAlgorithm, keyLength);
            } catch (GeneralSecurityException e) {
                throw logger.keyStoreProcessingError(e);
            }
            keys.put(domain, key);
        }
        return key;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.TrustKeyManager#addAdditionalOption(java.lang.String,
     *      java.lang.Object)
     */
    public void addAdditionalOption(String key, Object value) {
        this.options.put(key, value);
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.TrustKeyManager#getAdditionalOption(java.lang.String)
     */
    public Object getAdditionalOption(String key) {
        return this.options.get(key);
    }

    private void setUpKeyStore() throws GeneralSecurityException, IOException {
        // Keystore URL/Pass can be either by configuration or on the HTTPS connector
        if (this.keyStoreURL == null) {
            this.keyStoreURL = SecurityActions.getProperty("javax.net.ssl.keyStore", null);
        }
        if (this.keyStorePass == null) {
            this.keyStorePass = SecurityActions.getProperty("javax.net.ssl.keyStorePassword", null);
        }

        InputStream is = this.getKeyStoreInputStream(this.keyStoreURL);
        ks = KeyStoreUtil.getKeyStore(is, keyStorePass.toCharArray());
    }

    /**
     * Seek the input stream to the KeyStore
     *
     * @param keyStore
     *
     * @return
     */
    private InputStream getKeyStoreInputStream(String keyStore) {
        InputStream is = null;

        try {
            // Try the file method
            File file = new File(keyStore);
            is = new FileInputStream(file);
        } catch (Exception e) {
            URL url = null;
            try {
                url = new URL(keyStore);
                is = url.openStream();
            } catch (Exception ex) {
                url = SecurityActions.loadResource(getClass(), keyStore);
                if (url != null) {
                    try {
                        is = url.openStream();
                    } catch (IOException e1) {
                    }
                }
            }
        }

        if (is == null) {
            // Try the user.home dir
            String userHome = SecurityActions.getSystemProperty("user.home", "") + "/jbid-keystore";
            File ksDir = new File(userHome);
            if (ksDir.exists()) {
                try {
                    is = new FileInputStream(new File(userHome + "/" + keyStore));
                } catch (FileNotFoundException e) {
                    is = null;
                }
            }
        }
        if (is == null)
            throw logger.keyStoreNotLocated(keyStore);
        return is;
    }
}