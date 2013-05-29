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
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Unit test the wst:SecondaryParameters
 *
 * @author anil saldhana
 */
public class WSTrustSecondaryParametersTestCase {

    @Test
    public void testSecondaryParameters() throws Exception {

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-secondaryparameters.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);

        EndpointReferenceType endpoint = (EndpointReferenceType) requestToken.getAppliesTo().getAny().get(0);
        assertEquals("http://localhost:8080/jaxws-samples-wsse-policy-trust/SecurityService", endpoint.getAddress().getValue());
        assertEquals(WSTrustConstants.ISSUE_REQUEST, requestToken.getRequestType().toASCIIString());
        assertEquals(SAMLUtil.SAML2_TOKEN_TYPE, requestToken.getTokenType().toASCIIString());

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestToken);

        byte[] data = baos.toByteArray();
        Logger.getLogger(WSTrustSecondaryParametersTestCase.class).debug(new String(data));
        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(data));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }

}