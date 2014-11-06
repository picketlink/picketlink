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
package org.picketlink.test.scim.parsing;

import org.junit.Test;
import org.picketlink.scim.codec.SCIMParser;
import org.picketlink.scim.model.v11.ServiceProviderConfiguration;
import org.picketlink.scim.model.v11.ServiceProviderConfiguration.Bulk;
import org.picketlink.scim.model.v11.ServiceProviderConfiguration.Filter;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Validate parsing of SCIM Service Provider Configuration representation
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class ServiceProviderConfigurationParsingTestCase {

    @Test
    public void parse() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/serviceprovider.json");
        assertNotNull(is);

        SCIMParser parser = new SCIMParser();
        ServiceProviderConfiguration sp = parser.parseServiceProviderConfiguration(is);
        assertNotNull(sp);

        assertEquals("http://example.com/help/scim.html", sp.getDocumentationUrl());

        assertTrue(sp.getPatch().isSupported());
        assertEquals(2, sp.getAuthenticationSchemes().length);

        Bulk bulk = sp.getBulk();
        assertTrue(bulk.isSupported());
        assertEquals(1000, bulk.getMaxOperations());
        assertEquals(1048576, bulk.getMaxPayloadSize());

        Filter filter = sp.getFilter();
        assertTrue(filter.isSupported());
        assertEquals(200, filter.getMaxResults());

        assertTrue(sp.getChangePassword().isSupported());
        assertTrue(sp.getSort().isSupported());
        assertTrue(sp.getEtag().isSupported());
        assertTrue(sp.getXmlDataFormat().isSupported());
    }
}