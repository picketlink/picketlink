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
package org.picketlink.identity.federation.core.wstrust;

import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.w3c.dom.Document;

import java.security.Principal;

/**
 * <p>
 * The {@code WSTrustRequestHandler} interface defines the methods that will be responsible for handling the different
 * types of
 * WS-Trust request messages.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface WSTrustRequestHandler {

    /**
     * <p>
     * Initializes the concrete {@code WSTrustRequestHandler} instance.
     * </p>
     *
     * @param configuration a reference to object that contains the STS configuration.
     */
    void initialize(STSConfiguration configuration);

    /**
     * <p>
     * Generates a security token according to the information specified in the request message and returns the created
     * token in
     * the response.
     * </p>
     *
     * @param request the security token request message.
     * @param callerPrincipal the {@code Principal} of the ws-trust token requester.
     *
     * @return a {@code RequestSecurityTokenResponse} containing the generated token.
     *
     * @throws WSTrustException if an error occurs while handling the request message.
     */
    RequestSecurityTokenResponse issue(RequestSecurityToken request, Principal callerPrincipal) throws WSTrustException;

    /**
     * <p>
     * Renews the security token as specified in the request message, returning the renewed token in the response.
     * </p>
     *
     * @param request the request message that contains the token to be renewed.
     * @param callerPrincipal the {@code Principal} of the ws-trust token requester.
     *
     * @return a {@code RequestSecurityTokenResponse} containing the renewed token.
     *
     * @throws WSTrustException if an error occurs while handling the renewal process.
     */
    RequestSecurityTokenResponse renew(RequestSecurityToken request, Principal callerPrincipal) throws WSTrustException;

    /**
     * <p>
     * Cancels the security token as specified in the request message.
     * </p>
     *
     * @param request the request message that contains the token to be canceled.
     * @param callerPrincipal the {@code Principal} of the ws-trust token requester.
     *
     * @return a {@code RequestSecurityTokenResponse} indicating whether the token has been canceled or not.
     *
     * @throws WSTrustException if an error occurs while handling the cancellation process.
     */
    RequestSecurityTokenResponse cancel(RequestSecurityToken request, Principal callerPrincipal) throws WSTrustException;

    /**
     * <p>
     * Validates the security token as specified in the request message.
     * </p>
     *
     * @param request the request message that contains the token to be validated.
     * @param callerPrincipal the {@code Principal} of the ws-trust token requester.
     *
     * @return a {@code RequestSecurityTokenResponse} containing the validation status or a new token.
     *
     * @throws WSTrustException if an error occurs while handling the validation process.
     */
    RequestSecurityTokenResponse validate(RequestSecurityToken request, Principal callerPrincipal)
            throws WSTrustException;

    /**
     * Perform Post Processing on the generated RSTR Collection Document Steps such as signing and encryption need to
     * be
     * done
     * here.
     *
     * @param rstrDocument
     * @param request
     *
     * @return
     *
     * @throws WSTrustException
     */
    Document postProcess(Document rstrDocument, RequestSecurityToken request) throws WSTrustException;
}
