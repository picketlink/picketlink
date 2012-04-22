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
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Validate the parsing of wst-batch-validate.xml
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class WSTrustRenewTargetParsingTestCase
{
   @Test
   public void testWST_RenewTarget() throws Exception
   {
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

      //Now for the writing part
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

      rstWriter.write(requestToken);

      Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }
}