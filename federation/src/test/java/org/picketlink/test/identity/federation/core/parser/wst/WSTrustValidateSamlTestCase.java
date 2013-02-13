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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Validate the parsing of wst-validate-saml.xml
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class WSTrustValidateSamlTestCase {
    @Test
    public void testWST_ValidateSaml() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-validate-saml.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken rst1 = (RequestSecurityToken) parser.parse(configStream);
        assertEquals("validatecontext", rst1.getContext());
        assertEquals(WSTrustConstants.VALIDATE_REQUEST, rst1.getRequestType().toASCIIString());
        assertEquals(WSTrustConstants.RSTR_STATUS_TOKEN_TYPE, rst1.getTokenType().toASCIIString());

        ValidateTargetType validateTarget = rst1.getValidateTarget();
        Element assertionElement = (Element) validateTarget.getAny().get(0);
        AssertionType assertion = SAMLUtil.fromElement(assertionElement);
        assertEquals("ID_654b6092-c725-40ea-8044-de453b59cb28", assertion.getID());

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(rst1);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}
