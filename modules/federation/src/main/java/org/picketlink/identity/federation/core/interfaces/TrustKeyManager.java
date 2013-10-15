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
package org.picketlink.identity.federation.core.interfaces;

import org.picketlink.common.exceptions.TrustKeyConfigurationException;
import org.picketlink.common.exceptions.TrustKeyProcessingException;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.KeyValueType;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;

/**
 * Key Manager interface used in trust decisions
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 22, 2009
 */
public interface TrustKeyManager {

    /**
     * Provide a set of properties used for authentication into the storage of keys - keystore, ldap, db, HSM etc
     *
     * @param authList
     *
     * @throws {@link IOException}
     */
    void setAuthProperties(List<AuthPropertyType> authList) throws TrustKeyConfigurationException, TrustKeyProcessingException;

    /**
     * Set a list of (domain,alias) tuple to trust domains The alias is a string that represents the validating key
     * stored for a
     * domain
     *
     * @param aliases
     *
     * @throws {@link IOException}
     */
    void setValidatingAlias(List<KeyValueType> aliases) throws TrustKeyConfigurationException, TrustKeyProcessingException;

    /**
     * Get the Signing Key
     *
     * @return
     *
     * @throws {@link CertificateException}
     */
    PrivateKey getSigningKey() throws TrustKeyConfigurationException, TrustKeyProcessingException;

    /**
     * <p>
     * Constructs a {@code KeyPair} instance containing the signing key ({@code PrivateKey}) and associated {@code
     * PublicKey}.
     * </p>
     *
     * @return the constructed {@code KeyPair} object.
     */
    KeyPair getSigningKeyPair() throws TrustKeyConfigurationException, TrustKeyProcessingException;

    /**
     * Get the certificate given an alias
     *
     * @param alias
     *
     * @return
     *
     * @throws {@link CertificateException}
     */
    Certificate getCertificate(String alias) throws TrustKeyConfigurationException, TrustKeyProcessingException;

    /**
     * Get a Public Key given an alias
     *
     * @param alias
     *
     * @return
     *
     * @throws {@link CertificateException}
     */
    PublicKey getPublicKey(String alias) throws TrustKeyConfigurationException, TrustKeyProcessingException;

    /**
     * Given a domain, obtain a secret key
     *
     * @param domain
     * @param encryptionAlgorithm Encryption Algorithm
     * @param keyLength length of keys
     *
     * @return
     *
     * @see {@code EncryptionKeyUtil}
     */
    SecretKey getEncryptionKey(String domain, String encryptionAlgorithm, int keyLength) throws TrustKeyConfigurationException,
            TrustKeyProcessingException;

    /**
     * Get the Validating Public Key of the domain
     *
     * @param domain
     *
     * @return
     */
    PublicKey getValidatingKey(String domain) throws TrustKeyConfigurationException, TrustKeyProcessingException;

    /**
     * Add general options
     *
     * @param key
     * @param value
     */
    void addAdditionalOption(String key, Object value);

    /**
     * Get additional option
     *
     * @param key
     *
     * @return
     */
    Object getAdditionalOption(String key);
}