/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.core.parsers.saml.xacml;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLRequestAbstractParser;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Parse the XACML Elements as specified by the SAML-XACML Profile.
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 16, 2010
 */
public class SAMLXACMLRequestParser extends SAMLRequestAbstractParser implements ParserNamespaceSupport {

    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
        String tag = StaxParserUtil.getStartElementName(startElement);
        if (tag.equalsIgnoreCase("MessageBody")) {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader); // Lets skip
        }
        startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        if (tag.equals(JBossSAMLConstants.REQUEST_ABSTRACT.get())) {
            String xsiTypeValue = StaxParserUtil.getXSITypeValue(startElement);
            if (xsiTypeValue.contains(JBossSAMLConstants.XACML_AUTHZ_DECISION_QUERY_TYPE.get())) {
                return parseXACMLAuthzDecisionQuery(startElement, xmlEventReader);
            } else
                throw logger.parserUnknownXSI(xsiTypeValue);
        } else if (tag.equals(JBossSAMLConstants.XACML_AUTHZ_DECISION_QUERY.get())) {
            return parseXACMLAuthzDecisionQuery(startElement, xmlEventReader);
        }

        throw logger.parserUnknownStartElement(tag, startElement.getLocation());
    }

    public boolean supports(QName qname) {
        return false;
    }

    @SuppressWarnings("unchecked")
    private XACMLAuthzDecisionQueryType parseXACMLAuthzDecisionQuery(StartElement startElement, XMLEventReader xmlEventReader)
            throws ParsingException {
        super.parseRequiredAttributes(startElement);

        XACMLAuthzDecisionQueryType xacmlQuery = new XACMLAuthzDecisionQueryType(id, issueInstant);
        super.parseBaseAttributes(startElement, xacmlQuery);

        String inputContextOnly = StaxParserUtil.getAttributeValue(startElement, JBossSAMLConstants.INPUT_CONTEXT_ONLY.get());
        if (inputContextOnly != null) {
            xacmlQuery.setInputContextOnly(Boolean.parseBoolean(inputContextOnly));
        }
        String returnContext = StaxParserUtil.getAttributeValue(startElement, JBossSAMLConstants.RETURN_CONTEXT.get());
        if (returnContext != null) {
            xacmlQuery.setReturnContext(Boolean.parseBoolean(returnContext));
        }

        // Go thru the children
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) xmlEvent;
                if (!(StaxParserUtil.matches(endElement, JBossSAMLConstants.REQUEST_ABSTRACT.get()) || StaxParserUtil.matches(
                        endElement, JBossSAMLConstants.XACML_AUTHZ_DECISION_QUERY.get())))
                    throw logger.parserExpectedEndTag("RequestAbstract or XACMLAuthzDecisionQuery");
                break;
            }
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            super.parseCommonElements(startElement, xmlEventReader, xacmlQuery);
            String tag = StaxParserUtil.getStartElementName(startElement);

            if (tag.equals(JBossSAMLConstants.REQUEST.get())) {
                Element xacmlRequest = StaxParserUtil.getDOMElement(xmlEventReader);
                // xacml request
                String xacmlPath = "org.jboss.security.xacml.core.model.context";
                try {
                    JAXBContext jaxb = JAXBContext.newInstance(xacmlPath);
                    Unmarshaller un = jaxb.createUnmarshaller();
                    un.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
                    JAXBElement<RequestType> jaxbRequestType = (JAXBElement<RequestType>) un.unmarshal(DocumentUtil
                            .getNodeAsStream(xacmlRequest));
                    RequestType req = jaxbRequestType.getValue();
                    xacmlQuery.setRequest(req);
                } catch (Exception e) {
                    throw logger.parserException(e);
                }
            }
        }
        return xacmlQuery;
    }
}