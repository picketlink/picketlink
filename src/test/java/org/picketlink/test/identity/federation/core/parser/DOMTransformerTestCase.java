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
package org.picketlink.test.identity.federation.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.TransformerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Parse an xml file partially using StAX and then use JAXP Transformer
 * to parse a DOM Element and resume stax
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Oct 22, 2010
 */
public class DOMTransformerTestCase
{
   String xml = "<a xmlns=\'urn:a\'><b><c><d>SomeD</d></c></b></a>";

   @Test
   public void testDOMTransformer() throws Exception
   {
      ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
      XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(bis);

      StartElement a = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(a, "a");

      Document resultDocument = DocumentUtil.createDocument();
      DOMResult domResult = new DOMResult(resultDocument);

      //Let us parse <b><c><d> using transformer
      StAXSource source = new StAXSource(xmlEventReader);

      Transformer transformer = TransformerUtil.getStaxSourceToDomResultTransformer();
      transformer.transform(source, domResult);

      Document doc = (Document) domResult.getNode();
      Element elem = doc.getDocumentElement();
      assertEquals("b", elem.getLocalName());

      XMLEvent xmlEvent = xmlEventReader.nextEvent();
      assertTrue(xmlEvent instanceof EndElement);
      StaxParserUtil.validate((EndElement) xmlEvent, "a");
   }
}