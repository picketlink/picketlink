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
package org.picketlink.identity.federation.core.sts;

import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;

import javax.xml.namespace.QName;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Configuration for the STS Core
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author Anil.Saldhana@redhat.com
 * @since Dec 27, 2010
 */
public interface STSCoreConfig {

    /**
     * @return the certificate alias name
     *
     * @since v2.5.0
     */
    String getSigningCertificateAlias();

    void copy(STSCoreConfig thatConfig);

    /**
     * <p>
     * Obtains the unique name of the secure token service.
     * </p>
     *
     * @return a {@code String} representing the STS name.
     */
    String getSTSName();

    /**
     * <p>
     * Indicates whether the issued token should be encrypted or not.
     * </p>
     *
     * @return {@code true} if the issued token is to be encrypted; {@code false} otherwise.
     */
    boolean encryptIssuedToken();

    /**
     * <p>
     * Indicates whether the issued token should be digitally signed or not.
     * </p>
     *
     * @return {@code true} if the issued token is to be signed; {@code false} otherwise.
     */
    boolean signIssuedToken();

    /**
     * <p>
     * Obtains the timeout value (in milliseconds) for issued tokens.
     * </p>
     *
     * @return the token timeout value.
     */
    long getIssuedTokenTimeout();

    /**
     * <p>
     * Obtains a reference to the {@code KeyPair} object that contains the STS {@code PrivateKey} and {@code
     * PublicKey}.
     * </p>
     *
     * @return a reference to the STS {@code KeyPair}.
     */
    KeyPair getSTSKeyPair();

    /**
     * <p>
     * Given the name of a service provider, obtains the type of the token that should be used when issuing tokens to
     * clients of
     * that service.
     * </p>
     *
     * @param serviceName the name of the service provider that requires a token from its clients.
     *
     * @return a {@code String} representing the type of the token that suits the specified service.
     */
    String getTokenTypeForService(String serviceName);

    /**
     * Get a list of {@code SecurityTokenProvider} that belong to a family
     *
     * @param familyName
     *
     * @return
     *
     * @see {@code SecurityTokenProvider#family()}
     */
    List<SecurityTokenProvider> getProvidersByFamily(String familyName);

    /**
     * <p>
     * Given the name of a service provider, obtains the provider that must be used when issuing tokens to clients of
     * that
     * service. When requesting a token to the STS, a client can specify the service it needs the token for using the
     * {@code AppliesTo} element. Based on the service provider name, the STS identifies the type of the token that is
     * to be
     * issued and then selects the appropriate token provider to handle the request.
     * </p>
     *
     * @param serviceName the name of the service provider that requires a token from its clients.
     *
     * @return a reference to the {@code SecurityTokenProvider} that must be used in order to issue tokens to clients
     *         of
     *         the
     *         specified service.
     */
    SecurityTokenProvider getProviderForService(String serviceName);

    /**
     * <p>
     * Given a token type, obtains the token provider that should be used to handle token requests of that type. When a
     * client
     * doesn't specify the service provider name through the {@code AppliesTo} element, it must specify the token type
     * through
     * the {@code TokenType} element. The STS uses the supplied type to select the appropriate token provider.
     * </p>
     *
     * @param tokenType a {@code String} representing the type of the token.
     *
     * @return a reference to the {@code SecurityTokenProvider} that must be used to handle token requests of the
     *         specified
     *         type.
     */
    SecurityTokenProvider getProviderForTokenType(String tokenType);

    /**
     * <p>
     * Obtains the token provider that can handle tokens that have the specified local name and namespace. When a
     * validate,
     * renew, or cancel request is made, the token type is not set in the WS-Trust request. In these cases the
     * {@code SecurityTokenProvider} must be determined using the security token itself.
     * </p>
     *
     * @param family a {@code String} representing the family
     * @param qname a {@code QName} representing the token element namespace. (e.g.
     * {@code urn:oasis:names:tc:SAML:2.0:assertion}).
     *
     * @return a reference to the {@code SecurityTokenProvider} that must be used to handle the request that contains
     *         only the
     *         security token.
     */
    SecurityTokenProvider getProviderForTokenElementNS(String family, QName qname);

    /**
     * <p>
     * Obtains the public key of the specified service provider. The returned key is used to encrypt issued tokens.
     * </p>
     *
     * @param serviceName the name of the service provider (normally the provider URL).
     *
     * @return a reference to the provider's {@code PublicKey}
     */
    PublicKey getServiceProviderPublicKey(String serviceName);

    /**
     * <p>
     * Obtains the certificate of the specified service provider. The returned certificate is used to encrypt issued
     * tokens.
     * </p>
     *
     * @param serviceName the name of the service provider (normally the provider URL).
     *
     * @return a reference to the provider's {@code PublicKey}
     */
    X509Certificate getServiceProviderCertificate(String serviceName);

    /**
     * <p>
     * Obtains the certificate identified by the specified alias.
     * </p>
     *
     * @param alias the alias associated with the certificate in the keystore.
     *
     * @return the {@code Certificate} obtained from the keystore, or {@code null} if no certificate was found.
     */
    Certificate getCertificate(String alias);

    /**
     * Allows you to add a token provider to handle a particular namespace
     *
     * @param key
     * @param provider
     */
    void addTokenProvider(String key, SecurityTokenProvider provider);

    /**
     * Get an unmodifiable list of token providers
     *
     * @return
     */
    List<SecurityTokenProvider> getTokenProviders();

    /**
     * Remove a token provider with the passed key
     *
     * @param key
     */
    void removeTokenProvider(String key);
}