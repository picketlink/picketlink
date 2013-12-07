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

import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.picketlink.common.util.StringUtil.isNotNull;

/**
 * Handles SAML2 Signature
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2SignatureGenerationHandler extends AbstractSignatureHandler {

    public static final String SIGN_ASSERTION_ONLY = "SIGN_ASSERTION_ONLY";
    public static final String SIGN_RESPONSE_AND_ASSERTION = "SIGN_RESPONSE_AND_ASSERTION";

    @Override
    public void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        // Generate the signature
        Document samlDocument = response.getResultingDocument();

        if (samlDocument == null) {
            logger.trace("No document generated in the handler chain. Cannot generate signature");
            return;
        }

        this.sign(samlDocument, request, response);
    }

    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        Document responseDocument = response.getResultingDocument();

        if (responseDocument == null) {
            logger.trace("No response document found");
            return;
        }

        this.sign(responseDocument, request, response);
    }

    @Override
    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        Document responseDocument = response.getResultingDocument();
        if (responseDocument == null) {
            logger.trace("No response document found");
            return;
        }

        this.sign(responseDocument, request, response);
    }

    private void sign(Document samlDocument, SAML2HandlerRequest request, SAML2HandlerResponse response)
            throws ProcessingException {
        if (!isSupportsSignature(request)) {
            return;
        }

        // Get the Key Pair
        KeyPair keypair = (KeyPair) this.handlerChainConfig.getParameter(GeneralConstants.KEYPAIR);
        X509Certificate x509Certificate = (X509Certificate) this.handlerChainConfig.getParameter(GeneralConstants.X509CERTIFICATE);

        if (keypair == null) {
            logger.samlHandlerKeyPairNotFound();
            throw logger.samlHandlerKeyPairNotFoundError();
        }

        if (isSAMLResponse(samlDocument)) {
            if (isSignAssertionOnly() || isSignResponseAndAssertion()) {
                Element originalAssertionElement = DocumentUtil.getChildElement(samlDocument.getDocumentElement(), new QName(JBossSAMLURIConstants.ASSERTION_NSURI.get(), JBossSAMLConstants.ASSERTION.get()));
                Node clonedAssertionElement = originalAssertionElement.cloneNode(true);
                Document temporaryDocument;

                try {
                    temporaryDocument = DocumentUtil.createDocument();
                } catch (ConfigurationException e) {
                    throw this.logger.processingError(e);
                }

                temporaryDocument.adoptNode(clonedAssertionElement);
                temporaryDocument.appendChild(clonedAssertionElement);

                signDocument(temporaryDocument, keypair, x509Certificate);

                samlDocument.adoptNode(clonedAssertionElement);

                Element parentNode = (Element) originalAssertionElement.getParentNode();

                parentNode.replaceChild(clonedAssertionElement, originalAssertionElement);
            }

            if (!isSignAssertionOnly()) {
                signDocument(samlDocument, keypair, x509Certificate);
            }
        } else {
            signDocument(samlDocument, keypair, x509Certificate);
        }

        if (!response.isPostBindingForResponse()) {
            logger.trace("Going to sign response document with REDIRECT binding type");
            String destinationQueryString = signRedirect(samlDocument, response.getRelayState(), keypair,
                    response.getSendRequest());
            response.setDestinationQueryStringWithSignature(destinationQueryString);
        }
    }

    private void signDocument(Document samlDocument, KeyPair keypair, X509Certificate x509Certificate) throws ProcessingException {
        SAML2Signature samlSignature = new SAML2Signature();
        Node nextSibling = samlSignature.getNextSiblingOfIssuer(samlDocument);

        samlSignature.setNextSibling(nextSibling);

        if (x509Certificate != null) {
            samlSignature.setX509Certificate(x509Certificate);
        }

        logger.trace("Going to sign document.");
        samlSignature.signSAMLDocument(samlDocument, keypair);
    }

    private String signRedirect(Document samlDocument, String relayState, KeyPair keypair,
                                boolean willSendRequest)
            throws ProcessingException {
        try {
            String samlMessage = DocumentUtil.getDocumentAsString(samlDocument);
            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMessage.getBytes("UTF-8"));
            PrivateKey signingKey = keypair.getPrivate();

            String url;

            // Encode relayState before signing
            if (isNotNull(relayState))
                relayState = RedirectBindingUtil.urlEncode(relayState);

            if (willSendRequest) {
                url = RedirectBindingSignatureUtil.getSAMLRequestURLWithSignature(base64Request, relayState, signingKey);
            } else {
                url = RedirectBindingSignatureUtil.getSAMLResponseURLWithSignature(base64Request, relayState, signingKey);
            }

            return url;
        } catch (ConfigurationException ce) {
            logger.samlHandlerErrorSigningRedirectBindingMessage(ce);
            throw logger.samlHandlerSigningRedirectBindingMessageError(ce);
        } catch (GeneralSecurityException ce) {
            logger.samlHandlerErrorSigningRedirectBindingMessage(ce);
            throw logger.samlHandlerSigningRedirectBindingMessageError(ce);
        } catch (IOException ce) {
            logger.samlHandlerErrorSigningRedirectBindingMessage(ce);
            throw logger.samlHandlerSigningRedirectBindingMessageError(ce);
        }
    }

    private boolean isSAMLResponse(final Document samlDocument) {
        return samlDocument.getDocumentElement().getLocalName().equals(JBossSAMLConstants.RESPONSE.get());
    }

    private boolean isSignAssertionOnly() {
        return this.handlerConfig.getParameter(SIGN_ASSERTION_ONLY) != null ? Boolean.valueOf(this.handlerConfig.getParameter(SIGN_ASSERTION_ONLY).toString()) : false;
    }

    private boolean isSignResponseAndAssertion() {
        return this.handlerConfig.getParameter(SIGN_RESPONSE_AND_ASSERTION) != null ? Boolean.valueOf(this.handlerConfig.getParameter(SIGN_RESPONSE_AND_ASSERTION).toString()) : false;
    }

}