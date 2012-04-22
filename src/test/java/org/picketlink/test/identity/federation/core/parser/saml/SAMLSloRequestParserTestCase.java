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
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;

/**
 * Validate the parsing of SLO (log out) Request
 * @author Anil.Saldhana@redhat.com
 * @since Nov 3, 2010
 */
public class SAMLSloRequestParserTestCase extends AbstractParserTest
{
   @Test
   public void testSAMLLogOutRequestParsing() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-request.xml");

      SAMLParser parser = new SAMLParser();
      LogoutRequestType lotRequest = (LogoutRequestType) parser.parse(configStream);
      assertNotNull(lotRequest);

      assertEquals("ID_c3b5ae86-7fea-4d8b-a438-a3f47d8e92c3", lotRequest.getID());
      assertEquals(XMLTimeUtil.parse("2010-07-29T13:46:20.647-05:00"), lotRequest.getIssueInstant());
      assertEquals("2.0", lotRequest.getVersion());
      //Issuer
      assertEquals("http://localhost:8080/sales/", lotRequest.getIssuer().getValue());

      //Try out writing
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(lotRequest);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      DocumentUtil.getDocument(bis); //throws exceptions

      baos = new ByteArrayOutputStream();
      //Lets do the writing
      writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(lotRequest);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }
}