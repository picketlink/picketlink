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

package org.picketlink.test.idm.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.naming.CommunicationException;

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.internal.XMLBasedIdentityManagerProvider;

/**
 * Test case for configuring IDM via XML TODO: temporary. needs to be improved or deleted (in case that default tests will use XML)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class XMLIdentityManagerProviderTestCase {

    @Test
    public void testParseLdapConfiguration() {
        try {
            IdentityManager identityManager = createIdentityConfiguration("config/embedded-ldap-config.xml");

            // TODO: This is temporary for testing purpose. It expects thrown exception as ldap is not available. Needs to be fixed with LDAPTestUtil
            Assert.fail("expected exception thrown");
        } catch (RuntimeException re) {
            assertNotNull(re.getCause());
            assertEquals(CommunicationException.class, re.getCause().getClass());
            assertTrue(re.getMessage().contains("localhost:10389"));
        }
    }

    @Test
    public void testParseFileConfiguration() {
            IdentityManager identityManager = createIdentityConfiguration("config/embedded-file-config.xml");
            assertNotNull(identityManager);
    }

    @Test
    public void testParseJpaConfiguration() {
        try {
            IdentityManager identityManager = createIdentityConfiguration("config/embedded-jpa-config.xml");

            // TODO: This is temporary for testing purpose. It expects thrown exception as ldap is not available. Needs to be fixed with LDAPTestUtil
            Assert.fail("expected exception thrown");
        } catch (RuntimeException re) {
            assertNotNull(re.getCause());
            assertEquals(ClassNotFoundException.class, re.getCause().getClass());
        }
    }

    private IdentityManager createIdentityConfiguration(String identityConfigFile) {
        XMLBasedIdentityManagerProvider configProvider = new XMLBasedIdentityManagerProvider();
        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(identityConfigFile);
        return configProvider.buildIdentityManager(configStream);
    }


}
