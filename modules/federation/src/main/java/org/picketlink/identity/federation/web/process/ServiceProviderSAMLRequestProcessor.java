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

import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil.RedirectBindingUtilDestHolder;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletResponse;
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
public class ServiceProviderSAMLRequestProcessor extends ServiceProviderBaseProcessor {

    /**
     * Construct
     *
     * @param postBinding Whether it is the Post Binding
     * @param serviceURL Service URL of the SP
     */
    public ServiceProviderSAMLRequestProcessor(boolean postBinding, String serviceURL, PicketLinkType configuration) {
        super(postBinding, serviceURL, configuration);
    }

    /**
     * Process the message
     *
     * @param samlRequest
     * @param httpContext
     * @param handlers
     * @param chainLock A Lock on the chain of handlers that needs to be used for locking
     *
     * @return
     *
     * @throws ProcessingException
     * @throws IOException
     * @throws ParsingException
     * @throws ConfigurationException
     */
    public boolean process(String samlRequest, HTTPContext httpContext, Set<SAML2Handler> handlers, Lock chainLock)
            throws ProcessingException, IOException, ParsingException, ConfigurationException {
        SAML2Request saml2Request = new SAML2Request();
        SAML2HandlerResponse saml2HandlerResponse = null;
        SAML2Object samlObject = null;
        SAMLDocumentHolder documentHolder = null;

        if (this.postBinding) {
            // we got a logout request from IDP
            InputStream is = PostBindingUtil.base64DecodeAsStream(samlRequest);
            samlObject = saml2Request.getSAML2ObjectFromStream(is);
        } else {
            InputStream is = RedirectBindingUtil.base64DeflateDecode(samlRequest);
            samlObject = saml2Request.getSAML2ObjectFromStream(is);
        }

        documentHolder = saml2Request.getSamlDocumentHolder();

        // Create the request/response
        SAML2HandlerRequest saml2HandlerRequest = getSAML2HandlerRequest(documentHolder, httpContext);
        saml2HandlerResponse = new DefaultSAML2HandlerResponse();
        saml2HandlerResponse.setPostBindingForResponse(postBinding);

        SAMLHandlerChainProcessor chainProcessor = new SAMLHandlerChainProcessor(handlers, this.configuration);

        // Set some request options
        setRequestOptions(saml2HandlerRequest);

        chainProcessor.callHandlerChain(samlObject, saml2HandlerRequest, saml2HandlerResponse, httpContext, chainLock);

        Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
        String relayState = saml2HandlerResponse.getRelayState();

        String destination = saml2HandlerResponse.getDestination();

        boolean willSendRequest = saml2HandlerResponse.getSendRequest();

        if (destination != null && samlResponseDocument != null) {
            if (postBinding) {
                sendRequestToIDP(destination, samlResponseDocument, relayState, httpContext.getResponse(), willSendRequest);
            } else {
                String destinationQuery = saml2HandlerResponse.getDestinationQueryStringWithSignature();

                // This is the case with signatures disabled
                if (destinationQuery == null) {
                    boolean areWeSendingRequest = saml2HandlerResponse.getSendRequest();
                    String samlMsg = DocumentUtil.getDocumentAsString(samlResponseDocument);

                    String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMsg.getBytes("UTF-8"));
                    destinationQuery = RedirectBindingUtil.getDestinationQueryString(base64Request, relayState,
                            areWeSendingRequest);
                }

                RedirectBindingUtilDestHolder holder = new RedirectBindingUtilDestHolder();
                holder.setDestination(destination).setDestinationQueryString(destinationQuery);

                String destinationURL = RedirectBindingUtil.getDestinationURL(holder);

                HTTPRedirectUtil.sendRedirectForRequestor(destinationURL, httpContext.getResponse());
            }
            return true;
        }

        return false;
    }

    /**
     * Send the request to the IDP
     *
     * @param destination idp url
     * @param samlDocument request or response document
     * @param relayState
     * @param response
     * @param willSendRequest are we sending Request or Response to IDP
     *
     * @throws ProcessingException
     * @throws ConfigurationException
     * @throws IOException
     */
    protected void sendRequestToIDP(String destination, Document samlDocument, String relayState, HttpServletResponse response,
                                    boolean willSendRequest) throws ProcessingException, ConfigurationException, IOException {
        String samlMessage = DocumentUtil.getDocumentAsString(samlDocument);
        samlMessage = PostBindingUtil.base64Encode(samlMessage);
        PostBindingUtil.sendPost(new DestinationInfoHolder(destination, samlMessage, relayState), response, willSendRequest);
    }
}