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

import org.junit.Test;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenCollection;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit Test the WS Trust batch issue
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 11, 2010
 */
public class WSTrustBatchIssueParsingTestCase {

    /**
     * Parse and validate the parser/wst/wst-batch-issue.xml file
     *
     * @throws Exception
     */
    @Test
    public void testWST_BatchIssue() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-batch-issue.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityTokenCollection requestCollection = (RequestSecurityTokenCollection) parser.parse(configStream);
        assertNotNull("Request Security Token Collection is null?", requestCollection);

        List<RequestSecurityToken> tokens = requestCollection.getRequestSecurityTokens();
        assertEquals(2, tokens.size());

        RequestSecurityToken rst1 = tokens.get(0);
        assertEquals("context1", rst1.getContext());
        assertEquals(WSTrustConstants.BATCH_ISSUE_REQUEST, rst1.getRequestType().toASCIIString());
        assertEquals(SAMLUtil.SAML2_TOKEN_TYPE, rst1.getTokenType().toASCIIString());

        RequestSecurityToken rst2 = tokens.get(1);
        assertEquals("context2", rst2.getContext());
        assertEquals(WSTrustConstants.BATCH_ISSUE_REQUEST, rst2.getRequestType().toASCIIString());
        assertEquals("http://www.tokens.org/SpecialToken", rst2.getTokenType().toASCIIString());

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestCollection);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}