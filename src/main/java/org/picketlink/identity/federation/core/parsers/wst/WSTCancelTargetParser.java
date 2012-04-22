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
package org.picketlink.identity.federation.core.parsers.wst;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;

/**
 * Stax parser for the wst:CancelTarget element
 * @author Anil.Saldhana@redhat.com
 * @since Oct 13, 2010
 */
public class WSTCancelTargetParser implements ParserNamespaceSupport
{
   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      CancelTargetType cancelTarget = new CancelTargetType();
      StartElement startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
      // null start element indicates that the token to be canceled hasn't been specified.
      if (startElement == null)
      {
         throw new ParsingException(ErrorCodes.UNABLE_PARSING_NULL_TOKEN);
      }
      // this is an unknown type - parse using the transformer.
      try
      {
         cancelTarget.add(StaxParserUtil.getDOMElement(xmlEventReader));
      }
      catch (Exception e)
      {
         throw new ParsingException(ErrorCodes.PARSING_ERROR + e.getMessage(), e);
      }
      return cancelTarget;
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();
      String localPart = qname.getLocalPart();

      return WSTrustConstants.BASE_NAMESPACE.equals(nsURI) && WSTrustConstants.CANCEL_TARGET.equals(localPart);
   }
}