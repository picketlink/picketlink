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
package org.picketlink.identity.federation.web.servlets.saml;

import org.jboss.logging.Logger;
import org.jboss.security.xacml.core.JBossPDP;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.picketlink.common.ErrorCodes;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.util.SOAPSAMLXACMLUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Servlet that can read SOAP messages that contain an XACML query in saml payload
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 27, 2009
 */
public class SOAPSAMLXACMLServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(SOAPSAMLXACMLServlet.class);

    private final boolean trace = log.isTraceEnabled();

    private static final long serialVersionUID = 1L;

    private String policyConfigFileName = null;

    private String issuerId = null;

    private String issuer = null;

    boolean debug = false;

    private transient PolicyDecisionPoint pdp = null;

    private String soapVersion = "1.1";

    public void init(ServletConfig config) throws ServletException {
        issuerId = config.getInitParameter("issuerID");
        if (issuerId == null)
            issuerId = "issue-id:1";

        issuer = config.getInitParameter("issuer");
        if (issuer == null)
            issuer = "urn:jboss-identity";

        policyConfigFileName = config.getInitParameter("policyConfigFileName");
        if (policyConfigFileName == null)
            policyConfigFileName = "policyConfig.xml";

        String soap = config.getInitParameter("soapVersion");
        if (soap != null) {
            soapVersion = soap;
        }

        String debugStr = config.getInitParameter("debug");
        try {
            debug = Boolean.parseBoolean(debugStr);
        } catch (Exception ignore) {
            debug = false;
        }

        if (trace) {
            log.trace("Issuer=" + issuer + " :: issuerID=" + issuerId);
            log.trace("PolicyConfig File:" + policyConfigFileName);
            log.trace("Debug=" + debug);
        }

        if (debug) {
            SecurityActions.setSystemProperty("jaxb.debug", "true");
        }

        try {
            pdp = this.getPDP();
        } catch (IOException e) {
            log("Exception loading PDP::", e);
            throw new ServletException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to load PDP");
        }
        super.init(config);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        XACMLAuthzDecisionQueryType xacmlRequest = null;
        SOAPMessage returnSOAPMessage = null;
        try {
            try {
                SOAPMessage soapMessage = null;
                if (soapVersion.equals("1.2")) {
                    soapMessage = SOAPUtil.getSOAP12Message(req.getInputStream());
                } else {
                    soapMessage = SOAPUtil.getSOAPMessage(req.getInputStream());
                }
                SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
                SOAPBody soapBody = soapEnvelope.getBody();
                NodeList nl = soapBody.getChildNodes();
                Node node = null;

                int length = nl != null ? nl.getLength() : 0;
                for (int i = 0; i < length; i++) {
                    Node n = nl.item(i);
                    String localName = n.getLocalName();
                    if (localName != null && localName.equals("MessageBody")) {
                        n = getFirstElement(n);
                        localName = n.getLocalName();
                    }
                    if (localName != null
                            && (localName.contains(JBossSAMLConstants.XACML_AUTHZ_DECISION_QUERY.get()) || localName
                            .contains(JBossSAMLConstants.REQUEST_ABSTRACT.get()))) {
                        node = n;
                        break;
                    }
                }
                if (node == null)
                    throw new ServletException(ErrorCodes.NULL_VALUE + "Did not find XACML query nodes");
                xacmlRequest = SOAPSAMLXACMLUtil.getXACMLQueryType(node);
            } catch (SOAPException e) {
                e.printStackTrace();
                throw new ServletException(e);
            } catch (ParsingException e) {
                throw new ServletException(e);
            } catch (ConfigurationException e) {
                throw new ServletException(e);
            } catch (ProcessingException e) {
                throw new ServletException(e);
            }

            /*
             * JAXBElement<RequestAbstractType> jaxbRequestType = null;
             *
             * Envelope envelope = null; XACMLAuthzDecisionQueryType xacmlRequest = null;
             *
             * try { Document inputDoc = DocumentUtil.getDocument(req.getInputStream()); if(debug && trace)
             * log.trace("Received SOAP:"+DocumentUtil.asString(inputDoc));
             *
             * Unmarshaller un = JAXBUtil.getUnmarshaller(SOAPSAMLXACMLUtil.getPackage()); if(debug) un.setEventHandler(new
             * DefaultValidationEventHandler());
             *
             * Object unmarshalledObject = un.unmarshal(DocumentUtil.getNodeAsStream(inputDoc));
             *
             * if(unmarshalledObject instanceof JAXBElement) { JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshalledObject;
             * Object element = jaxbElement.getValue(); if(element instanceof Envelope) { envelope = (Envelope)element; Body
             * soapBody = envelope.getBody(); Object samlRequest = soapBody.getAny().get(0); if(samlRequest instanceof
             * JAXBElement) { jaxbRequestType = (JAXBElement<RequestAbstractType>)samlRequest; xacmlRequest =
             * (XACMLAuthzDecisionQueryType) jaxbRequestType.getValue(); } else if(samlRequest instanceof Element) { Element
             * elem = (Element) samlRequest; xacmlRequest = SOAPSAMLXACMLUtil.getXACMLQueryType(elem); } } else if(element
             * instanceof XACMLAuthzDecisionQueryType) { xacmlRequest = (XACMLAuthzDecisionQueryType) element; } }
             */

            if (xacmlRequest == null)
                throw new IOException(ErrorCodes.NULL_VALUE + "XACML Request not parsed");

            org.picketlink.identity.federation.saml.v2.protocol.ResponseType samlResponseType = SOAPSAMLXACMLUtil
                    .handleXACMLQuery(pdp, issuer, xacmlRequest);

            /*
             * RequestType requestType = xacmlRequest.getRequest();
             *
             * RequestContext requestContext = new JBossRequestContext(); requestContext.setRequest(requestType);
             *
             * //pdp evaluation is thread safe ResponseContext responseContext = pdp.evaluate(requestContext);
             *
             * ResponseType responseType = new ResponseType(); ResultType resultType = responseContext.getResult();
             * responseType.getResult().add(resultType);
             *
             * XACMLAuthzDecisionStatementType xacmlStatement =
             * XACMLContextFactory.createXACMLAuthzDecisionStatementType(requestType, responseType);
             *
             * //Place the xacml statement in an assertion //Then the assertion goes inside a SAML Response
             *
             * String ID = IDGenerator.create("ID_"); SAML2Response saml2Response = new SAML2Response(); IssuerInfoHolder
             * issuerInfo = new IssuerInfoHolder(this.issuer);
             *
             * List<StatementAbstractType> statements = new ArrayList<StatementAbstractType>(); statements.add(xacmlStatement);
             *
             * AssertionType assertion = SAMLAssertionFactory.createAssertion(ID, issuerInfo.getIssuer(),
             * XMLTimeUtil.getIssueInstant(), null, null, statements);
             *
             * org.picketlink.identity.federation.newmodel.saml.v2.protocol.ResponseType samlResponseType =
             * saml2Response.createResponseType(ID, issuerInfo, assertion);
             */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = StaxUtil.getXMLStreamWriter(baos);

            SAMLResponseWriter samlResponseWriter = new SAMLResponseWriter(xmlStreamWriter);
            samlResponseWriter.write(samlResponseType);
            Document responseDocument = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));

            if (soapVersion.equals("1.2")) {
                returnSOAPMessage = SOAPUtil.createSOAP12();
            } else {
                returnSOAPMessage = SOAPUtil.create();
            }
            SOAPBody returnSOAPBody = returnSOAPMessage.getSOAPBody();
            returnSOAPBody.addDocument(responseDocument);
        } catch (Exception e) {
            e.printStackTrace();
            String id = IDGenerator.create();
            log.error(id + "::Exception:", e);
            try {
                if (soapVersion.equals("1.2")) {
                    returnSOAPMessage = SOAPUtil.createFault12("Server Error");
                } else {
                    returnSOAPMessage = SOAPUtil.createFault("Server Error");
                }
            } catch (SOAPException e1) {
            }
        } finally {
            resp.setContentType("text/xml;charset=utf-8");

            OutputStream os = resp.getOutputStream();
            try {
                if (returnSOAPMessage == null)
                    throw new RuntimeException(ErrorCodes.NULL_VALUE + "SOAPMessage for return is null");
                returnSOAPMessage.writeTo(os);
            } catch (Exception e) {
                log("marshalling exception", e);
            }
        }
    }

    private PolicyDecisionPoint getPDP() throws IOException {
        InputStream is = SecurityActions.loadResource(getClass(), this.policyConfigFileName).openStream();
        if (is == null)
            throw new IllegalStateException(ErrorCodes.RESOURCE_NOT_FOUND + policyConfigFileName + " could not be located");
        return new JBossPDP(is);
    }

    private Node getFirstElement(Node node) throws Exception {
        NodeList nodeList = node.getChildNodes();
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
            Node n = nodeList.item(i);
            if (n instanceof Element)
                return n;
        }
        return null;
    }
}