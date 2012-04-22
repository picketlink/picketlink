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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAML11ResponseParser;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v1.writers.SAML11ResponseWriter;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11ResponseType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusCodeType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusType;

/**
 * Unit Test the {@link SAML11ResponseParser}
 * @author Anil.Saldhana@redhat.com
 * @since Jun 23, 2011
 */
public class SAML11ResponseParserTestCase extends AbstractParserTest
{
   @Test
   public void testSAML11Response() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml1/saml1-response.xml");

      SAMLParser parser = new SAMLParser();
      SAML11ResponseType response = (SAML11ResponseType) parser.parse(configStream);
      assertNotNull(response);

      assertEquals(1, response.getMajorVersion());
      assertEquals(1, response.getMinorVersion());
      assertEquals("P1234", response.getID());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:37.795Z"), response.getIssueInstant());

      assertNotNull(response.getSignature());

      SAML11StatusType status = response.getStatus();
      SAML11StatusCodeType statusCode = status.getStatusCode();
      assertEquals("samlp:Success", statusCode.getValue().toString());

      List<SAML11AssertionType> assertions = response.get();
      assertEquals(1, assertions.size());
      SAML11AssertionType assertion = assertions.get(0);
      assertEquals("buGxcG4gILg5NlocyLccDz6iXrUa", assertion.getID());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Lets do the writing
      SAML11ResponseWriter writer = new SAML11ResponseWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(response);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }
}