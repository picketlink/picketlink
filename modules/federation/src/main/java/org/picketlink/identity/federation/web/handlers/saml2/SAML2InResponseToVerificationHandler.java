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
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;

import javax.servlet.http.HttpSession;

/**
 * Handler is useful on SP side. It's used for verification that InResponseId from SAML Authentication Response is same
 * as ID of
 * previously sent SAML Authentication request
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SAML2InResponseToVerificationHandler extends BaseSAML2Handler {

    @Override
    public void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        if (SAML2HandlerRequest.GENERATE_REQUEST_TYPE.AUTH != request.getTypeOfRequestToBeGenerated())
            return;

        if (getType() == HANDLER_TYPE.IDP)
            return;

        // Determine Id of of request, which is saved into session thanks to SAML2AuthenticationHandler
        String authnRequestId = (String) request.getOptions().get(GeneralConstants.AUTH_REQUEST_ID);

        // Save it into session for later use
        HttpSession session = BaseSAML2Handler.getHttpSession(request);
        session.setAttribute(GeneralConstants.AUTH_REQUEST_ID, authnRequestId);

        logger.trace("ID of authentication request " + authnRequestId + " saved into HTTP session.");
    }

    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
    }

    @Override
    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        if (request.getSAML2Object() instanceof ResponseType == false)
            return;

        if (getType() == HANDLER_TYPE.IDP)
            return;

        // Obtain inResponseTo ID from Authentication response
        ResponseType responseType = (ResponseType) request.getSAML2Object();
        String inResponseTo = responseType.getInResponseTo();

        // Obtain ID from session, which was saved before sending AuthnRequest
        HttpSession session = BaseSAML2Handler.getHttpSession(request);
        String authnRequestId = (String) session.getAttribute(GeneralConstants.AUTH_REQUEST_ID);

        // Remove it from session now
        session.removeAttribute(GeneralConstants.AUTH_REQUEST_ID);

        // Compare both ID
        if (inResponseTo != null && inResponseTo.equals(authnRequestId)) {
            logger.trace("Successful verification of InResponseTo for request " + inResponseTo);
        } else {
            logger.samlHandlerFailedInResponseToVerification(inResponseTo, authnRequestId);
            throw logger.samlHandlerFailedInResponseToVerificarionError();
        }
    }
}
