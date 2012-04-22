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

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenCollection;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.w3c.dom.Document;

/**
 * Unit Test the WS Trust batch issue
 * @author Anil.Saldhana@redhat.com
 * @since Oct 11, 2010
 */
public class WSTrustBatchIssueParsingTestCase
{
   /**
    * Parse and validate the parser/wst/wst-batch-issue.xml file
    * @throws Exception
    */
   @Test
   public void testWST_BatchIssue() throws Exception
   {
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

      //Now for the writing part
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

      rstWriter.write(requestCollection);

      Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }
}