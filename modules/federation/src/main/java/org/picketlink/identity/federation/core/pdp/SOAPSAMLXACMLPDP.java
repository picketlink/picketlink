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
package org.picketlink.identity.federation.core.pdp;

import org.jboss.security.xacml.core.JBossPDP;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.common.util.SystemPropertiesUtil;
import org.picketlink.identity.federation.core.saml.v2.util.SOAPSAMLXACMLUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Document;

import javax.annotation.Resource;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedActionException;

/**
 * SOAP 1.2 based XACML PDP that accepts SAML requests
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 24, 2011
 */
@WebServiceProvider(serviceName = "SOAPSAMLXACMLPDP", portName = "SOAPSAMLXACMLPort", targetNamespace = "urn:picketlink:identity-federation:pdp", wsdlLocation = "WEB-INF/wsdl/SOAPSAMLXACMLPDP.wsdl")
public class SOAPSAMLXACMLPDP implements Provider<Source> {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    @Resource
    protected WebServiceContext context;

    protected String policyConfigFileName = "policyConfig.xml";

    protected PolicyDecisionPoint pdp;

    protected String issuer = "PicketLinkPDP";

    public SOAPSAMLXACMLPDP() {
        try {
            pdp = getPDP();
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e);
        }
    }

    public Source invoke(Source request) {
        try {
            Document doc = (Document) DocumentUtil.getNodeFromSource(request);

            if (logger.isTraceEnabled()) {
                logger.trace("XACML Received Message: " + DocumentUtil.asString(doc));
            }

            XACMLAuthzDecisionQueryType xacmlQuery = SOAPSAMLXACMLUtil.getXACMLQueryType(doc);
            ResponseType samlResponseType = SOAPSAMLXACMLUtil.handleXACMLQuery(pdp, issuer, xacmlQuery);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = StaxUtil.getXMLStreamWriter(baos);

            SAMLResponseWriter samlResponseWriter = new SAMLResponseWriter(xmlStreamWriter);
            samlResponseWriter.write(samlResponseType);
            Document responseDocument = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));

            return new DOMSource(responseDocument.getDocumentElement());
        } catch (Exception e) {
            throw logger.xacmlPDPMessageProcessingError(e);
        }
    }

    private PolicyDecisionPoint getPDP() throws PrivilegedActionException {
        SystemPropertiesUtil.ensure();

        URL url = SecurityActions.loadResource(getClass(), policyConfigFileName);
        if (url == null)
            throw logger.fileNotLocated(policyConfigFileName);

        InputStream is;
        try {
            is = url.openStream();
        } catch (IOException e) {
            throw new RuntimeException(logger.resourceNotFound(url.getPath()));
        }
        return new JBossPDP(is);
    }
}