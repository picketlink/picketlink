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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.TrustKeyConfigurationException;
import org.picketlink.common.exceptions.TrustKeyProcessingException;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler.HANDLER_TYPE;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest.GENERATE_REQUEST_TYPE;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.core.HTTPContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import static org.picketlink.common.util.StringUtil.isNotNull;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * A processor util at the SP
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */
public class ServiceProviderBaseProcessor {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected final PicketLinkType configuration;

    protected boolean postBinding;

    protected String serviceURL;

    protected String identityURL;

    protected TrustKeyManager keyManager;

    protected String issuer = null;

    protected PicketLinkAuditHelper auditHelper = null;

    public static final String IDP_KEY = "idp.key";

    /**
     * Construct
     *
     * @param postBinding Whether it is the Post Binding
     * @param serviceURL Service URL of the SP
     */
    public ServiceProviderBaseProcessor(boolean postBinding, String serviceURL, PicketLinkType configuration) {
        this.postBinding = postBinding;
        this.serviceURL = serviceURL;
        this.configuration = configuration;
    }

    /**
     * Set the {@code TrustKeyManager}
     *
     * @param tkm
     */
    public void setTrustKeyManager(TrustKeyManager tkm) {
        this.keyManager = tkm;
    }

    /**
     * Set the Identity URL
     *
     * @param identityURL
     */
    public void setIdentityURL(String identityURL) {
        this.identityURL = identityURL;
    }

    /**
     * Set a separate issuer that is different from the service url
     *
     * @param issuer
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Set the {@link PicketLinkAuditHelper}
     *
     * @param helper
     */
    public void setAuditHelper(PicketLinkAuditHelper helper) {
        this.auditHelper = helper;
    }

    public SAML2HandlerResponse process(HTTPContext httpContext, Set<SAML2Handler> handlers, Lock chainLock)
            throws ProcessingException, IOException, ParsingException, ConfigurationException {

        logger.trace("SAML Handlers are: " + handlers);

        // Neither saml request nor response from IDP
        // So this is a user request

        // Ask the handler chain to generate the saml request

        // Create the request/response
        SAML2HandlerRequest saml2HandlerRequest = getSAML2HandlerRequest(null, httpContext);
        saml2HandlerRequest.addOption(GeneralConstants.CONTEXT_PATH, httpContext.getServletContext().getContextPath());
        saml2HandlerRequest.addOption(GeneralConstants.SUPPORTS_SIGNATURES, getSpConfiguration().isSupportsSignature());

        SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

        saml2HandlerResponse.setPostBindingForResponse(postBinding);
        saml2HandlerResponse.setDestination(identityURL);

        // if the request is a GLO. Check if there is a specific URL for logout.
        if (isLogOutRequest(httpContext)) {
            String logoutUrl = ((SPType) getSpConfiguration()).getLogoutUrl();

            if (logoutUrl != null) {
                saml2HandlerResponse.setDestination(logoutUrl);
            }
        }

        // Reset the state
        try {
            if (this.configuration.getHandlers().isLocking()) {
                chainLock.lock();
            }

            for (SAML2Handler handler : handlers) {
                handler.reset();

                if (saml2HandlerResponse.isInError()) {
                    httpContext.getResponse().sendError(saml2HandlerResponse.getErrorCode());
                    break;
                }

                if (isLogOutRequest(httpContext))
                    saml2HandlerRequest.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.LOGOUT);
                else
                    saml2HandlerRequest.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

                handler.generateSAMLRequest(saml2HandlerRequest, saml2HandlerResponse);

                logger.trace("Finished Processing handler: " + handler.getClass().getCanonicalName());
            }
        } catch (ProcessingException pe) {
            logger.error(pe);
            throw logger.samlHandlerChainProcessingError(pe);
        } finally {
            if (this.configuration.getHandlers().isLocking()) {
                chainLock.unlock();
            }
        }

        return saml2HandlerResponse;
    }

    protected ProviderType getSpConfiguration() {
        return this.configuration.getIdpOrSP();
    }

    protected SAML2HandlerRequest getSAML2HandlerRequest(SAMLDocumentHolder documentHolder, HTTPContext httpContext) {
        IssuerInfoHolder holder = null;

        if (issuer == null) {
            holder = new IssuerInfoHolder(this.serviceURL);
        } else {
            holder = new IssuerInfoHolder(issuer);
        }

        return new DefaultSAML2HandlerRequest(httpContext, holder.getIssuer(), documentHolder, HANDLER_TYPE.SP);
    }

    protected boolean isLogOutRequest(HTTPContext httpContext) {
        HttpServletRequest request = httpContext.getRequest();
        String gloStr = request.getParameter(GeneralConstants.GLOBAL_LOGOUT);
        return isNotNull(gloStr) && "true".equalsIgnoreCase(gloStr);
    }

    protected URL safeURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * <p> Returns the PublicKey to be used to verify signatures for SAML tokens issued by the IDP. </p>
     *
     * @return
     *
     * @throws org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException
     * @throws org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException
     */
    protected PublicKey getIDPPublicKey() throws TrustKeyConfigurationException, TrustKeyProcessingException {
        if (this.keyManager == null) {
            throw logger.trustKeyManagerMissing();
        }
        String idpValidatingAlias = (String) this.keyManager.getAdditionalOption(ServiceProviderBaseProcessor.IDP_KEY);

        if (isNullOrEmpty(idpValidatingAlias)) {
            idpValidatingAlias = safeURL(getSpConfiguration().getIdentityURL()).getHost();
        }

        return keyManager.getValidatingKey(idpValidatingAlias);
    }

    protected void setRequestOptions(SAML2HandlerRequest saml2HandlerRequest) throws TrustKeyConfigurationException, TrustKeyProcessingException {
        Map<String, Object> requestOptions = new HashMap<String, Object>();

        requestOptions.put(GeneralConstants.CONFIGURATION, getSpConfiguration());

        if (auditHelper != null) {
            requestOptions.put(GeneralConstants.AUDIT_HELPER, auditHelper);
        }

        if (keyManager != null) {
            PublicKey validatingKey = getIDPPublicKey();

            requestOptions.put(GeneralConstants.SENDER_PUBLIC_KEY, validatingKey);
            requestOptions.put(GeneralConstants.DECRYPTING_KEY, keyManager.getSigningKey());
        }

        requestOptions.put(GeneralConstants.SUPPORTS_SIGNATURES, getSpConfiguration().isSupportsSignature());

        saml2HandlerRequest.setOptions(requestOptions);
    }

}