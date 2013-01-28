/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.picketlink.test.config.idm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.PicketLinkType;
import org.picketlink.identity.federation.core.config.idm.IDMType;
import org.picketlink.identity.federation.core.config.idm.IdentityStoreInvocationContextFactoryType;
import org.picketlink.identity.federation.core.config.idm.StoreConfigurationType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.config.PicketLinkConfigParser;

/**
 * Test for parsing of IDM configuration in picketlink.xml file
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IDMConfigParserTestCase {

    @Test
    public void testParseIDMConfiguration() throws ParsingException {
        System.setProperty("property.existing", "Property3SystemPropValue");

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
        assertEquals("org.picketlink.idm.internal.DefaultStoreFactory", idmType.getStoreFactoryClass());
        IdentityStoreInvocationContextFactoryType invStoreInvocationContextFactoryType = idmType.getIdentityStoreInvocationContextFactory();
        assertEquals("org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory", invStoreInvocationContextFactoryType.getClassName());
        assertEquals("org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory", invStoreInvocationContextFactoryType.getCredentialHandlerFactoryClass());
        assertEquals("org.picketlink.idm.DefaultEntityManagerFactory", invStoreInvocationContextFactoryType.getEntityManagerFactoryClass());
        assertEquals("org.picketlink.idm.internal.NullEventBridge", invStoreInvocationContextFactoryType.getEventBridgeClass());
        assertEquals("org.picketlink.idm.DefaultIdentityCache", invStoreInvocationContextFactoryType.getIdentityCacheClass());
        assertEquals("org.picketlink.idm.internal.DefaultIdGenerator", invStoreInvocationContextFactoryType.getIdGeneratorClass());

        StoreConfigurationType partitionStore = idmType.getIdentityConfigurationType().getPartitionStoreConfiguration();
        assertEquals(2, partitionStore.getAllProperties().keySet().size());
        assertEquals("Property3SystemPropValue", partitionStore.getProperty("property3"));
        assertEquals("Property4Value", partitionStore.getProperty("property4"));
        assertNull(partitionStore.getProperty("property5"));

        List<StoreConfigurationType> identityStoreConfigs = idmType.getIdentityConfigurationType().getIdentityStoreConfigurations();
        assertEquals(2, identityStoreConfigs.size());
        StoreConfigurationType ldapConfig = identityStoreConfigs.get(0);
        StoreConfigurationType anotherConfig = identityStoreConfigs.get(1);

        assertEquals(2, anotherConfig.getAllProperties().keySet().size());
        assertEquals("Property1Value", anotherConfig.getProperty("property1"));
        assertEquals("Property2Value", anotherConfig.getProperty("property2"));
        assertNull(anotherConfig.getProperty("property3"));

        assertEquals("org.picketlink.idm.ldap.internal.LDAPConfiguration", ldapConfig.getClassName());
        assertEquals(6, ldapConfig.getAllProperties().keySet().size());
        assertEquals("uid=admin,ou=system", ldapConfig.getProperty("bindDN"));
        assertEquals("ldap://localhost:10389", ldapConfig.getProperty("ldapURL"));
        assertEquals("ou=Groups,dc=jboss,dc=org", ldapConfig.getProperty("groupDNSuffix"));
        assertNull(ldapConfig.getProperty("property3"));
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
