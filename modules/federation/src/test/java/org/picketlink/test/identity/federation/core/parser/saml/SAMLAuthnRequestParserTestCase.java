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
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Validate the SAML2 AuthnRequest parse
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 2, 2010
 */
public class SAMLAuthnRequestParserTestCase extends AbstractParserTest {

    @Test
    public void testSAMLAuthnRequestParse() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-authnrequest.xml");

        SAMLParser parser = new SAMLParser();
        AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(configStream);
        assertNotNull("AuthnRequestType is not null", authnRequest);

        assertEquals("http://localhost/org.eclipse.higgins.saml2idp.test/SAMLEndpoint", authnRequest
                .getAssertionConsumerServiceURL().toString());
        assertEquals("http://localhost/org.eclipse.higgins.saml2idp.server/SAMLEndpoint", authnRequest.getDestination()
                .toString());
        assertEquals("a2sffdlgdhgfg32fdldsdghdsgdgfdglgx", authnRequest.getID());
        assertEquals(XMLTimeUtil.parse("2007-12-17T18:40:52.203Z"), authnRequest.getIssueInstant());
        assertEquals("urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect", authnRequest.getProtocolBinding().toString());
        assertEquals("Test SAML2 SP", authnRequest.getProviderName());
        assertEquals("2.0", authnRequest.getVersion());

        // Issuer
        assertEquals("Test SAML2 SP", authnRequest.getIssuer().getValue());

        // NameID Policy
        NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
        assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", nameIDPolicy.getFormat().toString());
        assertEquals(Boolean.TRUE, nameIDPolicy.isAllowCreate());

        // Try out writing
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(authnRequest);

        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        Document doc = DocumentUtil.getDocument(bis); // throws exceptions
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }

    @Test
    public void testSAMLAuthnRequestParse2() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-authnrequest-2.xml");

        JAXPValidationUtil.validate(configStream);
        configStream = tcl.getResourceAsStream("parser/saml2/saml2-authnrequest-2.xml");

        SAMLParser parser = new SAMLParser();
        AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(configStream);
        assertNotNull("AuthnRequestType is not null", authnRequest);

        /*
         * assertEquals("http://localhost/org.eclipse.higgins.saml2idp.test/SAMLEndpoint", authnRequest
         * .getAssertionConsumerServiceURL().toString());
         * assertEquals("http://localhost/org.eclipse.higgins.saml2idp.server/SAMLEndpoint", authnRequest.getDestination()
         * .toString()); assertEquals("a2sffdlgdhgfg32fdldsdghdsgdgfdglgx", authnRequest.getID());
         * assertEquals(XMLTimeUtil.parse("2007-12-17T18:40:52.203Z"), authnRequest.getIssueInstant());
         * assertEquals("urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect", authnRequest.getProtocolBinding().toString());
         * assertEquals("Test SAML2 SP", authnRequest.getProviderName()); assertEquals("2.0", authnRequest.getVersion());
         *
         * //Issuer assertEquals("Test SAML2 SP", authnRequest.getIssuer().getValue());
         *
         * //NameID Policy NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
         * assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", nameIDPolicy.getFormat().toString());
         * assertEquals(Boolean.TRUE, nameIDPolicy.isAllowCreate());
         */

        // Try out writing
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(authnRequest);

        byte[] data = baos.toByteArray();
        Logger.getLogger(SAMLAuthnRequestParserTestCase.class).debug(new String(data));
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        Document doc = DocumentUtil.getDocument(bis); // throws exceptions
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}