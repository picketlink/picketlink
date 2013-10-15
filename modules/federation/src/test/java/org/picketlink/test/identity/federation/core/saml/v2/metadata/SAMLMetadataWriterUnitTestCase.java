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

import org.junit.Test;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.md.providers.MetaDataBuilderDelegate;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;

import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Unit test the {@code SAMLMetadataWriter}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 11, 2011
 */
public class SAMLMetadataWriterUnitTestCase {

    @Test
    public void testWriteSPSSODescriptor() throws Exception {
        String fileName = "saml2/metadata/sp-entitydescriptor.xml";
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        assertNotNull(is);

        SAMLParser parser = new SAMLParser();
        EntityDescriptorType entityDesc = (EntityDescriptorType) parser.parse(is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);

        // write it back
        SAMLMetadataWriter mdWriter = new SAMLMetadataWriter(writer);
        mdWriter.writeEntityDescriptor(entityDesc);

    }

    @Test
    public void testWriteEntityDescWithContactPerson() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream("saml2/metadata/sp-entitydescOrgContact.xml");
        assertNotNull("Inputstream not null", is);

        SAMLParser parser = new SAMLParser();
        EntityDescriptorType entity = (EntityDescriptorType) parser.parse(is);
        assertNotNull(entity);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);

        // write it back
        SAMLMetadataWriter mdWriter = new SAMLMetadataWriter(writer);
        mdWriter.writeEntityDescriptor(entity);

    }

    /**
     * PLFED-142
     *
     * @throws Exception
     */
    @Test
    public void testDynamicMetadataCreation() throws Exception {
        OrganizationType org = new OrganizationType();
        AttributeType attributeType = new AttributeType("hello");
        List<AttributeType> attributes = new ArrayList<AttributeType>();
        attributes.add(attributeType);

        URI test = URI.create("http://test");
        EndpointType sloEndPoint = new EndpointType(test, test);
        KeyDescriptorType keyDescriptorType = new KeyDescriptorType();
        String str = "<a/>";
        keyDescriptorType.setKeyInfo(DocumentUtil.getDocument(str).getDocumentElement());

        SPSSODescriptorType spSSO = MetaDataBuilderDelegate.createSPSSODescriptor(false, keyDescriptorType, sloEndPoint,
                attributes, org);
        EntityDescriptorType entity = MetaDataBuilderDelegate.createEntityDescriptor(spSSO);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);

        // write it back
        SAMLMetadataWriter mdWriter = new SAMLMetadataWriter(writer);
        mdWriter.writeEntityDescriptor(entity);
    }
}