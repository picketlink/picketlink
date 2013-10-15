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
package org.picketlink.test.identity.federation.core.saml.v2.metadata;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.metadata.store.FileBasedMetadataConfigurationStore;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test the FileBasedMetadataConfigurationStore
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 28, 2009
 */
public class FileBasedMetadataConfigurationStoreUnitTestCase {

    String pkgName = "org.picketlink.identity.federation.saml.v2.metadata";

    String id = "test";

    @Before
    public void setup() throws Exception {
        // commented to allow build from jenkins openshift
        String userHome = System.getProperty("user.home");

        String testsUserHome = System.getProperty("user.home.tests");

        if (testsUserHome != null) {
            userHome = testsUserHome;
        }

        if (StringUtil.isNotNull(userHome) && "?".equals(userHome)) {
            System.setProperty("user.home", System.getProperty("user.dir"));
        } else {
            System.setProperty("user.home", userHome);
        }
    }

    @Test
    public void testStore() throws Exception {
        SAMLParser parser = new SAMLParser();

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream("saml2/metadata/idp-entitydescriptor.xml");
        assertNotNull("Inputstream not null", is);

        EntityDescriptorType edt = (EntityDescriptorType) parser.parse(is);
        assertNotNull(edt);
        FileBasedMetadataConfigurationStore fbd = new FileBasedMetadataConfigurationStore();
        fbd.persist(edt, id);

        EntityDescriptorType loaded = fbd.load(id);
        assertNotNull("loaded EntityDescriptorType not null", loaded);
        fbd.delete(id);

        try {
            fbd.load(id);
            fail("Did not delete the metadata persistent file");
        } catch (Exception t) {
            // pass
        }
    }

    @Test
    public void testTrustedProviders() throws Exception {
        FileBasedMetadataConfigurationStore fbd = new FileBasedMetadataConfigurationStore();
        Map<String, String> trustedProviders = new HashMap<String, String>();
        trustedProviders.put("idp1", "http://localhost:8080/idp1/metadata");
        trustedProviders.put("idp2", "http://localhost:8080/idp2/metadata");
        fbd.persistTrustedProviders(id, trustedProviders);

        // Lets get back
        Map<String, String> loadTP = fbd.loadTrustedProviders(id);
        assertNotNull("Loaded Trusted Providers not null", loadTP);

        assertTrue("idp1", loadTP.containsKey("idp1"));
        assertTrue("idp2", loadTP.containsKey("idp2"));
        assertTrue("size 2", loadTP.size() == 2);

        fbd.deleteTrustedProviders(id);
        try {
            fbd.loadTrustedProviders(id);
            fail("Did not delete the trusted providers file");
        } catch (Exception t) {
            // pass
        }
    }
}
