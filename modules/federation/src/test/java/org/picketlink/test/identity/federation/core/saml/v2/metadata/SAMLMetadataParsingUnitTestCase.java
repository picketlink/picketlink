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

import junit.framework.Assert;
import org.junit.Test;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.SAMLMetadataUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.saml.v2.metadata.ContactType;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;

import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test the SAML metadata parsing
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 31, 2011
 */
public class SAMLMetadataParsingUnitTestCase {

    @Test
    public void testEntitiesDescriptor() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream("saml2/metadata/seam-entities.xml");
        assertNotNull("Inputstream not null", is);

        SAMLParser parser = new SAMLParser();
        EntitiesDescriptorType entities = (EntitiesDescriptorType) parser.parse(is);
        Assert.assertNotNull(entities);
        Assert.assertEquals(2, entities.getEntityDescriptor().size());
        EntityDescriptorType entity = (EntityDescriptorType) entities.getEntityDescriptor().get(0);
        IDPSSODescriptorType idp = entity.getChoiceType().get(0).getDescriptors().get(0).getIdpDescriptor();
        KeyDescriptorType keyDescriptor = idp.getKeyDescriptor().get(0);
        X509Certificate cert = SAMLMetadataUtil.getCertificate(keyDescriptor);
        Assert.assertNotNull(cert);
        Assert.assertEquals("CN=test, OU=OpenSSO, O=Sun, L=Santa Clara, ST=California, C=US", cert.getIssuerDN().getName());
    }

    @Test
    public void parseOrganizationAndContactPerson() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream("saml2/metadata/sp-entitydescOrgContact.xml");
        assertNotNull("Inputstream not null", is);

        SAMLParser parser = new SAMLParser();
        EntityDescriptorType entity = (EntityDescriptorType) parser.parse(is);
        assertNotNull(entity);
        OrganizationType org = entity.getOrganization();
        assertNotNull(org);

        List<ContactType> contactPersons = entity.getContactPerson();
        assertNotNull(contactPersons);
        assertTrue(contactPersons.size() == 1);

        assertEquals("technical", contactPersons.get(0).getContactType().value());
        assertEquals("SAML SP Support", contactPersons.get(0).getSurName());
        assertEquals("mailto:saml-support@sp.example.com", contactPersons.get(0).getEmailAddress().get(0));
    }

    /**
     * PLFED-39
     *
     * @throws Exception
     */
    @Test
    public void testShibbolethMetadataExtensions() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream("saml2/metadata/testshib.org.idp-metadata.xml");
        assertNotNull("Inputstream not null", is);
        SAMLParser parser = new SAMLParser();

        EntitiesDescriptorType entities = (EntitiesDescriptorType) parser.parse(is);
        assertNotNull(entities);

        // Another md
        is = tcl.getResourceAsStream("saml2/metadata/shib.idp-metadata.xml");
        assertNotNull("Inputstream not null", is);

        EntityDescriptorType entity = (EntityDescriptorType) parser.parse(is);
        assertNotNull(entity);
    }

    @Test
    public void testShibbolethMetadata() throws Exception {
        boolean runTest = false;
        System.out.println("Test is disabled because of heap space issues in test env");
        if (runTest) {
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            InputStream is = tcl.getResourceAsStream("saml2/metadata/testshib-two-metadata.xml");
            assertNotNull("Inputstream not null", is);
            SAMLParser parser = new SAMLParser();

            EntitiesDescriptorType entities = (EntitiesDescriptorType) parser.parse(is);
            assertNotNull(entities);
            assertEquals("urn:mace:shibboleth:testshib:two", entities.getName());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);

            // write it back
            SAMLMetadataWriter mdWriter = new SAMLMetadataWriter(writer);
            mdWriter.writeEntitiesDescriptor(entities);

        }
    }
}