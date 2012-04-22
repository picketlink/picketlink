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
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResolveType;
import org.w3c.dom.Document;

/**
 * Unit test the parsing of {@link ArtifactResolveType}
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SAMLArtifactResolveParserTestCase
{
   @Test
   public void testSAMLArtifactResolveParse() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-artifact-resolve.xml");

      JAXPValidationUtil.validate(configStream);
      configStream = tcl.getResourceAsStream("parser/saml2/saml2-artifact-resolve.xml");

      SAMLParser parser = new SAMLParser();
      ArtifactResolveType artifactResolve = (ArtifactResolveType) parser.parse(configStream);
      assertNotNull("ArtifactResolveType is not null", artifactResolve);

      assertEquals("ID_cce4ee769ed970b501d680f697989d14", artifactResolve.getID());
      assertEquals(XMLTimeUtil.parse("2004-12-05T09:21:58Z"), artifactResolve.getIssueInstant());
      assertEquals("https://sp.example.com/SAML2/ArtifactResolution", artifactResolve.getDestination().toString());
      assertEquals("https://idp.example.org/SAML2", artifactResolve.getIssuer().getValue());
      assertEquals("AAQAAMh48/1oXIM+sDo7Dh2qMp1HM4IF5DaRNmDj6RdUmllwn9jJHyEgIi8=", artifactResolve.getArtifact());

      //Try out writing
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(artifactResolve);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      Document doc = DocumentUtil.getDocument(bis); //throws exceptions
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }
}