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
import org.picketlink.identity.federation.core.parsers.wsse.WSSecurityParser;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.ws.trust.OnBehalfOfType;
import org.picketlink.identity.federation.ws.wss.secext.UsernameTokenType;

/**
 * Parser to parse the OnBehalfOf tag
 * @author Anil.Saldhana@redhat.com
 * @since Oct 18, 2010
 */
public class WSTrustOnBehalfOfParser implements ParserNamespaceSupport
{
   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      OnBehalfOfType onBehalfType = new OnBehalfOfType();
      StartElement startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
      String tag = StaxParserUtil.getStartElementName(startElement);

      if (tag.equals(WSTrustConstants.WSSE.USERNAME_TOKEN))
      {
         WSSecurityParser wsseParser = new WSSecurityParser();

         UsernameTokenType userNameToken = (UsernameTokenType) wsseParser.parse(xmlEventReader);
         onBehalfType.add(userNameToken);
      }
      else
         throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + tag);

      return onBehalfType;
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();
      String localPart = qname.getLocalPart();

      return WSTrustConstants.BASE_NAMESPACE.equals(nsURI) && WSTrustConstants.ON_BEHALF_OF.equals(localPart);
   }
}