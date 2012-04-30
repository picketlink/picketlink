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

import org.apache.catalina.connector.Response;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil.RedirectBindingUtilDestHolder;
import org.w3c.dom.Document;

/**
 * Authenticator at the Service Provider that handles HTTP/Redirect binding of SAML 2 but falls back on Form Authentication
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Dec 12, 2008
 */
public class SPRedirectFormAuthenticator extends AbstractSPFormAuthenticator {
    /**
     * The SAML Web Browser SSO Profile says that the IDP cannot send response back in Redirect Binding. The user should use
     * this parameter to adhere to that requirement.
     */
    protected boolean idpPostBinding = false;

    /**
     * Set the Authenticator to expect a post response from IDP
     * 
     * @param idpPostBinding
     */
    public void setIdpPostBinding(Boolean idpPostBinding) {
        this.idpPostBinding = idpPostBinding;
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.BaseFormAuthenticator#getBinding()
     */
    @Override
    protected String getBinding() {
        return JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.get();
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.AbstractSPFormAuthenticator#isPOSTBindingResponse()
     */
    @Override
    protected boolean isPOSTBindingResponse() {
        return this.idpPostBinding;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.AbstractSPFormAuthenticator#sendRequestToIDP(java.lang.String,
     * org.w3c.dom.Document, java.lang.String, org.apache.catalina.connector.Response, boolean)
     */
    @Override
    protected void sendRequestToIDP(String destination, Document samlDocument, String relayState, Response response,
            boolean willSendRequest) throws ProcessingException, ConfigurationException, IOException {
        String samlMsg = DocumentUtil.getDocumentAsString(samlDocument);

        String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMsg.getBytes("UTF-8"));

        String destinationQuery = getDestinationQueryString(base64Request, relayState, willSendRequest);

        RedirectBindingUtilDestHolder holder = new RedirectBindingUtilDestHolder();
        holder.setDestination(destination).setDestinationQueryString(destinationQuery);

        String destinationURL = RedirectBindingUtil.getDestinationURL(holder);

        HTTPRedirectUtil.sendRedirectForRequestor(destinationURL, response);
    }

    protected String getDestinationQueryString(String urlEncodedRequest, String urlEncodedRelayState, boolean sendRequest) {
        return RedirectBindingUtil.getDestinationQueryString(urlEncodedRequest, urlEncodedRelayState, sendRequest);
    }
}