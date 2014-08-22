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
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.w3c.dom.Document;

import java.security.PublicKey;
import java.util.Map;

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
        if (!isSupportsSignature(request) || isIgnoreSignature(request)) {
            return;
        }

        Map<String, Object> requestOptions = request.getOptions();
        PicketLinkAuditHelper auditHelper = (PicketLinkAuditHelper) requestOptions.get(GeneralConstants.AUDIT_HELPER);

        Document signedDocument = request.getRequestDocument();

        if (logger.isTraceEnabled()) {
            logger.trace("Going to validate signature for: " + DocumentUtil.asString(signedDocument));
        }

        PublicKey publicKey = (PublicKey) request.getOptions().get(GeneralConstants.SENDER_PUBLIC_KEY);

        try {
            boolean isValid;

            HTTPContext httpContext = (HTTPContext) request.getContext();

            if (isPostBinding(request, httpContext)) {
                isValid = verifyPostBindingSignature(signedDocument, publicKey);
                logger.trace("HTTP method for validating response: POST");
            } else {
                isValid = verifyRedirectBindingSignature(httpContext, publicKey);
                logger.trace("HTTP method for validating response: GET");
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

    private boolean isPostBinding(SAML2HandlerRequest request, HTTPContext httpContext) {
        boolean isPost = httpContext.getRequest().getMethod().equalsIgnoreCase("POST");
        SAML2Object saml2Object = request.getSAML2Object();

        if (AuthnRequestType.class.isInstance(saml2Object)) {
            AuthnRequestType authnRequest = (AuthnRequestType) saml2Object;
            String authnRequestBinding = authnRequest.getProtocolBinding().toString();

            isPost = authnRequestBinding.equals(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get());
        }

        return isPost;
    }

    private Boolean isIgnoreSignature(SAML2HandlerRequest request) {
        Map<String, Object> requestOptions = request.getOptions();
        Boolean ignoreSignatures = (Boolean) requestOptions.get(GeneralConstants.IGNORE_SIGNATURES);

        if (ignoreSignatures == null){
            ignoreSignatures = Boolean.FALSE;
        }

        //TODO: check signatures for GLO logout requests when using a backchannel
        if (SAML2LogOutHandler.isBackChannelLogoutRequest(request)) {
            return Boolean.TRUE;
        }

        return ignoreSignatures;
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
     *
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
                throw logger.samlHandlerSignatureNotPresentError();
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