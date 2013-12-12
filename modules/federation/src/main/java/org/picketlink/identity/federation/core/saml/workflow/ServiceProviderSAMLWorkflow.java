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
package org.picketlink.identity.federation.core.saml.workflow;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.w3c.dom.Document;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import static org.picketlink.common.util.StringUtil.isNotNull;

/**
 * Common methods used by the SAML Service Provider
 * @author Anil Saldhana
 * @since December 03, 2013
 */
public class ServiceProviderSAMLWorkflow {

    private RedirectionHandler redirectionHandler = new RedirectionHandler();

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Set a web container specific {@link org.picketlink.identity.federation.core.saml.workflow.ServiceProviderSAMLWorkflow.RedirectionHandler}
     * @param theHandler
     * @return {@link org.picketlink.identity.federation.core.saml.workflow.ServiceProviderSAMLWorkflow} for chaining methods
     */
    public ServiceProviderSAMLWorkflow setRedirectionHandler(RedirectionHandler theHandler){
        this.redirectionHandler = theHandler;
        return this;
    }

    /**
     * Perform validation of the request object
     *
     * @param request
     * @return
     */
    public boolean validate(HttpServletRequest request) {
        return request.getParameter("SAMLResponse") != null;
    }

    /**
     * <p>
     * Indicates if the current request is a GlobalLogout request.
     * </p>
     *
     * @param request
     * @return
     */
    public boolean isGlobalLogout(HttpServletRequest request) {
        String gloStr = request.getParameter(GeneralConstants.GLOBAL_LOGOUT);
        return isNotNull(gloStr) && "true".equalsIgnoreCase(gloStr);
    }

    /**
     * Verify whether a {@link HttpServletRequest} is for Local Logout
     * @param request
     * @return
     */
    public boolean isLocalLogoutRequest(HttpServletRequest request){
        String lloStr = request.getParameter(GeneralConstants.LOCAL_LOGOUT);
        return isNotNull(lloStr) && "true".equalsIgnoreCase(lloStr);
    }

    public void sendToLogoutPage(HttpServletRequest request, HttpServletResponse response, HttpSession session,
                                    ServletContext servletContext,
                                    String logOutPage) throws IOException, ServletException {
        // we are invalidated.
        RequestDispatcher dispatch = servletContext.getRequestDispatcher(logOutPage);
        if (dispatch == null)
            logger.samlSPCouldNotDispatchToLogoutPage(logOutPage);
        else {
            logger.trace("Forwarding request to logOutPage: " + logOutPage);
            session.invalidate();
            try {
                dispatch.forward(request, response);
            } catch (Exception e) {
                // JBAS5.1 and 6 quirkiness
                //dispatch.forward(request.getRequest(), response);
            }
        }
    }

    /**
     * <p>
     * Send the request to the IDP. Subclasses should override this method to implement how requests must be sent to the IDP.
     * </p>
     *
     * @param destination idp url
     * @param samlDocument request or response document
     * @param relayState
     * @param response
     * @param willSendRequest are we sending Request or Response to IDP
     * @param destinationQueryStringWithSignature used only with Redirect binding and with signature enabled.
     * @param httpPostBinding
     * @throws org.picketlink.common.exceptions.ProcessingException
     * @throws org.picketlink.common.exceptions.ConfigurationException
     * @throws IOException
     */
    public void sendRequestToIDP(String destination, Document samlDocument, String relayState, HttpServletResponse response,
                                    boolean willSendRequest, String destinationQueryStringWithSignature,
                                    boolean httpPostBinding) throws ProcessingException, ConfigurationException, IOException {
        if (httpPostBinding) {
            sendHttpPostBindingRequest(destination, samlDocument, relayState, response, willSendRequest);
        } else {
            sendHttpRedirectRequest(destination, samlDocument, relayState, response, willSendRequest, destinationQueryStringWithSignature);
        }
    }

    /**
     * <p>
     * Sends a HTTP Redirect request to the IDP.
     * </p>
     *
     * @param destination
     * @param relayState
     * @param response
     * @param willSendRequest
     * @param destinationQueryStringWithSignature
     * @throws IOException
     * @throws java.io.UnsupportedEncodingException
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    public void sendHttpRedirectRequest(String destination, Document samlDocument, String relayState, HttpServletResponse response,
                                           boolean willSendRequest, String destinationQueryStringWithSignature) throws IOException,
            ProcessingException, ConfigurationException {
        String destinationQueryString = null;

        // We already have queryString with signature from SAML2SignatureGenerationHandler
        if (destinationQueryStringWithSignature != null) {
            destinationQueryString = destinationQueryStringWithSignature;
        }
        else {
            String samlMessage = DocumentUtil.getDocumentAsString(samlDocument);
            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMessage.getBytes("UTF-8"));
            destinationQueryString = RedirectBindingUtil.getDestinationQueryString(base64Request, relayState, willSendRequest);
        }

        RedirectBindingUtil.RedirectBindingUtilDestHolder holder = new RedirectBindingUtil.RedirectBindingUtilDestHolder();

        holder.setDestination(destination).setDestinationQueryString(destinationQueryString);

        //HTTPRedirectUtil.sendRedirectForRequestor(RedirectBindingUtil.getDestinationURL(holder), response);
        redirectionHandler.sendRedirectForRequestor(RedirectBindingUtil.getDestinationURL(holder), response);
    }

    /**
     * <p>
     * Sends a HTTP POST request to the IDP.
     * </p>
     *
     * @param destination
     * @param samlDocument
     * @param relayState
     * @param response
     * @param willSendRequest
     * @throws org.picketlink.common.exceptions.TrustKeyProcessingException
     * @throws ProcessingException
     * @throws IOException
     * @throws ConfigurationException
     */
    public void sendHttpPostBindingRequest(String destination, Document samlDocument, String relayState, HttpServletResponse response,
                                              boolean willSendRequest) throws ProcessingException, IOException,
            ConfigurationException {
        String samlMessage = PostBindingUtil.base64Encode(DocumentUtil.getDocumentAsString(samlDocument));

        DestinationInfoHolder destinationHolder = new DestinationInfoHolder(destination, samlMessage, relayState);

        //PostBindingUtil.sendPost(destinationHolder, response, willSendRequest);
        redirectionHandler.sendPost(destinationHolder, response, willSendRequest);
    }

    /**
     * Class that handles the web container specific behavior for POST
     * and REDIRECT workflows
     */
    public static class RedirectionHandler{
        /**
         * Send the payload via HTTP/POST
         * @param destinationHolder {@link org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder} holds info on the destination
         * @param response {@link javax.servlet.http.HttpServletResponse}
         * @param willSendRequest whether it is a SAML request or response so that the page title can be set
         * @throws IOException
         */
        public void sendPost(DestinationInfoHolder destinationHolder,HttpServletResponse response, boolean willSendRequest) throws IOException{
            PostBindingUtil.sendPost(destinationHolder, response, willSendRequest);
        }

        /**
         * Send the payload via HTTP/REDIRECT
         * @param url the redirect url
         * @param response {@link javax.servlet.http.HttpServletResponse}
         * @throws IOException
         */
        public void sendRedirectForRequestor(String url, HttpServletResponse response) throws IOException {
            HTTPRedirectUtil.sendRedirectForRequestor(url, response);
        }

        /**
         * Send the payload via HTTP/REDIRECT
         * @param destination the destination url
         * @param response {@link javax.servlet.http.HttpServletResponse}
         * @throws IOException
         */
        public void sendRedirectForResponder(String destination, HttpServletResponse response) throws IOException {
            HTTPRedirectUtil.sendRedirectForResponder(destination,response);
        }
    }
}