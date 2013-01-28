/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.test.identity.federation.core.parser.wst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenCollection;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.w3c.dom.Document;

/**
 * Validate the parsing of wst-batch-validate.xml
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class WSTrustBatchValidateParsingTestCase {
    @Test
    public void testWST_BatchValidate() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-batch-validate.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityTokenCollection requestCollection = (RequestSecurityTokenCollection) parser.parse(configStream);
        assertNotNull("Request Security Token Collection is null?", requestCollection);

        List<RequestSecurityToken> tokens = requestCollection.getRequestSecurityTokens();
        assertEquals(2, tokens.size());

        RequestSecurityToken rst1 = tokens.get(0);
        assertEquals("validatecontext1", rst1.getContext());
        assertEquals(WSTrustConstants.BATCH_VALIDATE_REQUEST, rst1.getRequestType().toASCIIString());
        assertEquals(WSTrustConstants.RSTR_STATUS_TOKEN_TYPE, rst1.getTokenType().toASCIIString());

        RequestSecurityToken rst2 = tokens.get(1);
        assertEquals("validatecontext2", rst2.getContext());
        assertEquals(WSTrustConstants.BATCH_VALIDATE_REQUEST, rst2.getRequestType().toASCIIString());
        assertEquals(WSTrustConstants.RSTR_STATUS_TOKEN_TYPE, rst2.getTokenType().toASCIIString());

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestCollection);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        baos.close();

        Logger.getLogger(WSTrustBatchValidateParsingTestCase.class).debug(DocumentUtil.asString(doc));

        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}