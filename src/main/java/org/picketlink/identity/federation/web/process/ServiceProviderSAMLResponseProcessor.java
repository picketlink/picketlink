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
package org.picketlink.identity.federation.web.process;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;

/**
 * Utility Class to handle processing of an SAML Request Message
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */
public class ServiceProviderSAMLResponseProcessor extends ServiceProviderBaseProcessor {

    private boolean validateSignature = false;
    private boolean idpPostBinding = false;

    public void setIdpPostBinding(boolean idpPostBinding) {
        this.idpPostBinding = idpPostBinding;
    }

    /**
     * Construct
     *
     * @param postBinding Whether it is the Post Binding
     * @param serviceURL Service URL of the SP
     */
    public ServiceProviderSAMLResponseProcessor(boolean postBinding, String serviceURL) {
        super(postBinding, serviceURL);
    }

    /**
     * Flag to indicate whether the response should be validated for signature
     *
     * @param validateSignature
     */
    public void setValidateSignature(boolean validateSignature) {
        this.validateSignature = validateSignature;
    }

    /**
     * Process the message
     *
     * @param samlResponse
     * @param httpContext
     * @param handlers
     * @param chainLock a lock that needs to be used to process the chain of handlers
     * @return
     * @throws ProcessingException
     * @throws IOException
     * @throws ParsingException
     * @throws ConfigurationException
     */
    public SAML2HandlerResponse process(String samlResponse, HTTPContext httpContext, Set<SAML2Handler> handlers, Lock chainLock)
            throws ProcessingException, IOException, ParsingException, ConfigurationException {
        SAMLDocumentHolder documentHolder = getSAMLDocumentHolder(samlResponse);

        validateSignature(httpContext, documentHolder);

        SAML2HandlerResponse saml2HandlerResponse = processHandlersChain(httpContext, handlers, chainLock, documentHolder);

        return saml2HandlerResponse;
    }

    private SAML2HandlerResponse processHandlersChain(HTTPContext httpContext, Set<SAML2Handler> handlers, Lock chainLock,
            SAMLDocumentHolder documentHolder) throws ConfigurationException, ProcessingException,
            TrustKeyConfigurationException, TrustKeyProcessingException, IOException {
        // Create the request/response
        SAML2HandlerRequest saml2HandlerRequest = getSAML2HandlerRequest(documentHolder, httpContext);
        SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

        SAMLHandlerChainProcessor chainProcessor = new SAMLHandlerChainProcessor(handlers);

        // Set some request options
        setRequestOptions(saml2HandlerRequest);

        chainProcessor.callHandlerChain(documentHolder.getSamlObject(), saml2HandlerRequest, saml2HandlerResponse, httpContext,
                chainLock);

        return saml2HandlerResponse;
    }

    private boolean isPostBinding() {
        return this.postBinding || idpPostBinding;
    }

    /**
     * <p>
     * Validates the SAML token signature of this option is enabled.
     * </p>
     *
     * @param httpContext
     * @param documentHolder
     * @throws ProcessingException
     */
    private void validateSignature(HTTPContext httpContext, SAMLDocumentHolder documentHolder) throws ProcessingException {
        if (this.validateSignature) {
            try {
                if (isPostBinding()) {
                    this.verifyPostBindingSignature(documentHolder);
                } else {
                    this.verifyRedirectBindingSignature(httpContext);
                }
            } catch (IssuerNotTrustedException e) {
                throw new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE
                        + "Signature Validation failed. Issuer is not trusted by this Service Provider", e);
            } catch (Exception e) {
                throw new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation failed", e);
            }
        }
    }

    private SAMLDocumentHolder getSAMLDocumentHolder(String samlResponse) throws ParsingException, ConfigurationException,
            ProcessingException {
        SAML2Response saml2Response = new SAML2Response();

        InputStream dataStream = null;

        if (isPostBinding()) {
            // deal with SAML response from IDP
            dataStream = PostBindingUtil.base64DecodeAsStream(samlResponse);
        } else {
            // deal with SAML response from IDP
            dataStream = RedirectBindingUtil.base64DeflateDecode(samlResponse);
        }

        saml2Response.getSAML2ObjectFromStream(dataStream);

        return saml2Response.getSamlDocumentHolder();
    }

    /**
     * <p>
     * Validates the signature for SAML tokens received via HTTP Redirect Binding.
     * </p>
     *
     * @param httpContext
     * @throws IssuerNotTrustedException
     * @throws ProcessingException
     */
    private void verifyRedirectBindingSignature(HTTPContext httpContext) throws IssuerNotTrustedException, ProcessingException {
        if (keyManager == null) {
            throw new IllegalStateException(ErrorCodes.TRUST_MANAGER_MISSING);
        }

        boolean isValidSignature = false;

        try {
            String queryString = httpContext.getRequest().getQueryString();

            // Check if there is a signature
            byte[] sigValue;

            sigValue = RedirectBindingSignatureUtil.getSignatureValueFromSignedURL(queryString);

            if (sigValue == null) {
                throw new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE
                        + "Signature Validation failed. Signature is not present. Check if the IDP is supporting signatures.");
            }

            isValidSignature = RedirectBindingSignatureUtil.validateSignature(queryString, getIDPPublicKey(), sigValue);
        } catch (Exception e) {
            throw new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation failed", e);
        }

        if (!isValidSignature) {
            throw new IssuerNotTrustedException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation failed");
        }

    }

    /**
     * Validate the signature of the IDP response
     *
     * @param samlDocumentHolder
     * @return
     * @throws IssuerNotTrustedException
     * @throws ProcessingException
     */
    private void verifyPostBindingSignature(SAMLDocumentHolder samlDocumentHolder) throws IssuerNotTrustedException,
            ProcessingException {
        if (keyManager == null) {
            throw new IllegalStateException(ErrorCodes.TRUST_MANAGER_MISSING);
        }

        boolean sigResult = false;

        try {
            PublicKey publicKey = getIDPPublicKey();

            if (trace)
                log.trace("Going to verify signature in the saml response from IDP");

            sigResult = new SAML2Signature().validate(samlDocumentHolder.getSamlDocument(), publicKey);

            if (trace)
                log.trace("Signature verification=" + sigResult);
        } catch (Exception e) {
            throw new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation failed", e);
        }

        if (!sigResult) {
            throw new IssuerNotTrustedException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation failed");
        }
    }
}