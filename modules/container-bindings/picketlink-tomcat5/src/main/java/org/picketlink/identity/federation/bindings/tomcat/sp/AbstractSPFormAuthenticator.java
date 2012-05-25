/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.picketlink.identity.federation.bindings.tomcat.sp;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.log4j.Logger;
import org.jboss.security.audit.AuditLevel;
import org.picketlink.identity.federation.bindings.tomcat.sp.holder.ServiceProviderSAMLContext;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v2.exceptions.AssertionExpiredException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.process.ServiceProviderBaseProcessor;
import org.picketlink.identity.federation.web.process.ServiceProviderSAMLRequestProcessor;
import org.picketlink.identity.federation.web.process.ServiceProviderSAMLResponseProcessor;
import org.picketlink.identity.federation.web.util.ServerDetector;
import org.w3c.dom.Document;

/**
 * <p>
 * Abstract class to be extended by Service Provider valves to handle SAML requests and responses.
 * </p>
 *
 * @author <a href="mailto:asaldhan@redhat.com">Anil Saldhana</a>
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractSPFormAuthenticator extends BaseFormAuthenticator {

    protected Logger log = Logger.getLogger(getClass());

    protected final boolean trace = log.isTraceEnabled();

    protected boolean jbossEnv = false;

    public AbstractSPFormAuthenticator() {
        super();
        ServerDetector detector = new ServerDetector();
        jbossEnv = detector.isJboss();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.BaseFormAuthenticator#processStart()
     */
    @Override
    protected void processStart() throws LifecycleException {
        super.processStart();
        initKeyProvider(context);
    }

    /**
     * <p>
     * Initialize the KeyProvider configurations. This configurations are to be used during signing and validation of SAML
     * assertions.
     * </p>
     *
     * @param context
     * @throws LifecycleException
     */
    protected void initKeyProvider(Context context) throws LifecycleException {
        if (!doSupportSignature()) {
            return;
        }

        KeyProviderType keyProvider = this.spConfiguration.getKeyProvider();

        if (keyProvider == null && doSupportSignature())
            throw new LifecycleException(ErrorCodes.NULL_VALUE + "KeyProvider is null for context=" + context.getName());

        try {
            String keyManagerClassName = keyProvider.getClassName();
            if (keyManagerClassName == null)
                throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyManager class name");

            Class<?> clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);

            if (clazz == null)
                throw new ClassNotFoundException(ErrorCodes.CLASS_NOT_LOADED + keyManagerClassName);
            this.keyManager = (TrustKeyManager) clazz.newInstance();

            List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);

            keyManager.setAuthProperties(authProperties);
            keyManager.setValidatingAlias(keyProvider.getValidatingAlias());

            String identityURL = this.spConfiguration.getIdentityURL();

            keyManager.addAdditionalOption(ServiceProviderBaseProcessor.IDP_KEY, new URL(identityURL).getHost());
        } catch (Exception e) {
            log.error("Exception reading configuration:", e);
            throw new LifecycleException(e.getLocalizedMessage());
        }

        if (trace)
            log.trace("Key Provider=" + keyProvider.getClassName());
    }

    /**
     * Authenticate the request
     *
     * @param request
     * @param response
     * @param config
     * @return
     * @throws IOException
     * @throws {@link RuntimeException} when the response is not of type catalina response object
     */
    public boolean authenticate(Request request, HttpServletResponse response, LoginConfig config) throws IOException {
        if (response instanceof Response) {
            Response catalinaResponse = (Response) response;
            return authenticate(request, catalinaResponse, config);
        }
        throw new RuntimeException(ErrorCodes.SERVICE_PROVIDER_NOT_CATALINA_RESPONSE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.catalina.authenticator.FormAuthenticator#authenticate(org.apache.catalina.connector.Request,
     * org.apache.catalina.connector.Response, org.apache.catalina.deploy.LoginConfig)
     */
    @Override
    public boolean authenticate(Request request, Response response, LoginConfig loginConfig) throws IOException {
        try {
            Session session = request.getSessionInternal(true);

            // Eagerly look for Local LogOut
            boolean localLogout = isLocalLogout(request);

            if (localLogout) {
                try {
                    sendToLogoutPage(request, response, session);
                } catch (ServletException e) {
                    log.error("Exception in logout::", e);
                    throw new IOException(e);
                }
                return false;
            }

            String samlRequest = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
            String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

            Principal principal = request.getUserPrincipal();

            // If we have already authenticated the user and there is no request from IDP or logout from user
            if (principal != null && !(isGlobalLogout(request) || isNotNull(samlRequest) || isNotNull(samlResponse)))
                return true;

            // General User Request
            if (!isNotNull(samlRequest) && !isNotNull(samlResponse)) {
                return generalUserRequest(request, response, loginConfig);
            }

            // Handle a SAML Response from IDP
            if (isNotNull(samlResponse)) {
                return handleSAMLResponse(request, response, loginConfig);
            }

            // Handle SAML Requests from IDP
            if (isNotNull(samlRequest)) {
                return handleSAMLRequest(request, response, loginConfig);
            }// end if

            return localAuthentication(request, response, loginConfig);
        } catch (IOException e) {
            if (StringUtil.isNotNull(spConfiguration.getErrorPage())) {
                try {
                    request.getRequestDispatcher(spConfiguration.getErrorPage()).forward(request.getRequest(), response);
                } catch (ServletException e1) {
                    log.error(ErrorCodes.FILE_NOT_LOCATED, e1);
                }
                return false;
            } else {
                throw e;
            }
        }
    }

    /**
     * <p>
     * Indicates if the current request is a GlobalLogout request.
     * </p>
     *
     * @param request
     * @return
     */
    private boolean isGlobalLogout(Request request) {
        String gloStr = request.getParameter(GeneralConstants.GLOBAL_LOGOUT);
        return isNotNull(gloStr) && "true".equalsIgnoreCase(gloStr);
    }

    /**
     * <p>
     * Indicates if the current request is a LocalLogout request.
     * </p>
     *
     * @param request
     * @return
     */
    private boolean isLocalLogout(Request request) {
        String lloStr = request.getParameter(GeneralConstants.LOCAL_LOGOUT);
        return isNotNull(lloStr) && "true".equalsIgnoreCase(lloStr);
    }

    /**
     * Handle the IDP Request
     *
     * @param request
     * @param response
     * @param loginConfig
     * @return
     * @throws IOException
     */
    private boolean handleSAMLRequest(Request request, Response response, LoginConfig loginConfig) throws IOException {
        String samlRequest = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
        HTTPContext httpContext = new HTTPContext(request, response, context.getServletContext());
        Set<SAML2Handler> handlers = chain.handlers();

        try {
            ServiceProviderSAMLRequestProcessor requestProcessor = new ServiceProviderSAMLRequestProcessor(
                    isPOSTBindingResponse(), this.serviceURL);
            requestProcessor.setTrustKeyManager(keyManager);
            requestProcessor.setConfiguration(spConfiguration);
            boolean result = requestProcessor.process(samlRequest, httpContext, handlers, chainLock);

            if (enableAudit) {
                PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                auditEvent.setType(PicketLinkAuditEventType.REQUEST_FROM_IDP);
                auditEvent.setWhoIsAuditing(context.getServletContext().getContextPath());
                auditHelper.audit(auditEvent);
            }

            // If response is already commited, we need to stop with processing of HTTP request
            if (response.isCommitted() || response.isAppCommitted())
                return false;

            if (result)
                return result;
        } catch (Exception e) {
            log.error("Server Exception:", e);
            throw new IOException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION);
        }

        return localAuthentication(request, response, loginConfig);
    }

    /**
     * Handle IDP Response
     *
     * @param request
     * @param response
     * @param loginConfig
     * @return
     * @throws IOException
     */
    private boolean handleSAMLResponse(Request request, Response response, LoginConfig loginConfig) throws IOException {
        SPUtil spUtil = new SPUtil();
        Session session = request.getSessionInternal(true);
        String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

        boolean willSendRequest = false;
        HTTPContext httpContext = new HTTPContext(request, response, context.getServletContext());
        Set<SAML2Handler> handlers = chain.handlers();

        Principal principal = request.getUserPrincipal();

        if (!super.validate(request)) {
            throw new IOException(ErrorCodes.VALIDATION_CHECK_FAILED);
        }

        // deal with SAML response from IDP
        try {
            ServiceProviderSAMLResponseProcessor responseProcessor = new ServiceProviderSAMLResponseProcessor(
                    isPOSTBindingResponse(), serviceURL);
            responseProcessor.setConfiguration(spConfiguration);
            if(auditHelper !=  null){
                responseProcessor.setAuditHelper(auditHelper);   
            }

            responseProcessor.setTrustKeyManager(keyManager);

            SAML2HandlerResponse saml2HandlerResponse = responseProcessor.process(samlResponse, httpContext, handlers,
                    chainLock);

            Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
            String relayState = saml2HandlerResponse.getRelayState();

            String destination = saml2HandlerResponse.getDestination();

            willSendRequest = saml2HandlerResponse.getSendRequest();

            String destinationQueryStringWithSignature = saml2HandlerResponse.getDestinationQueryStringWithSignature();

            if (destination != null && samlResponseDocument != null) {
                sendRequestToIDP(destination, samlResponseDocument, relayState, response, willSendRequest, destinationQueryStringWithSignature);
            } else {
                // See if the session has been invalidated

                boolean sessionValidity = session.isValid();

                if (!sessionValidity) {
                    sendToLogoutPage(request, response, session);
                    return false;
                }

                // We got a response with the principal
                List<String> roles = saml2HandlerResponse.getRoles();
                if (principal == null)
                    principal = (Principal) session.getSession().getAttribute(GeneralConstants.PRINCIPAL_ID);

                String username = principal.getName();
                String password = ServiceProviderSAMLContext.EMPTY_PASSWORD;

                if (trace)
                    log.trace("Roles determined for username=" + username + "=" + Arrays.toString(roles.toArray()));

                // Map to JBoss specific principal
                if ((new ServerDetector()).isJboss() || jbossEnv) {
                    // Push a context
                    ServiceProviderSAMLContext.push(username, roles);
                    principal = context.getRealm().authenticate(username, password);
                    ServiceProviderSAMLContext.clear();
                } else {
                    // tomcat env
                    principal = spUtil.createGenericPrincipal(request, username, roles);
                }

                session.setNote(Constants.SESS_USERNAME_NOTE, username);
                session.setNote(Constants.SESS_PASSWORD_NOTE, password);
                request.setUserPrincipal(principal);
                // Get the original saved request
                if (saveRestoreRequest) {
                    this.restoreRequest(request, session);
                }

                if (enableAudit) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.RESPONSE_FROM_IDP);
                    auditEvent.setSubjectName(username);
                    auditEvent.setWhoIsAuditing(context.getServletContext().getContextPath());
                    auditHelper.audit(auditEvent);
                }
                register(request, response, principal, Constants.FORM_METHOD, username, password);

                return true;
            }
        } catch (ProcessingException pe) {
            Throwable t = pe.getCause();
            if (t != null && t instanceof AssertionExpiredException) {
                log.error("Assertion has expired. Asking IDP for reissue");
                if (enableAudit) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.EXPIRED_ASSERTION);
                    auditEvent.setAssertionID(((AssertionExpiredException) t).getId());
                    auditHelper.audit(auditEvent);
                }
                // Just issue a fresh request back to IDP
                return generalUserRequest(request, response, loginConfig);
            }
            log.error("Server Exception:", pe);
            throw new IOException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION + pe.getLocalizedMessage());
        } catch (Exception e) {
            log.error("Server Exception:", e);
            throw new IOException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION);
        }

        return localAuthentication(request, response, loginConfig);
    }

    protected boolean isPOSTBindingResponse() {
        return spConfiguration.isIdpUsesPostBinding();
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
     * @throws ProcessingException
     * @throws ConfigurationException
     * @throws IOException
     */
    protected abstract void sendRequestToIDP(String destination, Document samlDocument, String relayState, Response response,
            boolean willSendRequest, String destinationQueryStringWithSignature) throws ProcessingException, ConfigurationException, IOException;

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.BaseFormAuthenticator#getBinding()
     */
    @Override
    protected String getBinding() {
        return spConfiguration.getBindingType();
    }

    /**
     * Handle the user invocation for the first time
     *
     * @param request
     * @param response
     * @param loginConfig
     * @return
     * @throws IOException
     */
    private boolean generalUserRequest(Request request, Response response, LoginConfig loginConfig) throws IOException {
        Session session = request.getSessionInternal(true);
        boolean willSendRequest = false;
        HTTPContext httpContext = new HTTPContext(request, response, context.getServletContext());
        Set<SAML2Handler> handlers = chain.handlers();

        boolean postBinding = spConfiguration.getBindingType().equals("POST");

        // Neither saml request nor response from IDP
        // So this is a user request
        SAML2HandlerResponse saml2HandlerResponse = null;
        try {
            ServiceProviderBaseProcessor baseProcessor = new ServiceProviderBaseProcessor(postBinding, serviceURL);
            if (issuerID != null)
                baseProcessor.setIssuer(issuerID);

            baseProcessor.setIdentityURL(identityURL);
            baseProcessor.setAuditHelper(auditHelper);
            baseProcessor.setConfiguration(this.spConfiguration);
            
            saml2HandlerResponse = baseProcessor.process(httpContext, handlers, chainLock);
        } catch (ProcessingException pe) {
            log.error("Processing Exception:", pe);
            throw new RuntimeException(pe);
        } catch (ParsingException pe) {
            log.error("Parsing Exception:", pe);
            throw new RuntimeException(pe);
        } catch (ConfigurationException pe) {
            log.error("Config Exception:", pe);
            throw new RuntimeException(pe);
        }

        willSendRequest = saml2HandlerResponse.getSendRequest();

        Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
        String relayState = saml2HandlerResponse.getRelayState();

        String destination = saml2HandlerResponse.getDestination();
        String destinationQueryStringWithSignature = saml2HandlerResponse.getDestinationQueryStringWithSignature();

        if (destination != null && samlResponseDocument != null) {
            try {
                if (saveRestoreRequest) {
                    this.saveRequest(request, session);
                }
                if (enableAudit) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.REQUEST_TO_IDP);
                    auditEvent.setWhoIsAuditing(context.getServletContext().getContextPath());
                    auditHelper.audit(auditEvent);
                }
                sendRequestToIDP(destination, samlResponseDocument, relayState, response, willSendRequest, destinationQueryStringWithSignature);
                return false;
            } catch (Exception e) {
                log.error("Server Exception:", e);
                throw new IOException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION);
            }
        }

        return localAuthentication(request, response, loginConfig);
    }

    /**
     * <p>
     * Indicates if the SP is configure with HTTP POST Binding.
     * </p>
     *
     * @return
     */
    protected boolean isHttpPostBinding() {
        return getBinding().equalsIgnoreCase("POST");
    }
}