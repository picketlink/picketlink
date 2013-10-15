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
package org.picketlink.test.identity.federation.core.parser.wst;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustResponseWriter;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Validate the parsing of wst-batch-validate.xml
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class WSTrustRenewTargetParsingTestCase {

    @Test
    public void testWST_RenewTarget() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-renew-saml.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);
        assertEquals("renewcontext", requestToken.getContext());
        assertEquals(WSTrustConstants.RENEW_REQUEST, requestToken.getRequestType().toASCIIString());
        assertEquals(SAMLUtil.SAML2_TOKEN_TYPE, requestToken.getTokenType().toASCIIString());

        RenewTargetType renewTarget = requestToken.getRenewTarget();
        Element assertionElement = (Element) renewTarget.getAny().get(0);
        AssertionType assertion = SAMLUtil.fromElement(assertionElement);
        assertEquals("ID_654b6092-c725-40ea-8044-de453b59cb28", assertion.getID());
        assertEquals("Test STS", assertion.getIssuer().getValue());
        SubjectType subject = assertion.getSubject();
        assertEquals("jduke", ((NameIDType) subject.getSubType().getBaseID()).getValue());

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestToken);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }

    @Test
    public void testWST_ResponseRenew() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-response-renew.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityTokenResponseCollection responseCollection = (RequestSecurityTokenResponseCollection) parser
                .parse(configStream);
        assertNotNull(responseCollection);

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustResponseWriter rstrWriter = new WSTrustResponseWriter(baos);

        rstrWriter.write(responseCollection);

        byte[] data = baos.toByteArray();
        Logger.getLogger(WSTrustRenewTargetParsingTestCase.class).debug(new String(data));
        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(data));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}