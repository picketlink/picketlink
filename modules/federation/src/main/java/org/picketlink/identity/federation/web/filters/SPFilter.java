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

import org.jboss.security.audit.AuditLevel;
import org.picketlink.common.ErrorCodes;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.fed.AssertionExpiredException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StringUtil;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.SPType;
import org.picketlink.config.federation.handler.Handlers;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.factories.SAML2HandlerChainFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.HandlerUtil;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AuthenticationStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11StatementAbstractType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11ResponseType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.config.AbstractSAMLConfigurationProvider;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.interfaces.IRoleValidator;
import org.picketlink.identity.federation.web.process.ServiceProviderBaseProcessor;
import org.picketlink.identity.federation.web.process.ServiceProviderSAMLRequestProcessor;
import org.picketlink.identity.federation.web.process.ServiceProviderSAMLResponseProcessor;
import org.picketlink.identity.federation.web.roles.DefaultRoleValidator;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.picketlink.common.constants.GeneralConstants.CONFIG_FILE_LOCATION;
import static org.picketlink.common.util.StringUtil.isNotNull;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * A service provider filter for web container agnostic providers
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public class SPFilter implements Filter {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static final String ISSUER_ID = "ISSUER_ID";

    public static final String DESIRED_IDP = "picketlink.desired.idp";
    public static final String CHARACTER_ENCODING = "CHARACTER_ENCODING";
    public static final String CONFIGURATION_PROVIDER = "CONFIGURATION_PROVIDER";
    public static final String SAML_HANDLER_CHAIN_CLASS = "SAML_HANDLER_CHAIN_CLASS";

    private final boolean trace = logger.isTraceEnabled();

    protected SPType spConfiguration = null;

    protected PicketLinkType picketLinkConfiguration = null;

    protected String configFile;

    protected String serviceURL = null;

    protected String identityURL = null;

    protected transient String samlHandlerChainClass = null;

    private TrustKeyManager keyManager;

    private ServletContext servletContext = null;

    private transient SAML2HandlerChain chain = null;

    protected boolean ignoreSignatures = false;

    private IRoleValidator roleValidator = new DefaultRoleValidator();

    private String logOutPage = GeneralConstants.LOGOUT_PAGE_NAME;

    protected String canonicalizationMethod = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

    protected volatile PicketLinkAuditHelper auditHelper = null;

    protected volatile String issuerID = null;

    protected IDPSSODescriptorType idpMetadata;

    protected Lock chainLock = new ReentrantLock();
    private String characterEncoding;

    protected SAMLConfigurationProvider configProvider = null;
    private Map<String, Object> chainConfigOptions;

    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = createHttpServletRequestWrapper((HttpServletRequest) servletRequest);
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            // needs to be done first, *before* accessing any parameters. super.authenticate(..) gets called to late
            String characterEncoding = getCharacterEncoding();

            if (characterEncoding != null) {
                request.setCharacterEncoding(characterEncoding);
            }

            HttpSession session = request.getSession(true);

            // Eagerly look for Local LogOut
            boolean localLogout = isLocalLogout(request);

            if (localLogout) {
                try {
                    sendToLogoutPage(request, response, session);
                } catch (ServletException e) {
                    logger.samlLogoutError(e);
                    throw new IOException(e);
                }
                return;
            }

            String samlRequest = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
            String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

            Principal principal = request.getUserPrincipal();

            // If we have already authenticated the user and there is no request from IDP or logout from user
            if (principal != null && !(isGlobalLogout(request) || isNotNull(samlRequest) || isNotNull(samlResponse))) {
                filterChain.doFilter(request, response);
            } else {

                // General User Request
                if (!isNotNull(samlRequest) && !isNotNull(samlResponse)) {
                    generalUserRequest(request, response);
                }

                // Handle a SAML Response from IDP
                if (isNotNull(samlResponse)) {
                    handleSAMLResponse(request, response);
                }

                // Handle SAML Requests from IDP
                if (isNotNull(samlRequest)) {
                    handleSAMLRequest(request, response);
                }// end if

                request = createHttpServletRequestWrapper((HttpServletRequest) servletRequest);

                principal = request.getUserPrincipal();

                if (principal != null && !response.isCommitted()) {
                    filterChain.doFilter(request, response);
                } else {
                    localAuthentication(request, response);
                }
            }
        } catch (IOException e) {
            SPType configuration = getConfiguration();

            if (StringUtil.isNotNull(configuration.getErrorPage())) {
                try {
                    request.getRequestDispatcher(configuration.getErrorPage()).forward(request, response);
                } catch (ServletException e1) {
                    logger.samlErrorPageForwardError(configuration.getErrorPage(), e1);
                }
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                throw e;
            }
        }
    }

    private HttpServletRequest createHttpServletRequestWrapper(final HttpServletRequest request) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public Principal getUserPrincipal() {
                HttpSession session = getSession(false);

                if (session != null) {
                    return (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);
                }

                return super.getUserPrincipal();
            }
        };
    }

    private String getCharacterEncoding() {
        return this.characterEncoding;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
        processConfiguration(filterConfig);
    }

    /**
     * Create a SAML2 auth request
     *
     * @param serviceURL URL of the service
     * @param identityURL URL of the identity provider
     *
     * @return
     *
     * @throws ConfigurationException
     */
    private AuthnRequestType createSAMLRequest(String serviceURL, String identityURL) throws ConfigurationException {
        if (serviceURL == null)
            throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "serviceURL");
        if (identityURL == null)
            throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "identityURL");

        SAML2Request saml2Request = new SAML2Request();
        String id = IDGenerator.create("ID_");
        return saml2Request.createAuthnRequestType(id, serviceURL, identityURL, serviceURL);
    }



    protected void sendToDestination(Document samlDocument, String relayState, String destination,
                                     HttpServletResponse response, boolean request) throws IOException, SAXException, GeneralSecurityException {
        if (!ignoreSignatures) {
            SAML2Signature samlSignature = new SAML2Signature();

            Node nextSibling = samlSignature.getNextSiblingOfIssuer(samlDocument);
            if (nextSibling != null) {
                samlSignature.setNextSibling(nextSibling);
            }
            KeyPair keypair = keyManager.getSigningKeyPair();
            samlSignature.signSAMLDocument(samlDocument, keypair);
        }
        String samlMessage = PostBindingUtil.base64Encode(DocumentUtil.getDocumentAsString(samlDocument));
        PostBindingUtil.sendPost(new DestinationInfoHolder(destination, samlMessage, relayState), response, request);
    }

    private boolean handleSAMLResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validate(request)) {
            throw new IOException(ErrorCodes.VALIDATION_CHECK_FAILED);
        }

        String samlVersion = getSAMLVersion(request);

        if (!JBossSAMLConstants.VERSION_2_0.get().equals(samlVersion)) {
            return handleSAML11UnsolicitedResponse(request, response);
        }

        return handleSAML2Response(request, response);
    }

    private boolean isLocalLogout(HttpServletRequest request) {
        String lloStr = request.getParameter(GeneralConstants.LOCAL_LOGOUT);
        return isNotNull(lloStr) && "true".equalsIgnoreCase(lloStr);
    }

    protected void sendToLogoutPage(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException, ServletException {
        // we are invalidated.
        RequestDispatcher dispatch = this.servletContext.getRequestDispatcher(this.getConfiguration().getLogOutPage());
        if (dispatch == null) {
            logger.samlSPCouldNotDispatchToLogoutPage(this.getConfiguration().getLogOutPage());
        } else {
            logger.trace("Forwarding request to logOutPage: " + this.getConfiguration().getLogOutPage());

            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                // if session was already invalidated we just ignore the exception.
            }

            try {
                dispatch.forward(request, response);
            } catch (Exception e) {
                // JBAS5.1 and 6 quirkiness
                dispatch.forward(request, response);
            }
        }
    }

    private SPType getConfiguration() {
        return (SPType) this.picketLinkConfiguration.getIdpOrSP();
    }

    private boolean isGlobalLogout(HttpServletRequest request) {
        String gloStr = request.getParameter(GeneralConstants.GLOBAL_LOGOUT);
        return isNotNull(gloStr) && "true".equalsIgnoreCase(gloStr);
    }

    private boolean generalUserRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        boolean willSendRequest = false;
        HTTPContext httpContext = new HTTPContext(request, response, this.servletContext);
        Set<SAML2Handler> handlers = chain.handlers();

        boolean postBinding = getConfiguration().getBindingType().equals("POST");

        // Neither saml request nor response from IDP
        // So this is a user request
        SAML2HandlerResponse saml2HandlerResponse = null;
        try {
            ServiceProviderBaseProcessor baseProcessor = new ServiceProviderBaseProcessor(postBinding, serviceURL, this.picketLinkConfiguration, this.idpMetadata);
            if (issuerID != null) {
                baseProcessor.setIssuer(issuerID);
            }

            // If the user has a different desired idp
            String idp = (String) request.getAttribute(DESIRED_IDP);
            if (StringUtil.isNotNull(idp)) {
                baseProcessor.setIdentityURL(idp);
            } else {
                baseProcessor.setIdentityURL(getIdentityURL());
            }
            baseProcessor.setAuditHelper(auditHelper);

            saml2HandlerResponse = baseProcessor.process(httpContext, handlers, chainLock);
        } catch (ProcessingException pe) {
            logger.samlSPHandleRequestError(pe);
            throw new RuntimeException(pe);
        } catch (ParsingException pe) {
            logger.samlSPHandleRequestError(pe);
            throw new RuntimeException(pe);
        } catch (ConfigurationException pe) {
            logger.samlSPHandleRequestError(pe);
            throw new RuntimeException(pe);
        }

        willSendRequest = saml2HandlerResponse.getSendRequest();

        Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
        String relayState = saml2HandlerResponse.getRelayState();

        String destination = saml2HandlerResponse.getDestination();
        String destinationQueryStringWithSignature = saml2HandlerResponse.getDestinationQueryStringWithSignature();

        if (destination != null && samlResponseDocument != null) {
            try {
                if (isEnableAudit()) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.REQUEST_TO_IDP);
                    auditEvent.setWhoIsAuditing(getContextPath());
                    auditHelper.audit(auditEvent);
                }
                sendRequestToIDP(destination, samlResponseDocument, relayState, request, response, willSendRequest, destinationQueryStringWithSignature);
                return false;
            } catch (Exception e) {
                logger.samlSPHandleRequestError(e);
                throw logger.samlSPProcessingExceptionError(e);
            }
        }

        return localAuthentication(request, response);
    }

    private String getContextPath() {
        return this.servletContext.getContextPath();
    }

    public String getIdentityURL() {
        return getConfiguration().getIdentityURL();
    }

    private boolean isEnableAudit() {
        return this.picketLinkConfiguration.isEnableAudit();
    }

    protected void sendRequestToIDP(String destination, Document samlDocument, String relayState, HttpServletRequest request, HttpServletResponse response,
        boolean willSendRequest, String destinationQueryStringWithSignature) throws ProcessingException, ConfigurationException, IOException {

        if (isAjaxRequest(request) && request.getUserPrincipal() == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            if (isHttpPostBinding()) {
                sendHttpPostBindingRequest(destination, samlDocument, relayState, response, willSendRequest);
            } else {
                sendHttpRedirectRequest(destination, samlDocument, relayState, response, willSendRequest, destinationQueryStringWithSignature);
            }
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWithHeader = request.getHeader(GeneralConstants.HTTP_HEADER_X_REQUESTED_WITH);
        return requestedWithHeader != null && "XMLHttpRequest".equalsIgnoreCase(requestedWithHeader);
    }

    protected boolean isHttpPostBinding() {
        return getBinding().equalsIgnoreCase("POST");
    }

    protected void sendHttpPostBindingRequest(String destination, Document samlDocument, String relayState, HttpServletResponse response,
        boolean willSendRequest) throws ProcessingException, IOException,
        ConfigurationException {
        String samlMessage = PostBindingUtil.base64Encode(DocumentUtil.getDocumentAsString(samlDocument));

        DestinationInfoHolder destinationHolder = new DestinationInfoHolder(destination, samlMessage, relayState);

        PostBindingUtil.sendPost(destinationHolder, response, willSendRequest);
    }

    protected void sendHttpRedirectRequest(String destination, Document samlDocument, String relayState, HttpServletResponse response,
        boolean willSendRequest, String destinationQueryStringWithSignature) throws IOException,
        ProcessingException, ConfigurationException {
        String destinationQueryString = null;

        // We already have queryString with signature from SAML2SignatureGenerationHandler
        if (destinationQueryStringWithSignature != null) {
            destinationQueryString = destinationQueryStringWithSignature;
        } else {
            String samlMessage = DocumentUtil.getDocumentAsString(samlDocument);
            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMessage.getBytes("UTF-8"));
            destinationQueryString = RedirectBindingUtil.getDestinationQueryString(base64Request, relayState, willSendRequest);
        }

        RedirectBindingUtil.RedirectBindingUtilDestHolder holder = new RedirectBindingUtil.RedirectBindingUtilDestHolder();

        holder.setDestination(destination).setDestinationQueryString(destinationQueryString);

        HTTPRedirectUtil.sendRedirectForRequestor(RedirectBindingUtil.getDestinationURL(holder), response);
    }

    protected String getBinding() {
        return getConfiguration().getBindingType();
    }

    protected boolean localAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // check how to deal with local authentication
        return true;
    }

    protected boolean validate(HttpServletRequest request) {
        return request.getParameter("SAMLResponse") != null;
    }

    private String getSAMLVersion(HttpServletRequest request) {
        String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);
        String version;

        try {
            Document samlDocument = toSAMLResponseDocument(samlResponse, "POST".equalsIgnoreCase(request.getMethod()));
            Element element = samlDocument.getDocumentElement();

            // let's try SAML 2.0 Version attribute first
            version = element.getAttribute("Version");

            if (isNullOrEmpty(version)) {
                // fallback to SAML 1.1 Minor and Major attributes
                String minorVersion = element.getAttribute("MinorVersion");
                String majorVersion = element.getAttribute("MajorVersion");

                version = minorVersion + "." + majorVersion;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not extract version from SAML Response.", e);
        }

        return version;
    }

    private Document toSAMLResponseDocument(String samlResponse, boolean isPostBinding) throws ParsingException {
        InputStream dataStream = null;

        if (isPostBinding) {
            // deal with SAML response from IDP
            dataStream = PostBindingUtil.base64DecodeAsStream(samlResponse);
        } else {
            // deal with SAML response from IDP
            dataStream = RedirectBindingUtil.base64DeflateDecode(samlResponse);
        }

        try {
            return DocumentUtil.getDocument(dataStream);
        } catch (Exception e) {
            logger.samlResponseFromIDPParsingFailed();
            throw new ParsingException("", e);
        }
    }

    public boolean handleSAML11UnsolicitedResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

        Principal principal = request.getUserPrincipal();

        // If we have already authenticated the user and there is no request from IDP or logout from user
        if (principal != null) {
            return true;
        }

        HttpSession session = request.getSession(true);

        // See if we got a response from IDP
        if (isNotNull(samlResponse)) {
            boolean isValid = false;
            try {
                isValid = validate(request);
            } catch (Exception e) {
                logger.samlSPHandleRequestError(e);
                throw new IOException();
            }
            if (!isValid) {
                throw new IOException(ErrorCodes.VALIDATION_CHECK_FAILED);
            }

            try {
                InputStream base64DecodedResponse = null;

                if ("GET".equalsIgnoreCase(request.getMethod())) {
                    base64DecodedResponse = RedirectBindingUtil.base64DeflateDecode(samlResponse);
                } else {
                    base64DecodedResponse = PostBindingUtil.base64DecodeAsStream(samlResponse);
                }

                SAMLParser parser = new SAMLParser();
                SAML11ResponseType saml11Response = (SAML11ResponseType) parser.parse(base64DecodedResponse);

                List<SAML11AssertionType> assertions = saml11Response.get();
                if (assertions.size() > 1) {
                    logger.trace("More than one assertion from IDP. Considering the first one.");
                }
                String username = null;
                List<String> roles = new ArrayList<String>();
                SAML11AssertionType assertion = assertions.get(0);
                if (assertion != null) {
                    // Get the subject
                    List<SAML11StatementAbstractType> statements = assertion.getStatements();
                    for (SAML11StatementAbstractType statement : statements) {
                        if (statement instanceof SAML11AuthenticationStatementType) {
                            SAML11AuthenticationStatementType subStat = (SAML11AuthenticationStatementType) statement;
                            SAML11SubjectType subject = subStat.getSubject();
                            username = subject.getChoice().getNameID().getValue();
                        }
                    }
                    roles = AssertionUtil.getRoles(assertion, null);
                }

                return true;
            } catch (Exception e) {
                logger.samlSPHandleRequestError(e);
            }
        }

        return false;
    }

    private boolean handleSAML2Response(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);
        HTTPContext httpContext = new HTTPContext(request, response, this.servletContext);
        Set<SAML2Handler> handlers = chain.handlers();

        Principal principal = request.getUserPrincipal();

        boolean willSendRequest;// deal with SAML response from IDP

        try {
            ServiceProviderSAMLResponseProcessor responseProcessor = new ServiceProviderSAMLResponseProcessor(request.getMethod()
                .equals("POST"), serviceURL, this.picketLinkConfiguration, this.idpMetadata);
            if (auditHelper != null) {
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
                sendRequestToIDP(destination, samlResponseDocument, relayState, request, response, willSendRequest, destinationQueryStringWithSignature);
            } else {
                // See if the session has been invalidated
                boolean sessionValidity = request.getUserPrincipal() != null;

                if (!sessionValidity) {
                    sendToLogoutPage(request, response, session);
                    return false;
                }

                // We got a response with the principal
                List<String> roles = saml2HandlerResponse.getRoles();
                if (principal == null) {
                    principal = (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);
                }

                if(principal == null) {
                    throw new RuntimeException(ErrorCodes.NULL_VALUE + " principal");
                }

                if (isEnableAudit()) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.RESPONSE_FROM_IDP);
                    auditEvent.setSubjectName(principal.getName());
                    auditEvent.setWhoIsAuditing(getContextPath());
                    auditHelper.audit(auditEvent);
                }

                return true;
            }
        } catch (ProcessingException pe) {
            Throwable t = pe.getCause();
            if (t != null && t instanceof AssertionExpiredException) {
                logger.error("Assertion has expired. Asking IDP for reissue");
                if (isEnableAudit()) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.EXPIRED_ASSERTION);
                    auditEvent.setAssertionID(((AssertionExpiredException) t).getId());
                    auditHelper.audit(auditEvent);
                }
                // Just issue a fresh request back to IDP
                return generalUserRequest(request, response);
            }
            logger.samlSPHandleRequestError(pe);
            throw logger.samlSPProcessingExceptionError(pe);
        } catch (Exception e) {
            logger.samlSPHandleRequestError(e);
            throw logger.samlSPProcessingExceptionError(e);
        }

        return localAuthentication(request, response);
    }

    private void processIdPMetadata(SPType spConfiguration) {
        IDPSSODescriptorType idpssoDescriptorType = null;

        if (isNotNull(spConfiguration.getIdpMetadataFile())) {
            idpssoDescriptorType = getIdpMetadataFromFile(spConfiguration);
        } else {
            idpssoDescriptorType = getIdpMetadataFromProvider(spConfiguration);
        }

        if (idpssoDescriptorType != null) {
            List<EndpointType> endpoints = idpssoDescriptorType.getSingleSignOnService();
            for (EndpointType endpoint : endpoints) {
                String endpointBinding = endpoint.getBinding().toString();
                if (endpointBinding.contains("HTTP-POST")) {
                    endpointBinding = "POST";
                } else if (endpointBinding.contains("HTTP-Redirect")) {
                    endpointBinding = "REDIRECT";
                }
                if (spConfiguration.getBindingType().equals(endpointBinding)) {
                    spConfiguration.setIdentityURL(endpoint.getLocation().toString());
                    break;
                }
            }

            this.idpMetadata = idpssoDescriptorType;
        }
    }

    private IDPSSODescriptorType getIdpMetadataFromProvider(SPType spConfiguration) {
        List<EntityDescriptorType> entityDescriptors = CoreConfigUtil.getMetadataConfiguration(spConfiguration,
            this.servletContext);

        if (entityDescriptors != null) {
            for (EntityDescriptorType entityDescriptorType : entityDescriptors) {
                IDPSSODescriptorType idpssoDescriptorType = handleMetadata(entityDescriptorType);

                if (idpssoDescriptorType != null) {
                    return idpssoDescriptorType;
                }
            }
        }

        return null;
    }

    protected IDPSSODescriptorType handleMetadata(EntitiesDescriptorType entities) {
        IDPSSODescriptorType idpSSO = null;

        List<Object> entityDescs = entities.getEntityDescriptor();
        for (Object entityDescriptor : entityDescs) {
            if (entityDescriptor instanceof EntitiesDescriptorType) {
                idpSSO = getIDPSSODescriptor(entities);
            } else {
                idpSSO = handleMetadata((EntityDescriptorType) entityDescriptor);
            }
            if (idpSSO != null) {
                break;
            }
        }
        return idpSSO;
    }

    protected IDPSSODescriptorType handleMetadata(EntityDescriptorType entityDescriptor) {
        return CoreConfigUtil.getIDPDescriptor(entityDescriptor);
    }

    protected IDPSSODescriptorType getIDPSSODescriptor(EntitiesDescriptorType entities) {
        List<Object> entityDescs = entities.getEntityDescriptor();
        for (Object entityDescriptor : entityDescs) {

            if (entityDescriptor instanceof EntitiesDescriptorType) {
                return getIDPSSODescriptor((EntitiesDescriptorType) entityDescriptor);
            }
            return CoreConfigUtil.getIDPDescriptor((EntityDescriptorType) entityDescriptor);
        }
        return null;
    }

    protected IDPSSODescriptorType getIdpMetadataFromFile(SPType configuration) {
        InputStream is = this.servletContext.getResourceAsStream(configuration.getIdpMetadataFile());
        if (is == null) {
            return null;
        }

        Object metadata = null;
        try {
            Document samlDocument = DocumentUtil.getDocument(is);
            SAMLParser parser = new SAMLParser();
            metadata = parser.parse(DocumentUtil.getNodeAsStream(samlDocument));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        IDPSSODescriptorType idpSSO = null;
        if (metadata instanceof EntitiesDescriptorType) {
            EntitiesDescriptorType entities = (EntitiesDescriptorType) metadata;
            idpSSO = handleMetadata(entities);
        } else {
            idpSSO = handleMetadata((EntityDescriptorType) metadata);
        }
        if (idpSSO == null) {
            logger.samlSPUnableToGetIDPDescriptorFromMetadata();
            return idpSSO;
        }

        return idpSSO;
    }

    private boolean handleSAMLRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String samlRequest = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
        HTTPContext httpContext = new HTTPContext(request, response, this.servletContext);
        Set<SAML2Handler> handlers = chain.handlers();

        try {
            ServiceProviderSAMLRequestProcessor requestProcessor = new ServiceProviderSAMLRequestProcessor(
                request.getMethod().equals("POST"), this.serviceURL, this.picketLinkConfiguration, this.idpMetadata);
            requestProcessor.setTrustKeyManager(keyManager);
            boolean result = requestProcessor.process(samlRequest, httpContext, handlers, chainLock);

            if (isEnableAudit()) {
                PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                auditEvent.setType(PicketLinkAuditEventType.REQUEST_FROM_IDP);
                auditEvent.setWhoIsAuditing(getContextPath());
                auditHelper.audit(auditEvent);
            }

            // If response is already commited, we need to stop with processing of HTTP request
            if (response.isCommitted()) {
                return false;
            }

            if (result) {
                return result;
            }
        } catch (Exception e) {
            logger.samlSPHandleRequestError(e);
            throw logger.samlSPProcessingExceptionError(e);
        }

        return localAuthentication(request, response);
    }

    protected void processConfiguration(FilterConfig filterConfig) {
        InputStream is;

        if (isNullOrEmpty(this.configFile)) {
            is = servletContext.getResourceAsStream(CONFIG_FILE_LOCATION);
        } else {
            try {
                is = new FileInputStream(this.configFile);
            } catch (FileNotFoundException e) {
                throw logger.samlIDPConfigurationError(e);
            }
        }

        PicketLinkType picketLinkType;

        String configurationProviderName = filterConfig.getInitParameter(CONFIGURATION_PROVIDER);

        if (configurationProviderName != null) {
            try {
                Class<?> clazz = SecurityActions.loadClass(getClass(), configurationProviderName);

                if (clazz == null) {
                    throw new ClassNotFoundException(ErrorCodes.CLASS_NOT_LOADED + configurationProviderName);
                }

                this.configProvider = (SAMLConfigurationProvider) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not create configuration provider [" + configurationProviderName + "].", e);
            }
        }

        try {
            // Work on the IDP Configuration
            if (configProvider != null) {
                try {
                    if (is == null) {
                        // Try the older version
                        is = servletContext.getResourceAsStream(GeneralConstants.DEPRECATED_CONFIG_FILE_LOCATION);

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

                    picketLinkType = configProvider.getPicketLinkConfiguration();
                    picketLinkType.setIdpOrSP(configProvider.getSPConfiguration());
                } catch (ProcessingException e) {
                    throw logger.samlSPConfigurationError(e);
                } catch (ParsingException e) {
                    throw logger.samlSPConfigurationError(e);
                }
            } else {
                if (is != null) {
                    try {
                        picketLinkType = ConfigurationUtil.getConfiguration(is);
                    } catch (ParsingException e) {
                        logger.trace(e);
                        throw logger.samlSPConfigurationError(e);
                    }
                } else {
                    is = servletContext.getResourceAsStream(GeneralConstants.DEPRECATED_CONFIG_FILE_LOCATION);
                    if (is == null) {
                        throw logger.configurationFileMissing(configFile);
                    }

                    picketLinkType = new PicketLinkType();

                    picketLinkType.setIdpOrSP(ConfigurationUtil.getSPConfiguration(is));
                }
            }

            //Close the InputStream as we no longer need it
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    //ignore
                }
            }

            Boolean enableAudit = picketLinkType.isEnableAudit();

            //See if we have the system property enabled
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

            SPType spConfiguration = (SPType) picketLinkType.getIdpOrSP();
            processIdPMetadata(spConfiguration);

            this.serviceURL = spConfiguration.getServiceURL();
            this.canonicalizationMethod = spConfiguration.getCanonicalizationMethod();
            this.picketLinkConfiguration = picketLinkType;

            this.issuerID = filterConfig.getInitParameter(ISSUER_ID);
            this.characterEncoding = filterConfig.getInitParameter(CHARACTER_ENCODING);
            this.samlHandlerChainClass = filterConfig.getInitParameter(SAML_HANDLER_CHAIN_CLASS);

            logger.samlSPSettingCanonicalizationMethod(canonicalizationMethod);
            XMLSignatureUtil.setCanonicalizationMethodType(canonicalizationMethod);

            try {
                this.initKeyProvider();
                this.initializeHandlerChain(picketLinkType);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            logger.trace("Identity Provider URL=" + getConfiguration().getIdentityURL());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void initKeyProvider() {
        if (!doSupportSignature()) {
            return;
        }

        SPType configuration = getConfiguration();
        KeyProviderType keyProvider = configuration.getKeyProvider();

        if (keyProvider == null && doSupportSignature()) {
            throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyProvider is null for context=" + getContextPath());
        }

        try {
            String keyManagerClassName = keyProvider.getClassName();
            if (keyManagerClassName == null) {
                throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyManager class name");
            }

            Class<?> clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);

            if (clazz == null) {
                throw new ClassNotFoundException(ErrorCodes.CLASS_NOT_LOADED + keyManagerClassName);
            }

            TrustKeyManager keyManager = (TrustKeyManager) clazz.newInstance();

            List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);

            keyManager.setAuthProperties(authProperties);
            keyManager.setValidatingAlias(keyProvider.getValidatingAlias());

            String identityURL = configuration.getIdentityURL();

            //Special case when you need X509Data in SignedInfo
            if (authProperties != null) {
                for (AuthPropertyType authPropertyType : authProperties) {
                    String key = authPropertyType.getKey();
                    if (GeneralConstants.X509CERTIFICATE.equals(key)) {
                        //we need X509Certificate in SignedInfo. The value is the alias name
                        keyManager.addAdditionalOption(GeneralConstants.X509CERTIFICATE, authPropertyType.getValue());
                        break;
                    }
                }
            }
            keyManager.addAdditionalOption(ServiceProviderBaseProcessor.IDP_KEY, new URL(identityURL).getHost());
            this.keyManager = keyManager;
        } catch (Exception e) {
            logger.trustKeyManagerCreationError(e);
            throw new RuntimeException(e.getLocalizedMessage());
        }

        logger.trace("Key Provider=" + keyProvider.getClassName());
    }

    protected boolean doSupportSignature() {
        return getConfiguration().isSupportsSignature();
    }

    protected void initializeHandlerChain(PicketLinkType picketLinkType) throws Exception {
        SAML2HandlerChain handlerChain;

        // Get the chain from config
        if (isNullOrEmpty(samlHandlerChainClass)) {
            handlerChain = SAML2HandlerChainFactory.createChain();
        } else {
            try {
                handlerChain = SAML2HandlerChainFactory.createChain(this.samlHandlerChainClass);
            } catch (ProcessingException e1) {
                throw new RuntimeException(e1);
            }
        }

        Handlers handlers = picketLinkType.getHandlers();

        if (handlers == null) {
            // Get the handlers
            String handlerConfigFileName = GeneralConstants.HANDLER_CONFIG_FILE_LOCATION;
            handlers = ConfigurationUtil.getHandlers(servletContext.getResourceAsStream(handlerConfigFileName));
        }

        picketLinkType.setHandlers(handlers);

        handlerChain.addAll(HandlerUtil.getHandlers(handlers));

        populateChainConfig(picketLinkType);
        SAML2HandlerChainConfig handlerChainConfig = new DefaultSAML2HandlerChainConfig(chainConfigOptions);

        Set<SAML2Handler> samlHandlers = handlerChain.handlers();

        for (SAML2Handler handler : samlHandlers) {
            handler.initChainConfig(handlerChainConfig);
        }

        chain = handlerChain;
    }

    protected void populateChainConfig(PicketLinkType picketLinkType) throws ConfigurationException, ProcessingException {
        Map<String, Object> chainConfigOptions = new HashMap<String, Object>();

        chainConfigOptions.put(GeneralConstants.CONFIGURATION, picketLinkType.getIdpOrSP());
        chainConfigOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "false"); // No validator as tomcat realm does validn

        if (doSupportSignature()) {
            chainConfigOptions.put(GeneralConstants.KEYPAIR, keyManager.getSigningKeyPair());
            //If there is a need for X509Data in signedinfo
            String certificateAlias = (String) keyManager.getAdditionalOption(GeneralConstants.X509CERTIFICATE);
            if (certificateAlias != null) {
                chainConfigOptions.put(GeneralConstants.X509CERTIFICATE, keyManager.getCertificate(certificateAlias));
            }
        }

        this.chainConfigOptions = chainConfigOptions;
    }

}