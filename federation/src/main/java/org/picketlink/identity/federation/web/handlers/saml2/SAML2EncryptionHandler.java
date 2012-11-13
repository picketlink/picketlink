/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * <p>Handles the encryption and signing of SAML Assertions.</p>
 * <p>This handler should be used only on the IDP side and it must be positioned after the {@link SAML2AuthenticationHandler} in the chain.</p>
 * <p>Configuration options are: <code>GeneralConstants.SAML_ENC_KEY_SIZE</code> and <code>GeneralConstants.SAML_ENC_ALGORITHM</code>.</p>
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
