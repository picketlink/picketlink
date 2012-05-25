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
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;

/**
 * Utility Class to handle processing of an SAML Request Message
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */
public class ServiceProviderSAMLResponseProcessor extends ServiceProviderBaseProcessor {

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
        saml2HandlerRequest.addOption(GeneralConstants.CONTEXT_PATH, httpContext.getServletContext().getContextPath());
        saml2HandlerRequest.addOption(GeneralConstants.SUPPORTS_SIGNATURES, this.spConfiguration.isSupportsSignature());

        chainProcessor.callHandlerChain(documentHolder.getSamlObject(), saml2HandlerRequest, saml2HandlerResponse, httpContext,
                chainLock);

        return saml2HandlerResponse;
    }

    private boolean isPostBinding() {
        return this.postBinding || idpPostBinding;
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
}