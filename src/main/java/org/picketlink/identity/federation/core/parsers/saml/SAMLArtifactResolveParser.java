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
package org.picketlink.identity.federation.core.parsers.saml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResolveType;

/**
 * Parse the {@link ArtifactResolveType}
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SAMLArtifactResolveParser extends SAMLRequestAbstractParser implements ParserNamespaceSupport
{
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      //Get the startelement
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.ARTIFACT_RESOLVE.get());

      ArtifactResolveType artifactResolve = parseBaseAttributes(startElement);

      while (xmlEventReader.hasNext())
      {
         //Let us peek at the next start element
         startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         if (startElement == null)
            break;
         super.parseCommonElements(startElement, xmlEventReader, artifactResolve);
         String elementName = StaxParserUtil.getStartElementName(startElement);

         if (JBossSAMLConstants.ARTIFACT.get().equals(elementName))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            artifactResolve.setArtifact(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.ISSUER.get().equals(elementName))
         {
            continue;
         }
         else if (JBossSAMLConstants.SIGNATURE.get().equals(elementName))
         {
            continue;
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_START_ELEMENT + elementName + "::location="
                  + startElement.getLocation());
      }
      return artifactResolve;
   }

   public boolean supports(QName qname)
   {
      return JBossSAMLURIConstants.PROTOCOL_NSURI.get().equals(qname.getNamespaceURI());
   }

   /**
    * Parse the attributes at the authnrequesttype element
    * @param startElement
    * @return 
    * @throws ParsingException 
    */
   private ArtifactResolveType parseBaseAttributes(StartElement startElement) throws ParsingException
   {
      super.parseRequiredAttributes(startElement);
      ArtifactResolveType authnRequest = new ArtifactResolveType(id, issueInstant);
      //Let us get the attributes
      super.parseBaseAttributes(startElement, authnRequest);

      return authnRequest;
   }
}