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

import java.security.PublicKey;
import java.util.Map;

import org.jboss.security.audit.AuditLevel;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.w3c.dom.Document;

/**
 * Validates Signatures inside the SAML payload
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 13, 2009
 */
public class SAML2SignatureValidationHandler extends AbstractSignatureHandler {

    private SAML2Signature saml2Signature = new SAML2Signature();

    /**
     * @see {@code SAML2Handler#handleRequestType(SAML2HandlerRequest, SAML2HandlerResponse)}
     */
    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        validateSender(request, response);
    }

    @Override
    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        validateSender(request, response);
    }

    // Same method can be used for "handleRequestType" and "handleStatusResponseType" validations
    private void validateSender(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        if (!isSupportsSignature(request)) {
            return;
        }

        Map<String, Object> requestOptions = request.getOptions();
        PicketLinkAuditHelper auditHelper = (PicketLinkAuditHelper) requestOptions.get(GeneralConstants.AUDIT_HELPER);
        Boolean ignoreSignatures = (Boolean) requestOptions.get(GeneralConstants.IGNORE_SIGNATURES);
        if (ignoreSignatures == Boolean.TRUE)
            return;

        Document signedDocument = request.getRequestDocument();

        if (logger.isTraceEnabled()) {
            logger.signatureValidatingDocument(DocumentUtil.asString(signedDocument));
        }

        PublicKey publicKey = (PublicKey) request.getOptions().get(GeneralConstants.SENDER_PUBLIC_KEY);
        try {
            boolean isValid;

            HTTPContext httpContext = (HTTPContext) request.getContext();
            boolean isPost = httpContext.getRequest().getMethod().equalsIgnoreCase("POST");

            logger.samlHandlerValidatingResponseForHTTPMethod(httpContext.getRequest().getMethod());

            if (isPost) {
                isValid = verifyPostBindingSignature(signedDocument, publicKey);
            } else {
                isValid = verifyRedirectBindingSignature(httpContext, publicKey);
            }

            if (!isValid) {
                if (auditHelper != null) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setWhoIsAuditing((String) requestOptions.get(GeneralConstants.CONTEXT_PATH));
                    auditEvent.setType(PicketLinkAuditEventType.ERROR_SIG_VALIDATION);
                    auditHelper.audit(auditEvent);
                }

                throw constructSignatureException();
            }
        } catch (ProcessingException pe) {
            if (auditHelper != null) {
                PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                auditEvent.setWhoIsAuditing((String) requestOptions.get(GeneralConstants.CONTEXT_PATH));
                auditEvent.setType(PicketLinkAuditEventType.ERROR_SIG_VALIDATION);
                auditHelper.audit(auditEvent);
            }
            response.setError(SAML2HandlerErrorCodes.SIGNATURE_INVALID, "Signature Validation Failed");
            throw pe;
        }
    }

    private boolean verifyPostBindingSignature(Document signedDocument, PublicKey publicKey) throws ProcessingException {
        try {
            return this.saml2Signature.validate(signedDocument, publicKey);
        } catch (Exception e) {
            logger.samlHandlerErrorValidatingSignature(e);
            throw logger.samlHandlerInvalidSignatureError();
        }
    }

    /**
     * <p>
     * Validates the signature for SAML tokens received via HTTP Redirect Binding.
     * </p>
     *
     * @param httpContext
     * @throws org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException
     * @throws ProcessingException
     */
    private boolean verifyRedirectBindingSignature(HTTPContext httpContext, PublicKey publicKey) throws ProcessingException {
        try {
            String queryString = httpContext.getRequest().getQueryString();

            // Check if there is a signature
            byte[] sigValue;

            sigValue = RedirectBindingSignatureUtil.getSignatureValueFromSignedURL(queryString);

            if (sigValue == null) {
                throw logger.samlHandlerSignatureNorPresentError();
            }

            return RedirectBindingSignatureUtil.validateSignature(queryString, publicKey, sigValue);
        } catch (Exception e) {
            throw logger.samlHandlerSignatureValidationError(e);
        }
    }

    private ProcessingException constructSignatureException() {
        return new ProcessingException(logger.samlHandlerSignatureValidationFailed());
    }
}