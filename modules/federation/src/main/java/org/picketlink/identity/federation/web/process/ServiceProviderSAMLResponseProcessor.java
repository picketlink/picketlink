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
package org.picketlink.identity.federation.web.process;

import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.locks.Lock;

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
    public ServiceProviderSAMLResponseProcessor(boolean postBinding, String serviceURL, PicketLinkType picketLinkType) {
        super(postBinding, serviceURL, picketLinkType);
    }

    /**
     * Process the message
     *
     * @param samlResponse
     * @param httpContext
     * @param handlers
     * @param chainLock a lock that needs to be used to process the chain of handlers
     *
     * @return
     *
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
                                                      SAMLDocumentHolder documentHolder) throws ConfigurationException, ProcessingException, IOException {
        // Create the request/response
        SAML2HandlerRequest saml2HandlerRequest = getSAML2HandlerRequest(documentHolder, httpContext);
        SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

        SAMLHandlerChainProcessor chainProcessor = new SAMLHandlerChainProcessor(handlers, this.configuration);

        // Set some request options
        setRequestOptions(saml2HandlerRequest);
        saml2HandlerRequest.addOption(GeneralConstants.CONTEXT_PATH, httpContext.getServletContext().getContextPath());
        saml2HandlerRequest.addOption(GeneralConstants.SUPPORTS_SIGNATURES, getSpConfiguration().isSupportsSignature());

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

        try {
            saml2Response.getSAML2ObjectFromStream(dataStream);
        } catch (ProcessingException pe) {
            logger.samlResponseFromIDPParsingFailed();
            throw pe;
        } catch (ParsingException pe) {
            logger.samlResponseFromIDPParsingFailed();
            throw pe;
        }


        return saml2Response.getSamlDocumentHolder();
    }
}