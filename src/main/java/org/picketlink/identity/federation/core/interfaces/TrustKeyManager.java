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
package org.picketlink.identity.federation.core.interfaces;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;

import javax.crypto.SecretKey;

import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.KeyValueType;

/**
 * Key Manager interface used in trust decisions
 * @author Anil.Saldhana@redhat.com
 * @since Jan 22, 2009
 */
public interface TrustKeyManager
{
   /**
    * Provide a set of properties used for authentication
    * into the storage of keys - keystore, ldap, db, HSM etc
    * @param authList
    * @throws {@link IOException}
    */
   void setAuthProperties(List<AuthPropertyType> authList) throws TrustKeyConfigurationException,
         TrustKeyProcessingException;

   /**
    * Set a list of (domain,alias) tuple to trust domains
    * The alias is a string that represents the validating key stored
    * for a domain
    * @param aliases
    * @throws {@link IOException}
    */
   void setValidatingAlias(List<KeyValueType> aliases) throws TrustKeyConfigurationException,
         TrustKeyProcessingException;

   /**
    * Get the Signing Key
    * @return
    * @throws {@link CertificateException}
    */
   PrivateKey getSigningKey() throws TrustKeyConfigurationException, TrustKeyProcessingException;

   /**
    * <p>
    * Constructs a {@code KeyPair} instance containing the signing key ({@code PrivateKey}) and associated
    * {@code PublicKey}.
    * </p>
    * 
    * @return the constructed {@code KeyPair} object.
    */
   KeyPair getSigningKeyPair() throws TrustKeyConfigurationException, TrustKeyProcessingException;

   /**
    * Get the certificate given an alias
    * @param alias
    * @return
    * @throws {@link CertificateException}
    */
   Certificate getCertificate(String alias) throws TrustKeyConfigurationException, TrustKeyProcessingException;

   /**
    * Get a Public Key given an alias
    * @param alias
    * @return
    * @throws {@link CertificateException}
    */
   PublicKey getPublicKey(String alias) throws TrustKeyConfigurationException, TrustKeyProcessingException;

   /**
    * Given a domain, obtain a secret key
    * @see {@code EncryptionKeyUtil}
    * @param domain
    * @param encryptionAlgorithm Encryption Algorithm
    * @param keyLength length of keys
    * @return 
    */
   SecretKey getEncryptionKey(String domain, String encryptionAlgorithm, int keyLength)
         throws TrustKeyConfigurationException, TrustKeyProcessingException;

   /**
    * Get the Validating Public Key of the domain
    * @param domain
    * @return 
    */
   PublicKey getValidatingKey(String domain) throws TrustKeyConfigurationException, TrustKeyProcessingException;

   /**
    * Add general options
    * @param key
    * @param value
    */
   void addAdditionalOption(String key, Object value);

   /**
    * Get additional option
    * @param key
    * @return
    */
   Object getAdditionalOption(String key);
}