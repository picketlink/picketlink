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
package org.picketlink.identity.federation.api.soap;

import org.jboss.security.xacml.core.model.context.DecisionType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResultType;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLResponseParser;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLRequestWriter;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Class that deals with sending XACML Request Response bundled in SAML pay load as SOAP Requests
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 30, 2009
 */
public class SOAPSAMLXACML {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Given an xacml request
     *
     * @param endpoint
     * @param issuer
     * @param xacmlRequest
     *
     * @return
     *
     * @throws ProcessingException
     * @throws SOAPException
     * @throws ParsingException
     */
    public Result send(String endpoint, String issuer, RequestType xacmlRequest) throws ProcessingException, SOAPException,
            ParsingException {
        try {
            String id = IDGenerator.create("ID_");

            XACMLAuthzDecisionQueryType queryType = new XACMLAuthzDecisionQueryType(id, XMLTimeUtil.getIssueInstant());

            queryType.setRequest(xacmlRequest);

            // Create Issuer
            NameIDType nameIDType = new NameIDType();
            nameIDType.setValue(issuer);
            queryType.setIssuer(nameIDType);

            SOAPMessage soapMessage = SOAPUtil.create();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = StaxUtil.getXMLStreamWriter(baos);

            SAMLRequestWriter samlRequestWriter = new SAMLRequestWriter(xmlStreamWriter);
            samlRequestWriter.write(queryType);

            logger.trace("Sending XACML Decision Query: " + new String(baos.toByteArray()));

            Document reqDocument = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));

            soapMessage.getSOAPBody().addDocument(reqDocument);

            /*
             * Envelope envelope = createEnvelope(jaxbQueryType);
             *
             * JAXBElement<?> soapRequest = SOAPFactory.getObjectFactory().createEnvelope(envelope);
             *
             * Marshaller marshaller = SOAPSAMLXACMLUtil.getMarshaller(); Unmarshaller unmarshaller =
             * SOAPSAMLXACMLUtil.getUnmarshaller();
             */

            SOAPConnectionFactory connectFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection connection = connectFactory.createConnection();
            // Send it across the wire
            URL url = new URL(endpoint);

            SOAPMessage response = connection.call(soapMessage, url);

            NodeList nl = response.getSOAPBody().getChildNodes();
            Node node = null;

            int length = nl != null ? nl.getLength() : 0;
            for (int i = 0; i < length; i++) {
                Node n = nl.item(i);
                String localName = n.getLocalName();
                if (localName.contains(JBossSAMLConstants.RESPONSE.get())) {
                    node = n;
                    break;
                }
            }
            if (node == null)
                throw logger.nullValueError("Did not find Response node");

            XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(DocumentUtil.getNodeAsStream(node));
            SAMLResponseParser samlResponseParser = new SAMLResponseParser();
            ResponseType responseType = (ResponseType) samlResponseParser.parse(xmlEventReader);

            // ResponseType responseType = (ResponseType) response;
            AssertionType at = responseType.getAssertions().get(0).getAssertion();
            XACMLAuthzDecisionStatementType xst = (XACMLAuthzDecisionStatementType) at.getStatements().iterator().next();
            ResultType rt = xst.getResponse().getResult().get(0);
            DecisionType dt = rt.getDecision();

            return new Result(dt, null);
        } catch (IOException e) {
            throw logger.processingError(e);
        } catch (ConfigurationException e) {
            throw logger.processingError(e);
        }
    }

    public static class Result {

        private Element fault = null;

        private final DecisionType decisionType;

        Result(DecisionType decision, Element fault) {
            this.decisionType = decision;
            this.fault = fault;
        }

        public boolean isResponseAvailable() {
            return decisionType != null;
        }

        public boolean isFault() {
            return fault != null;
        }

        public DecisionType getDecision() {
            return decisionType;
        }

        public Element getFault() {
            return fault;
        }

        public boolean isPermit() {
            return decisionType == DecisionType.PERMIT;
        }

        public boolean isDeny() {
            return decisionType == DecisionType.DENY;
        }
    }
}