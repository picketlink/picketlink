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
package org.picketlink.identity.federation.bindings.tomcat.sp;

import java.io.IOException;
import java.security.KeyPair;

import org.apache.catalina.connector.Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Document;

/**
 * JBID-142: POST form authenticator that can handle signatures at the SP side
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Jul 24, 2009
 */
public class SPPostSignatureFormAuthenticator extends SPPostFormAuthenticator {

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.AbstractSPFormAuthenticator#doSupportSignature()
     */
    @Override
    protected boolean doSupportSignature() {
        return true;
    }
    
    /**
     * Send the request to the IDP
     * 
     * @param destination idp url
     * @param samlDocument request or response document
     * @param relayState
     * @param response
     * @param willSendRequest are we sending Request or Response to IDP
     * @throws ProcessingException
     * @throws ConfigurationException
     * @throws IOException
     */
    @Override
    protected void sendRequestToIDP(String destination, Document samlDocument, String relayState, Response response,
            boolean willSendRequest) throws ProcessingException, ConfigurationException, IOException {
        if (keyManager == null)
            throw new IllegalStateException(ErrorCodes.NULL_VALUE + "Key Manager");
        // Sign the document
        SAML2Signature samlSignature = new SAML2Signature();
        KeyPair keypair = keyManager.getSigningKeyPair();
        samlSignature.signSAMLDocument(samlDocument, keypair);

        if (trace)
            log.trace("Sending to IDP:" + DocumentUtil.asString(samlDocument));
        // Let the super class handle the sending
        super.sendRequestToIDP(destination, samlDocument, relayState, response, willSendRequest);
    }
}