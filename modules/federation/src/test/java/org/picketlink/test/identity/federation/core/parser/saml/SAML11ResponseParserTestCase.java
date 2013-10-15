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
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAML11ResponseParser;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v1.writers.SAML11ResponseWriter;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11ResponseType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusCodeType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusType;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit Test the {@link SAML11ResponseParser}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 23, 2011
 */
public class SAML11ResponseParserTestCase extends AbstractParserTest {

    @Test
    public void testSAML11Response() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml1/saml1-response.xml");

        SAMLParser parser = new SAMLParser();
        SAML11ResponseType response = (SAML11ResponseType) parser.parse(configStream);
        assertNotNull(response);

        assertEquals(1, response.getMajorVersion());
        assertEquals(1, response.getMinorVersion());
        assertEquals("P1234", response.getID());
        assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:37.795Z"), response.getIssueInstant());

        assertNotNull(response.getSignature());

        SAML11StatusType status = response.getStatus();
        SAML11StatusCodeType statusCode = status.getStatusCode();
        assertEquals("samlp:Success", statusCode.getValue().toString());

        List<SAML11AssertionType> assertions = response.get();
        assertEquals(1, assertions.size());
        SAML11AssertionType assertion = assertions.get(0);
        assertEquals("buGxcG4gILg5NlocyLccDz6iXrUa", assertion.getID());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Lets do the writing
        SAML11ResponseWriter writer = new SAML11ResponseWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(response);
        String writtenString = new String(baos.toByteArray());
        Logger.getLogger(SAML11ResponseParserTestCase.class).debug(writtenString);
        validateSchema(writtenString);
    }

    @Test
    public void testSAML11ResponseWithStatusMessage() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml1/saml1-response-status-message.xml");

        SAMLParser parser = new SAMLParser();
        SAML11ResponseType response = (SAML11ResponseType) parser.parse(configStream);
        assertNotNull(response);

        assertEquals(1, response.getMajorVersion());
        assertEquals(1, response.getMinorVersion());
        assertEquals("P1234", response.getID());
        assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:37.795Z"), response.getIssueInstant());

        assertNotNull(response.getSignature());

        SAML11StatusType status = response.getStatus();
        SAML11StatusCodeType statusCode = status.getStatusCode();
        assertEquals("samlp:Success", statusCode.getValue().toString());

        List<SAML11AssertionType> assertions = response.get();
        assertEquals(1, assertions.size());
        SAML11AssertionType assertion = assertions.get(0);
        assertEquals("buGxcG4gILg5NlocyLccDz6iXrUa", assertion.getID());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Lets do the writing
        SAML11ResponseWriter writer = new SAML11ResponseWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(response);
        String writtenString = new String(baos.toByteArray());
        Logger.getLogger(SAML11ResponseParserTestCase.class).debug(writtenString);
        validateSchema(writtenString);
    }
}