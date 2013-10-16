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

import org.jboss.logging.Logger;
import org.junit.Test;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLRequestWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Validate the parsing of SLO (log out) Request
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 3, 2010
 */
public class SAMLSloRequestParserTestCase extends AbstractParserTest {

    @Test
    public void testSAMLLogOutRequestParsing() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-request.xml");

        SAMLParser parser = new SAMLParser();
        LogoutRequestType lotRequest = (LogoutRequestType) parser.parse(configStream);
        assertNotNull(lotRequest);

        assertEquals("ID_c3b5ae86-7fea-4d8b-a438-a3f47d8e92c3", lotRequest.getID());
        assertEquals(XMLTimeUtil.parse("2010-07-29T13:46:20.647-05:00"), lotRequest.getIssueInstant());
        assertEquals("2.0", lotRequest.getVersion());
        // Issuer
        assertEquals("http://localhost:8080/sales/", lotRequest.getIssuer().getValue());

        // Try out writing
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(lotRequest);

        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        DocumentUtil.getDocument(bis); // throws exceptions

        baos = new ByteArrayOutputStream();
        // Lets do the writing
        writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(lotRequest);
        String writtenString = new String(baos.toByteArray());
        Logger.getLogger(SAMLSloRequestParserTestCase.class).debug(writtenString);
        validateSchema(writtenString);
    }

    @Test
    public void testSAMLSLOParsing() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-request-2.xml");

        JAXPValidationUtil.validate(configStream);
        configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-request-2.xml");

        SAMLParser parser = new SAMLParser();
        LogoutRequestType lotRequest = (LogoutRequestType) parser.parse(configStream);
        assertNotNull(lotRequest);

        // Try out writing
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(lotRequest);

        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        DocumentUtil.getDocument(bis); // throws exceptions

        baos = new ByteArrayOutputStream();
        // Lets do the writing
        writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(lotRequest);
        String writtenString = new String(baos.toByteArray());
        Logger.getLogger(SAMLSloRequestParserTestCase.class).debug(writtenString);
        validateSchema(writtenString);
    }
}