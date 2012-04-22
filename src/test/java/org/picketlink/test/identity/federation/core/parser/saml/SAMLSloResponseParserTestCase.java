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
import static org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants.LOGOUT_RESPONSE;
import static org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants.PROTOCOL_NSURI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusType;

/**
 * Validate the parsing of SLO Response
 * @author Anil.Saldhana@redhat.com
 * @since Nov 3, 2010
 */
public class SAMLSloResponseParserTestCase extends AbstractParserTest
{
   @Test
   public void testSAMLResponseParse() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-response.xml");

      SAMLParser parser = new SAMLParser();
      StatusResponseType response = (StatusResponseType) parser.parse(configStream);
      assertNotNull("ResponseType is not null", response);

      assertEquals(XMLTimeUtil.parse("2010-07-29T13:46:03.862-05:00"), response.getIssueInstant());
      assertEquals("2.0", response.getVersion());
      assertEquals("ID_97d332a8-3224-4653-a1ff-65c966e56852", response.getID());

      //Issuer
      assertEquals("http://localhost:8080/employee-post/", response.getIssuer().getValue());

      //Status
      StatusType status = response.getStatus();
      assertEquals("urn:oasis:names:tc:SAML:2.0:status:Responder", status.getStatusCode().getValue().toString());
      assertEquals("urn:oasis:names:tc:SAML:2.0:status:Success", status.getStatusCode().getStatusCode().getValue()
            .toString());

      //Let us do some writing - currently only visual inspection. We will do proper validation later.
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(response, new QName(PROTOCOL_NSURI.get(), LOGOUT_RESPONSE.get(), "samlp"));

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      DocumentUtil.getDocument(bis); //throws exceptions

      baos = new ByteArrayOutputStream();
      //Lets do the writing
      writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(response, new QName(PROTOCOL_NSURI.get(), LOGOUT_RESPONSE.get(), "samlp"));
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }

   @Test
   public void testSLOResponseWithSig() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-logout-response-sig.xml");

      SAMLParser parser = new SAMLParser();
      StatusResponseType response = (StatusResponseType) parser.parse(configStream);
      assertNotNull("ResponseType is not null", response);

      assertEquals(XMLTimeUtil.parse("2011-04-04T11:48:32.372-05:00"), response.getIssueInstant());
      assertEquals("2.0", response.getVersion());
      assertEquals("ID_2b178fbb-224c-4f01-950d-e3d1be2d3821", response.getID());

      //Issuer
      assertEquals("http://localhost:8080/idp-sig/", response.getIssuer().getValue());

      //Status
      StatusType status = response.getStatus();
      assertEquals("urn:oasis:names:tc:SAML:2.0:status:Responder", status.getStatusCode().getValue().toString());
      assertEquals("urn:oasis:names:tc:SAML:2.0:status:Success", status.getStatusCode().getStatusCode().getValue()
            .toString());
   }
}