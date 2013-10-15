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
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusType;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.picketlink.common.constants.JBossSAMLConstants.LOGOUT_RESPONSE;
import static org.picketlink.common.constants.JBossSAMLURIConstants.PROTOCOL_NSURI;

/**
 * Validate the parsing of SLO Response
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 3, 2010
 */
public class SAMLSloResponseParserTestCase extends AbstractParserTest {

    @Test
    public void testSAMLResponseParse() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-response.xml");

        SAMLParser parser = new SAMLParser();
        StatusResponseType response = (StatusResponseType) parser.parse(configStream);
        assertNotNull("ResponseType is not null", response);

        assertEquals(XMLTimeUtil.parse("2010-07-29T13:46:03.862-05:00"), response.getIssueInstant());
        assertEquals("2.0", response.getVersion());
        assertEquals("ID_97d332a8-3224-4653-a1ff-65c966e56852", response.getID());

        // Issuer
        assertEquals("http://localhost:8080/employee-post/", response.getIssuer().getValue());

        // Status
        StatusType status = response.getStatus();
        assertEquals("urn:oasis:names:tc:SAML:2.0:status:Responder", status.getStatusCode().getValue().toString());
        assertEquals("urn:oasis:names:tc:SAML:2.0:status:Success", status.getStatusCode().getStatusCode().getValue().toString());

        // Let us do some writing - currently only visual inspection. We will do proper validation later.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(response, new QName(PROTOCOL_NSURI.get(), LOGOUT_RESPONSE.get(), "samlp"));

        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        DocumentUtil.getDocument(bis); // throws exceptions

        baos = new ByteArrayOutputStream();
        // Lets do the writing
        writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(response, new QName(PROTOCOL_NSURI.get(), LOGOUT_RESPONSE.get(), "samlp"));
        String writtenString = new String(baos.toByteArray());
        Logger.getLogger(SAMLSloResponseParserTestCase.class).debug(writtenString);
        validateSchema(writtenString);
    }

    @Test
    public void testSLOResponseWithSig() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-response-sig.xml");

        SAMLParser parser = new SAMLParser();
        StatusResponseType response = (StatusResponseType) parser.parse(configStream);
        assertNotNull("ResponseType is not null", response);

        assertEquals(XMLTimeUtil.parse("2011-04-04T11:48:32.372-05:00"), response.getIssueInstant());
        assertEquals("2.0", response.getVersion());
        assertEquals("ID_2b178fbb-224c-4f01-950d-e3d1be2d3821", response.getID());

        // Issuer
        assertEquals("http://localhost:8080/idp-sig/", response.getIssuer().getValue());

        // Status
        StatusType status = response.getStatus();
        assertEquals("urn:oasis:names:tc:SAML:2.0:status:Responder", status.getStatusCode().getValue().toString());
        assertEquals("urn:oasis:names:tc:SAML:2.0:status:Success", status.getStatusCode().getStatusCode().getValue().toString());
    }

    @Test
    public void testSLOResponseFromSalesforce() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-response-salesforce.xml");

        SAMLParser parser = new SAMLParser();
        StatusResponseType response = (StatusResponseType) parser.parse(configStream);
        assertNotNull("ResponseType is not null", response);

        assertEquals(XMLTimeUtil.parse("2012-06-08T10:00:31.924Z"), response.getIssueInstant());
        assertEquals("2.0", response.getVersion());
        assertEquals("_580ef9943601e7d453514edab43ff2d01339149631922", response.getID());

        // Issuer
        assertEquals("https://saml.salesforce.com", response.getIssuer().getValue());

        // Status
        StatusType status = response.getStatus();
        assertEquals("urn:oasis:names:tc:SAML:2.0:status:Success", status.getStatusCode().getValue().toString());
        assertNull(status.getStatusCode().getStatusCode());
    }
}