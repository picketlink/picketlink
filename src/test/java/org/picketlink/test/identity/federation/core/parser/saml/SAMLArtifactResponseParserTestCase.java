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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusType;
import org.w3c.dom.Document;

/**
 * Unit test the parsing of {@link ArtifactResponseType}
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SAMLArtifactResponseParserTestCase
{
   @Test
   public void testSAMLArtifactResponseWithAuthnRequestParse() throws Exception
   {
      String file = "parser/saml2/saml2-artifact-response-authnrequest.xml";
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream(file);

      JAXPValidationUtil.validate(configStream);
      configStream = tcl.getResourceAsStream(file);

      SAMLParser parser = new SAMLParser();
      ArtifactResponseType artifactResponse = (ArtifactResponseType) parser.parse(configStream);
      assertNotNull("ArtifactResponseType is not null", artifactResponse);

      assertEquals("ID_d84a49e5958803dedcff4c984c2b0d95", artifactResponse.getID());
      assertEquals(XMLTimeUtil.parse("2004-12-05T09:21:59Z"), artifactResponse.getIssueInstant());
      assertEquals("ID_cce4ee769ed970b501d680f697989d14", artifactResponse.getInResponseTo());
      assertTrue(artifactResponse.getAny() instanceof AuthnRequestType);

      StatusType status = artifactResponse.getStatus();
      assertNotNull(status);
      assertEquals("urn:oasis:names:tc:SAML:2.0:status:Success", status.getStatusCode().getValue().toString());

      //Try out writing
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(artifactResponse);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      Document doc = DocumentUtil.getDocument(bis); //throws exceptions
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }

   @Test
   public void testSAMLArtifactResponseWithResponseParse() throws Exception
   {
      String file = "parser/saml2/saml2-artifact-response-response.xml";
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream(file);

      JAXPValidationUtil.validate(configStream);
      configStream = tcl.getResourceAsStream(file);

      SAMLParser parser = new SAMLParser();
      ArtifactResponseType artifactResponse = (ArtifactResponseType) parser.parse(configStream);
      assertNotNull("ArtifactResponseType is not null", artifactResponse);

      assertEquals("ID_d84a49e5958803dedcff4c984c2b0d95", artifactResponse.getID());
      assertEquals(XMLTimeUtil.parse("2004-12-05T09:21:59Z"), artifactResponse.getIssueInstant());
      assertEquals("ID_cce4ee769ed970b501d680f697989d14", artifactResponse.getInResponseTo());
      assertTrue(artifactResponse.getAny() instanceof ResponseType);

      StatusType status = artifactResponse.getStatus();
      assertNotNull(status);
      assertEquals("urn:oasis:names:tc:SAML:2.0:status:Success", status.getStatusCode().getValue().toString());

      //Try out writing
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(artifactResponse);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      Document doc = DocumentUtil.getDocument(bis); //throws exceptions
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }
}