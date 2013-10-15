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
package org.picketlink.test.identity.federation.web.saml.config;

import org.junit.Test;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.web.config.SPPostMetadataConfigurationProvider;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test the {@link SPPostMetadataConfigurationProvider}
 *
 * @author Anil Saldhana
 * @since Feb 15, 2012
 */
public class SPPostMetadataConfigurationProviderUnitTestCase {

    @Test
    public void testSPType() throws ProcessingException {
        SPPostMetadataConfigurationProvider provider = new SPPostMetadataConfigurationProvider();
        SPType sp = provider.getSPConfiguration();
        assertNotNull(sp);
        assertEquals("https://sp.testshib.org/Shibboleth.sso/SAML2/POST", sp.getServiceURL());
        assertEquals("https://idp.testshib.org/idp/profile/SAML2/POST/SLO", sp.getLogoutUrl());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSPTypeWithConfig() throws Exception {
        SPPostMetadataConfigurationProvider provider = new SPPostMetadataConfigurationProvider();
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("saml2/logout/sp/sales/WEB-INF/picketlink-idfed.xml");
        assertNotNull(is);
        provider.setConfigFile(is);

        SPType sp = provider.getSPConfiguration();
        assertNotNull(sp);
        assertEquals("https://sp.testshib.org/Shibboleth.sso/SAML2/POST", sp.getServiceURL());
    }

}