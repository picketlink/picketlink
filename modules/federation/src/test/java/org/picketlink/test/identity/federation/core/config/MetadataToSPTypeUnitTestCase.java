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
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Given an IDP metadata, construct {@link SPType}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 28, 2011
 */
public class MetadataToSPTypeUnitTestCase {

    private final String idpMetadata = "saml2/metadata/testshib.org.idp-metadata.xml";

    @Test
    public void testMetadataToSP() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(idpMetadata);
        assertNotNull(is);
        SAMLParser parser = new SAMLParser();
        EntitiesDescriptorType entities = (EntitiesDescriptorType) parser.parse(is);
        assertNotNull(entities);

        ProviderType sp = CoreConfigUtil.getSPConfiguration((EntityDescriptorType) entities.getEntityDescriptor().get(0),
                JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get());
        assertNotNull(sp);
        assertEquals("https://idp.testshib.org/idp/profile/SAML2/POST/SSO", sp.getIdentityURL());
    }
}