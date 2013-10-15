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
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;
import org.picketlink.identity.federation.ws.trust.BinarySecretType;
import org.picketlink.identity.federation.ws.trust.EntropyType;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Validate parsing of RST with Use Key set to Symmetric Key
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 18, 2010
 */
public class WSTrustIssueSymmetricKeyTestCase {

    @Test
    public void testSymKey() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-issue-symmetric-key.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);

        assertEquals("testcontext", requestToken.getContext());
        assertEquals(WSTrustConstants.ISSUE_REQUEST, requestToken.getRequestType().toASCIIString());

        AppliesTo appliesTo = requestToken.getAppliesTo();
        EndpointReferenceType endpoint = (EndpointReferenceType) appliesTo.getAny().get(0);
        assertEquals("http://services.testcorp.org/provider2", endpoint.getAddress().getValue());

        assertEquals(WSTrustConstants.BS_TYPE_SYMMETRIC, requestToken.getKeyType().toASCIIString());

        EntropyType entropy = requestToken.getEntropy();
        BinarySecretType binarySecret = (BinarySecretType) entropy.getAny().get(0);

        assertEquals(WSTrustConstants.BS_TYPE_NONCE, binarySecret.getType());
        assertEquals("M0/7qLpV49c=", new String(binarySecret.getValue()));

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestToken);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}