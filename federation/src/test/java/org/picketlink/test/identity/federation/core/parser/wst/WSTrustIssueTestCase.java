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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.ws.trust.RenewingType;
import org.w3c.dom.Document;

/**
 * Validate simple RST parsing
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 18, 2010
 */
public class WSTrustIssueTestCase {
    @Test
    public void testIssue() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-issue.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);

        assertEquals("testcontext", requestToken.getContext());
        assertEquals(WSTrustConstants.ISSUE_REQUEST, requestToken.getRequestType().toASCIIString());
        assertEquals(SAMLUtil.SAML2_TOKEN_TYPE, requestToken.getTokenType().toASCIIString());
        
        RenewingType renewingType = requestToken.getRenewing();
        assertNotNull(renewingType);
        assertTrue(renewingType.isAllow());
        assertFalse(renewingType.isOK());
        
        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestToken);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        System.out.println(DocumentUtil.asString(doc));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}