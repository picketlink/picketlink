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

package org.picketlink.test.idm.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.naming.CommunicationException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.config.idm.XMLBasedIdentityManagerProvider;
import org.picketlink.idm.IdentityManager;

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
            assertTrue(re.getCause().getMessage().contains("localhost:10389"));
        }
    }

    @Test
    @Ignore
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
