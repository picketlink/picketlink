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
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

/**
 * Handles SAML2 Signature
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2SignatureGenerationHandler extends BaseSAML2Handler {
    private static Logger log = Logger.getLogger(SAML2SignatureGenerationHandler.class);

    private final boolean trace = log.isTraceEnabled();

    @Override
    public void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        // Generate the signature
        Document samlDocument = response.getResultingDocument();

        if (samlDocument == null && trace) {
            log.trace("No document generated in the handler chain. Cannot generate signature");
            return;
        }

        this.sign(samlDocument, response);
    }

    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        Document responseDocument = response.getResultingDocument();
        if (responseDocument == null) {
            if (trace) {
                log.trace("handleRequestType:No response document found");
            }
            return;
        }

        this.sign(responseDocument, response);
    }

    @Override
    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        Document responseDocument = response.getResultingDocument();
        if (responseDocument == null) {
            if (trace) {
                log.trace("handleStatusResponseType:No response document found");
            }
            return;
        }

        this.sign(responseDocument, response);
    }

    private void sign(Document samlDocument, SAML2HandlerResponse response) throws ProcessingException {
        // Get the Key Pair
        KeyPair keypair = (KeyPair) this.handlerChainConfig.getParameter(GeneralConstants.KEYPAIR);

        if (keypair == null) {
            log.error("Key Pair cannot be found");
            throw new ProcessingException(ErrorCodes.NULL_VALUE + "KeyPair not found");
        }

        if (response.isPostBindingForResponse()) {
            if (trace)
                log.trace("Going to sign response document with POST binding type");
            signPost(samlDocument, keypair);
        }
        else {
            if (trace)
                log.trace("Going to sign response document with REDIRECT binding type");
            String destinationQueryString = signRedirect(samlDocument, response.getRelayState(), keypair, response.getSendRequest());
            response.setDestinationQueryStringWithSignature(destinationQueryString);
        }
    }

    private void signPost(Document samlDocument, KeyPair keypair) throws ProcessingException {
        SAML2Signature samlSignature = new SAML2Signature();
        Node nextSibling = samlSignature.getNextSiblingOfIssuer(samlDocument);
        samlSignature.setNextSibling(nextSibling);
        samlSignature.signSAMLDocument(samlDocument, keypair);
    }

    private String signRedirect(Document samlDocument, String relayState, KeyPair keypair, boolean willSendRequest) throws ProcessingException {
        try {
            String samlMessage = DocumentUtil.getDocumentAsString(samlDocument);
            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMessage.getBytes("UTF-8"));
            PrivateKey signingKey = keypair.getPrivate();

            String url;

            // Encode relayState before signing
            if (isNotNull(relayState))
                relayState = RedirectBindingUtil.urlEncode(relayState);

            if (willSendRequest) {
                url = RedirectBindingSignatureUtil.getSAMLRequestURLWithSignature(base64Request, relayState,
                    signingKey);
            } else {
                url = RedirectBindingSignatureUtil.getSAMLResponseURLWithSignature(base64Request, relayState,
                    signingKey);
            }

            return url;
        }
        catch (ConfigurationException ce) {
           log.error("Error when trying to sign message for redirection", ce);
           throw new RuntimeException(ce);
        }
        catch (GeneralSecurityException ce) {
           log.error("Error when trying to sign message for redirection", ce);
           throw new RuntimeException(ce);
        }
        catch (IOException ce) {
           log.error("Error when trying to sign message for redirection", ce);
           throw new RuntimeException(ce);
        }
    }
}