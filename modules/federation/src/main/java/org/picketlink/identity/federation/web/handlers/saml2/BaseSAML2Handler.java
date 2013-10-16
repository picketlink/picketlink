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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.core.HTTPContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Base Class for SAML2 handlers
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
public abstract class BaseSAML2Handler implements SAML2Handler {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected SAML2HandlerConfig handlerConfig = null;
    protected SAML2HandlerChainConfig handlerChainConfig = null;

    private ProviderType providerConfig;

    /**
     * Initialize the handler
     *
     * @param options
     */
    public void initHandlerConfig(SAML2HandlerConfig handlerConfig) throws ConfigurationException {
        this.handlerConfig = handlerConfig;
    }

    public void initChainConfig(SAML2HandlerChainConfig handlerChainConfig) throws ConfigurationException {
        this.handlerChainConfig = handlerChainConfig;
        this.providerConfig = (ProviderType) this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION);

        if (!isSupportedProviderType(this.providerConfig)) {
            throw logger.unsupportedType(this.providerConfig.getClass().getName());
        }

    }

    /**
     * Get the type of handler - handler at IDP or SP
     *
     * @return
     */
    public HANDLER_TYPE getType() {
        if (this.providerConfig instanceof IDPType) {
            return HANDLER_TYPE.IDP;
        } else {
            return HANDLER_TYPE.SP;
        }
    }

    public void reset() throws ProcessingException {
    }

    /**
     * @see SAML2Handler#generateSAMLRequest(SAML2HandlerRequest, SAML2HandlerResponse)
     */
    public void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
    }

    /**
     * @see {@code SAML2Handler#handleStatusResponseType(SAML2HandlerRequest, SAML2HandlerResponse)}
     */
    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
    }

    public static HttpServletRequest getHttpRequest(SAML2HandlerRequest request) {
        HTTPContext context = (HTTPContext) request.getContext();
        return context.getRequest();
    }

    public static HttpSession getHttpSession(SAML2HandlerRequest request) {
        HTTPContext context = (HTTPContext) request.getContext();
        return context.getRequest().getSession(false);
    }

    protected ProviderType getProviderconfig() {
        return this.providerConfig;
    }

    /**
     * <p>Checks if the given {@link ProviderType} is supported by the handler.</p>
     *
     * @return
     */
    private boolean isSupportedProviderType(ProviderType providerType) {
        if (providerType == null) {
            throw logger.nullArgumentError("ProviderType configuration.");
        }

        return (providerType instanceof IDPType) || (providerType instanceof SPType);
    }

}