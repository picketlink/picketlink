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
package org.picketlink.identity.federation.web.listeners;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLProtocolContext;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * An instance of {@link HttpSessionListener} at the IDP that performs actions when an {@link HttpSession} is created
 * or
 * destroyed.
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 3, 2012
 */
public class IDPHttpSessionListener implements HttpSessionListener {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public void sessionCreated(HttpSessionEvent se) {
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession httpSession = se.getSession();
        if (httpSession == null)
            throw logger.nullArgumentError("session");
        AssertionType assertion = (AssertionType) httpSession.getAttribute(GeneralConstants.ASSERTION);

        // If the user had logged out, then the assertion would not be available in the session.
        // The case when the user closes the browser and does not logout, the session will time out on the
        // server. So we know that the token has not been canceled by the STS.
        if (assertion != null) {

            logger.trace("User has closed the browser. So we proceed to cancel the STS issued token.");

            PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
            SAMLProtocolContext samlProtocolContext = new SAMLProtocolContext();
            samlProtocolContext.setIssuedAssertion(assertion);
            try {
                sts.cancelToken(samlProtocolContext);
            } catch (ProcessingException e) {
                logger.error(e);
            }
            httpSession.removeAttribute(GeneralConstants.ASSERTION);
        }
    }
}