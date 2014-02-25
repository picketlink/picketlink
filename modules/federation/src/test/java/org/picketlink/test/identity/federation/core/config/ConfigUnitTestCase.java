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
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.SPType;
import org.picketlink.config.federation.STSType;
import org.picketlink.config.federation.ServiceProviderType;
import org.picketlink.config.federation.ServiceProvidersType;
import org.picketlink.config.federation.TokenProviderType;
import org.picketlink.config.federation.TokenProvidersType;
import org.picketlink.config.federation.TrustType;
import org.picketlink.config.federation.handler.Handler;
import org.picketlink.config.federation.handler.Handlers;
import org.picketlink.config.federation.parsers.SAMLConfigParser;
import org.picketlink.config.federation.parsers.STSConfigParser;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit Test the various config
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 21, 2009
 */
public class ConfigUnitTestCase {

    String config = "config/test-config-";

    @Test
    public void test01() throws Exception {
        Object object = this.unmarshall(config + "1.xml");
        assertNotNull("IDP is not null", object);
        /*
         * assertTrue(object instanceof JAXBElement);
         *
         * IDPType idp = ((JAXBElement<IDPType>) object).getValue();
         */
        IDPType idp = (IDPType) object;
        assertEquals("org.picketlink.identity.federation.bindings.tomcat.TomcatRoleGenerator", idp.getRoleGenerator());

        TrustType trust = idp.getTrust();
        assertNotNull("Trust is not null", trust);
        String domains = trust.getDomains();
        assertTrue("localhost trusted", domains.indexOf("localhost") > -1);
        assertTrue("jboss.com trusted", domains.indexOf("jboss.com") > -1);
    }

    @Test
    public void test02() throws Exception {
        Object object = this.unmarshall(config + "2.xml");
        assertNotNull("IDP is not null", object);

        IDPType idp = (IDPType) object;
        assertEquals("somefqn", idp.getRoleGenerator());
        assertTrue(idp.isEncrypt());
        assertEquals(CanonicalizationMethod.EXCLUSIVE, idp.getCanonicalizationMethod());
        KeyProviderType kp = idp.getKeyProvider();
        assertNotNull("KeyProvider is not null", kp);
        assertEquals("SomeClass", "SomeClass", kp.getClassName());
        List<AuthPropertyType> authProps = kp.getAuth();
        AuthPropertyType authProp = authProps.get(0);
        assertEquals("SomeKey", "SomeKey", authProp.getKey());
        assertEquals("SomeValue", "SomeValue", authProp.getValue());

        authProp = authProps.get(1);
        assertEquals("DBURL", "DBURL", authProp.getKey());
        assertEquals("SomeDBURL", "SomeDBURL", authProp.getValue());

        List<KeyValueType> validatingAliases = kp.getValidatingAlias();
        assertEquals("Validating Alias length is 2", 2, validatingAliases.size());

        KeyValueType kv = validatingAliases.get(0);
        assertEquals("localhost", kv.getKey());
        assertEquals("localhostalias", kv.getValue());

        kv = validatingAliases.get(1);
        assertEquals("jboss.com", kv.getKey());
        assertEquals("jbossalias", kv.getValue());

        TrustType trust = idp.getTrust();
        assertNotNull("Trust is not null", trust);
        String domains = trust.getDomains();
        assertTrue("localhost trusted", domains.indexOf("localhost") > -1);
        assertTrue("jboss.com trusted", domains.indexOf("jboss.com") > -1);
    }

    @Test
    public void test03() throws Exception {
        Object object = this.unmarshall(config + "3.xml");
        assertNotNull("SP is null", object);

        SPType sp = (SPType) object;
        assertEquals("http://localhost:8080/idp", sp.getIdentityURL());
        assertEquals("http://localhost:8080/sales", sp.getServiceURL());
        assertEquals(CanonicalizationMethod.EXCLUSIVE, sp.getCanonicalizationMethod());
    }

    /**
     * <p>
     * Tests the parsing of a Security Token Service configuration.
     * </p>
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void test04() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(this.config + "4.xml");
        assertNotNull("Inputstream not null for config file:" + this.config + "4.xml", is);

        STSConfigParser parser = new STSConfigParser();

        Object object = parser.parse(is);
        assertNotNull("Found a null STS configuration", object);

        STSType stsType = (STSType) object;
        // general STS configurations.
        assertEquals("Unexpected STS name", "Test STS", stsType.getSTSName());
        assertEquals("Unexpected token timeout value", 7200, stsType.getTokenTimeout());
        assertTrue("Encryption of tokens should have been enabled", stsType.isEncryptToken());
        // we don't verify all values of the key provider config as it has been done in the other test scenarios.
        assertNotNull("Unexpected null key provider", stsType.getKeyProvider());
        // request handler and configurations based on the token type.
        assertEquals("Unexpected request handler class", "org.picketlink.identity.federation.wstrust.Handler",
                stsType.getRequestHandler());
        // configuration of the token providers.
        TokenProvidersType tokenProviders = stsType.getTokenProviders();
        assertNotNull("Unexpected null list of token providers", tokenProviders);
        assertEquals("Unexpected number of token providers", 1, tokenProviders.getTokenProvider().size());
        TokenProviderType tokenProvider = tokenProviders.getTokenProvider().get(0);
        assertNotNull("Unexpected null token provider", tokenProvider);
        assertEquals("Unexpected provider class name", "org.jboss.SpecialTokenProvider", tokenProvider.getProviderClass());
        assertEquals("Unexpected token type", "specialToken", tokenProvider.getTokenType());
        assertEquals("Unexpected token element name", "SpecialToken", tokenProvider.getTokenElement());
        assertEquals("Unexpected token namespace", "http://www.tokens.org", tokenProvider.getTokenElementNS());
        List<KeyValueType> properties = tokenProvider.getProperty();
        assertEquals("Invalid number of properties", 2, properties.size());
        // configuration of the service providers.
        ServiceProvidersType serviceProviders = stsType.getServiceProviders();
        assertNotNull("Unexpected null list of service providers", serviceProviders);
        assertEquals("Unexpected number of service providers", 1, serviceProviders.getServiceProvider().size());
        ServiceProviderType serviceProvider = serviceProviders.getServiceProvider().get(0);
        assertNotNull("Unexpected null service provider", serviceProvider);
        assertEquals("Unexpected provider endpoint", "http://provider.endpoint/provider", serviceProvider.getEndpoint());
        assertEquals("Unexpected truststore alias", "providerAlias", serviceProvider.getTruststoreAlias());
        assertEquals("Unexpected token type", "specialToken", serviceProvider.getTokenType());
    }

    @Test
    public void test05() throws Exception {
        Handlers handlers = (Handlers) this.unmarshall(config + "5.xml");
        List<Handler> handlerList = handlers.getHandler();
        assertEquals("1 handler", 1, handlerList.size());

        Handler handler = handlerList.get(0);
        assertEquals("Class Name", "a", handler.getClazz());
        List<KeyValueType> options = handler.getOption();
        assertEquals("2 options", 2, options.size());
        KeyValueType k1 = options.get(0);
        assertEquals("1", "1", k1.getKey());
        assertEquals("1.1", "1.1", k1.getValue());
        KeyValueType k2 = options.get(1);
        assertEquals("2", "2", k2.getKey());
        assertEquals("2.2", "2.2", k2.getValue());
    }

    @Test
    public void test06() throws Exception {
          ClassLoader tcl = Thread.currentThread().getContextClassLoader();
          InputStream is = tcl.getResourceAsStream(this.config + "6.xml");
          assertNotNull("Inputstream not null for config file:" + this.config + "4.xml", is);

          STSConfigParser parser = new STSConfigParser();

          Object object = parser.parse(is);
          assertNotNull("Found a null STS configuration", object);

          STSType stsType = (STSType) object;
          // general STS configurations.
          assertEquals("Unexpected STS name", "Test STS", stsType.getSTSName());
          assertEquals("Unexpected token timeout value", 7200, stsType.getTokenTimeout());
          assertTrue("Encryption of tokens should have been enabled", stsType.isEncryptToken());
          // we don't verify all values of the key provider config as it has been done in the other test scenarios.
          assertNotNull("Unexpected null key provider", stsType.getKeyProvider());
          // request handler and configurations based on the token type.
          assertEquals("Unexpected request handler class", "org.picketlink.identity.federation.wstrust.Handler",
                  stsType.getRequestHandler());
          // configuration of the token providers.
          TokenProvidersType tokenProviders = stsType.getTokenProviders();
          assertNotNull("Unexpected null list of token providers", tokenProviders);
          assertEquals("Unexpected number of token providers", 1, tokenProviders.getTokenProvider().size());
          TokenProviderType tokenProvider = tokenProviders.getTokenProvider().get(0);
          assertNotNull("Unexpected null token provider", tokenProvider);
          assertEquals("Unexpected provider class name", "org.jboss.SpecialTokenProvider", tokenProvider.getProviderClass());
          assertEquals("Unexpected token type", "specialToken", tokenProvider.getTokenType());
          assertEquals("Unexpected token element name", "SpecialToken", tokenProvider.getTokenElement());
          assertEquals("Unexpected token namespace", "http://www.tokens.org", tokenProvider.getTokenElementNS());
          List<KeyValueType> properties = tokenProvider.getProperty();
          assertEquals("Invalid number of properties", 2, properties.size());
          // configuration of the service providers with RegEx endpoints
          ServiceProvidersType serviceProviders = stsType.getServiceProviders();
          assertNotNull("Unexpected null list of service providers", serviceProviders);
          assertEquals("Unexpected number of service providers", 2, serviceProviders.getServiceProvider().size());
          ServiceProviderType serviceProviderRegEx = serviceProviders.getServiceProvider().get(0);
          assertNotNull("Unexpected null service provider", serviceProviderRegEx);
          assertEquals("Unexpected provider endpoint", "http://provider.endpoint/provider", serviceProviderRegEx.getEndpoint());
          assertEquals("Unexpected truststore alias", "providerAlias", serviceProviderRegEx.getTruststoreAlias());
          assertEquals("Unexpected token type", "specialToken", serviceProviderRegEx.getTokenType());


          // configuration of the service providers.
          assertNotNull("Unexpected null list of service providers", serviceProviders);
          assertEquals("Unexpected number of service providers", 2, serviceProviders.getServiceProvider().size());
          ServiceProviderType serviceProvider = serviceProviders.getServiceProvider().get(1);
          assertNotNull("Unexpected null service provider regex", serviceProvider);
          assertEquals("Unexpected provider endpoint regex", "http://provider.endpoint/provider[A-Z]", serviceProvider.getEndpointRegEx());
          assertEquals("Unexpected truststore alias regex", "providerAliasRegEx", serviceProvider.getTruststoreAlias());
          assertEquals("Unexpected token type regex", "specialTokenRegEx", serviceProvider.getTokenType());
      }


    private Object unmarshall(String configFile) throws Exception {

        /*
         * String[] schemas = new String[] { "schema/config/picketlink-fed.xsd", "schema/config/picketlink-fed-handler.xsd"};
         */

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(configFile);
        assertNotNull("Inputstream not null for config file:" + configFile, is);

        SAMLConfigParser parser = new SAMLConfigParser();
        return parser.parse(is);

        /*
         * String[] pkgNames = new String[] {"org.picketlink.identity.federation.core.config",
         * "org.picketlink.identity.federation.core.handler.config"}; Unmarshaller un =
         * JAXBUtil.getValidatingUnmarshaller(pkgNames, schemas); return un.unmarshal(is);
         */
    }
}