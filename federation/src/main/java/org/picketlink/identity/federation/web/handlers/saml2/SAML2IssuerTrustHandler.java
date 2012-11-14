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
package org.picketlink.identity.federation.web.handlers.saml2;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;

import org.jboss.security.audit.AuditLevel;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.ProviderType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

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

                    logger.trace("Domains that SP trusts = " + domainsTrusted +" and issuer domain = " + issuerDomain);
                    
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
     * @return
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