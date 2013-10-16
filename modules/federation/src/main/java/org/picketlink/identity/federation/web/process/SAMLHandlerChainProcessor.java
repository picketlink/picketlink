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

import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.web.core.HTTPContext;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * Processor for the SAML2 Handler Chain
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */
public class SAMLHandlerChainProcessor {

    private final Set<SAML2Handler> handlers = new LinkedHashSet<SAML2Handler>();
    private final PicketLinkType configuration;

    public SAMLHandlerChainProcessor(Set<SAML2Handler> handlers, PicketLinkType configuration) {
        this.handlers.addAll(handlers);
        this.configuration = configuration;
    }

    public void callHandlerChain(SAML2Object samlObject, SAML2HandlerRequest saml2HandlerRequest,
                                 SAML2HandlerResponse saml2HandlerResponse, HTTPContext httpContext, Lock chainLock) throws ProcessingException,
            IOException {
        try {
            if (this.configuration.getHandlers().isLocking()) {
                chainLock.lock();
            }

            // Deal with handler chains
            for (SAML2Handler handler : handlers) {
                if (saml2HandlerResponse.isInError()) {
                    httpContext.getResponse().sendError(saml2HandlerResponse.getErrorCode());
                    break;
                }
                if (samlObject instanceof RequestAbstractType) {
                    handler.handleRequestType(saml2HandlerRequest, saml2HandlerResponse);
                } else {
                    handler.handleStatusResponseType(saml2HandlerRequest, saml2HandlerResponse);
                }
            }
        } finally {
            if (this.configuration.getHandlers().isLocking()) {
                chainLock.unlock();
            }
        }
    }
}