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
package org.picketlink.test.identity.federation.core.config;

import org.junit.Test;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.ClaimsProcessorType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.STSType;
import org.picketlink.config.federation.ServiceProviderType;
import org.picketlink.config.federation.TokenProviderType;
import org.picketlink.config.federation.parsers.STSConfigParser;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * <p>
 * This class tests the STS configuration file parser.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class STSConfigParserUnitTestCase {

    /**
     * <p>
     * Parses a sample configuration file and verifies if the all data has been extracted as expected.
     * </p>
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testSTSConfiguration() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink-sts.xml");

        // parse the test configuration file.
        STSConfigParser parser = new STSConfigParser();
        STSType stsType = (STSType) parser.parse(configStream);

        // check if the STS attributes have been correctly set, including the ones with default values.
        assertEquals("PicketLinkSTS", stsType.getSTSName());
        assertEquals(7200, stsType.getTokenTimeout());
        assertEquals(1000, stsType.getClockSkew());
        assertEquals(true, stsType.isSignToken());
        assertEquals(false, stsType.isEncryptToken());
        assertEquals("http://www.w3.org/2001/10/xml-exc-c14n#WithComments", stsType.getCanonicalizationMethod());

        // check if the key provider has been set according to the configuration file.
        KeyProviderType keyProvider = stsType.getKeyProvider();
        assertNotNull(keyProvider);
        assertEquals("org.picketlink.identity.federation.core.impl.KeyStoreKeyManager", keyProvider.getClassName());
        assertNull(keyProvider.getSigningAlias());
        List<AuthPropertyType> authProperties = keyProvider.getAuth();
        assertEquals(4, authProperties.size());
        assertEquals("KeyStoreURL", authProperties.get(0).getKey());
        assertEquals("sts_keystore.jks", authProperties.get(0).getValue());
        assertEquals("KeyStorePass", authProperties.get(1).getKey());
        assertEquals("testpass", authProperties.get(1).getValue());
        assertEquals("SigningKeyAlias", authProperties.get(2).getKey());
        assertEquals("sts", authProperties.get(2).getValue());
        assertEquals("SigningKeyPass", authProperties.get(3).getKey());
        assertEquals("keypass", authProperties.get(3).getValue());
        List<KeyValueType> validatingAliases = keyProvider.getValidatingAlias();
        assertEquals(2, validatingAliases.size());
        assertEquals("http://services.testcorp.org/provider1", validatingAliases.get(0).getKey());
        assertEquals("service1", validatingAliases.get(0).getValue());
        assertEquals("http://services.testcorp.org/provider2", validatingAliases.get(1).getKey());
        assertEquals("service2", validatingAliases.get(1).getValue());

        // check if the request handler has been set according to the configuration file.
        assertNotNull(stsType.getRequestHandler());
        assertEquals("org.picketlink.identity.federation.core.wstrust.StandardRequestHandler", stsType.getRequestHandler());

        // check if the claims processors have been set according to the configuration file.
        assertNotNull(stsType.getClaimsProcessors());
        List<ClaimsProcessorType> claimsProcessors = stsType.getClaimsProcessors().getClaimsProcessor();
        assertEquals(2, claimsProcessors.size());
        ClaimsProcessorType claimsProcessor = claimsProcessors.get(0);
        assertEquals("org.picketlink.test.Processor1", claimsProcessor.getProcessorClass());
        assertEquals("urn:test-org:test-dialect:1.0", claimsProcessor.getDialect());
        assertEquals(0, claimsProcessor.getProperty().size());
        claimsProcessor = claimsProcessors.get(1);
        assertEquals("org.picketlink.test.Processor2", claimsProcessor.getProcessorClass());
        assertEquals("urn:test-org:test-dialect:2.0", claimsProcessor.getDialect());
        assertEquals(1, claimsProcessor.getProperty().size());
        assertEquals("SomeKey", claimsProcessor.getProperty().get(0).getKey());
        assertEquals("SomeValue", claimsProcessor.getProperty().get(0).getValue());

        // check if the token providers have been set according to the configuration file.
        assertNotNull(stsType.getTokenProviders());
        List<TokenProviderType> tokenProviders = stsType.getTokenProviders().getTokenProvider();
        assertEquals(2, tokenProviders.size());
        TokenProviderType tokenProvider = tokenProviders.get(0);
        assertEquals("org.picketlink.test.identity.federation.core.wstrust.SpecialTokenProvider",
                tokenProvider.getProviderClass());
        assertEquals("http://www.tokens.org/SpecialToken", tokenProvider.getTokenType());
        assertEquals("SpecialToken", tokenProvider.getTokenElement());
        assertEquals("http://www.tokens.org", tokenProvider.getTokenElementNS());
        assertEquals(2, tokenProvider.getProperty().size());
        assertEquals("Property1", tokenProvider.getProperty().get(0).getKey());
        assertEquals("Value1", tokenProvider.getProperty().get(0).getValue());
        assertEquals("Property2", tokenProvider.getProperty().get(1).getKey());
        assertEquals("Value2", tokenProvider.getProperty().get(1).getValue());
        tokenProvider = tokenProviders.get(1);
        assertEquals("org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider",
                tokenProvider.getProviderClass());
        assertEquals("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0", tokenProvider.getTokenType());
        assertEquals("Assertion", tokenProvider.getTokenElement());
        assertEquals("urn:oasis:names:tc:SAML:2.0:assertion", tokenProvider.getTokenElementNS());
        assertEquals(0, tokenProvider.getProperty().size());

        // finally check if the service providers have been set according to the configuration file.
        assertNotNull(stsType.getServiceProviders());
        List<ServiceProviderType> serviceProviders = stsType.getServiceProviders().getServiceProvider();
        assertEquals(2, serviceProviders.size());
        ServiceProviderType serviceProvider = serviceProviders.get(0);
        assertEquals("http://services.testcorp.org/provider1", serviceProvider.getEndpoint());
        assertEquals("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0", serviceProvider.getTokenType());
        assertEquals("service1", serviceProvider.getTruststoreAlias());
        serviceProvider = serviceProviders.get(1);
        assertEquals("http://services.testcorp.org/provider2", serviceProvider.getEndpoint());
        assertEquals("http://www.tokens.org/SpecialToken", serviceProvider.getTokenType());
        assertEquals("service2", serviceProvider.getTruststoreAlias());
    }
}