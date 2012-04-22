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

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserController;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.wsa.WSAddressingConstants;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.ws.addressing.AttributedURIType;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;
import org.picketlink.identity.federation.ws.trust.BinarySecretType;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;
import org.picketlink.identity.federation.ws.trust.EntropyType;
import org.picketlink.identity.federation.ws.trust.LifetimeType;
import org.picketlink.identity.federation.ws.trust.OnBehalfOfType;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;
import org.picketlink.identity.federation.ws.trust.UseKeyType;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.picketlink.identity.federation.ws.wss.utility.AttributedDateTime;
import org.w3c.dom.Element;

/**
 * Parse the WS-Trust RequestSecurityToken
 * @author Anil.Saldhana@redhat.com
 * @since Oct 11, 2010
 */
public class WSTRequestSecurityTokenParser implements ParserNamespaceSupport
{
   protected Logger log = Logger.getLogger(WSTRequestSecurityTokenParser.class);

   protected boolean trace = log.isTraceEnabled();

   public static final String X509CERTIFICATE = "X509Certificate";

   public static final String KEYVALUE = "KeyValue";

   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

      RequestSecurityToken requestToken = new RequestSecurityToken();

      QName contextQName = new QName("", WSTrustConstants.RST_CONTEXT);
      Attribute contextAttribute = startElement.getAttributeByName(contextQName);
      if (contextAttribute != null)
      {
         String contextValue = StaxParserUtil.getAttributeValue(contextAttribute);
         requestToken.setContext(contextValue);
      }
      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent == null)
            break;
         if (xmlEvent instanceof EndElement)
         {
            xmlEvent = StaxParserUtil.getNextEvent(xmlEventReader);
            EndElement endElement = (EndElement) xmlEvent;
            String endElementTag = StaxParserUtil.getEndElementName(endElement);
            if (endElementTag.equals(WSTrustConstants.RST))
               break;
            else
               throw new RuntimeException(ErrorCodes.UNKNOWN_END_ELEMENT + endElementTag);
         }

         try
         {
            StartElement subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (subEvent == null)
               break;

            String tag = StaxParserUtil.getStartElementName(subEvent);
            if (tag.equals(WSTrustConstants.REQUEST_TYPE))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);

               if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                  throw new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "request type");

               String value = StaxParserUtil.getElementText(xmlEventReader);
               requestToken.setRequestType(new URI(value));
            }
            else if (tag.equals(WSTrustConstants.TOKEN_TYPE))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);

               if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                  throw new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "token type");

               String value = StaxParserUtil.getElementText(xmlEventReader);
               requestToken.setTokenType(new URI(value));
            }
            else if (tag.equals(WSTrustConstants.LIFETIME))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               StaxParserUtil.validate(subEvent, WSTrustConstants.LIFETIME);

               LifetimeType lifeTime = new LifetimeType();
               // Get the Created
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               String subTag = StaxParserUtil.getStartElementName(subEvent);
               if (subTag.equals(WSTrustConstants.CREATED))
               {
                  AttributedDateTime created = new AttributedDateTime();
                  created.setValue(StaxParserUtil.getElementText(xmlEventReader));
                  lifeTime.setCreated(created);
               }
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               subTag = StaxParserUtil.getStartElementName(subEvent);

               if (subTag.equals(WSTrustConstants.EXPIRES))
               {
                  AttributedDateTime expires = new AttributedDateTime();
                  expires.setValue(StaxParserUtil.getElementText(xmlEventReader));
                  lifeTime.setExpires(expires);
               }
               else
                  throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + subTag);

               requestToken.setLifetime(new Lifetime(lifeTime));
               EndElement lifeTimeElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(lifeTimeElement, WSTrustConstants.LIFETIME);
            }
            else if (tag.equals(WSTrustConstants.CANCEL_TARGET))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               StaxParserUtil.validate(subEvent, WSTrustConstants.CANCEL_TARGET);
               WSTCancelTargetParser wstCancelTargetParser = new WSTCancelTargetParser();
               CancelTargetType cancelTarget = (CancelTargetType) wstCancelTargetParser.parse(xmlEventReader);
               requestToken.setCancelTarget(cancelTarget);
               EndElement cancelTargetEndElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(cancelTargetEndElement, WSTrustConstants.CANCEL_TARGET);
            }
            else if (tag.equals(WSTrustConstants.VALIDATE_TARGET))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               WSTValidateTargetParser wstValidateTargetParser = new WSTValidateTargetParser();
               ValidateTargetType validateTarget = (ValidateTargetType) wstValidateTargetParser.parse(xmlEventReader);
               requestToken.setValidateTarget(validateTarget);
               EndElement validateTargetEndElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(validateTargetEndElement, WSTrustConstants.VALIDATE_TARGET);
            }
            else if (tag.equals(WSTrustConstants.RENEW_TARGET))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               WSTRenewTargetParser wstValidateTargetParser = new WSTRenewTargetParser();
               RenewTargetType validateTarget = (RenewTargetType) wstValidateTargetParser.parse(xmlEventReader);
               requestToken.setRenewTarget(validateTarget);
               EndElement validateTargetEndElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(validateTargetEndElement, WSTrustConstants.RENEW_TARGET);
            }
            else if (tag.equals(WSTrustConstants.ON_BEHALF_OF))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);

               WSTrustOnBehalfOfParser wstOnBehalfOfParser = new WSTrustOnBehalfOfParser();
               OnBehalfOfType onBehalfOf = (OnBehalfOfType) wstOnBehalfOfParser.parse(xmlEventReader);
               requestToken.setOnBehalfOf(onBehalfOf);
               EndElement onBehalfOfEndElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(onBehalfOfEndElement, WSTrustConstants.ON_BEHALF_OF);
            }
            else if (tag.equals(WSTrustConstants.KEY_TYPE))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                  throw new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "key type");

               String keyType = StaxParserUtil.getElementText(xmlEventReader);
               try
               {
                  URI keyTypeURI = new URI(keyType);
                  requestToken.setKeyType(keyTypeURI);
               }
               catch (URISyntaxException e)
               {
                  throw new ParsingException(e);
               }
            }
            else if (tag.equals(WSTrustConstants.KEY_SIZE))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);

               if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                  throw new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "key size");

               String keySize = StaxParserUtil.getElementText(xmlEventReader);
               try
               {
                  requestToken.setKeySize(Long.parseLong(keySize));
               }
               catch (NumberFormatException e)
               {
                  throw new ParsingException(e);
               }
            }
            else if (tag.equals(WSTrustConstants.ENTROPY))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               EntropyType entropy = new EntropyType();
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               if (StaxParserUtil.matches(subEvent, WSTrustConstants.BINARY_SECRET))
               {
                  BinarySecretType binarySecret = new BinarySecretType();
                  Attribute typeAttribute = subEvent.getAttributeByName(new QName("", "Type"));
                  binarySecret.setType(StaxParserUtil.getAttributeValue(typeAttribute));

                  if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                     throw new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "binary secret value");

                  binarySecret.setValue(StaxParserUtil.getElementText(xmlEventReader).getBytes());
                  entropy.addAny(binarySecret);
               }
               requestToken.setEntropy(entropy);
               EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(endElement, WSTrustConstants.ENTROPY);
            }
            else if (tag.equals(WSTrustConstants.ISSUER))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               StaxParserUtil.validate(subEvent, WSTrustConstants.ISSUER);

               //Look for addressing
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               StaxParserUtil.validate(subEvent, WSAddressingConstants.ADDRESS);
               String addressValue = StaxParserUtil.getElementText(xmlEventReader);

               EndpointReferenceType endpointRef = new EndpointReferenceType();
               AttributedURIType attrURI = new AttributedURIType();
               attrURI.setValue(addressValue);
               endpointRef.setAddress(new AttributedURIType());
               requestToken.setIssuer(endpointRef);

               EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(endElement, WSTrustConstants.ISSUER);
            }
            else if (tag.equals(WSTrustConstants.USE_KEY))
            {
               subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
               UseKeyType useKeyType = new UseKeyType();
               StaxParserUtil.validate(subEvent, WSTrustConstants.USE_KEY);

               //We peek at the next start element as the stax source has to be in the START_ELEMENT mode
               subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
               if (StaxParserUtil.matches(subEvent, X509CERTIFICATE))
               {
                  Element domElement = StaxParserUtil.getDOMElement(xmlEventReader);
                  //Element domElement = getX509CertificateAsDomElement( subEvent, xmlEventReader );

                  useKeyType.add(domElement);
                  requestToken.setUseKey(useKeyType);
               }
               else if (StaxParserUtil.matches(subEvent, KEYVALUE))
               {
                  //Element domElement = getKeyValueAsDomElement( subEvent, xmlEventReader );
                  Element domElement = StaxParserUtil.getDOMElement(xmlEventReader);//
                  useKeyType.add(domElement);
                  requestToken.setUseKey(useKeyType);

                  EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                  StaxParserUtil.validate(endElement, WSTrustConstants.USE_KEY);
               }
               else
                  throw new RuntimeException(ErrorCodes.UNSUPPORTED_TYPE + StaxParserUtil.getStartElementName(subEvent));
            }
            else
            {
               QName qname = subEvent.getName();
               if (trace)
               {
                  log.trace("Looking for Parser for :" + qname);
               }
               ParserNamespaceSupport parser = ParserController.get(qname);
               if (parser == null)
                  throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + qname);

               Object parsedObject = parser.parse(xmlEventReader);
               if (parsedObject instanceof AppliesTo)
               {
                  requestToken.setAppliesTo((AppliesTo) parsedObject);
               }
            }
         }
         catch (URISyntaxException e)
         {
            throw new ParsingException(e);
         }
      }

      return requestToken;
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();
      String localPart = qname.getLocalPart();

      return WSTrustConstants.BASE_NAMESPACE.equals(nsURI) && WSTrustConstants.RST.equals(localPart);
   }
}