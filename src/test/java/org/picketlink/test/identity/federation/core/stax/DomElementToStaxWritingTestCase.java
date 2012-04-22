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
package org.picketlink.test.identity.federation.core.stax;

import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.w3c.dom.Document;

/**
 * Test how we write a DOM Element to Stax writer
 * @author Anil.Saldhana@redhat.com
 * @since Nov 8, 2010
 */
public class DomElementToStaxWritingTestCase
{
   @Test
   public void testDOM2Stax() throws Exception
   {
      String xml = "<a xmlns=\'urn:hello\' >  <b> <c/> <d xmlns=\'urn:t\' test=\'tt\'/> </b></a>";

      Document doc = DocumentUtil.getDocument(xml);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);
      StaxUtil.writeDOMElement(writer, doc.getDocumentElement());

      String writtenDoc = new String(baos.toByteArray());
      doc = DocumentUtil.getDocument(writtenDoc);
   }
}