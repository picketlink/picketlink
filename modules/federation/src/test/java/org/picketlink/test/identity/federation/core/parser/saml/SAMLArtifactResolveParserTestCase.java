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
package org.picketlink.test.identity.federation.core.parser.saml;

import org.junit.Test;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLRequestWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResolveType;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test the parsing of {@link ArtifactResolveType}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SAMLArtifactResolveParserTestCase {

    @Test
    public void testSAMLArtifactResolveParse() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-artifact-resolve.xml");

        JAXPValidationUtil.validate(configStream);
        configStream = tcl.getResourceAsStream("parser/saml2/saml2-artifact-resolve.xml");

        SAMLParser parser = new SAMLParser();
        ArtifactResolveType artifactResolve = (ArtifactResolveType) parser.parse(configStream);
        assertNotNull("ArtifactResolveType is not null", artifactResolve);

        assertEquals("ID_cce4ee769ed970b501d680f697989d14", artifactResolve.getID());
        assertEquals(XMLTimeUtil.parse("2004-12-05T09:21:58Z"), artifactResolve.getIssueInstant());
        assertEquals("https://sp.example.com/SAML2/ArtifactResolution", artifactResolve.getDestination().toString());
        assertEquals("https://idp.example.org/SAML2", artifactResolve.getIssuer().getValue());
        assertEquals("AAQAAMh48/1oXIM+sDo7Dh2qMp1HM4IF5DaRNmDj6RdUmllwn9jJHyEgIi8=", artifactResolve.getArtifact());

        // Try out writing
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(artifactResolve);

        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        Document doc = DocumentUtil.getDocument(bis); // throws exceptions
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}