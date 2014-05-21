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
package org.picketlink.identity.federation.web.filters;

import static org.picketlink.common.constants.GeneralConstants.AUDIT_HELPER;
import static org.picketlink.common.constants.GeneralConstants.CONFIG_FILE_LOCATION;
import static org.picketlink.common.constants.GeneralConstants.CONFIG_PROVIDER;
import static org.picketlink.common.constants.GeneralConstants.DEPRECATED_CONFIG_FILE_LOCATION;
import static org.picketlink.common.util.StringUtil.isNotNull;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.security.audit.AuditLevel;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.fed.IssuerNotTrustedException;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.common.util.StringUtil;
import org.picketlink.common.util.SystemPropertiesUtil;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.handler.Handlers;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.impl.DelegatedAttributeManager;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v1.SAML11ProtocolContext;
import org.picketlink.identity.federation.core.saml.v1.writers.SAML11ResponseWriter;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.factories.SAML2HandlerChainFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.HandlerUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTSConfiguration;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11NameIdentifierType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11ResponseType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusType;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.web.config.AbstractSAMLConfigurationProvider;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.core.IdentityParticipantStack;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;
import org.w3c.dom.Document;

/**
 * A {@link javax.servlet.Filter} that can be configured to convert a
 * JavaEE Web Application to an IDP
 *
 * @author Anil Saldhana
 * @since December 05, 2013
 */
public class IDPFilter implements Filter {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected ServletContext servletContext;

    protected boolean enableAudit = false;

    protected PicketLinkAuditHelper auditHelper = null;

    protected IDPType idpConfiguration = null;

    protected PicketLinkType picketLinkConfiguration = null;

    private RoleGenerator roleGenerator = null;

    private TrustKeyManager keyManager;

    private transient DelegatedAttributeManager attribManager = new DelegatedAttributeManager();

    private final List<String> attributeKeys = new ArrayList<String>();

    private transient SAML2HandlerChain chain = null;

    /**
     * The user can inject a fully qualified name of a {@link org.picketlink.identity.federation.web.util.SAMLConfigurationProvider}
     */
    protected SAMLConfigurationProvider configProvider = null;

    protected int timerInterval = -1;

    protected Timer timer = null;

    protected String authMethod = "PASSWORD";

    /**
     * <p>Specifies a different location for the configuration file.</p>
     */
    private String configFile;

    /**
     * A Lock for Handler operations in the chain
     */
    private final Lock chainLock = new ReentrantLock();

    private Map<String, SPSSODescriptorType> spSSOMetadataMap = new HashMap<String, SPSSODescriptorType>();
    private Handlers handlers;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
        configureConfigurationProvider();
        configureAuditHelper();

        startPicketLink();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // Look for unauthorized status
        if (isUnauthorized(httpServletResponse)) {
            handleUnauthorizedResponse(httpServletRequest, httpServletResponse);
            return;
        }

        // first, we populate all required parameters sent into session for later retrieval. If they exists.
        //populateSessionWithSAMLParameters(httpServletRequest);

        // get an authenticated user or tries to authenticate if this is a authentication request
        Principal userPrincipal = getUserPrincipal(httpServletRequest, httpServletResponse);

        // we only handle SAML messages for authenticated users.
        if (userPrincipal != null) {
            handleSAMLMessage(httpServletRequest, httpServletResponse, chain);
        }else {
            chain.doFilter(request,response);
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * <p>
     * Handles SAML messages.
     * </p>
     *
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    private void handleSAMLMessage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (hasSAML11Target(request)) {
            // We have SAML 1.1 IDP first scenario. Now we need to create a SAMLResponse and send back
            // to SP as per target
            handleSAML11(request, response);
        } else {

            HttpSession session = request.getSession();

            String samlRequestMessage = (String) request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
            String samlResponseMessage = (String) request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

            /**
             * Since the container has finished the authentication, we can retrieve the original saml message as well as any
             * relay state from the SP
             */
            String relayState = (String) request.getParameter(GeneralConstants.RELAY_STATE);
            String signature = (String) request.getParameter(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
            String sigAlg = (String) request.getParameter(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);

            if (logger.isTraceEnabled()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Retrieved saml messages and relay state from session");
                builder.append("saml Request message=").append(samlRequestMessage);
                builder.append("::").append("SAMLResponseMessage=");
                builder.append(samlResponseMessage).append(":").append("relay state=").append(relayState);

                builder.append("Signature=").append(signature).append("::sigAlg=").append(sigAlg);
                logger.trace(builder.toString());
            }

            if (isNotNull(samlRequestMessage)) {
                processSAMLRequestMessage(request, response);
            } else if (isNotNull(samlResponseMessage)) {
                processSAMLResponseMessage(request, response);
            } else if (request.getRequestURI().equals(request.getContextPath() + "/")) {
                // no SAML processing and the request is asking for /.
                forwardHosted(request, response);
            } else {
                chain.doFilter(request, response);
            }

            /*HttpSession session = request.getSession();

            String samlRequestMessage = (String) session.getAttribute(GeneralConstants.SAML_REQUEST_KEY);
            String samlResponseMessage = (String) session.getAttribute(GeneralConstants.SAML_RESPONSE_KEY);

            *//**
             * Since the container has finished the authentication, we can retrieve the original saml message as well as any
             * relay state from the SP
             *//*
            String relayState = (String) session.getAttribute(GeneralConstants.RELAY_STATE);
            String signature = (String) session.getAttribute(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
            String sigAlg = (String) session.getAttribute(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);

            if (logger.isTraceEnabled()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Retrieved saml messages and relay state from session");
                builder.append("saml Request message=").append(samlRequestMessage);
                builder.append("::").append("SAMLResponseMessage=");
                builder.append(samlResponseMessage).append(":").append("relay state=").append(relayState);

                builder.append("Signature=").append(signature).append("::sigAlg=").append(sigAlg);
                logger.trace(builder.toString());
            }

            if (isNotNull(samlRequestMessage)) {
                processSAMLRequestMessage(request, response);
            } else if (isNotNull(samlResponseMessage)) {
                processSAMLResponseMessage(request, response);
            } else if (request.getRequestURI().equals(request.getContextPath() + "/")) {
                // no SAML processing and the request is asking for /.
                forwardHosted(request, response);
            }*/
        }
    }

    /**
     * <p>
     * Checks if the given {@link javax.servlet.http.HttpServletRequest} containes a SAML11 Target parameter. Usually this indicates that the given request is
     * a SAML11 request.
     * </p>
     *
     * @param request
     * @return
     */
    private boolean hasSAML11Target(HttpServletRequest request) {
        return isNotNull(request.getParameter(JBossSAMLConstants.UNSOLICITED_RESPONSE_TARGET.get()));
    }

    private void forwardHosted(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.trace("SAML 1.1::Proceeding to IDP index page");
        RequestDispatcher dispatch = servletContext
                .getRequestDispatcher(this.idpConfiguration.getHostedURI());

        recycle(response);

        try {
            includeResource(request, response, dispatch);
        } catch (ClassCastException cce) {
            throw new IOException(cce);
            // JBAS5.1 and 6 quirkiness
            //includeResource(request.getRequest(), response, dispatch);
        }
    }

    /**
     * <p>
     * Before forwarding we need to know the content length of the target resource in order to configure the response properly.
     * This is necessary because the valve already have written to the response, and we want to override with the target
     * resource data.
     * </p>
     *
     * @param request
     * @param response
     * @param dispatch
     * @throws ServletException
     * @throws IOException
     */
    private void includeResource(ServletRequest request, HttpServletResponse response, RequestDispatcher dispatch)
            throws ServletException, IOException {
        dispatch.include(request, response);

        // we need to re-configure the content length because Tomcat will truncate the output with the size of the welcome page
        // (eg.: index.html).
        //response.setContentLength(response.g.getContentCount());
    }

    /**
     * <p>
     * SAML parameters are also populated into session if they are present in the request. This allows the IDP to retrieve them
     * later when handling a specific SAML request or response.
     * </p>
     *
     * @param request
     * @return
     * @throws IOException
     */
    private void populateSessionWithSAMLParameters(HttpServletRequest request) throws IOException {
        String samlRequestMessage = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
        String samlResponseMessage = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

        boolean containsSAMLRequestMessage = isNotNull(samlRequestMessage);
        boolean containsSAMLResponseMessage = isNotNull(samlResponseMessage);

        String signature = request.getParameter(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        String sigAlg = request.getParameter(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);
        String relayState = request.getParameter(GeneralConstants.RELAY_STATE);

        HttpSession session = request.getSession();

        if (containsSAMLRequestMessage || containsSAMLResponseMessage) {
            logger.trace("Storing the SAMLRequest/SAMLResponse and RelayState in session");
            if (isNotNull(samlRequestMessage))
                session.setAttribute(GeneralConstants.SAML_REQUEST_KEY, samlRequestMessage);
            if (isNotNull(samlResponseMessage))
                session.setAttribute(GeneralConstants.SAML_RESPONSE_KEY, samlResponseMessage);
            if (isNotNull(relayState))
                session.setAttribute(GeneralConstants.RELAY_STATE, relayState.trim());
            if (isNotNull(signature))
                session.setAttribute(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY, signature.trim());
            if (isNotNull(sigAlg))
                session.setAttribute(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY, sigAlg.trim());
        }
    }

    /**
     * <p>
     * Handles an unauthorized response returned by a service provider.
     * </p>
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    private void handleUnauthorizedResponse(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        IDPWebRequestUtil webRequestUtil = new IDPWebRequestUtil(request, idpConfiguration, keyManager);
        Document samlErrorResponse = null;
        String referer = request.getHeader("Referer");
        String relayState = request.getParameter(GeneralConstants.RELAY_STATE);

        try {
            samlErrorResponse = webRequestUtil.getErrorResponse(referer, JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(),
                    getIdentityURL(), this.idpConfiguration.isSupportsSignature());

            IDPWebRequestUtil.WebRequestUtilHolder holder = webRequestUtil.getHolder();
            holder.setResponseDoc(samlErrorResponse).setDestination(referer).setRelayState(relayState)
                    .setAreWeSendingRequest(false).setPrivateKey(null).setSupportSignature(false).setServletResponse(response)
                    .setErrorResponse(true);
            holder.setPostBindingRequested(webRequestUtil.hasSAMLRequestInPostProfile());

            if (this.idpConfiguration.isSupportsSignature()) {
                holder.setSupportSignature(true).setPrivateKey(keyManager.getSigningKey());
            }

            holder.setStrictPostBinding(this.idpConfiguration.isStrictPostBinding());

            webRequestUtil.send(holder);
        } catch (GeneralSecurityException e) {
            throw new ServletException(e);
        }
    }

    private boolean isUnauthorized(HttpServletResponse response) {
        return response.getStatus() == HttpServletResponse.SC_FORBIDDEN;
    }

    /**
     * <p>
     * Returns the authenticated principal. If there is no principal associated with the {@link javax.servlet.http.HttpServletRequest}, null is returned.
     * </p>
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    private Principal getUserPrincipal(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Principal userPrincipal = request.getUserPrincipal();

        if (userPrincipal == null) {
            userPrincipal = request.getUserPrincipal();
        }

        return userPrincipal;
    }

    protected void handleSAML11(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            IDPWebRequestUtil webRequestUtil = new IDPWebRequestUtil(request, idpConfiguration, keyManager);

            Principal userPrincipal = request.getUserPrincipal();
            String contextPath = servletContext.getContextPath();

            String target = request.getParameter(JBossSAMLConstants.UNSOLICITED_RESPONSE_TARGET.get());

            HttpSession session = request.getSession();
            SAML11AssertionType saml11Assertion = (SAML11AssertionType) session.getAttribute("SAML11");
            if (saml11Assertion == null) {
                SAML11ProtocolContext saml11Protocol = new SAML11ProtocolContext();
                saml11Protocol.setIssuerID(getIdentityURL());
                SAML11SubjectType subject = new SAML11SubjectType();
                SAML11SubjectType.SAML11SubjectTypeChoice subjectChoice = new SAML11SubjectType.SAML11SubjectTypeChoice(new SAML11NameIdentifierType(
                        userPrincipal.getName()));
                subject.setChoice(subjectChoice);
                saml11Protocol.setSubjectType(subject);

                PicketLinkCoreSTS.instance().issueToken(saml11Protocol);
                saml11Assertion = saml11Protocol.getIssuedAssertion();
                session.setAttribute("SAML11", saml11Assertion);

                if (AssertionUtil.hasExpired(saml11Assertion)) {
                    saml11Protocol.setIssuedAssertion(saml11Assertion);
                    PicketLinkCoreSTS.instance().renewToken(saml11Protocol);
                    saml11Assertion = saml11Protocol.getIssuedAssertion();
                    session.setAttribute("SAML11", saml11Assertion);
                }
            }

            List<String> roles = this.roleGenerator.generateRoles(userPrincipal);
            SAML11AttributeStatementType attributeStatement = this.createAttributeStatement(roles);

            if (attributeStatement != null) {
                saml11Assertion.add(attributeStatement);
            }

            // Send it as SAMLResponse
            String id = IDGenerator.create("ID_");
            SAML11ResponseType saml11Response = new SAML11ResponseType(id, XMLTimeUtil.getIssueInstant());
            saml11Response.add(saml11Assertion);
            saml11Response.setStatus(SAML11StatusType.successType());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SAML11ResponseWriter writer = new SAML11ResponseWriter(StaxUtil.getXMLStreamWriter(baos));

            writer.write(saml11Response);

            Document samlResponse = org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
            IDPWebRequestUtil.WebRequestUtilHolder holder = webRequestUtil.getHolder();

            holder.setResponseDoc(samlResponse).setDestination(target).setRelayState("").setAreWeSendingRequest(false)
                    .setPrivateKey(null).setSupportSignature(false).setServletResponse(response);

            if (enableAudit) {
                PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                auditEvent.setType(PicketLinkAuditEventType.RESPONSE_TO_SP);
                auditEvent.setDestination(target);
                auditEvent.setWhoIsAuditing(contextPath);
                auditHelper.audit(auditEvent);
            }

            recycle(response);

            webRequestUtil.send(holder);
        } catch (GeneralSecurityException e) {
            logger.samlIDPHandlingSAML11Error(e);
            throw new ServletException();
        }
    }

    protected void processSAMLRequestMessage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Principal userPrincipal = request.getUserPrincipal();
        HttpSession session = request.getSession();
        SAMLDocumentHolder samlDocumentHolder = null;
        SAML2Object samlObject = null;

        Document samlResponse = null;
        boolean isErrorResponse = false;
        String destination = null;
        String destinationQueryStringWithSignature = null;

        Boolean requestedPostProfile = null;

        String samlRequestMessage = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);

        String relayState = request.getParameter(GeneralConstants.RELAY_STATE);

        String contextPath = servletContext.getContextPath();

        boolean willSendRequest = false;

        String referer = request.getHeader("Referer");

        //cleanUpSessionNote(request);

        // Determine the transport mechanism
        boolean isSecure = request.isSecure();
        String loginType = determineLoginType(isSecure);

        IDPWebRequestUtil webRequestUtil = new IDPWebRequestUtil(request, idpConfiguration, keyManager);

        try {
            samlDocumentHolder = webRequestUtil.getSAMLDocumentHolder(samlRequestMessage);
            samlObject = samlDocumentHolder.getSamlObject();

            if (!(samlObject instanceof RequestAbstractType)) {
                throw logger.wrongTypeError(samlObject.getClass().getName());
            }

            // Get the SAML Request Message
            RequestAbstractType requestAbstractType = (RequestAbstractType) samlObject;
            String issuer = requestAbstractType.getIssuer().getValue();

            if (samlRequestMessage == null)
                throw logger.samlIDPValidationCheckFailed();

            IssuerInfoHolder idpIssuer = new IssuerInfoHolder(getIdentityURL());
            ProtocolContext protocolContext = new HTTPContext(request, response, servletContext);
            // Create the request/response
            SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext, idpIssuer.getIssuer(),
                    samlDocumentHolder, SAML2Handler.HANDLER_TYPE.IDP);
            saml2HandlerRequest.setRelayState(relayState);
            if (StringUtil.isNotNull(loginType)) {
                saml2HandlerRequest.addOption(GeneralConstants.LOGIN_TYPE, loginType);
            }

            String assertionID = (String) session.getAttribute(GeneralConstants.ASSERTION_ID);

            // Set the options on the handler request
            Map<String, Object> requestOptions = new HashMap<String, Object>();

            requestOptions.put(GeneralConstants.IGNORE_SIGNATURES, willIgnoreSignatureOfCurrentRequest(issuer));
            requestOptions.put(GeneralConstants.SP_SSO_METADATA_DESCRIPTOR, spSSOMetadataMap.get(issuer));
            requestOptions.put(GeneralConstants.ROLE_GENERATOR, roleGenerator);
            requestOptions.put(GeneralConstants.CONFIGURATION, this.idpConfiguration);
            requestOptions.put(GeneralConstants.SAML_IDP_STRICT_POST_BINDING, this.idpConfiguration.isStrictPostBinding());
            requestOptions.put(GeneralConstants.SUPPORTS_SIGNATURES, this.idpConfiguration.isSupportsSignature());

            if (assertionID != null)
                requestOptions.put(GeneralConstants.ASSERTION_ID, assertionID);

            if (this.keyManager != null) {
                PublicKey validatingKey = getIssuerPublicKey(request, issuer);
                requestOptions.put(GeneralConstants.SENDER_PUBLIC_KEY, validatingKey);
                requestOptions.put(GeneralConstants.DECRYPTING_KEY, keyManager.getSigningKey());
            }

            // if this is a SAML AuthnRequest load the roles using the generator.
            if (requestAbstractType instanceof AuthnRequestType) {
                List<String> roles = roleGenerator.generateRoles(userPrincipal);
                session.setAttribute(GeneralConstants.ROLES_ID, roles);

                Map<String, Object> attribs = this.attribManager.getAttributes(userPrincipal, attributeKeys);
                requestOptions.put(GeneralConstants.ATTRIBUTES, attribs);
            }

            if (auditHelper != null) {
                requestOptions.put(GeneralConstants.AUDIT_HELPER, auditHelper);
                requestOptions.put(GeneralConstants.CONTEXT_PATH, contextPath);
            }

            saml2HandlerRequest.setOptions(requestOptions);

            SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

            Set<SAML2Handler> handlers = chain.handlers();

            logger.trace("Handlers are=" + handlers);

            if (handlers != null) {
                try {
                    if (getConfiguration().getHandlers().isLocking()) {
                        chainLock.lock();
                    }
                    for (SAML2Handler handler : handlers) {
                        handler.handleRequestType(saml2HandlerRequest, saml2HandlerResponse);
                        willSendRequest = saml2HandlerResponse.getSendRequest();
                    }
                } finally {
                    if (getConfiguration().getHandlers().isLocking()) {
                        chainLock.unlock();
                    }
                }
            }

            samlResponse = saml2HandlerResponse.getResultingDocument();
            relayState = saml2HandlerResponse.getRelayState();

            destination = saml2HandlerResponse.getDestination();

            requestedPostProfile = saml2HandlerResponse.isPostBindingForResponse();
            destinationQueryStringWithSignature = saml2HandlerResponse.getDestinationQueryStringWithSignature();
        } catch (Exception e) {
            String status = JBossSAMLURIConstants.STATUS_AUTHNFAILED.get();
            if (e instanceof IssuerNotTrustedException || e.getCause() instanceof IssuerNotTrustedException) {
                status = JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get();
            }
            logger.samlIDPRequestProcessingError(e);
            samlResponse = webRequestUtil.getErrorResponse(referer, status, getIdentityURL(),
                    this.idpConfiguration.isSupportsSignature());
            isErrorResponse = true;
        } finally {
            try {
                // if the destination is null, probably because some error occur during authentication, use the AuthnRequest
                // AssertionConsumerServiceURL as the destination
                if (destination == null && samlObject instanceof AuthnRequestType) {
                    AuthnRequestType authRequest = (AuthnRequestType) samlObject;

                    destination = authRequest.getAssertionConsumerServiceURL().toASCIIString();
                }

                // if destination is still empty redirect the user to the identity url. If the user is already authenticated he
                // will be probably redirected to the idp hosted page.
                if (destination == null) {
                    response.sendRedirect(getIdentityURL());
                } else {
                    IDPWebRequestUtil.WebRequestUtilHolder holder = webRequestUtil.getHolder();
                    holder.setResponseDoc(samlResponse).setDestination(destination).setRelayState(relayState)
                            .setAreWeSendingRequest(willSendRequest).setPrivateKey(null).setSupportSignature(false)
                            .setErrorResponse(isErrorResponse).setServletResponse(response)
                            .setDestinationQueryStringWithSignature(destinationQueryStringWithSignature);

                    holder.setStrictPostBinding(this.idpConfiguration.isStrictPostBinding());

                    if (requestedPostProfile != null)
                        holder.setPostBindingRequested(requestedPostProfile);
                    else
                        holder.setPostBindingRequested(webRequestUtil.hasSAMLRequestInPostProfile());

                    if (this.idpConfiguration.isSupportsSignature()) {
                        holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
                    }

                    if (holder.isPostBinding())
                        recycle(response);

                    if (enableAudit) {
                        PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                        auditEvent.setType(PicketLinkAuditEventType.RESPONSE_TO_SP);
                        auditEvent.setDestination(destination);
                        auditEvent.setWhoIsAuditing(contextPath);
                        auditHelper.audit(auditEvent);
                    }

                    webRequestUtil.send(holder);
                }
            } catch (ParsingException e) {
                logger.samlAssertionPasingFailed(e);
            } catch (GeneralSecurityException e) {
                logger.trace("Security Exception:", e);
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return;
    }

    /**
     * Returns the PublicKey to be used for the token's signature verification. This key is related with the issuer of the SAML
     * message received by the IDP.
     *
     * @param request
     * @param issuer
     * @return
     * @throws org.picketlink.common.exceptions.ProcessingException
     * @throws org.picketlink.common.exceptions.ConfigurationException
     */
    private PublicKey getIssuerPublicKey(HttpServletRequest request, String issuer) throws ConfigurationException, ProcessingException {
        String issuerHost = null;
        PublicKey issuerPublicKey = null;

        try {
            issuerHost = new URL(issuer).getHost();
        } catch (MalformedURLException e) {
            logger.trace("Token issuer is not a valid URL: " + issuer, e);
            issuerHost = issuer;
        }

        logger.trace("Trying to find a PK for issuer: " + issuerHost);
        try {
            issuerPublicKey = CoreConfigUtil.getValidatingKey(keyManager, issuerHost);
        } catch (IllegalStateException ise) {
            logger.trace("Token issuer is not found for: " + issuer, ise);
        }

        if (issuerPublicKey == null) {
            issuerHost = request.getRemoteAddr();

            logger.trace("Trying to find a PK for issuer " + issuerHost);
            issuerPublicKey = CoreConfigUtil.getValidatingKey(keyManager, issuerHost);
        }

        logger.trace("Using Validating Alias=" + issuerHost + " to check signatures.");

        return issuerPublicKey;
    }

    protected void processSAMLResponseMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        SAMLDocumentHolder samlDocumentHolder = null;
        SAML2Object samlObject = null;

        Document samlResponse = null;
        boolean isErrorResponse = false;
        String destination = null;
        String destinationQueryStringWithSignature = null;

        String contextPath = servletContext.getContextPath();

        boolean requestedPostProfile = false;

        // Get the SAML Response Message

        String samlResponseMessage = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);
        String relayState = request.getParameter(GeneralConstants.RELAY_STATE);

        boolean willSendRequest = false;

        String referer = request.getHeader("Referer");

        //cleanUpSessionNote(request);

        IDPWebRequestUtil webRequestUtil = new IDPWebRequestUtil(request, idpConfiguration, keyManager);

        try {
            samlDocumentHolder = webRequestUtil.getSAMLDocumentHolder(samlResponseMessage);
            samlObject = samlDocumentHolder.getSamlObject();

            if (!(samlObject instanceof StatusResponseType)) {
                throw logger.wrongTypeError(samlObject.getClass().getName());
            }

            StatusResponseType statusResponseType = (StatusResponseType) samlObject;
            String issuer = statusResponseType.getIssuer().getValue();

            boolean isValid = samlResponseMessage != null;

            if (!isValid)
                throw logger.samlIDPValidationCheckFailed();

            IssuerInfoHolder idpIssuer = new IssuerInfoHolder(getIdentityURL());
            ProtocolContext protocolContext = new HTTPContext(request, response, servletContext);
            // Create the request/response
            SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext, idpIssuer.getIssuer(),
                    samlDocumentHolder, SAML2Handler.HANDLER_TYPE.IDP);
            Map<String, Object> options = new HashMap<String, Object>();

            if (this.idpConfiguration.isSupportsSignature() || this.idpConfiguration.isEncrypt()) {
                PublicKey publicKey = getIssuerPublicKey(request, issuer);
                options.put(GeneralConstants.SENDER_PUBLIC_KEY, publicKey);
            }

            options.put(GeneralConstants.SAML_IDP_STRICT_POST_BINDING, this.idpConfiguration.isStrictPostBinding());
            options.put(GeneralConstants.SUPPORTS_SIGNATURES, this.idpConfiguration.isSupportsSignature());
            if (auditHelper != null) {
                options.put(GeneralConstants.AUDIT_HELPER, auditHelper);
                options.put(GeneralConstants.CONTEXT_PATH, contextPath);
            }

            saml2HandlerRequest.setOptions(options);
            saml2HandlerRequest.setRelayState(relayState);

            SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

            Set<SAML2Handler> handlers = chain.handlers();

            // the trusted domains is done by a handler
            // webRequestUtil.isTrusted(issuer);

            if (handlers != null) {
                try {
                    chainLock.lock();
                    for (SAML2Handler handler : handlers) {
                        handler.reset();
                        handler.handleStatusResponseType(saml2HandlerRequest, saml2HandlerResponse);
                        willSendRequest = saml2HandlerResponse.getSendRequest();
                    }
                } finally {
                    chainLock.unlock();
                }
            }

            samlResponse = saml2HandlerResponse.getResultingDocument();
            relayState = saml2HandlerResponse.getRelayState();

            destination = saml2HandlerResponse.getDestination();
            requestedPostProfile = saml2HandlerResponse.isPostBindingForResponse();
            destinationQueryStringWithSignature = saml2HandlerResponse.getDestinationQueryStringWithSignature();
        } catch (Exception e) {
            String status = JBossSAMLURIConstants.STATUS_AUTHNFAILED.get();
            if (e instanceof IssuerNotTrustedException) {
                status = JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get();
            }
            logger.samlIDPRequestProcessingError(e);
            samlResponse = webRequestUtil.getErrorResponse(referer, status, getIdentityURL(),
                    this.idpConfiguration.isSupportsSignature());
            isErrorResponse = true;
        } finally {
            try {
                IDPWebRequestUtil.WebRequestUtilHolder holder = webRequestUtil.getHolder();
                if (destination == null)
                    throw new ServletException(logger.nullValueError("Destination"));
                holder.setResponseDoc(samlResponse).setDestination(destination).setRelayState(relayState)
                        .setAreWeSendingRequest(willSendRequest).setPrivateKey(null).setSupportSignature(false)
                        .setErrorResponse(isErrorResponse).setServletResponse(response)
                        .setPostBindingRequested(requestedPostProfile)
                        .setDestinationQueryStringWithSignature(destinationQueryStringWithSignature);

                /*
                 * if (requestedPostProfile) holder.setPostBindingRequested(requestedPostProfile); else
                 * holder.setPostBindingRequested(postProfile);
                 */

                if (this.idpConfiguration.isSupportsSignature()) {
                    holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
                }

                holder.setStrictPostBinding(this.idpConfiguration.isStrictPostBinding());

                if (holder.isPostBinding())
                    recycle(response);

                if (enableAudit) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.RESPONSE_TO_SP);
                    auditEvent.setWhoIsAuditing(contextPath);
                    auditEvent.setDestination(destination);
                    auditHelper.audit(auditEvent);
                }
                webRequestUtil.send(holder);
            } catch (ParsingException e) {
                logger.samlAssertionPasingFailed(e);
            } catch (GeneralSecurityException e) {
                logger.trace("Security Exception:", e);
            }
        }
        return;
    }

    protected void cleanUpSessionNote(HttpServletRequest request) {
        HttpSession session = request.getSession();
        /**
         * Since the container has finished the authentication, we can retrieve the original saml message as well as any relay
         * state from the SP
         */
        String samlRequestMessage = (String) session.getAttribute(GeneralConstants.SAML_REQUEST_KEY);

        String samlResponseMessage = (String) session.getAttribute(GeneralConstants.SAML_RESPONSE_KEY);
        String relayState = (String) session.getAttribute(GeneralConstants.RELAY_STATE);
        String signature = (String) session.getAttribute(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        String sigAlg = (String) session.getAttribute(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);

        if (logger.isTraceEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Retrieved saml messages and relay state from session");
            builder.append("saml Request message=").append(samlRequestMessage);
            builder.append("::").append("SAMLResponseMessage=");
            builder.append(samlResponseMessage).append(":").append("relay state=").append(relayState);

            builder.append("Signature=").append(signature).append("::sigAlg=").append(sigAlg);
            logger.trace(builder.toString());
        }

        if (isNotNull(samlRequestMessage))
            session.removeAttribute(GeneralConstants.SAML_REQUEST_KEY);
        if (isNotNull(samlResponseMessage))
            session.removeAttribute(GeneralConstants.SAML_RESPONSE_KEY);

        if (isNotNull(relayState))
            session.removeAttribute(GeneralConstants.RELAY_STATE);

        if (isNotNull(signature))
            session.removeAttribute(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        if (isNotNull(sigAlg))
            session.removeAttribute(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);
    }

    protected void sendErrorResponseToSP(String referrer, HttpServletResponse response, String relayState, IDPWebRequestUtil webRequestUtil)
            throws ServletException, IOException, ConfigurationException {

        logger.trace("About to send error response to SP:" + referrer);

        String contextPath = servletContext.getContextPath();

        Document samlResponse = webRequestUtil.getErrorResponse(referrer, JBossSAMLURIConstants.STATUS_RESPONDER.get(),
                getIdentityURL(), this.idpConfiguration.isSupportsSignature());
        try {

            IDPWebRequestUtil.WebRequestUtilHolder holder = webRequestUtil.getHolder();
            holder.setResponseDoc(samlResponse).setDestination(referrer).setRelayState(relayState)
                    .setAreWeSendingRequest(false).setPrivateKey(null).setSupportSignature(false).setServletResponse(response);
            holder.setPostBindingRequested(webRequestUtil.hasSAMLRequestInPostProfile());

            if (this.idpConfiguration.isSupportsSignature()) {
                holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
            }

            holder.setStrictPostBinding(this.idpConfiguration.isStrictPostBinding());

            if (holder.isPostBinding())
                recycle(response);

            if (enableAudit) {
                PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                auditEvent.setType(PicketLinkAuditEventType.ERROR_RESPONSE_TO_SP);
                auditEvent.setWhoIsAuditing(contextPath);
                auditEvent.setDestination(referrer);
                auditHelper.audit(auditEvent);
            }
            webRequestUtil.send(holder);
        } catch (ParsingException e1) {
            throw new ServletException(e1);
        } catch (GeneralSecurityException e) {
            throw new ServletException(e);
        }
    }

    /**
     * <p>
     * Initializes the {@link org.picketlink.identity.federation.web.core.IdentityServer}.
     * </p>
     */
    protected void initIdentityServer() {
        // The Identity Server on the servlet context gets set
        // in the implementation of IdentityServer
        // Create an Identity Server and set it on the context
        IdentityServer identityServer = (IdentityServer) servletContext.getAttribute(
                GeneralConstants.IDENTITY_SERVER);
        if (identityServer == null) {
            identityServer = new IdentityServer();
            servletContext.setAttribute(GeneralConstants.IDENTITY_SERVER, identityServer);
            if (StringUtil.isNotNull(this.idpConfiguration.getIdentityParticipantStack())) {
                try {
                    Class<?> clazz = SecurityActions.loadClass(getClass(), this.idpConfiguration.getIdentityParticipantStack());
                    if (clazz == null)
                        throw logger.classNotLoadedError(this.idpConfiguration.getIdentityParticipantStack());

                    identityServer.setStack((IdentityParticipantStack) clazz.newInstance());
                } catch (Exception e) {
                    logger.samlIDPUnableToSetParticipantStackUsingDefault(e);
                }
            }
        }
    }

    /**
     * <p>
     * Initialize the Handlers chain.
     * </p>
     */
    protected void initHandlersChain() {
        try {
            if (picketLinkConfiguration != null) {
                this.handlers = picketLinkConfiguration.getHandlers();
            } else {
                // Get the handlers
                String handlerConfigFileName = GeneralConstants.HANDLER_CONFIG_FILE_LOCATION;
                this.handlers = ConfigurationUtil.getHandlers(servletContext.getResourceAsStream(
                        handlerConfigFileName));
            }

            // Get the chain from config
            String handlerChainClass = this.handlers.getHandlerChainClass();

            if (StringUtil.isNullOrEmpty(handlerChainClass))
                chain = SAML2HandlerChainFactory.createChain();
            else {
                try {
                    chain = SAML2HandlerChainFactory.createChain(handlerChainClass);
                } catch (ProcessingException e1) {
                    throw new RuntimeException(e1);
                }
            }

            chain.addAll(HandlerUtil.getHandlers(this.handlers));

            Map<String, Object> chainConfigOptions = new HashMap<String, Object>();
            chainConfigOptions.put(GeneralConstants.ROLE_GENERATOR, roleGenerator);
            chainConfigOptions.put(GeneralConstants.CONFIGURATION, idpConfiguration);
            if (this.keyManager != null)
                chainConfigOptions.put(GeneralConstants.KEYPAIR, keyManager.getSigningKeyPair());

            SAML2HandlerChainConfig handlerChainConfig = new DefaultSAML2HandlerChainConfig(chainConfigOptions);

            Set<SAML2Handler> samlHandlers = chain.handlers();

            for (SAML2Handler handler : samlHandlers) {
                handler.initChainConfig(handlerChainConfig);
            }
        } catch (Exception e) {
            logger.samlHandlerConfigurationError(e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    protected void initKeyManager() {
        if (this.idpConfiguration.isSupportsSignature() || this.idpConfiguration.isEncrypt()) {
            KeyProviderType keyProvider = this.idpConfiguration.getKeyProvider();
            if (keyProvider == null)
                throw new RuntimeException(
                        logger.nullValueError("Key Provider is null for context=" + servletContext.getContextPath()));

            try {
                this.keyManager = CoreConfigUtil.getTrustKeyManager(keyProvider);

                List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
                keyManager.setAuthProperties(authProperties);
                keyManager.setValidatingAlias(keyProvider.getValidatingAlias());
            } catch (Exception e) {
                logger.trustKeyManagerCreationError(e);
                throw new RuntimeException(e.getLocalizedMessage());
            }

            logger.samlIDPSettingCanonicalizationMethod(idpConfiguration.getCanonicalizationMethod());

            XMLSignatureUtil.setCanonicalizationMethodType(idpConfiguration.getCanonicalizationMethod());

            logger.trace("Key Provider=" + keyProvider.getClassName());
        }
    }

    /**
     * <p>
     * Initializes the IDP configuration.
     * </p>
     */
    @SuppressWarnings("deprecation")
    protected void initIDPConfiguration() {
        InputStream is = null;

        if (isNullOrEmpty(this.configFile)) {
            is = servletContext.getResourceAsStream(CONFIG_FILE_LOCATION);
        } else {
            try {
                is = new FileInputStream(this.configFile);
            } catch (FileNotFoundException e) {
                throw logger.samlIDPConfigurationError(e);
            }
        }

        // Work on the IDP Configuration
        if (configProvider != null) {
            try {
                if (is == null) {
                    // Try the older version
                    is = servletContext.getResourceAsStream(DEPRECATED_CONFIG_FILE_LOCATION);

                    // Additionally parse the deprecated config file
                    if (is != null && configProvider instanceof AbstractSAMLConfigurationProvider) {
                        ((AbstractSAMLConfigurationProvider) configProvider).setConfigFile(is);
                    }
                } else {
                    // Additionally parse the consolidated config file
                    if (is != null && configProvider instanceof AbstractSAMLConfigurationProvider) {
                        ((AbstractSAMLConfigurationProvider) configProvider).setConsolidatedConfigFile(is);
                    }
                }

                picketLinkConfiguration = configProvider.getPicketLinkConfiguration();
                idpConfiguration = configProvider.getIDPConfiguration();
            } catch (ProcessingException e) {
                throw logger.samlIDPConfigurationError(e);
            } catch (ParsingException e) {
                throw logger.samlIDPConfigurationError(e);
            }
        }

        if (idpConfiguration == null) {
            if (is != null) {
                try {
                    picketLinkConfiguration = ConfigurationUtil.getConfiguration(is);
                    idpConfiguration = (IDPType) picketLinkConfiguration.getIdpOrSP();
                } catch (ParsingException e) {
                    logger.trace(e);
                    logger.samlIDPConfigurationError(e);
                }
            }

            if (is == null) {
                // Try the older version
                is = servletContext.getResourceAsStream(DEPRECATED_CONFIG_FILE_LOCATION);
                if (is == null)
                    throw logger.configurationFileMissing(DEPRECATED_CONFIG_FILE_LOCATION);
                try {
                    idpConfiguration = ConfigurationUtil.getIDPConfiguration(is);
                } catch (ParsingException e) {
                    logger.samlIDPConfigurationError(e);
                }
            }
        }

        try {
            if (this.picketLinkConfiguration != null) {
                enableAudit = picketLinkConfiguration.isEnableAudit();

                // See if we have the system property enabled
                if (!enableAudit) {
                    String sysProp = SecurityActions.getSystemProperty(GeneralConstants.AUDIT_ENABLE, "NULL");
                    if (!"NULL".equals(sysProp)) {
                        enableAudit = Boolean.parseBoolean(sysProp);
                    }
                }

                if (enableAudit) {
                    if (auditHelper == null) {
                        String securityDomainName = PicketLinkAuditHelper.getSecurityDomainName(servletContext);
                        auditHelper = new PicketLinkAuditHelper(securityDomainName);
                    }
                }
            }

            logger.trace("Identity Provider URL=" + getIdentityURL());

            // Get the attribute manager
            String attributeManager = idpConfiguration.getAttributeManager();
            if (attributeManager != null && !"".equals(attributeManager)) {
                Class<?> clazz = SecurityActions.loadClass(getClass(), attributeManager);
                if (clazz == null)
                    throw new RuntimeException(logger.classNotLoadedError(attributeManager));
                AttributeManager delegate = (AttributeManager) clazz.newInstance();
                this.attribManager.setDelegate(delegate);
            }

            // Get the role generator
            String roleGeneratorAttribute = idpConfiguration.getRoleGenerator();

            if (roleGeneratorAttribute != null && !"".equals(roleGeneratorAttribute)) {
                Class<?> clazz = SecurityActions.loadClass(getClass(), roleGeneratorAttribute);
                if (clazz == null)
                    throw new RuntimeException(logger.classNotLoadedError(roleGeneratorAttribute));
                roleGenerator = (RoleGenerator) clazz.newInstance();
            }

            // Read SP Metadata if provided
            List<EntityDescriptorType> entityDescriptors = CoreConfigUtil.getMetadataConfiguration(idpConfiguration,
                    servletContext);
            if (entityDescriptors != null) {
                for (EntityDescriptorType entityDescriptorType : entityDescriptors) {
                    SPSSODescriptorType spSSODescriptor = CoreConfigUtil.getSPDescriptor(entityDescriptorType);
                    if (spSSODescriptor != null) {
                        spSSOMetadataMap.put(entityDescriptorType.getEntityID(), spSSODescriptor);
                    }
                }
            }
        } catch (Exception e) {
            throw logger.samlIDPConfigurationError(e);
        }

        initHostedURI();
    }

    /**
     * Initializes the STS configuration.
     */
    protected void initSTSConfiguration() {
        // if the sts configuration is present in the picketlink.xml then load it.
        if (this.picketLinkConfiguration != null && this.picketLinkConfiguration.getStsType() != null) {
            PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
            sts.initialize(new PicketLinkSTSConfiguration(this.picketLinkConfiguration.getStsType()));
        } else {
            // Try to load from /WEB-INF/picketlink-sts.xml.

            // Ensure that the Core STS has the SAML20 Token Provider
            PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
            // Let us look for a file
            String configPath = servletContext.getRealPath("/WEB-INF/picketlink-sts.xml");
            File stsTokenConfigFile = configPath != null ? new File(configPath) : null;

            if (stsTokenConfigFile == null || stsTokenConfigFile.exists() == false) {
                logger.samlIDPInstallingDefaultSTSConfig();
                sts.installDefaultConfiguration();
            } else
                sts.installDefaultConfiguration(stsTokenConfigFile.toURI().toString());
        }
    }

    protected String getIdentityURL() {
        return this.idpConfiguration.getIdentityURL();
    }

    protected String determineLoginType(boolean isSecure) {
        String result = JBossSAMLURIConstants.AC_PASSWORD.get();
        if (authMethod != null) {
            if (StringUtil.isNotNull(authMethod)) {
                if ("CLIENT-CERT".equals(authMethod))
                    result = JBossSAMLURIConstants.AC_TLS_CLIENT.get();
                else if (isSecure)
                    result = JBossSAMLURIConstants.AC_PASSWORD_PROTECTED_TRANSPORT.get();
            }
        }
        return result;
    }

    protected void startPicketLink() {
        SystemPropertiesUtil.ensure();

        //Introduce a timer to reload configuration if desired
        if(timerInterval > 0 ){
            if(timer == null){
                timer = new Timer();
            }
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //Clear the configuration
                    picketLinkConfiguration = null;
                    idpConfiguration = null;

                    initIDPConfiguration();
                    initKeyManager();
                    initHandlersChain();
                }
            }, timerInterval, timerInterval);
        }

        initIDPConfiguration();
        initSTSConfiguration();
        initKeyManager();
        initHandlersChain();
        initIdentityServer();

        // Add some keys to the attibutes
        String[] ak = new String[] { "mail", "cn", "commonname", "givenname", "surname", "employeeType", "employeeNumber",
                "facsimileTelephoneNumber" };

        this.attributeKeys.addAll(Arrays.asList(ak));

        if (this.picketLinkConfiguration == null) {
            this.picketLinkConfiguration = new PicketLinkType();

            this.picketLinkConfiguration.setIdpOrSP(this.idpConfiguration);
            this.picketLinkConfiguration.setHandlers(this.handlers);
        }
    }

    /**
     * Given a set of roles, create an attribute statement
     *
     * @param roles
     * @return
     */
    private SAML11AttributeStatementType createAttributeStatement(List<String> roles) {
        SAML11AttributeStatementType attrStatement = null;
        for (String role : roles) {
            if (attrStatement == null) {
                attrStatement = new SAML11AttributeStatementType();
            }
            SAML11AttributeType attr = new SAML11AttributeType("Role", URI.create("urn:picketlink:role"));
            attr.add(role);
            attrStatement.add(attr);
        }
        return attrStatement;
    }

    /**
     * We will ignore signatures of current SAMLRequest if SP Metadata are provided for current SP and if metadata specifies
     * that SAMLRequest is not signed for this SP.
     *
     * @param spIssuer
     * @return true if signature is not expected in SAMLRequest and so signature validation should be ignored
     */
    private Boolean willIgnoreSignatureOfCurrentRequest(String spIssuer) {
        SPSSODescriptorType currentSPMetadata = spSSOMetadataMap.get(spIssuer);

        if (currentSPMetadata == null) {
            return false;
        }

        Boolean isRequestSigned = currentSPMetadata.isAuthnRequestsSigned();
        if(isRequestSigned == null){
            isRequestSigned = Boolean.FALSE;
        }

        logger.trace("Issuer: " + spIssuer + ", isRequestSigned: " + isRequestSigned);

        return !isRequestSigned;
    }

    private void initHostedURI() {
        String hostedURI = this.idpConfiguration.getHostedURI();

        if (isNullOrEmpty(hostedURI)) {
            hostedURI = "/hosted/";
        } else if (!hostedURI.contains(".") && !hostedURI.endsWith("/")) {
            // make sure the hosted uri have a slash at the end if it points to a directory
            hostedURI = hostedURI + "/";
        }

        this.idpConfiguration.setHostedURI(hostedURI);
    }

    protected void recycle(HttpServletResponse response) {
        /**
         * Since the container finished authentication, it will try to locate index.jsp or index.html. We need to recycle
         * whatever is in the response object such that we direct it to the html that is being created as part of the HTTP/POST
         * binding
         */
        response.reset();
    }

    /**
     * <p>
     * Returns the configurations used.
     * </p>
     *
     * @return
     */
    protected PicketLinkType getConfiguration() {
        return this.picketLinkConfiguration;
    }

    private void configureAuditHelper() throws ServletException {
        this.auditHelper = (PicketLinkAuditHelper) this.servletContext.getAttribute(AUDIT_HELPER);

        if (this.auditHelper == null) {
            String auditHelperType = this.servletContext.getInitParameter(AUDIT_HELPER);

            if (auditHelperType != null) {
                try {
                    this.auditHelper = (PicketLinkAuditHelper) SecurityActions
                        .loadClass(Thread.currentThread().getContextClassLoader(), auditHelperType).newInstance();
                } catch (Exception e) {
                    throw new ServletException("Could not create audit helper [" + auditHelperType + "].", e);
                }
            }
        }
    }

    private void configureConfigurationProvider() throws ServletException {
        this.configProvider = (SAMLConfigurationProvider) this.servletContext.getAttribute(CONFIG_PROVIDER);

        if (this.configProvider == null) {
            String configProviderType = this.servletContext.getInitParameter(CONFIG_PROVIDER);

            if (configProviderType != null) {
                try {
                    this.configProvider = (SAMLConfigurationProvider) SecurityActions
                        .loadClass(Thread.currentThread().getContextClassLoader(), configProviderType).newInstance();
                } catch (Exception e) {
                    throw new ServletException("Could not create config provider [" + configProviderType + "].", e);
                }
            }
        }
    }

    public SAMLConfigurationProvider getConfigProvider() {
        return this.configProvider;
    }
}
