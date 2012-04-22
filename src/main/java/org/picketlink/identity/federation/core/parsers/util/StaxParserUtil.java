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
package org.picketlink.identity.federation.core.parsers.util;

import static org.picketlink.identity.federation.core.ErrorCodes.EXPECTED_END_TAG;
import static org.picketlink.identity.federation.core.ErrorCodes.EXPECTED_TAG;
import static org.picketlink.identity.federation.core.ErrorCodes.EXPECTED_XSI;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.util.TransformerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Utility for the stax based parser
 * @author Anil.Saldhana@redhat.com
 * @since Feb 8, 2010
 */
public class StaxParserUtil
{
   protected static Logger log = Logger.getLogger(StaxParserUtil.class);

   protected static boolean trace = log.isTraceEnabled();

   protected static Validator validator = null;

   /**
    * Bypass an entire XML element block from startElement to endElement
    * @param xmlEventReader
    * @param tag Tag of the XML element that we need to bypass
    * @throws ParsingException
    */
   public static void bypassElementBlock(XMLEventReader xmlEventReader, String tag) throws ParsingException
   {
      while (xmlEventReader.hasNext())
      {
         EndElement endElement = getNextEndElement(xmlEventReader);
         if (endElement == null)
            return;

         if (StaxParserUtil.matches(endElement, tag))
            return;
      }
   }

   /**
    * Given an {@code Attribute}, get its trimmed value
    * @param attribute
    * @return
    */
   public static String getAttributeValue(Attribute attribute)
   {
      String str = trim(attribute.getValue());
      str = StringUtil.getSystemPropertyAsString(str);
      return str;
   }

   /**
    * Get the Attribute value
    * @param startElement
    * @param tag localpart of the qname of the attribute
    * @return
    */
   public static String getAttributeValue(StartElement startElement, String tag)
   {
      String result = null;
      Attribute attr = startElement.getAttributeByName(new QName(tag));
      if (attr != null)
         result = getAttributeValue(attr);
      return result;
   }

   /**
    * Given that the {@code XMLEventReader} is in {@code XMLStreamConstants.START_ELEMENT}
    * mode, we parse into a DOM Element
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static Element getDOMElement(XMLEventReader xmlEventReader) throws ParsingException
   {
      Transformer transformer = null;

      final String JDK_TRANSFORMER_PROPERTY = "picketlink.jdk.transformer";

      boolean useJDKTransformer = Boolean.parseBoolean(SecurityActions.getSystemProperty(JDK_TRANSFORMER_PROPERTY,
            "false"));

      try
      {
         if (useJDKTransformer)
         {
            transformer = TransformerUtil.getTransformer();
         }
         else
         {
            transformer = TransformerUtil.getStaxSourceToDomResultTransformer();
         }

         Document resultDocument = DocumentUtil.createDocument();
         DOMResult domResult = new DOMResult(resultDocument);

         StAXSource source = new StAXSource(xmlEventReader);

         TransformerUtil.transform(transformer, source, domResult);

         Document doc = (Document) domResult.getNode();
         return doc.getDocumentElement();
      }
      catch (ConfigurationException e)
      {
         throw new ParsingException(e);
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }
   }

   /**
    * Get the element text.  
    * @param xmlEventReader
    * @return A <b>trimmed</b> string value
    * @throws ParsingException
    */
   public static String getElementText(XMLEventReader xmlEventReader) throws ParsingException
   {
      String str = null;
      try
      {
         str = xmlEventReader.getElementText().trim();
         str = StringUtil.getSystemPropertyAsString(str);
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }
      return str;
   }

   /**
    * Get the XML event reader
    * @param is
    * @return
    */
   public static XMLEventReader getXMLEventReader(InputStream is)
   {
      XMLInputFactory xmlInputFactory = null;
      XMLEventReader xmlEventReader = null;
      try
      {
         xmlInputFactory = XMLInputFactory.newInstance();
         xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
         xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
         xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
         xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);

         xmlEventReader = xmlInputFactory.createXMLEventReader(is);
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
      return xmlEventReader;
   }

   /**
    * Given a {@code Location}, return a formatted string
    * [lineNum,colNum]
    * @param location
    * @return
    */
   public static String getLineColumnNumber(Location location)
   {
      StringBuilder builder = new StringBuilder("[");
      builder.append(location.getLineNumber()).append(",").append(location.getColumnNumber()).append("]");
      return builder.toString();
   }

   /**
    * Get the next xml event
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static XMLEvent getNextEvent(XMLEventReader xmlEventReader) throws ParsingException
   {
      try
      {
         return xmlEventReader.nextEvent();
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }
   }

   /**
    * Get the next {@code StartElement }
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static StartElement getNextStartElement(XMLEventReader xmlEventReader) throws ParsingException
   {
      try
      {
         while (xmlEventReader.hasNext())
         {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent == null || xmlEvent.isStartElement())
               return (StartElement) xmlEvent;
         }
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }
      return null;
   }

   /**
    * Get the next {@code EndElement}
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static EndElement getNextEndElement(XMLEventReader xmlEventReader) throws ParsingException
   {
      try
      {
         while (xmlEventReader.hasNext())
         {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent == null || xmlEvent.isEndElement())
               return (EndElement) xmlEvent;
         }
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }
      return null;
   }

   /**
    * Return the name of the start element
    * @param startElement
    * @return
    */
   public static String getStartElementName(StartElement startElement)
   {
      return trim(startElement.getName().getLocalPart());
   }

   /**
    * Return the name of the end element
    * @param endElement
    * @return
    */
   public static String getEndElementName(EndElement endElement)
   {
      return trim(endElement.getName().getLocalPart());
   }

   /**
    * Given a start element, obtain the xsi:type defined
    * @param startElement
    * @return
    * @throws RuntimeException if xsi:type is missing
    */
   public static String getXSITypeValue(StartElement startElement)
   {
      Attribute xsiType = startElement.getAttributeByName(new QName(JBossSAMLURIConstants.XSI_NSURI.get(),
            JBossSAMLConstants.TYPE.get()));
      if (xsiType == null)
         throw new RuntimeException(EXPECTED_XSI);
      return StaxParserUtil.getAttributeValue(xsiType);
   }

   /**
    * Return whether the next event is going to be text
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static boolean hasTextAhead(XMLEventReader xmlEventReader) throws ParsingException
   {
      XMLEvent event = peek(xmlEventReader);
      return event.getEventType() == XMLEvent.CHARACTERS;
   }

   /**
    * Match that the start element with the expected tag
    * @param startElement    
    * @param tag
    * @return boolean if the tags match 
    */
   public static boolean matches(StartElement startElement, String tag)
   {
      String elementTag = getStartElementName(startElement);
      return tag.equals(elementTag);
   }

   /**
    * Match that the end element with the expected tag
    * @param endElement
    * @param tag
    * @return boolean if the tags match 
    */
   public static boolean matches(EndElement endElement, String tag)
   {
      String elementTag = getEndElementName(endElement);
      return tag.equals(elementTag);
   }

   /**
    * Peek at the next event
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static XMLEvent peek(XMLEventReader xmlEventReader) throws ParsingException
   {
      try
      {
         return xmlEventReader.peek();
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }
   }

   /**
    * Peek the next {@code StartElement }
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static StartElement peekNextStartElement(XMLEventReader xmlEventReader) throws ParsingException
   {
      try
      {
         while (true)
         {
            XMLEvent xmlEvent = xmlEventReader.peek();

            if (xmlEvent == null || xmlEvent.isStartElement())
               return (StartElement) xmlEvent;
            else
               xmlEvent = xmlEventReader.nextEvent();
         }
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }
   }

   /**
    * Peek the next {@code EndElement}
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static EndElement peekNextEndElement(XMLEventReader xmlEventReader) throws ParsingException
   {
      try
      {
         while (true)
         {
            XMLEvent xmlEvent = xmlEventReader.peek();

            if (xmlEvent == null || xmlEvent.isEndElement())
               return (EndElement) xmlEvent;
            else
               xmlEvent = xmlEventReader.nextEvent();
         }
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }
   }

   /**
    * Given a string, trim it
    * @param str
    * @return
    * @throws {@code IllegalArgumentException} if the passed str is null
    */
   public static final String trim(String str)
   {
      if (str == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT);
      return str.trim();
   }

   /**
    * Validate that the start element has the expected tag
    * @param startElement
    * @param tag
    * @throws RuntimeException mismatch
    */
   public static void validate(StartElement startElement, String tag)
   {
      String elementTag = getStartElementName(startElement);
      if (!tag.equals(elementTag))
         throw new RuntimeException(EXPECTED_TAG + tag + ">.  Found <" + elementTag + ">");
   }

   /**
    * Validate that the end element has the expected tag
    * @param endElement
    * @param tag
    * @throws RuntimeException mismatch
    */
   public static void validate(EndElement endElement, String tag)
   {
      String elementTag = getEndElementName(endElement);
      if (!tag.equals(elementTag))
         throw new RuntimeException(EXPECTED_END_TAG + tag + ">.  Found </" + elementTag + ">");
   }

   /**
    * Get the {@link Validator} for JAXP Validation
    * @return
    * @throws SAXException
    * @throws IOException
    */
   public static Validator getSchemaValidator() throws SAXException, IOException
   {
      return JAXPValidationUtil.validator();
   }
}