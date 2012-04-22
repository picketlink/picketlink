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
import java.util.List;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLRequestWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResolveType;
import org.picketlink.identity.federation.saml.v2.protocol.AttributeQueryType;
import org.w3c.dom.Document;

/**
 * Unit test the parsing of {@link ArtifactResolveType}
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SAMLAttributeQueryParserTestCase
{
   @Test
   public void testSAMLAttributeQueryParse() throws Exception
   {
      String file = "parser/saml2/saml2-attributequery.xml";
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream(file);

      JAXPValidationUtil.validate(configStream);
      configStream = tcl.getResourceAsStream(file);

      SAMLParser parser = new SAMLParser();
      AttributeQueryType attributeQuery = (AttributeQueryType) parser.parse(configStream);
      assertNotNull("ArtifactResolveType is not null", attributeQuery);

      assertEquals("ID_aaf23196-1773-2113-474a-fe114412ab72", attributeQuery.getID());
      assertEquals(XMLTimeUtil.parse("2006-07-17T20:31:40Z"), attributeQuery.getIssueInstant());
      assertEquals("CN=anil,OU=User,O=TEST,C=US", attributeQuery.getIssuer().getValue());

      SubjectType subject = attributeQuery.getSubject();
      NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();
      assertEquals("CN=anil,OU=User,O=TEST,C=US", nameID.getValue());
      List<AttributeType> attributes = attributeQuery.getAttribute();
      assertEquals(2, attributes.size());

      //Try out writing
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(attributeQuery);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      Document doc = DocumentUtil.getDocument(bis); //throws exceptions
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }
}