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
package org.picketlink.test.identity.federation.core.saml.v2.metadata;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.md.providers.MetaDataBuilderDelegate;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;

/**
 * Unit test the {@code SAMLMetadataWriter}
 * @author Anil.Saldhana@redhat.com
 * @since Feb 11, 2011
 */
public class SAMLMetadataWriterUnitTestCase
{
   @Test
   public void testWriteSPSSODescriptor() throws Exception
   {
      String fileName = "saml2/metadata/sp-entitydescriptor.xml";
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
      assertNotNull(is);

      SAMLParser parser = new SAMLParser();
      EntityDescriptorType entityDesc = (EntityDescriptorType) parser.parse(is);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);

      //write it back
      SAMLMetadataWriter mdWriter = new SAMLMetadataWriter(writer);
      mdWriter.writeEntityDescriptor(entityDesc);

   }

   @Test
   public void testWriteEntityDescWithContactPerson() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream is = tcl.getResourceAsStream("saml2/metadata/sp-entitydescOrgContact.xml");
      assertNotNull("Inputstream not null", is);

      SAMLParser parser = new SAMLParser();
      EntityDescriptorType entity = (EntityDescriptorType) parser.parse(is);
      assertNotNull(entity);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);

      //write it back
      SAMLMetadataWriter mdWriter = new SAMLMetadataWriter(writer);
      mdWriter.writeEntityDescriptor(entity);

   }

   /**
    * PLFED-142
    * @throws Exception
    */
   @Test
   public void testDynamicMetadataCreation() throws Exception
   {
      OrganizationType org = new OrganizationType();
      AttributeType attributeType = new AttributeType("hello");
      List<AttributeType> attributes = new ArrayList<AttributeType>();
      attributes.add(attributeType);

      URI test = URI.create("http://test");
      EndpointType sloEndPoint = new EndpointType(test, test);
      KeyDescriptorType keyDescriptorType = new KeyDescriptorType();
      String str = "<a/>";
      keyDescriptorType.setKeyInfo(DocumentUtil.getDocument(str).getDocumentElement());

      SPSSODescriptorType spSSO = MetaDataBuilderDelegate.createSPSSODescriptor(false, keyDescriptorType, sloEndPoint,
            attributes, org);
      EntityDescriptorType entity = MetaDataBuilderDelegate.createEntityDescriptor(spSSO);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);

      //write it back
      SAMLMetadataWriter mdWriter = new SAMLMetadataWriter(writer);
      mdWriter.writeEntityDescriptor(entity);
   }
}