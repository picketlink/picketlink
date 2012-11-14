/*
 * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.parsers.saml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.SAMLParserUtil;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.w3c.dom.Element;

/**
 * Parse the saml subject
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class SAMLSubjectParser implements ParserNamespaceSupport {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    
    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        StaxParserUtil.getNextEvent(xmlEventReader);

        SubjectType subject = new SubjectType();

        // Peek at the next event
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) xmlEvent;
                if (StaxParserUtil.matches(endElement, JBossSAMLConstants.SUBJECT.get())) {
                    endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                    break;
                } else
                    throw logger.parserUnknownEndElement(StaxParserUtil.getEndElementName(endElement));
            }

            StartElement peekedElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (peekedElement == null)
                break;

            String tag = StaxParserUtil.getStartElementName(peekedElement);

            if (JBossSAMLConstants.NAMEID.get().equalsIgnoreCase(tag)) {
                NameIDType nameID = SAMLParserUtil.parseNameIDType(xmlEventReader);
                STSubType subType = new STSubType();
                subType.addBaseID(nameID);
                subject.setSubType(subType);
            } else if (JBossSAMLConstants.BASEID.get().equalsIgnoreCase(tag)) {
                throw new ParsingException(ErrorCodes.UNSUPPORTED_TYPE + JBossSAMLConstants.BASEID.get());
            } else if (JBossSAMLConstants.ENCRYPTED_ID.get().equals(tag)) {
                Element domElement = StaxParserUtil.getDOMElement(xmlEventReader);
                STSubType subType = new STSubType();
                subType.setEncryptedID(new EncryptedElementType(domElement));
                subject.setSubType(subType);
            } else if (JBossSAMLConstants.SUBJECT_CONFIRMATION.get().equalsIgnoreCase(tag)) {
                StartElement subjectConfirmationElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                Attribute method = subjectConfirmationElement.getAttributeByName(new QName(JBossSAMLConstants.METHOD.get()));

                SubjectConfirmationType subjectConfirmationType = new SubjectConfirmationType();

                if (method != null) {
                    subjectConfirmationType.setMethod(StaxParserUtil.getAttributeValue(method));
                }

                // There may be additional things under subject confirmation
                xmlEvent = StaxParserUtil.peek(xmlEventReader);
                if (xmlEvent instanceof StartElement) {
                    StartElement startElement = (StartElement) xmlEvent;
                    String startTag = StaxParserUtil.getStartElementName(startElement);

                    if (startTag.equals(JBossSAMLConstants.NAMEID.get())) {
                        NameIDType nameID = SAMLParserUtil.parseNameIDType(xmlEventReader);
                        subjectConfirmationType.setNameID(nameID);
                    } else if (JBossSAMLConstants.BASEID.get().equalsIgnoreCase(tag)) {
                        throw logger.unsupportedType(JBossSAMLConstants.BASEID.get());
                    } else if (JBossSAMLConstants.ENCRYPTED_ID.get().equals(tag)) {
                        Element domElement = StaxParserUtil.getDOMElement(xmlEventReader);
                        subjectConfirmationType.setEncryptedID(new EncryptedElementType(domElement));
                    } else if (startTag.equals(JBossSAMLConstants.SUBJECT_CONFIRMATION_DATA.get())) {
                        SubjectConfirmationDataType subjectConfirmationData = parseSubjectConfirmationData(xmlEventReader);
                        subjectConfirmationType.setSubjectConfirmationData(subjectConfirmationData);
                    }
                }

                subject.addConfirmation(subjectConfirmationType);

                // Get the end tag
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                StaxParserUtil.matches(endElement, JBossSAMLConstants.SUBJECT_CONFIRMATION.get());
            } else
                throw logger.parserUnknownTag(tag, peekedElement.getLocation());
        }
        return subject;
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        String nsURI = qname.getNamespaceURI();
        String localPart = qname.getLocalPart();

        return nsURI.equals(JBossSAMLURIConstants.ASSERTION_NSURI.get()) && localPart.equals(JBossSAMLConstants.SUBJECT.get());
    }

    private SubjectConfirmationDataType parseSubjectConfirmationData(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, JBossSAMLConstants.SUBJECT_CONFIRMATION_DATA.get());

        SubjectConfirmationDataType subjectConfirmationData = new SubjectConfirmationDataType();

        Attribute inResponseTo = startElement.getAttributeByName(new QName(JBossSAMLConstants.IN_RESPONSE_TO.get()));
        if (inResponseTo != null) {
            subjectConfirmationData.setInResponseTo(StaxParserUtil.getAttributeValue(inResponseTo));
        }

        Attribute notBefore = startElement.getAttributeByName(new QName(JBossSAMLConstants.NOT_BEFORE.get()));
        if (notBefore != null) {
            subjectConfirmationData.setNotBefore(XMLTimeUtil.parse(StaxParserUtil.getAttributeValue(notBefore)));
        }

        Attribute notOnOrAfter = startElement.getAttributeByName(new QName(JBossSAMLConstants.NOT_ON_OR_AFTER.get()));
        if (notOnOrAfter != null) {
            subjectConfirmationData.setNotOnOrAfter(XMLTimeUtil.parse(StaxParserUtil.getAttributeValue(notOnOrAfter)));
        }

        Attribute recipient = startElement.getAttributeByName(new QName(JBossSAMLConstants.RECIPIENT.get()));
        if (recipient != null) {
            subjectConfirmationData.setRecipient(StaxParserUtil.getAttributeValue(recipient));
        }

        Attribute address = startElement.getAttributeByName(new QName(JBossSAMLConstants.ADDRESS.get()));
        if (address != null) {
            subjectConfirmationData.setAddress(StaxParserUtil.getAttributeValue(address));
        }

        XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
        if (!(xmlEvent instanceof EndElement)) {
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            String tag = StaxParserUtil.getStartElementName(startElement);
            if (tag.equals(WSTrustConstants.XMLDSig.KEYINFO)) {
                KeyInfoType keyInfo = SAMLParserUtil.parseKeyInfo(xmlEventReader);
                subjectConfirmationData.setAnyType(keyInfo);
            } else if (tag.equals(WSTrustConstants.XMLEnc.ENCRYPTED_KEY)) {
                subjectConfirmationData.setAnyType(StaxParserUtil.getDOMElement(xmlEventReader));
            } else
                throw logger.parserUnknownTag(tag, startElement.getLocation());
        }

        // Get the end tag
        EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
        StaxParserUtil.matches(endElement, JBossSAMLConstants.SUBJECT_CONFIRMATION_DATA.get());
        return subjectConfirmationData;
    }
}