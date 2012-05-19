/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.bindings.tomcat.idp;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.valves.ValveBase;
import org.apache.log4j.Logger;
import org.jboss.security.audit.AuditLevel;
import org.picketlink.identity.federation.bindings.tomcat.TomcatRoleGenerator;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.config.PicketLinkType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.impl.DelegatedAttributeManager;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v1.SAML11ProtocolContext;
import org.picketlink.identity.federation.core.saml.v1.writers.SAML11ResponseWriter;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.factories.SAML2HandlerChainFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler.HANDLER_TYPE;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.HandlerUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.util.SystemPropertiesUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTSConfiguration;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11NameIdentifierType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType.SAML11SubjectTypeChoice;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11ResponseType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusType;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.web.config.AbstractSAMLConfigurationProvider;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.core.IdentityParticipantStack;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil.WebRequestUtilHolder;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;
import org.w3c.dom.Document;

/**
 * Generic Web Browser SSO valve for the IDP
 * 
 * Handles both the SAML Redirect as well as Post Bindings
 * 
 * Note: Most of the work is done by {@code IDPWebRequestUtil}
 * 
 * @author Anil.Saldhana@redhat.com
 * @since May 18, 2009
 */
public class IDPWebBrowserSSOValve extends ValveBase implements Lifecycle {
    private static Logger log = Logger.getLogger(IDPWebBrowserSSOValve.class);

    private final boolean trace = log.isTraceEnabled();

    protected boolean enableAudit = false;
    protected PicketLinkAuditHelper auditHelper = null;

    protected IDPType idpConfiguration = null;

    protected PicketLinkType picketLinkConfiguration = null;

    private RoleGenerator roleGenerator = new TomcatRoleGenerator();

    private long assertionValidity = 5000; // 5 seconds in miliseconds

    private String identityURL = null;

    private TrustKeyManager keyManager;

    // Option "signOutgoingMessages" is used for error messages.
    // Normal (not-error) messages are signed with SAML2SignatureGenerationHandler
    private Boolean signOutgoingMessages = true;

    private transient DelegatedAttributeManager attribManager = new DelegatedAttributeManager();

    private final List<String> attributeKeys = new ArrayList<String>();

    private transient SAML2HandlerChain chain = null;

    protected String canonicalizationMethod = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

    /**
     * The user can inject a fully qualified name of a {@link SAMLConfigurationProvider}
     */
    protected SAMLConfigurationProvider configProvider = null;

    /**
     * If the user wants to set a particular {@link IdentityParticipantStack}
     */
    protected String identityParticipantStack = null;

    /**
     * A Lock for Handler operations in the chain
     */
    private final Lock chainLock = new ReentrantLock();

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        String referer = request.getHeader("Referer");
        String relayState = request.getParameter(GeneralConstants.RELAY_STATE);

        if (isNotNull(relayState))
            relayState = RedirectBindingUtil.urlDecode(relayState);

        String samlRequestMessage = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
        String samlResponseMessage = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

        String signature = request.getParameter(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        String sigAlg = request.getParameter(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);

        boolean containsSAMLRequestMessage = isNotNull(samlRequestMessage);
        boolean containsSAMLResponseMessage = isNotNull(samlResponseMessage);

        Session session = request.getSessionInternal();

        if (containsSAMLRequestMessage || containsSAMLResponseMessage) {
            if (trace)
                log.trace("Storing the SAMLRequest/SAMLResponse and RelayState in session");
            if (isNotNull(samlRequestMessage))
                session.setNote(GeneralConstants.SAML_REQUEST_KEY, samlRequestMessage);
            if (isNotNull(samlResponseMessage))
                session.setNote(GeneralConstants.SAML_RESPONSE_KEY, samlResponseMessage);
            if (isNotNull(relayState))
                session.setNote(GeneralConstants.RELAY_STATE, relayState.trim());
            if (isNotNull(signature))
                session.setNote(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY, signature.trim());
            if (isNotNull(sigAlg))
                session.setNote(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY, sigAlg.trim());
        }

        // Lets check if the user has been authenticated
        Principal userPrincipal = request.getPrincipal();
        if (userPrincipal == null) {
            try {
                // Next in the invocation chain
                getNext().invoke(request, response);
            } finally {
                userPrincipal = request.getPrincipal();
                referer = request.getHeader("Referer");
                if (trace)
                    log.trace("Referer in finally block=" + referer + ":user principal=" + userPrincipal);
            }
        }

        IDPWebRequestUtil webRequestUtil = new IDPWebRequestUtil(request, idpConfiguration, keyManager);

        Document samlErrorResponse = null;
        // Look for unauthorized status
        if (response.getStatus() == HttpServletResponse.SC_FORBIDDEN) {
            try {
                samlErrorResponse = webRequestUtil.getErrorResponse(referer, JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(),
                        this.identityURL, this.signOutgoingMessages);

                WebRequestUtilHolder holder = webRequestUtil.getHolder();
                holder.setResponseDoc(samlErrorResponse).setDestination(referer).setRelayState(relayState)
                        .setAreWeSendingRequest(false).setPrivateKey(null).setSupportSignature(false)
                        .setServletResponse(response).setErrorResponse(true);
                holder.setPostBindingRequested(webRequestUtil.hasSAMLRequestInPostProfile());

                if (this.signOutgoingMessages) {
                    holder.setSupportSignature(true).setPrivateKey(keyManager.getSigningKey());
                }

                holder.setStrictPostBinding(this.idpConfiguration.isStrictPostBinding());

                webRequestUtil.send(holder);
            } catch (GeneralSecurityException e) {
                throw new ServletException(e);
            }
            return;
        }

        if (userPrincipal != null) {
            /**
             * Since the container has finished the authentication, we can retrieve the original saml message as well as any
             * relay state from the SP
             */
            samlRequestMessage = (String) session.getNote(GeneralConstants.SAML_REQUEST_KEY);

            samlResponseMessage = (String) session.getNote(GeneralConstants.SAML_RESPONSE_KEY);
            relayState = (String) session.getNote(GeneralConstants.RELAY_STATE);
            signature = (String) session.getNote(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
            sigAlg = (String) session.getNote(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);

            if (trace) {
                StringBuilder builder = new StringBuilder();
                builder.append("Retrieved saml messages and relay state from session");
                builder.append("saml Request message=").append(samlRequestMessage);
                builder.append("::").append("SAMLResponseMessage=");
                builder.append(samlResponseMessage).append(":").append("relay state=").append(relayState);

                builder.append("Signature=").append(signature).append("::sigAlg=").append(sigAlg);
                log.trace(builder.toString());
            }

            // Send valid saml response after processing the request
            if (samlRequestMessage != null) {
                processSAMLRequestMessage(webRequestUtil, request, response);
            } else if (isNotNull(samlResponseMessage)) {
                processSAMLResponseMessage(webRequestUtil, request, response);
            } else {
                String target = request.getParameter(SAML11Constants.TARGET);
                if (isNotNull(target)) {
                    // We have SAML 1.1 IDP first scenario. Now we need to create a SAMLResponse and send back
                    // to SP as per target
                    handleSAML11(webRequestUtil, request, response);
                } else {
                    if (trace)
                        log.trace("SAML 1.1::Proceeding to IDP index page");
                    RequestDispatcher dispatch = getContext().getServletContext().getRequestDispatcher("/hosted/");
                    try {
                        dispatch.forward(request, response);
                    } catch (Exception e) {
                        // JBAS5.1 and 6 quirkiness
                        dispatch.forward(request.getRequest(), response);
                    }
                }
            }
        }
    }

    protected void handleSAML11(IDPWebRequestUtil webRequestUtil, Request request, Response response) throws ServletException,
            IOException {
        try {
            Principal userPrincipal = request.getPrincipal();

            String target = request.getParameter(SAML11Constants.TARGET);

            Session session = request.getSessionInternal();
            SAML11AssertionType saml11Assertion = (SAML11AssertionType) session.getNote("SAML11");
            if (saml11Assertion == null) {
                SAML11ProtocolContext saml11Protocol = new SAML11ProtocolContext();
                saml11Protocol.setIssuerID(this.identityURL);
                SAML11SubjectType subject = new SAML11SubjectType();
                SAML11SubjectTypeChoice subjectChoice = new SAML11SubjectTypeChoice(new SAML11NameIdentifierType(
                        userPrincipal.getName()));
                subject.setChoice(subjectChoice);
                saml11Protocol.setSubjectType(subject);

                PicketLinkCoreSTS.instance().issueToken(saml11Protocol);
                saml11Assertion = saml11Protocol.getIssuedAssertion();
                session.setNote("SAML11", saml11Assertion);

                if (AssertionUtil.hasExpired(saml11Assertion)) {
                    saml11Protocol.setIssuedAssertion(saml11Assertion);
                    PicketLinkCoreSTS.instance().renewToken(saml11Protocol);
                    saml11Assertion = saml11Protocol.getIssuedAssertion();
                    session.setNote("SAML11", saml11Assertion);
                }
            }
            GenericPrincipal genericPrincipal = (GenericPrincipal) userPrincipal;
            String[] roles = genericPrincipal.getRoles();
            SAML11AttributeStatementType attributeStatement = this.createAttributeStatement(Arrays.asList(roles));
            saml11Assertion.add(attributeStatement);

            // Send it as SAMLResponse
            String id = IDGenerator.create("ID_");
            SAML11ResponseType saml11Response = new SAML11ResponseType(id, XMLTimeUtil.getIssueInstant());
            saml11Response.add(saml11Assertion);
            saml11Response.setStatus(SAML11StatusType.successType());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SAML11ResponseWriter writer = new SAML11ResponseWriter(StaxUtil.getXMLStreamWriter(baos));
            writer.write(saml11Response);

            Document samlResponse = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));

            WebRequestUtilHolder holder = webRequestUtil.getHolder();
            holder.setResponseDoc(samlResponse).setDestination(target).setRelayState("").setAreWeSendingRequest(false)
                    .setPrivateKey(null).setSupportSignature(false).setServletResponse(response);

            if (enableAudit) {
                PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                auditEvent.setType(PicketLinkAuditEventType.RESPONSE_TO_SP);
                auditEvent.setDestination(target);
                auditEvent.setWhoIsAuditing(getContext().getServletContext().getContextPath());
                auditHelper.audit(auditEvent);
            }
            webRequestUtil.send(holder);
        } catch (GeneralSecurityException e) {
            log.error("Exception handling saml 11 use case:", e);
            throw new ServletException();
        }
    }

    protected void processSAMLRequestMessage(IDPWebRequestUtil webRequestUtil, Request request, Response response)
            throws IOException {
        Principal userPrincipal = request.getPrincipal();
        Session session = request.getSessionInternal();
        SAMLDocumentHolder samlDocumentHolder = null;
        SAML2Object samlObject = null;

        Document samlResponse = null;
        boolean isErrorResponse = false;
        String destination = null;
        String destinationQueryStringWithSignature = null;

        Boolean requestedPostProfile = null;

        String samlRequestMessage = (String) session.getNote(GeneralConstants.SAML_REQUEST_KEY);

        String relayState = (String) session.getNote(GeneralConstants.RELAY_STATE);

        boolean willSendRequest = false;

        String referer = request.getHeader("Referer");

        cleanUpSessionNote(request);

        // Determine the transport mechanism
        boolean isSecure = request.isSecure();
        String loginType = determineLoginType(isSecure);

        try {
            samlDocumentHolder = webRequestUtil.getSAMLDocumentHolder(samlRequestMessage);
            samlObject = samlDocumentHolder.getSamlObject();

            if (!(samlObject instanceof RequestAbstractType)) {
                throw new RuntimeException(ErrorCodes.WRONG_TYPE + samlObject.getClass().getName());
            }

            // Get the SAML Request Message
            RequestAbstractType requestAbstractType = (RequestAbstractType) samlObject;
            String issuer = requestAbstractType.getIssuer().getValue();

            if (samlRequestMessage == null)
                throw new GeneralSecurityException(ErrorCodes.VALIDATION_CHECK_FAILED);

            IssuerInfoHolder idpIssuer = new IssuerInfoHolder(this.identityURL);
            ProtocolContext protocolContext = new HTTPContext(request, response, getContext().getServletContext());
            // Create the request/response
            SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext, idpIssuer.getIssuer(),
                    samlDocumentHolder, HANDLER_TYPE.IDP);
            saml2HandlerRequest.setRelayState(relayState);
            if (StringUtil.isNotNull(loginType)) {
                saml2HandlerRequest.addOption(GeneralConstants.LOGIN_TYPE, loginType);
            }

            String assertionID = (String) session.getSession().getAttribute(GeneralConstants.ASSERTION_ID);

            // Set the options on the handler request
            Map<String, Object> requestOptions = new HashMap<String, Object>();

            requestOptions.put(GeneralConstants.IGNORE_SIGNATURES, Boolean.FALSE);
            requestOptions.put(GeneralConstants.ROLE_GENERATOR, roleGenerator);
            requestOptions.put(GeneralConstants.ASSERTIONS_VALIDITY, this.assertionValidity);
            requestOptions.put(GeneralConstants.CONFIGURATION, this.idpConfiguration);
            requestOptions.put(GeneralConstants.SAML_IDP_STRICT_POST_BINDING, this.idpConfiguration.isStrictPostBinding());

            if (assertionID != null)
                requestOptions.put(GeneralConstants.ASSERTION_ID, assertionID);

            if (this.keyManager != null) {
                PublicKey validatingKey = getIssuerPublicKey(request, issuer);
                requestOptions.put(GeneralConstants.SENDER_PUBLIC_KEY, validatingKey);
                requestOptions.put(GeneralConstants.DECRYPTING_KEY, keyManager.getSigningKey());
            }

            Map<String, Object> attribs = this.attribManager.getAttributes(userPrincipal, attributeKeys);
            requestOptions.put(GeneralConstants.ATTRIBUTES, attribs);

            saml2HandlerRequest.setOptions(requestOptions);

            List<String> roles = roleGenerator.generateRoles(userPrincipal);
            session.getSession().setAttribute(GeneralConstants.ROLES_ID, roles);

            SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

            Set<SAML2Handler> handlers = chain.handlers();

            if (trace) {
                log.trace("Handlers are=" + handlers);
            }
            
            // the trusted domains is done by a handler
            // webRequestUtil.isTrusted(issuer);

            if (handlers != null) {
                try {
                    chainLock.lock();
                    for (SAML2Handler handler : handlers) {
                        handler.handleRequestType(saml2HandlerRequest, saml2HandlerResponse);
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
            log.error("Exception in processing request:", e);
            samlResponse = webRequestUtil.getErrorResponse(referer, status, this.identityURL, this.signOutgoingMessages);
            isErrorResponse = true;
        } finally {
            try {
                // if the destination is null, probably because some error occur during authentication, use the AuthnRequest
                // issuer as the destination
                if (destination == null && samlObject instanceof AuthnRequestType) {
                    AuthnRequestType authRequest = (AuthnRequestType) samlObject;

                    destination = authRequest.getIssuer().getValue();
                }

                boolean postProfile = webRequestUtil.hasSAMLRequestInPostProfile();

                if (postProfile)
                    recycle(response);

                WebRequestUtilHolder holder = webRequestUtil.getHolder();
                holder.setResponseDoc(samlResponse).setDestination(destination).setRelayState(relayState)
                        .setAreWeSendingRequest(willSendRequest).setPrivateKey(null).setSupportSignature(false)
                        .setErrorResponse(isErrorResponse).setServletResponse(response)
                        .setDestinationQueryStringWithSignature(destinationQueryStringWithSignature);

                holder.setStrictPostBinding(this.idpConfiguration.isStrictPostBinding());

                if (requestedPostProfile != null)
                    holder.setPostBindingRequested(requestedPostProfile);
                else
                    holder.setPostBindingRequested(postProfile);

                if (this.signOutgoingMessages) {
                    holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
                }

                if (enableAudit) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.RESPONSE_TO_SP);
                    auditEvent.setDestination(destination);
                    auditEvent.setWhoIsAuditing(getContext().getServletContext().getContextPath());
                    auditHelper.audit(auditEvent);
                }
                webRequestUtil.send(holder);
            } catch (ParsingException e) {
                if (trace)
                    log.trace("Parsing exception:", e);
            } catch (GeneralSecurityException e) {
                if (trace)
                    log.trace("Security Exception:", e);
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
     * @throws ProcessingException
     * @throws ConfigurationException
     */
    private PublicKey getIssuerPublicKey(Request request, String issuer) throws ConfigurationException, ProcessingException {
        String issuerHost = request.getRemoteAddr();

        try {
            issuerHost = new URL(issuer).getHost();
        } catch (MalformedURLException e) {
            if (trace) {
                log.warn("Token issuer is not a valid URL: " + issuer + ". Using the requester address instead.", e);
            }
        }

        PublicKey issuerPublicKey = CoreConfigUtil.getValidatingKey(keyManager, issuerHost);

        if (trace) {
            log.trace("Remote Host=" + request.getRemoteAddr());
            log.trace("Using Validating Alias=" + issuerHost + " to check signatures.");
        }

        return issuerPublicKey;
    }

    protected void processSAMLResponseMessage(IDPWebRequestUtil webRequestUtil, Request request, Response response)
            throws ServletException, IOException {
        Session session = request.getSessionInternal();
        SAMLDocumentHolder samlDocumentHolder = null;
        SAML2Object samlObject = null;

        Document samlResponse = null;
        boolean isErrorResponse = false;
        String destination = null;
        String destinationQueryStringWithSignature = null;

        boolean requestedPostProfile = false;

        // Get the SAML Response Message
        String samlResponseMessage = (String) session.getNote(GeneralConstants.SAML_RESPONSE_KEY);
        String relayState = (String) session.getNote(GeneralConstants.RELAY_STATE);

        boolean willSendRequest = false;

        String referer = request.getHeader("Referer");

        cleanUpSessionNote(request);

        try {
            samlDocumentHolder = webRequestUtil.getSAMLDocumentHolder(samlResponseMessage);
            samlObject = samlDocumentHolder.getSamlObject();

            if (!(samlObject instanceof StatusResponseType)) {
                throw new RuntimeException(ErrorCodes.WRONG_TYPE + samlObject.getClass().getName());
            }

            StatusResponseType statusResponseType = (StatusResponseType) samlObject;
            String issuer = statusResponseType.getIssuer().getValue();

            boolean isValid = samlResponseMessage != null;

            if (!isValid)
                throw new GeneralSecurityException(ErrorCodes.VALIDATION_CHECK_FAILED);

            IssuerInfoHolder idpIssuer = new IssuerInfoHolder(this.identityURL);
            ProtocolContext protocolContext = new HTTPContext(request, response, getContext().getServletContext());
            // Create the request/response
            SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext, idpIssuer.getIssuer(),
                    samlDocumentHolder, HANDLER_TYPE.IDP);
            Map<String, Object> options = new HashMap<String, Object>();

            if (signOutgoingMessages) {
                PublicKey publicKey = getIssuerPublicKey(request, issuer);
                options.put(GeneralConstants.SENDER_PUBLIC_KEY, publicKey);
            }

            options.put(GeneralConstants.SAML_IDP_STRICT_POST_BINDING, this.idpConfiguration.isStrictPostBinding());

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
            log.error("Exception in processing request:", e);
            samlResponse = webRequestUtil.getErrorResponse(referer, status, this.identityURL, this.signOutgoingMessages);
            isErrorResponse = true;
        } finally {
            try {
                boolean postProfile = webRequestUtil.hasSAMLRequestInPostProfile();
                if (postProfile)
                    recycle(response);

                WebRequestUtilHolder holder = webRequestUtil.getHolder();
                if (destination == null)
                    throw new ServletException(ErrorCodes.NULL_VALUE + "Destination");
                holder.setResponseDoc(samlResponse).setDestination(destination).setRelayState(relayState)
                        .setAreWeSendingRequest(willSendRequest).setPrivateKey(null).setSupportSignature(false)
                        .setErrorResponse(isErrorResponse).setServletResponse(response)
                        .setPostBindingRequested(requestedPostProfile)
                        .setDestinationQueryStringWithSignature(destinationQueryStringWithSignature);

                /*
                 * if (requestedPostProfile) holder.setPostBindingRequested(requestedPostProfile); else
                 * holder.setPostBindingRequested(postProfile);
                 */

                if (this.signOutgoingMessages) {
                    holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
                }

                holder.setStrictPostBinding(this.idpConfiguration.isStrictPostBinding());

                if (enableAudit) {
                    PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                    auditEvent.setType(PicketLinkAuditEventType.RESPONSE_TO_SP);
                    auditEvent.setWhoIsAuditing(getContext().getServletContext().getContextPath());
                    auditEvent.setDestination(destination);
                    auditHelper.audit(auditEvent);
                }
                webRequestUtil.send(holder);
            } catch (ParsingException e) {
                if (trace)
                    log.trace("Parsing exception:", e);
            } catch (GeneralSecurityException e) {
                if (trace)
                    log.trace("Security Exception:", e);
            }
        }
        return;
    }

    protected void cleanUpSessionNote(Request request) {
        Session session = request.getSessionInternal();
        /**
         * Since the container has finished the authentication, we can retrieve the original saml message as well as any relay
         * state from the SP
         */
        String samlRequestMessage = (String) session.getNote(GeneralConstants.SAML_REQUEST_KEY);

        String samlResponseMessage = (String) session.getNote(GeneralConstants.SAML_RESPONSE_KEY);
        String relayState = (String) session.getNote(GeneralConstants.RELAY_STATE);
        String signature = (String) session.getNote(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        String sigAlg = (String) session.getNote(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);

        if (trace) {
            StringBuilder builder = new StringBuilder();
            builder.append("Retrieved saml messages and relay state from session");
            builder.append("saml Request message=").append(samlRequestMessage);
            builder.append("::").append("SAMLResponseMessage=");
            builder.append(samlResponseMessage).append(":").append("relay state=").append(relayState);

            builder.append("Signature=").append(signature).append("::sigAlg=").append(sigAlg);
            log.trace(builder.toString());
        }

        if (isNotNull(samlRequestMessage))
            session.removeNote(GeneralConstants.SAML_REQUEST_KEY);
        if (isNotNull(samlResponseMessage))
            session.removeNote(GeneralConstants.SAML_RESPONSE_KEY);

        if (isNotNull(relayState))
            session.removeNote(GeneralConstants.RELAY_STATE);

        if (isNotNull(signature))
            session.removeNote(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        if (isNotNull(sigAlg))
            session.removeNote(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);
    }

    protected void sendErrorResponseToSP(String referrer, Response response, String relayState, IDPWebRequestUtil webRequestUtil)
            throws ServletException, IOException, ConfigurationException {
        if (trace)
            log.trace("About to send error response to SP:" + referrer);

        Document samlResponse = webRequestUtil.getErrorResponse(referrer, JBossSAMLURIConstants.STATUS_RESPONDER.get(),
                this.identityURL, this.signOutgoingMessages);
        try {

            boolean postProfile = webRequestUtil.hasSAMLRequestInPostProfile();
            if (postProfile)
                recycle(response);

            WebRequestUtilHolder holder = webRequestUtil.getHolder();
            holder.setResponseDoc(samlResponse).setDestination(referrer).setRelayState(relayState)
                    .setAreWeSendingRequest(false).setPrivateKey(null).setSupportSignature(false).setServletResponse(response);
            holder.setPostBindingRequested(postProfile);

            if (this.signOutgoingMessages) {
                holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
            }

            holder.setStrictPostBinding(this.idpConfiguration.isStrictPostBinding());

            if (enableAudit) {
                PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                auditEvent.setType(PicketLinkAuditEventType.ERROR_RESPONSE_TO_SP);
                auditEvent.setWhoIsAuditing(getContext().getServletContext().getContextPath());
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

    // ***************Lifecycle
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * Has this component been started yet?
     */
    private boolean started = false;

    /**
     * Add a lifecycle event listener to this component.
     * 
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    /**
     * Get the lifecycle listeners associated with this lifecycle. If this Lifecycle has no listeners registered, a zero-length
     * array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    /**
     * Remove a lifecycle event listener from this component.
     * 
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    /**
     * Prepare for the beginning of active use of the public methods of this component. This method should be called after
     * <code>configure()</code>, and before any of the public methods of the component are utilized.
     * 
     * @exception LifecycleException if this component detects a fatal error that prevents this component from being used
     */
    public void start() throws LifecycleException {
        // Validate and update our current component state
        if (started)
            throw new LifecycleException(ErrorCodes.IDP_WEBBROWSER_VALVE_ALREADY_STARTED);
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        SystemPropertiesUtil.ensure();

        initIDPConfiguration();
        initSTSConfiguration();
        initKeyManager();
        initHandlersChain();
        initIdentityServver();
        
        // Add some keys to the attibutes
        String[] ak = new String[] { "mail", "cn", "commonname", "givenname", "surname", "employeeType", "employeeNumber",
                "facsimileTelephoneNumber" };

        this.attributeKeys.addAll(Arrays.asList(ak));
    }

    /**
     * <p>
     * Initializes the {@link IdentityServer}.
     * </p>
     */
    private void initIdentityServver() {
        // The Identity Server on the servlet context gets set
        // in the implementation of IdentityServer
        // Create an Identity Server and set it on the context
        IdentityServer identityServer = (IdentityServer) getContext().getServletContext().getAttribute(
                GeneralConstants.IDENTITY_SERVER);
        if (identityServer == null) {
            identityServer = new IdentityServer();
            getContext().getServletContext().setAttribute(GeneralConstants.IDENTITY_SERVER, identityServer);
            if (StringUtil.isNotNull(this.identityParticipantStack)) {
                try {
                    Class<?> clazz = SecurityActions.loadClass(getClass(), this.identityParticipantStack);
                    if (clazz == null)
                        throw new ClassNotFoundException(ErrorCodes.CLASS_NOT_LOADED + this.identityParticipantStack);

                    identityServer.setStack((IdentityParticipantStack) clazz.newInstance());
                } catch (Exception e) {
                    log.error("Unable to set the Identity Participant Stack Class. Will just use the default", e);
                }
            }
        }
    }

    /**
     * <p>
     * Initialize the Handlers chain.
     * </p>
     * 
     * @throws LifecycleException
     */
    private void initHandlersChain() throws LifecycleException {
        Handlers handlers = null;
        
        try {
            if (picketLinkConfiguration != null) {
                handlers = picketLinkConfiguration.getHandlers();
            } else {
                // Get the handlers
                String handlerConfigFileName = GeneralConstants.HANDLER_CONFIG_FILE_LOCATION;
                handlers = ConfigurationUtil
                        .getHandlers(getContext().getServletContext().getResourceAsStream(handlerConfigFileName));
            }

            // Get the chain from config
            String handlerChainClass = handlers.getHandlerChainClass();
            
            if (StringUtil.isNullOrEmpty(handlerChainClass))
                chain = SAML2HandlerChainFactory.createChain();
            else {
                try {
                    chain = SAML2HandlerChainFactory.createChain(handlerChainClass);
                } catch (ProcessingException e1) {
                    throw new LifecycleException(e1);
                }
            }

            chain.addAll(HandlerUtil.getHandlers(handlers));

            Map<String, Object> chainConfigOptions = new HashMap<String, Object>();
            chainConfigOptions.put(GeneralConstants.ROLE_GENERATOR, roleGenerator);
            chainConfigOptions.put(GeneralConstants.CONFIGURATION, idpConfiguration);
            chainConfigOptions.put(GeneralConstants.CANONICALIZATION_METHOD, canonicalizationMethod);
            if (this.keyManager != null)
                chainConfigOptions.put(GeneralConstants.KEYPAIR, keyManager.getSigningKeyPair());

            SAML2HandlerChainConfig handlerChainConfig = new DefaultSAML2HandlerChainConfig(chainConfigOptions);

            Set<SAML2Handler> samlHandlers = chain.handlers();

            for (SAML2Handler handler : samlHandlers) {
                handler.initChainConfig(handlerChainConfig);
            }
        } catch (Exception e) {
            log.error("Exception dealing with handler configuration:", e);
            throw new LifecycleException(e.getLocalizedMessage());
        }
    }

    private void initKeyManager() throws LifecycleException {
        if (this.signOutgoingMessages) {
            KeyProviderType keyProvider = this.idpConfiguration.getKeyProvider();
            if (keyProvider == null)
                throw new LifecycleException(ErrorCodes.NULL_VALUE + "Key Provider is null for context=" + getContext().getName());

            try {
                this.keyManager = CoreConfigUtil.getTrustKeyManager(keyProvider);

                List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
                keyManager.setAuthProperties(authProperties);
                keyManager.setValidatingAlias(keyProvider.getValidatingAlias());
            } catch (Exception e) {
                log.error("Exception reading configuration:", e);
                throw new LifecycleException(e.getLocalizedMessage());
            }
            if (trace)
                log.trace("Key Provider=" + keyProvider.getClassName());
        }
    }

    /**
     * <p>
     * Initializes the IDP configuration.
     * </p>
     */
    private void initIDPConfiguration() {
        String configFile = GeneralConstants.CONFIG_FILE_LOCATION;
        InputStream is = getContext().getServletContext().getResourceAsStream(configFile);

        // Work on the IDP Configuration
        if (configProvider != null) {
            try {
                idpConfiguration = configProvider.getIDPConfiguration();

                // Additionally parse the config file
                if (is != null && configProvider instanceof AbstractSAMLConfigurationProvider) {
                    ((AbstractSAMLConfigurationProvider) configProvider).setConfigFile(is);
                }
            } catch (ProcessingException e) {
                throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + e.getLocalizedMessage());
            } catch (ParsingException e) {
                throw new RuntimeException(ErrorCodes.PARSING_ERROR + e.getLocalizedMessage());
            }
        }

        if (idpConfiguration == null) {
            if (is != null) {
                try {
                    picketLinkConfiguration = ConfigurationUtil.getConfiguration(is);
                    idpConfiguration = (IDPType) picketLinkConfiguration.getIdpOrSP();
                    enableAudit = picketLinkConfiguration.isEnableAudit();

                    if (enableAudit) {
                        String securityDomainName = PicketLinkAuditHelper.getSecurityDomainName(getContext().getServletContext());
                        auditHelper = new PicketLinkAuditHelper(securityDomainName);
                    }
                } catch (ParsingException e) {
                    if (trace)
                        log.trace(e);
                    throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION, e);
                } catch (ConfigurationException e) {
                    if (trace)
                        log.trace(e);
                    throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION, e);
                }
            }
            if (is == null) {
                // Try the older version
                is = getContext().getServletContext().getResourceAsStream(GeneralConstants.DEPRECATED_CONFIG_FILE_LOCATION);
                if (is == null)
                    throw new RuntimeException(ErrorCodes.IDP_WEBBROWSER_VALVE_CONF_FILE_MISSING + configFile);
                try {
                    idpConfiguration = ConfigurationUtil.getIDPConfiguration(is);
                } catch (ParsingException e) {
                    if (trace)
                        log.trace(e);
                    throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION, e);
                }
            }
        }
        
        try {
            this.identityURL = idpConfiguration.getIdentityURL();
            if (trace)
                log.trace("Identity Provider URL=" + this.identityURL);
            this.assertionValidity = idpConfiguration.getAssertionValidity();
            this.canonicalizationMethod = idpConfiguration.getCanonicalizationMethod();
            log.info("IDPWebBrowserSSOValve:: Setting the CanonicalizationMethod on XMLSignatureUtil::"
                    + canonicalizationMethod);
            XMLSignatureUtil.setCanonicalizationMethodType(canonicalizationMethod);

            // Get the attribute manager
            String attributeManager = idpConfiguration.getAttributeManager();
            if (attributeManager != null && !"".equals(attributeManager)) {
                Class<?> clazz = SecurityActions.loadClass(getClass(), attributeManager);
                if (clazz == null)
                    throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + attributeManager);
                AttributeManager delegate = (AttributeManager) clazz.newInstance();
                this.attribManager.setDelegate(delegate);
            }
        } catch (Exception e) {
            throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION, e);
        }
    }

    private Context getContext() {
        return (Context) getContainer();
    }

    /**
     * Initializes the STS configuration.
     */
    private void initSTSConfiguration() {
        // if the sts configuration is present in the picketlink.xml then load it.
        if (this.picketLinkConfiguration != null && this.picketLinkConfiguration.getStsType() != null) {
            PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
            sts.initialize(new PicketLinkSTSConfiguration(this.picketLinkConfiguration.getStsType()));
        } else {
            //Try to load from /WEB-INF/picketlink-sts.xml.
            
            // Ensure that the Core STS has the SAML20 Token Provider
            PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
            // Let us look for a file
            String configPath = getContext().getServletContext().getRealPath("/WEB-INF/picketlink-sts.xml");
            File stsTokenConfigFile = configPath != null ? new File(configPath) : null;

            if (stsTokenConfigFile == null || stsTokenConfigFile.exists() == false) {
                log.info("Did not find picketlink-sts.xml. We will install default configuration");
                sts.installDefaultConfiguration();
            } else
                sts.installDefaultConfiguration(stsTokenConfigFile.toURI().toString());
        }
    }

    /**
     * Gracefully terminate the active use of the public methods of this component. This method should be the last one called on
     * a given instance of this component.
     * 
     * @exception LifecycleException if this component detects a fatal error that needs to be reported
     */
    public void stop() throws LifecycleException {
        // Validate and update our current component state
        if (!started)
            throw new LifecycleException(ErrorCodes.IDP_WEBBROWSER_VALVE_NOT_STARTED);
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;
    }

    // Private Methods

    private void recycle(Response response) {
        /**
         * Since the container finished authentication, it will try to locate index.jsp or index.html. We need to recycle
         * whatever is in the response object such that we direct it to the html that is being created as part of the HTTP/POST
         * binding
         */
        response.recycle();
    }

    protected String determineLoginType(boolean isSecure) {
        String result = JBossSAMLURIConstants.AC_PASSWORD.get();
        LoginConfig loginConfig = getContext().getLoginConfig();
        if (loginConfig != null) {
            String auth = loginConfig.getAuthMethod();
            if (StringUtil.isNotNull(auth)) {
                if ("CLIENT-CERT".equals(auth))
                    result = JBossSAMLURIConstants.AC_TLS_CLIENT.get();
                else if (isSecure)
                    result = JBossSAMLURIConstants.AC_PASSWORD_PROTECTED_TRANSPORT.get();
            }
        }
        return result;
    }

    /**
     * Given a set of roles, create an attribute statement
     * 
     * @param roles
     * @return
     */
    private SAML11AttributeStatementType createAttributeStatement(List<String> roles) {
        SAML11AttributeStatementType attrStatement = new SAML11AttributeStatementType();
        for (String role : roles) {
            SAML11AttributeType attr = new SAML11AttributeType("Role", URI.create("urn:picketlink:role"));
            attr.add(role);
            attrStatement.add(attr);
        }
        return attrStatement;
    }
    
 // Set a list of attributes we are interested in separated by comma
    public void setAttributeList(String attribList) {
        if (StringUtil.isNotNull(attribList)) {
            this.attributeKeys.clear();
            this.attributeKeys.addAll(StringUtil.tokenize(attribList));
        }
    }

    public void setConfigProvider(String cp) {
        if (cp == null)
            throw new IllegalStateException(ErrorCodes.NULL_ARGUMENT + cp);
        Class<?> clazz = SecurityActions.loadClass(getClass(), cp);
        if (clazz == null)
            throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + cp);
        try {
            configProvider = (SAMLConfigurationProvider) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(ErrorCodes.CANNOT_CREATE_INSTANCE + cp + ":" + e.getMessage());
        }
    }

    public void setRoleGenerator(String rgName) {
        try {
            Class<?> clazz = SecurityActions.loadClass(getClass(), rgName);
            if (clazz == null)
                throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + rgName);
            roleGenerator = (RoleGenerator) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void setSamlHandlerChainClass(String samlHandlerChainClass) {
        log.warn("Option 'samlHandlerChainClass' is deprecated and should not be used. This configuration is now set in picketlink.xml.");
    }

    public void setIdentityParticipantStack(String fqn) {
        this.identityParticipantStack = fqn;
    }
    
    @Deprecated
    public void setStrictPostBinding(Boolean strictPostBinding) {
        log.warn("Option 'strictPostBinding' is deprecated and should not be used. This configuration is now set in picketlink.xml.");
    }

    @Deprecated
    public Boolean getIgnoreIncomingSignatures() {
        log.warn("Option 'ignoreIncomingSignatures' is deprecated and should not be used. Signatures are verified if "
                + "SAML2SignatureValidationHandler is available.");
        return false;
    }

    @Deprecated
    public void setIgnoreIncomingSignatures(Boolean ignoreIncomingSignature) {
        log.warn("Option 'ignoreIncomingSignatures' is deprecated and not used. Signatures are verified if "
                + "SAML2SignatureValidationHandler is available.");
    }

    /**
     * PLFED-248 Allows to validate the token's signature against the keystore using the token's issuer.
     */
    @Deprecated
    public void setValidatingAliasToTokenIssuer(Boolean validatingAliasToTokenIssuer) {
        log.warn("Option 'validatingAliasToTokenIssuer' is deprecated and not used. The IDP will always use the issuer host to validate signatures.");
    }

    /**
     * IDP should not do any attributes such as generation of roles etc
     * 
     * @param ignoreAttributes
     */
    public void setIgnoreAttributesGeneration(Boolean ignoreAttributes) {
        if (ignoreAttributes == Boolean.TRUE)
            this.attribManager = null;
    }

    @Deprecated
    public Boolean getSignOutgoingMessages() {
        log.warn("Option signOutgoingMessages is used for signing of error messages. Normal SAML messages are "
                + "signed by SAML2SignatureGenerationHandler.");
        return signOutgoingMessages;
    }

    @Deprecated
    public void setSignOutgoingMessages(Boolean signOutgoingMessages) {
        log.warn("Option signOutgoingMessages is used for signing of error messages. Normal SAML messages are "
                + "signed by SAML2SignatureGenerationHandler.");
        this.signOutgoingMessages = signOutgoingMessages;
    }

}