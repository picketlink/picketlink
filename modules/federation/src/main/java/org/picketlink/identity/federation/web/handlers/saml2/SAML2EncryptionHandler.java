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
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import java.security.PublicKey;

/**
 * <p>Handles the encryption and signing of SAML Assertions.</p>
 * <p>This handler should be used only on the IDP side and it must be positioned after the {@link
 * SAML2AuthenticationHandler} in the chain.</p>
 * <p>Configuration options are: <code>GeneralConstants.SAML_ENC_KEY_SIZE</code> and
 * <code>GeneralConstants.SAML_ENC_ALGORITHM</code>.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class SAML2EncryptionHandler extends SAML2SignatureGenerationHandler {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureGenerationHandler#handleRequestType(org.picketlink
     * .identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest,
     * org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse)
     */
    @Override
    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        if (supportsRequest(request) && isEncryptionEnabled()) {
            Document samlResponseDocument = response.getResultingDocument();

            if (samlResponseDocument == null) {
                throwResponseDocumentOrAssertionNotFound();
            }

            String samlNSPrefix = getSAMLNSPrefix(samlResponseDocument);

            try {
                QName encryptedAssertionElementQName = new QName(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
                        JBossSAMLConstants.ENCRYPTED_ASSERTION.get(), samlNSPrefix);

                byte[] secret = WSTrustUtil.createRandomSecret(128 / 8);
                SecretKey secretKey = new SecretKeySpec(secret, getAlgorithm());

                // encrypt the Assertion element and replace it with a EncryptedAssertion element.
                XMLEncryptionUtil.encryptElement(new QName(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
                        JBossSAMLConstants.ASSERTION.get(), samlNSPrefix), samlResponseDocument, getSenderPublicKey(request),
                        secretKey, getKeySize(), encryptedAssertionElementQName, true);
            } catch (Exception e) {
                throw logger.processingError(e);
            }
        }

        // proceed with the signing process.
        super.handleRequestType(request, response);
    }

    private String getSAMLNSPrefix(Document samlResponseDocument) {
        Node assertionElement = samlResponseDocument.getDocumentElement()
                .getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), JBossSAMLConstants.ASSERTION.get()).item(0);

        if (assertionElement == null) {
            throwResponseDocumentOrAssertionNotFound();
        }

        return assertionElement.getPrefix();
    }

    /**
     * <p>Indicates if the IDP has encryption enabled.</p>
     *
     * @return
     */
    private boolean isEncryptionEnabled() {
        return getType() == HANDLER_TYPE.IDP && getConfiguration().isEncrypt();
    }

    /**
     * <p>
     * Indicates if this handler supports the specified {@link SAML2HandlerRequest}.
     * </p>
     *
     * @param request
     *
     * @return
     */
    private boolean supportsRequest(SAML2HandlerRequest request) {
        return getType() == HANDLER_TYPE.IDP && (request.getSAML2Object() instanceof AuthnRequestType);
    }

    private IDPType getConfiguration() {
        IDPType configuration = (IDPType) handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION);

        if (configuration == null) {
            throw logger.nullArgumentError("IDP Configuration");
        }

        return configuration;
    }

    private int getKeySize() {
        String keySize = (String) handlerConfig.getParameter(GeneralConstants.SAML_ENC_KEY_SIZE);

        if (keySize == null) {
            keySize = String.valueOf(128);
        }

        return Integer.valueOf(keySize);
    }

    private String getAlgorithm() {
        String algorithm = (String) handlerConfig.getParameter(GeneralConstants.SAML_ENC_ALGORITHM);

        if (algorithm == null) {
            algorithm = "AES";
        }

        return algorithm;
    }

    private PublicKey getSenderPublicKey(SAML2HandlerRequest request) {
        PublicKey publicKey = (PublicKey) request.getOptions().get(GeneralConstants.SENDER_PUBLIC_KEY);

        if (publicKey == null) {
            throw logger.nullArgumentError("Sender Public Key");
        }

        return publicKey;
    }

    private void throwResponseDocumentOrAssertionNotFound() {
        throw new IllegalStateException(
                "No response document/assertions found. Check if this handler is after the SAML2AuthenticationHandler.");
    }

}
