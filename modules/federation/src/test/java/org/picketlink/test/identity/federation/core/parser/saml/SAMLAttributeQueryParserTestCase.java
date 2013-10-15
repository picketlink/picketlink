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
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResolveType;
import org.picketlink.identity.federation.saml.v2.protocol.AttributeQueryType;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test the parsing of {@link ArtifactResolveType}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SAMLAttributeQueryParserTestCase {

    @Test
    public void testSAMLAttributeQueryParse() throws Exception {
        String file = "parser/saml2/saml2-attributequery.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream(file);

        JAXPValidationUtil.validate(configStream);
        configStream = tcl.getResourceAsStream(file);

        SAMLParser parser = new SAMLParser();
        AttributeQueryType attributeQuery = (AttributeQueryType) parser.parse(configStream);
        assertNotNull("ArtifactResolveType is not null", attributeQuery);

        assertEquals("ID_aaf23196-1773-2113-474a-fe114412ab72", attributeQuery.getID());
        assertEquals(XMLTimeUtil.parse("2006-07-17T20:31:40Z"), attributeQuery.getIssueInstant());
        assertEquals("CN=anil,OU=User,O=TEST,C=US", attributeQuery.getIssuer().getValue());

        SubjectType subject = attributeQuery.getSubject();
        NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();
        assertEquals("CN=anil,OU=User,O=TEST,C=US", nameID.getValue());
        List<AttributeType> attributes = attributeQuery.getAttribute();
        assertEquals(2, attributes.size());

        // Try out writing
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(attributeQuery);

        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        Document doc = DocumentUtil.getDocument(bis); // throws exceptions
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}