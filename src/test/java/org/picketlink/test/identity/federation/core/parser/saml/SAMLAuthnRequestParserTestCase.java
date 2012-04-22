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
package org.picketlink.test.identity.federation.core.parser.saml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLRequestWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.w3c.dom.Document;

/**
 * Validate the SAML2 AuthnRequest parse
 * @author Anil.Saldhana@redhat.com
 * @since Nov 2, 2010
 */
public class SAMLAuthnRequestParserTestCase extends AbstractParserTest
{
   @Test
   public void testSAMLAuthnRequestParse() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-authnrequest.xml");

      SAMLParser parser = new SAMLParser();
      AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(configStream);
      assertNotNull("AuthnRequestType is not null", authnRequest);

      assertEquals("http://localhost/org.eclipse.higgins.saml2idp.test/SAMLEndpoint", authnRequest
            .getAssertionConsumerServiceURL().toString());
      assertEquals("http://localhost/org.eclipse.higgins.saml2idp.server/SAMLEndpoint", authnRequest.getDestination()
            .toString());
      assertEquals("a2sffdlgdhgfg32fdldsdghdsgdgfdglgx", authnRequest.getID());
      assertEquals(XMLTimeUtil.parse("2007-12-17T18:40:52.203Z"), authnRequest.getIssueInstant());
      assertEquals("urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect", authnRequest.getProtocolBinding().toString());
      assertEquals("Test SAML2 SP", authnRequest.getProviderName());
      assertEquals("2.0", authnRequest.getVersion());

      //Issuer
      assertEquals("Test SAML2 SP", authnRequest.getIssuer().getValue());

      //NameID Policy
      NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
      assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", nameIDPolicy.getFormat().toString());
      assertEquals(Boolean.TRUE, nameIDPolicy.isAllowCreate());

      //Try out writing
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(authnRequest);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      Document doc = DocumentUtil.getDocument(bis); //throws exceptions
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }
}