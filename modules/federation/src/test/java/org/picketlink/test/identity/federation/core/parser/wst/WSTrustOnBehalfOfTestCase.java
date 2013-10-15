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
import org.picketlink.identity.federation.ws.trust.OnBehalfOfType;
import org.picketlink.identity.federation.ws.wss.secext.UsernameTokenType;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Validate the OnBehalfOf parsing
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 18, 2010
 */
public class WSTrustOnBehalfOfTestCase {

    @Test
    public void testOnBehalfOfParsing() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-issue-onbehalfof.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);

        assertEquals("testcontext", requestToken.getContext());
        assertEquals(WSTrustConstants.ISSUE_REQUEST, requestToken.getRequestType().toASCIIString());

        OnBehalfOfType onBehalfOf = requestToken.getOnBehalfOf();
        List<Object> theList = onBehalfOf.getAny();
        assertNotNull(theList);
        UsernameTokenType userNameToken = (UsernameTokenType) theList.get(0);
        assertEquals("id", userNameToken.getId());
        assertEquals("anotherduke", userNameToken.getUsername().getValue());

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestToken);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}