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
package org.picketlink.identity.federation.core.saml.v2.util;

import org.jboss.security.xacml.core.JBossRequestContext;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.jboss.security.xacml.core.model.context.ResultType;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.ResponseContext;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.core.factories.XACMLContextFactory;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.parsers.saml.xacml.SAMLXACMLRequestParser;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.factories.JBossSAMLAuthnResponseFactory;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Node;

import javax.xml.stream.XMLEventReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility associated with SOAP 1.1 Envelope, SAML2 and XACML2
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 28, 2009
 */
public class SOAPSAMLXACMLUtil {

    /**
     * Parse the XACML Authorization Decision Query from the Dom Element
     *
     * @param samlRequest
     *
     * @return
     *
     * @throws ProcessingException
     * @throws ConfigurationException
     * @throws ParsingException
     */
    public static XACMLAuthzDecisionQueryType getXACMLQueryType(Node samlRequest) throws ParsingException,
            ConfigurationException, ProcessingException {
        // We reparse it because the document may have issues with namespaces
        // String elementString = DocumentUtil.getDOMElementAsString(samlRequest);

        XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(DocumentUtil.getNodeAsStream(samlRequest));
        SAMLXACMLRequestParser samlXACMLRequestParser = new SAMLXACMLRequestParser();
        return (XACMLAuthzDecisionQueryType) samlXACMLRequestParser.parse(xmlEventReader);
    }

    public static XACMLAuthzDecisionStatementType getDecisionStatement(Node samlResponse) throws ConfigurationException,
            ProcessingException, ParsingException {
        XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(DocumentUtil.getNodeAsStream(samlResponse));
        SAMLParser samlParser = new SAMLParser();

        JAXPValidationUtil.checkSchemaValidation(samlResponse);

        org.picketlink.identity.federation.saml.v2.protocol.ResponseType response = (org.picketlink.identity.federation.saml.v2.protocol.ResponseType) samlParser
                .parse(xmlEventReader);
        List<RTChoiceType> choices = response.getAssertions();
        for (RTChoiceType rst : choices) {
            AssertionType assertion = rst.getAssertion();
            if (assertion == null)
                continue;
            Set<StatementAbstractType> stats = assertion.getStatements();
            for (StatementAbstractType stat : stats) {
                if (stat instanceof XACMLAuthzDecisionStatementType) {
                    return (XACMLAuthzDecisionStatementType) stat;
                }
            }
        }

        throw new RuntimeException("Not found XACMLAuthzDecisionStatementType");
    }

    public static synchronized org.picketlink.identity.federation.saml.v2.protocol.ResponseType handleXACMLQuery(
            PolicyDecisionPoint pdp, String issuer, XACMLAuthzDecisionQueryType xacmlRequest) throws ProcessingException,
            ConfigurationException {
        RequestType requestType = xacmlRequest.getRequest();

        RequestContext requestContext = new JBossRequestContext();
        try {
            requestContext.setRequest(requestType);
        } catch (IOException e) {
            throw new ProcessingException(e);
        }

        // pdp evaluation is thread safe
        ResponseContext responseContext = pdp.evaluate(requestContext);

        ResponseType responseType = new ResponseType();
        ResultType resultType = responseContext.getResult();
        responseType.getResult().add(resultType);

        XACMLAuthzDecisionStatementType xacmlStatement = XACMLContextFactory.createXACMLAuthzDecisionStatementType(requestType,
                responseType);

        // Place the xacml statement in an assertion
        // Then the assertion goes inside a SAML Response

        String ID = IDGenerator.create("ID_");
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder(issuer);

        List<StatementAbstractType> statements = new ArrayList<StatementAbstractType>();
        statements.add(xacmlStatement);

        AssertionType assertion = SAMLAssertionFactory.createAssertion(ID, issuerInfo.getIssuer(),
                XMLTimeUtil.getIssueInstant(), null, null, statements);

        org.picketlink.identity.federation.saml.v2.protocol.ResponseType samlResponseType = JBossSAMLAuthnResponseFactory
                .createResponseType(ID, issuerInfo, assertion);

        return samlResponseType;
    }
}