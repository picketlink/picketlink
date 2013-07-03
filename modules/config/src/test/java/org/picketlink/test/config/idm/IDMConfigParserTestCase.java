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

package org.picketlink.test.config.idm;

import org.junit.Test;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.config.PicketLinkConfigParser;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.idm.IDMType;
import org.picketlink.config.idm.IdentityStoreInvocationContextFactoryType;
import org.picketlink.config.idm.ObjectType;
import org.picketlink.config.idm.StoreConfigurationType;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test for parsing of IDM configuration in picketlink.xml file
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IDMConfigParserTestCase {

    @Test
    public void testParseIDMConfiguration() throws ParsingException {
        System.setProperty("property.existing", "org.picketlink.idm.jpa.schema.IdentityObject");

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink.xml");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        Object result = parser.parse(configStream);
        assertNotNull(result);
        PicketLinkType picketlink = (PicketLinkType) result;

        // Check that other types (handlers, IDP config and STS) are presented
        IDPType idp = (IDPType) picketlink.getIdpOrSP();
        assertNotNull(idp);
        assertFalse(picketlink.isEnableAudit());
        assertNotNull(picketlink.getStsType());
        assertNotNull(picketlink.getHandlers());

        // test IDM configuration
        IDMType idmType = picketlink.getIdmType();
        assertNotNull(idmType);
        assertEquals("org.picketlink.idm.internal.DefaultIdentityManager", idmType.getIdentityManagerClass());
        assertEquals("org.picketlink.idm.internal.DefaultStoreSelector", idmType.getStoreFactoryClass());
        IdentityStoreInvocationContextFactoryType invStoreInvocationContextFactoryType = idmType.getIdentityStoreInvocationContextFactory();
        assertEquals("org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory", invStoreInvocationContextFactoryType.getClassName());
        assertEquals("org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory", invStoreInvocationContextFactoryType.getCredentialHandlerFactoryClass());
        assertEquals("org.picketlink.idm.DefaultEntityManagerFactory", invStoreInvocationContextFactoryType.getEntityManagerFactoryClass());
        assertEquals("org.picketlink.idm.internal.NullEventBridge", invStoreInvocationContextFactoryType.getEventBridgeClass());
        assertEquals("org.picketlink.idm.DefaultIdentityCache", invStoreInvocationContextFactoryType.getIdentityCacheClass());
        assertEquals("org.picketlink.idm.internal.DefaultIdGenerator", invStoreInvocationContextFactoryType.getIdGeneratorClass());

        StoreConfigurationType partitionStore = idmType.getIdentityConfigurationType().getPartitionStoreConfiguration();
        assertNull(partitionStore);

        List<StoreConfigurationType> identityStoreConfigs = idmType.getIdentityConfigurationType().getIdentityStoreConfigurations();
        assertEquals(3, identityStoreConfigs.size());
        StoreConfigurationType ldapConfig = identityStoreConfigs.get(0);
        StoreConfigurationType fileConfig = identityStoreConfigs.get(1);
        StoreConfigurationType jpaConfig = identityStoreConfigs.get(2);

        assertEquals("org.picketlink.idm.ldap.internal.LDAPConfiguration", ldapConfig.getClassName());
        assertEquals(8, ldapConfig.getAllProperties().keySet().size());
        assertEquals("uid=admin,ou=system", ldapConfig.getProperty("bindDN"));
        assertEquals("ldap://localhost:10389", ldapConfig.getProperty("ldapURL"));
        assertEquals("ou=Groups,dc=jboss,dc=org", ldapConfig.getProperty("groupDNSuffix"));
        assertNull(ldapConfig.getProperty("property3"));
        assertEquals("false", ldapConfig.getProperty("activeDirectory"));
        ObjectType additionalProps = (ObjectType)ldapConfig.getProperty("additionalProperties");
        assertEquals("java.util.Properties", additionalProps.getClassName());
        assertEquals("Value1", additionalProps.getProperty("property1"));
        assertEquals("Value2", additionalProps.getProperty("property2"));

        assertEquals(1, fileConfig.getAllProperties().keySet().size());
        ObjectType fileDatasource =  (ObjectType)fileConfig.getProperty("dataSource");
        assertEquals("/tmp/example", fileDatasource.getProperty("workingDir"));
        assertEquals("true", fileDatasource.getProperty("alwaysCreateFiles"));

        assertEquals("org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration", jpaConfig.getClassName());
        assertEquals(4, jpaConfig.getAllProperties().keySet().size());
        assertEquals("org.picketlink.idm.jpa.schema.IdentityObject", jpaConfig.getProperty("identityClass"));
        assertEquals("org.picketlink.idm.jpa.schema.RelationshipObject", jpaConfig.getProperty("relationshipClass"));
        assertEquals("USERRR", jpaConfig.getProperty("identityTypeUser"));
    }

    @Test
    public void testParseMinimalIDMConfiguration() throws ParsingException {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink-minimal.xml");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        Object result = parser.parse(configStream);
        assertNotNull(result);
        PicketLinkType picketlink = (PicketLinkType) result;

        // test IDM configuration
        IDMType idmType = picketlink.getIdmType();
        assertNotNull(idmType);
        assertNull(idmType.getIdentityManagerClass());
        assertNull(idmType.getStoreFactoryClass());
        assertNull(idmType.getIdentityStoreInvocationContextFactory());
        assertNull(idmType.getIdentityConfigurationType().getPartitionStoreConfiguration());

        List<StoreConfigurationType> identityStoreConfigs = idmType.getIdentityConfigurationType().getIdentityStoreConfigurations();
        assertEquals(1, identityStoreConfigs.size());
        StoreConfigurationType identityStoreConfig = identityStoreConfigs.get(0);

        assertEquals("org.picketlink.SomeIdentityStoreConfiguration", identityStoreConfig.getClassName());
        assertEquals(2, identityStoreConfig.getAllProperties().keySet().size());
        assertEquals("Prop1Value", identityStoreConfig.getProperty("property1"));
        assertEquals("Prop2Value", identityStoreConfig.getProperty("property2"));

    }
}
