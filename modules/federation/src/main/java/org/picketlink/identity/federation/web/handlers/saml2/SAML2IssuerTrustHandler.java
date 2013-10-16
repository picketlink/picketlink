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
package org.picketlink.identity.federation.web.handlers.saml2;

import org.jboss.security.audit.AuditLevel;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.TrustType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Handles Issuer trust
 * <p>
 * Trust decisions are based on the url of the issuer of the saml request/response sent to the handler chain
 * </p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 8, 2009
 */
public class SAML2IssuerTrustHandler extends BaseSAML2Handler {

    private final IDPTrustHandler idp = new IDPTrustHandler();

    private final SPTrustHandler sp = new SPTrustHandler();

    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        if (getType() == HANDLER_TYPE.IDP) {
            idp.handleRequestType(request, response,
                    (IDPType) this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION));
        } else {
            sp.handleRequestType(request, response,
                    (ProviderType) this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION));
        }
    }

    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        if (getType() == HANDLER_TYPE.IDP) {
            idp.handleStatusResponseType(request, response,
                    (IDPType) this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION));
        } else {
            sp.handleStatusResponseType(request, response,
                    (ProviderType) this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION));
        }
    }

    private class IDPTrustHandler {

        public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response, IDPType idpConfiguration)
                throws ProcessingException {
            RequestAbstractType requestType = (RequestAbstractType) request.getSAML2Object();

            if (requestType == null)
                throw logger.nullValueError("AuthnRequest");

            String issuer = requestType.getIssuer().getValue();

            trustIssuer(idpConfiguration, issuer);
        }

        public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response,
                                             IDPType idpConfiguration) throws ProcessingException {
            String issuer = request.getIssuer().getValue();

            trustIssuer(idpConfiguration, issuer);
        }

        private void trustIssuer(IDPType idpConfiguration, String issuer) throws ProcessingException {
            if (idpConfiguration == null)
                throw logger.nullArgumentError("IDP Configuration");
            try {
                String issuerDomain = getDomain(issuer);
                TrustType idpTrust = idpConfiguration.getTrust();
                if (idpTrust != null) {
                    String domainsTrusted = idpTrust.getDomains();

                    logger.trace("Domains that IDP trusts = " + domainsTrusted + " and issuer domain = " + issuerDomain);

                    if (domainsTrusted.indexOf(issuerDomain) < 0) {
                        // Let us do string parts checking
                        StringTokenizer st = new StringTokenizer(domainsTrusted, ",");
                        while (st != null && st.hasMoreTokens()) {
                            String uriBit = st.nextToken();

                            logger.trace("Matching uri bit = " + uriBit);

                            if (issuerDomain.indexOf(uriBit) > 0) {
                                logger.trace("Matched " + uriBit + " trust for " + issuerDomain);
                                return;
                            }
                        }
                        throw logger.samlIssuerNotTrustedError(issuer);
                    }
                } else
                    throw logger.samlHandlerTrustElementMissingError();
            } catch (Exception e) {
                throw new ProcessingException(logger.samlIssuerNotTrustedException(e));
            }
        }
    }

    private class SPTrustHandler {

        public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response, ProviderType spConfiguration)
                throws ProcessingException {
            trustIssuer(spConfiguration, request);
        }

        public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response, ProviderType spConfiguration)
                throws ProcessingException {
            trustIssuer(spConfiguration, request);
        }

        private void trustIssuer(ProviderType spConfiguration, SAML2HandlerRequest request) throws ProcessingException {
            if (spConfiguration == null)
                throw logger.nullArgumentError("SP Configuration");

            String issuer = request.getIssuer().getValue();
            Map<String, Object> requestOptions = request.getOptions();
            PicketLinkAuditHelper auditHelper = (PicketLinkAuditHelper) requestOptions.get(GeneralConstants.AUDIT_HELPER);
            String contextPath = (String) requestOptions.get(GeneralConstants.CONTEXT_PATH);
            try {
                String issuerDomain = getDomain(issuer);
                TrustType spTrust = spConfiguration.getTrust();
                if (spTrust != null) {
                    String domainsTrusted = spTrust.getDomains();

                    logger.trace("Domains that SP trusts = " + domainsTrusted + " and issuer domain = " + issuerDomain);

                    if (domainsTrusted.indexOf(issuerDomain) < 0) {
                        // Let us do string parts checking
                        StringTokenizer st = new StringTokenizer(domainsTrusted, ",");
                        while (st != null && st.hasMoreTokens()) {
                            String uriBit = st.nextToken();

                            logger.trace("Matching uri bit = " + uriBit);

                            if (issuerDomain.indexOf(uriBit) > 0) {
                                logger.trace("Matched " + uriBit + " trust for " + issuerDomain);
                                return;
                            }
                        }
                        if (auditHelper != null) {
                            PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                            auditEvent.setWhoIsAuditing(contextPath);
                            auditEvent.setType(PicketLinkAuditEventType.ERROR_TRUSTED_DOMAIN);
                            auditHelper.audit(auditEvent);
                        }
                        throw logger.samlIssuerNotTrustedError(issuer);
                    }
                } else
                    throw logger.samlHandlerTrustElementMissingError();
            } catch (Exception e) {
                throw new ProcessingException(logger.samlIssuerNotTrustedException(e));
            }
        }
    }

    /**
     * Given a SP or IDP issuer from the assertion, return the host
     *
     * @param domainURL
     *
     * @return
     *
     * @throws IOException
     */
    private static String getDomain(String domainURL) throws IOException {
        try {
            URL url = new URL(domainURL);
            return url.getHost();
        }
        // This could be the case if argument is not full URL (like "google.com" or "google.com/a/mydomain.com")
        catch (MalformedURLException me) {
            domainURL = "http://" + domainURL;
            URL url = new URL(domainURL);
            return url.getHost();
        }
    }
}