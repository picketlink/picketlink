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
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.valves.ValveBase;
import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.bindings.tomcat.TomcatRoleGenerator;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.impl.DelegatedAttributeManager;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
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
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11NameIdentifierType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType.SAML11SubjectTypeChoice;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11ResponseType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusType;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.core.IdentityParticipantStack;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil.WebRequestUtilHolder;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.w3c.dom.Document;

/**
 * Generic Web Browser SSO valve for the IDP
 * 
 * Handles both the SAML Redirect as well as Post Bindings
 * 
 * Note: Most of the work is done by {@code IDPWebRequestUtil}
 * @author Anil.Saldhana@redhat.com
 * @since May 18, 2009
 */
public class IDPWebBrowserSSOValve extends ValveBase implements Lifecycle
{
   private static Logger log = Logger.getLogger(IDPWebBrowserSSOValve.class);

   private final boolean trace = log.isTraceEnabled();

   protected IDPType idpConfiguration = null;

   private RoleGenerator roleGenerator = new TomcatRoleGenerator();

   private long assertionValidity = 5000; // 5 seconds in miliseconds

   private String identityURL = null;

   private TrustKeyManager keyManager;

   private Boolean ignoreIncomingSignatures = false;

   private Boolean signOutgoingMessages = true;

   private transient DelegatedAttributeManager attribManager = new DelegatedAttributeManager();

   private final List<String> attributeKeys = new ArrayList<String>();

   private transient SAML2HandlerChain chain = null;

   private Context context = null;

   private transient String samlHandlerChainClass = null;

   protected String canonicalizationMethod = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

   /**
    * If the user wants to set a particular {@link IdentityParticipantStack}
    */
   protected String identityParticipantStack = null;

   /**
    * A Lock for Handler operations in the chain
    */
   private final Lock chainLock = new ReentrantLock();

   //Set a list of attributes we are interested in separated by comma
   public void setAttributeList(String attribList)
   {
      if (StringUtil.isNotNull(attribList))
      {
         this.attributeKeys.clear();
         this.attributeKeys.addAll(StringUtil.tokenize(attribList));
      }
   }

   public Boolean getIgnoreIncomingSignatures()
   {
      return ignoreIncomingSignatures;
   }

   public void setIgnoreIncomingSignatures(Boolean ignoreIncomingSignature)
   {
      this.ignoreIncomingSignatures = ignoreIncomingSignature;
   }

   /**
    * IDP should not do any attributes such as generation of roles etc
    * @param ignoreAttributes
    */
   public void setIgnoreAttributesGeneration(Boolean ignoreAttributes)
   {
      if (ignoreAttributes == Boolean.TRUE)
         this.attribManager = null;
   }

   public Boolean getSignOutgoingMessages()
   {
      return signOutgoingMessages;
   }

   public void setSignOutgoingMessages(Boolean signOutgoingMessages)
   {
      this.signOutgoingMessages = signOutgoingMessages;
   }

   public void setRoleGenerator(String rgName)
   {
      try
      {
         Class<?> clazz = SecurityActions.getContextClassLoader().loadClass(rgName);
         roleGenerator = (RoleGenerator) clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void setSamlHandlerChainClass(String samlHandlerChainClass)
   {
      this.samlHandlerChainClass = samlHandlerChainClass;
   }

   public void setIdentityParticipantStack(String fqn)
   {
      this.identityParticipantStack = fqn;
   }

   @Override
   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      String referer = request.getHeader("Referer");
      String relayState = request.getParameter(GeneralConstants.RELAY_STATE);

      if (isNotNull(relayState))
         relayState = RedirectBindingUtil.urlDecode(relayState);

      String samlRequestMessage = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
      String samlResponseMessage = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

      String signature = request.getParameter("Signature");
      String sigAlg = request.getParameter("SigAlg");

      boolean containsSAMLRequestMessage = isNotNull(samlRequestMessage);
      boolean containsSAMLResponseMessage = isNotNull(samlResponseMessage);

      Session session = request.getSessionInternal();

      if (containsSAMLRequestMessage || containsSAMLResponseMessage)
      {
         if (trace)
            log.trace("Storing the SAMLRequest/SAMLResponse and RelayState in session");
         if (isNotNull(samlRequestMessage))
            session.setNote(GeneralConstants.SAML_REQUEST_KEY, samlRequestMessage);
         if (isNotNull(samlResponseMessage))
            session.setNote(GeneralConstants.SAML_RESPONSE_KEY, samlResponseMessage);
         if (isNotNull(relayState))
            session.setNote(GeneralConstants.RELAY_STATE, relayState.trim());
         if (isNotNull(signature))
            session.setNote("Signature", signature.trim());
         if (isNotNull(sigAlg))
            session.setNote("sigAlg", sigAlg.trim());
      }

      //Lets check if the user has been authenticated
      Principal userPrincipal = request.getPrincipal();
      if (userPrincipal == null)
      {
         try
         {
            //Next in the invocation chain
            getNext().invoke(request, response);
         }
         finally
         {
            userPrincipal = request.getPrincipal();
            referer = request.getHeader("Referer");
            if (trace)
               log.trace("Referer in finally block=" + referer + ":user principal=" + userPrincipal);
         }
      }

      IDPWebRequestUtil webRequestUtil = new IDPWebRequestUtil(request, idpConfiguration, keyManager);

      Document samlErrorResponse = null;
      //Look for unauthorized status
      if (response.getStatus() == HttpServletResponse.SC_FORBIDDEN)
      {
         try
         {
            samlErrorResponse = webRequestUtil.getErrorResponse(referer,
                  JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), this.identityURL, this.signOutgoingMessages);

            WebRequestUtilHolder holder = webRequestUtil.getHolder();
            holder.setResponseDoc(samlErrorResponse).setDestination(referer).setRelayState(relayState)
                  .setAreWeSendingRequest(false).setPrivateKey(null).setSupportSignature(false)
                  .setServletResponse(response);
            holder.setPostBindingRequested(webRequestUtil.hasSAMLRequestInPostProfile());

            if (this.signOutgoingMessages)
            {
               holder.setSupportSignature(true).setPrivateKey(keyManager.getSigningKey());
               webRequestUtil.send(holder);
               //webRequestUtil.send(samlErrorResponse, referer, relayState, response, true, 
               //this.keyManager.getSigningKey(), false); 
            }
            webRequestUtil.send(holder);
         }
         catch (GeneralSecurityException e)
         {
            throw new ServletException(e);
         }
         return;
      }

      if (userPrincipal != null)
      {
         /**
          * Since the container has finished the authentication,
          * we can retrieve the original saml message as well as
          * any relay state from the SP
          */
         samlRequestMessage = (String) session.getNote(GeneralConstants.SAML_REQUEST_KEY);

         samlResponseMessage = (String) session.getNote(GeneralConstants.SAML_RESPONSE_KEY);
         relayState = (String) session.getNote(GeneralConstants.RELAY_STATE);
         signature = (String) session.getNote("Signature");
         sigAlg = (String) session.getNote("sigAlg");

         if (trace)
         {
            StringBuilder builder = new StringBuilder();
            builder.append("Retrieved saml messages and relay state from session");
            builder.append("saml Request message=").append(samlRequestMessage);
            builder.append("::").append("SAMLResponseMessage=");
            builder.append(samlResponseMessage).append(":").append("relay state=").append(relayState);

            builder.append("Signature=").append(signature).append("::sigAlg=").append(sigAlg);
            log.trace(builder.toString());
         }

         //Send valid saml response after processing the request
         if (samlRequestMessage != null)
         {
            processSAMLRequestMessage(webRequestUtil, request, response);
         }
         else if (isNotNull(samlResponseMessage))
         {
            processSAMLResponseMessage(webRequestUtil, request, response);
         }
         else
         {
            String target = request.getParameter(SAML11Constants.TARGET);
            if (isNotNull(target))
            {
               //We have SAML 1.1 IDP first scenario. Now we need to create a SAMLResponse and send back
               //to SP as per target
               handleSAML11(webRequestUtil, request, response);
            }
            else
            {
               if (trace)
                  log.trace("SAML 1.1::Proceeding to IDP index page");
               RequestDispatcher dispatch = context.getServletContext().getRequestDispatcher("/hosted/");
               try
               {
                  dispatch.forward(request, response);
               }
               catch (Exception e)
               {
                  //JBAS5.1 and 6 quirkiness
                  dispatch.forward(request.getRequest(), response);
               }
            }
         }
      }
   }

   protected void handleSAML11(IDPWebRequestUtil webRequestUtil, Request request, Response response)
         throws ServletException, IOException
   {
      try
      {
         Principal userPrincipal = request.getPrincipal();

         String target = request.getParameter(SAML11Constants.TARGET);

         Session session = request.getSessionInternal();
         SAML11AssertionType saml11Assertion = (SAML11AssertionType) session.getNote("SAML11");
         if (saml11Assertion == null)
         {
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

            if (AssertionUtil.hasExpired(saml11Assertion))
            {
               saml11Protocol.setIssuedAssertion(saml11Assertion);
               PicketLinkCoreSTS.instance().renewToken(saml11Protocol);
               saml11Assertion = saml11Protocol.getIssuedAssertion();
               session.setNote("SAML11", saml11Assertion);
            }
         }
         //Send it as SAMLResponse
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
         webRequestUtil.send(holder);
      }
      catch (GeneralSecurityException e)
      {
         log.error("Exception handling saml 11 use case:", e);
         throw new ServletException();
      }
   }

   protected void processSAMLRequestMessage(IDPWebRequestUtil webRequestUtil, Request request, Response response)
         throws IOException
   {
      Principal userPrincipal = request.getPrincipal();
      Session session = request.getSessionInternal();
      SAMLDocumentHolder samlDocumentHolder = null;
      SAML2Object samlObject = null;

      Document samlResponse = null;
      String destination = null;

      Boolean requestedPostProfile = null;

      //Get the SAML Request Message
      RequestAbstractType requestAbstractType = null;
      String samlRequestMessage = (String) session.getNote(GeneralConstants.SAML_REQUEST_KEY);

      String relayState = (String) session.getNote(GeneralConstants.RELAY_STATE);
      String signature = (String) session.getNote("Signature");
      String sigAlg = (String) session.getNote("sigAlg");

      boolean willSendRequest = false;

      String referer = request.getHeader("Referer");

      cleanUpSessionNote(request);

      try
      {
         samlDocumentHolder = webRequestUtil.getSAMLDocumentHolder(samlRequestMessage);
         samlObject = samlDocumentHolder.getSamlObject();

         boolean isPost = webRequestUtil.hasSAMLRequestInPostProfile();
         boolean isValid = validate(request.getRemoteAddr(), request.getQueryString(), new SessionHolder(
               samlRequestMessage, signature, sigAlg), isPost);

         if (!isValid)
            throw new GeneralSecurityException("Validation check failed");

         String issuer = null;
         IssuerInfoHolder idpIssuer = new IssuerInfoHolder(this.identityURL);
         ProtocolContext protocolContext = new HTTPContext(request, response, context.getServletContext());
         //Create the request/response
         SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext,
               idpIssuer.getIssuer(), samlDocumentHolder, HANDLER_TYPE.IDP);
         saml2HandlerRequest.setRelayState(relayState);

         String assertionID = (String) session.getSession().getAttribute(GeneralConstants.ASSERTION_ID);

         //Set the options on the handler request
         Map<String, Object> requestOptions = new HashMap<String, Object>();
         if (this.ignoreIncomingSignatures)
            requestOptions.put(GeneralConstants.IGNORE_SIGNATURES, Boolean.TRUE);
         requestOptions.put(GeneralConstants.ROLE_GENERATOR, roleGenerator);
         requestOptions.put(GeneralConstants.ASSERTIONS_VALIDITY, this.assertionValidity);
         requestOptions.put(GeneralConstants.CONFIGURATION, this.idpConfiguration);
         if (assertionID != null)
            requestOptions.put(GeneralConstants.ASSERTION_ID, assertionID);

         if (this.keyManager != null)
         {
            String remoteHost = request.getRemoteAddr();
            if (trace)
            {
               log.trace("Remote Host=" + remoteHost);
            }
            PublicKey validatingKey = CoreConfigUtil.getValidatingKey(keyManager, remoteHost);
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

         if (trace)
         {
            log.trace("Handlers are=" + handlers);
         }

         if (samlObject instanceof RequestAbstractType)
         {
            requestAbstractType = (RequestAbstractType) samlObject;
            issuer = requestAbstractType.getIssuer().getValue();
            webRequestUtil.isTrusted(issuer);

            if (handlers != null)
            {
               try
               {
                  chainLock.lock();
                  for (SAML2Handler handler : handlers)
                  {
                     handler.handleRequestType(saml2HandlerRequest, saml2HandlerResponse);
                     willSendRequest = saml2HandlerResponse.getSendRequest();
                  }
               }
               finally
               {
                  chainLock.unlock();
               }
            }
         }
         else
            throw new RuntimeException("Unknown type:" + samlObject.getClass().getName());

         samlResponse = saml2HandlerResponse.getResultingDocument();
         relayState = saml2HandlerResponse.getRelayState();

         destination = saml2HandlerResponse.getDestination();

         requestedPostProfile = saml2HandlerResponse.isPostBindingForResponse();
      }
      catch (Exception e)
      {
         String status = JBossSAMLURIConstants.STATUS_AUTHNFAILED.get();
         if (e instanceof IssuerNotTrustedException)
         {
            status = JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get();
         }
         log.error("Exception in processing request:", e);
         samlResponse = webRequestUtil.getErrorResponse(referer, status, this.identityURL, this.signOutgoingMessages);
      }
      finally
      {
         try
         {
            boolean postProfile = webRequestUtil.hasSAMLRequestInPostProfile();
            if (postProfile)
               recycle(response);

            WebRequestUtilHolder holder = webRequestUtil.getHolder();
            holder.setResponseDoc(samlResponse).setDestination(destination).setRelayState(relayState)
                  .setAreWeSendingRequest(willSendRequest).setPrivateKey(null).setSupportSignature(false)
                  .setServletResponse(response);

            if (requestedPostProfile != null)
               holder.setPostBindingRequested(requestedPostProfile);
            else
               holder.setPostBindingRequested(postProfile);

            if (this.signOutgoingMessages)
            {
               holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
            }

            webRequestUtil.send(holder);
         }
         catch (ParsingException e)
         {
            if (trace)
               log.trace("Parsing exception:", e);
         }
         catch (GeneralSecurityException e)
         {
            if (trace)
               log.trace("Security Exception:", e);
         }
      }
      return;
   }

   protected void processSAMLResponseMessage(IDPWebRequestUtil webRequestUtil, Request request, Response response)
         throws ServletException, IOException
   {
      Session session = request.getSessionInternal();
      SAMLDocumentHolder samlDocumentHolder = null;
      SAML2Object samlObject = null;

      Document samlResponse = null;
      String destination = null;

      Boolean requestedPostProfile = null;

      //Get the SAML Response Message 
      String samlResponseMessage = (String) session.getNote(GeneralConstants.SAML_RESPONSE_KEY);
      String relayState = (String) session.getNote(GeneralConstants.RELAY_STATE);
      String signature = (String) session.getNote("Signature");
      String sigAlg = (String) session.getNote("sigAlg");

      boolean willSendRequest = false;

      String referer = request.getHeader("Referer");

      cleanUpSessionNote(request);

      StatusResponseType statusResponseType = null;
      try
      {
         samlDocumentHolder = webRequestUtil.getSAMLDocumentHolder(samlResponseMessage);
         samlObject = samlDocumentHolder.getSamlObject();

         boolean isPost = webRequestUtil.hasSAMLRequestInPostProfile();
         boolean isValid = false;

         String remoteAddress = request.getRemoteAddr();

         if (isPost)
         {
            //Validate
            SAML2Signature samlSignature = new SAML2Signature();

            if (ignoreIncomingSignatures == false && signOutgoingMessages == true)
            {
               PublicKey publicKey = keyManager.getValidatingKey(remoteAddress);
               isValid = samlSignature.validate(samlDocumentHolder.getSamlDocument(), publicKey);
            }
            else
               isValid = true;
         }
         else
         {
            isValid = validate(remoteAddress, request.getQueryString(), new SessionHolder(samlResponseMessage,
                  signature, sigAlg), isPost);
         }

         if (!isValid)
            throw new GeneralSecurityException("Validation check failed");

         String issuer = null;
         IssuerInfoHolder idpIssuer = new IssuerInfoHolder(this.identityURL);
         ProtocolContext protocolContext = new HTTPContext(request, response, context.getServletContext());
         //Create the request/response
         SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext,
               idpIssuer.getIssuer(), samlDocumentHolder, HANDLER_TYPE.IDP);
         saml2HandlerRequest.setRelayState(relayState);

         SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

         Set<SAML2Handler> handlers = chain.handlers();

         if (samlObject instanceof StatusResponseType)
         {
            statusResponseType = (StatusResponseType) samlObject;
            issuer = statusResponseType.getIssuer().getValue();
            webRequestUtil.isTrusted(issuer);

            if (handlers != null)
            {
               try
               {
                  chainLock.lock();
                  for (SAML2Handler handler : handlers)
                  {
                     handler.reset();
                     handler.handleStatusResponseType(saml2HandlerRequest, saml2HandlerResponse);
                     willSendRequest = saml2HandlerResponse.getSendRequest();
                  }
               }
               finally
               {
                  chainLock.unlock();
               }
            }
         }
         else
            throw new RuntimeException("Unknown type:" + samlObject.getClass().getName());

         samlResponse = saml2HandlerResponse.getResultingDocument();
         relayState = saml2HandlerResponse.getRelayState();

         destination = saml2HandlerResponse.getDestination();
         requestedPostProfile = saml2HandlerResponse.isPostBindingForResponse();
      }
      catch (Exception e)
      {
         String status = JBossSAMLURIConstants.STATUS_AUTHNFAILED.get();
         if (e instanceof IssuerNotTrustedException)
         {
            status = JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get();
         }
         log.error("Exception in processing request:", e);
         samlResponse = webRequestUtil.getErrorResponse(referer, status, this.identityURL, this.signOutgoingMessages);
      }
      finally
      {
         try
         {
            boolean postProfile = webRequestUtil.hasSAMLRequestInPostProfile();
            if (postProfile)
               recycle(response);

            WebRequestUtilHolder holder = webRequestUtil.getHolder();
            if (destination == null)
               throw new ServletException("Destination is null");
            holder.setResponseDoc(samlResponse).setDestination(destination).setRelayState(relayState)
                  .setAreWeSendingRequest(willSendRequest).setPrivateKey(null).setSupportSignature(false)
                  .setServletResponse(response).setPostBindingRequested(requestedPostProfile);

            if (requestedPostProfile != null)
               holder.setPostBindingRequested(requestedPostProfile);
            else
               holder.setPostBindingRequested(postProfile);

            if (this.signOutgoingMessages)
            {
               holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
            }
            webRequestUtil.send(holder);
         }
         catch (ParsingException e)
         {
            if (trace)
               log.trace("Parsing exception:", e);
         }
         catch (GeneralSecurityException e)
         {
            if (trace)
               log.trace("Security Exception:", e);
         }
      }
      return;
   }

   protected void cleanUpSessionNote(Request request)
   {
      Session session = request.getSessionInternal();
      /**
       * Since the container has finished the authentication,
       * we can retrieve the original saml message as well as
       * any relay state from the SP
       */
      String samlRequestMessage = (String) session.getNote(GeneralConstants.SAML_REQUEST_KEY);

      String samlResponseMessage = (String) session.getNote(GeneralConstants.SAML_RESPONSE_KEY);
      String relayState = (String) session.getNote(GeneralConstants.RELAY_STATE);
      String signature = (String) session.getNote("Signature");
      String sigAlg = (String) session.getNote("sigAlg");

      if (trace)
      {
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
         session.removeNote("Signature");
      if (isNotNull(sigAlg))
         session.removeNote("sigAlg");
   }

   protected void sendErrorResponseToSP(String referrer, Response response, String relayState,
         IDPWebRequestUtil webRequestUtil) throws ServletException, IOException, ConfigurationException
   {
      if (trace)
         log.trace("About to send error response to SP:" + referrer);

      Document samlResponse = webRequestUtil.getErrorResponse(referrer, JBossSAMLURIConstants.STATUS_RESPONDER.get(),
            this.identityURL, this.signOutgoingMessages);
      try
      {

         boolean postProfile = webRequestUtil.hasSAMLRequestInPostProfile();
         if (postProfile)
            recycle(response);

         WebRequestUtilHolder holder = webRequestUtil.getHolder();
         holder.setResponseDoc(samlResponse).setDestination(referrer).setRelayState(relayState)
               .setAreWeSendingRequest(false).setPrivateKey(null).setSupportSignature(false)
               .setServletResponse(response);
         holder.setPostBindingRequested(postProfile);

         if (this.signOutgoingMessages)
         {
            holder.setPrivateKey(keyManager.getSigningKey()).setSupportSignature(true);
         }
         webRequestUtil.send(holder);
      }
      catch (ParsingException e1)
      {
         throw new ServletException(e1);
      }
      catch (GeneralSecurityException e)
      {
         throw new ServletException(e);
      }
   }

   protected boolean validate(String remoteAddress, String queryString, SessionHolder holder, boolean isPost)
         throws IOException, GeneralSecurityException
   {
      if (!isNotNull(holder.samlRequest))
      {
         return false;
      }

      if (!this.ignoreIncomingSignatures && !isPost)
      {
         String sig = holder.signature;
         if (!isNotNull(sig))
         {
            log.error("Signature received from SP is null:" + remoteAddress);
            return false;
         }

         //Check if there is a signature   
         byte[] sigValue = RedirectBindingSignatureUtil.getSignatureValueFromSignedURL(queryString);
         if (sigValue == null)
            return false;

         PublicKey validatingKey;
         try
         {
            validatingKey = keyManager.getValidatingKey(remoteAddress);
         }
         catch (TrustKeyConfigurationException e)
         {
            throw new GeneralSecurityException(e.getCause());
         }
         catch (TrustKeyProcessingException e)
         {
            throw new GeneralSecurityException(e.getCause());
         }

         return RedirectBindingSignatureUtil.validateSignature(queryString, validatingKey, sigValue);
      }
      else
      {
         //Post binding no signature verification. The SAML message signature is verified
         return true;
      }
   }

   //***************Lifecycle
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
   public void addLifecycleListener(LifecycleListener listener)
   {
      lifecycle.addLifecycleListener(listener);
   }

   /**
    * Get the lifecycle listeners associated with this lifecycle. If this
    * Lifecycle has no listeners registered, a zero-length array is returned.
    */
   public LifecycleListener[] findLifecycleListeners()
   {
      return lifecycle.findLifecycleListeners();
   }

   /**
    * Remove a lifecycle event listener from this component.
    *
    * @param listener The listener to add
    */
   public void removeLifecycleListener(LifecycleListener listener)
   {
      lifecycle.removeLifecycleListener(listener);
   }

   /**
    * Prepare for the beginning of active use of the public methods of this
    * component.  This method should be called after <code>configure()</code>,
    * and before any of the public methods of the component are utilized.
    *
    * @exception LifecycleException if this component detects a fatal error
    *  that prevents this component from being used
    */
   public void start() throws LifecycleException
   {
      Handlers handlers = null;

      // Validate and update our current component state
      if (started)
         throw new LifecycleException("IDPWebBrowserSSOValve already Started");
      lifecycle.fireLifecycleEvent(START_EVENT, null);
      started = true;

      SystemPropertiesUtil.ensure();

      //Get the chain from config
      if (StringUtil.isNullOrEmpty(samlHandlerChainClass))
         chain = SAML2HandlerChainFactory.createChain();
      else
         try
         {
            chain = SAML2HandlerChainFactory.createChain(this.samlHandlerChainClass);
         }
         catch (ProcessingException e1)
         {
            throw new LifecycleException(e1);
         }

      String configFile = GeneralConstants.CONFIG_FILE_LOCATION;

      context = (Context) getContainer();
      InputStream is = context.getServletContext().getResourceAsStream(configFile);
      if (is == null)
         throw new RuntimeException(configFile + " missing");
      try
      {
         idpConfiguration = ConfigurationUtil.getIDPConfiguration(is);
         this.identityURL = idpConfiguration.getIdentityURL();
         if (trace)
            log.trace("Identity Provider URL=" + this.identityURL);
         this.assertionValidity = idpConfiguration.getAssertionValidity();
         this.canonicalizationMethod = idpConfiguration.getCanonicalizationMethod();
         log.info("IDPWebBrowserSSOValve:: Setting the CanonicalizationMethod on XMLSignatureUtil::"
               + canonicalizationMethod);
         XMLSignatureUtil.setCanonicalizationMethodType(canonicalizationMethod);

         //Get the attribute manager
         String attributeManager = idpConfiguration.getAttributeManager();
         if (attributeManager != null && !"".equals(attributeManager))
         {
            ClassLoader tcl = SecurityActions.getContextClassLoader();
            AttributeManager delegate = (AttributeManager) tcl.loadClass(attributeManager).newInstance();
            this.attribManager.setDelegate(delegate);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      //Ensure that the Core STS has the SAML20 Token Provider
      PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
      //Let us look for a file
      String configPath = context.getServletContext().getRealPath("/WEB-INF/picketlink-sts.xml");
      File stsTokenConfigFile = configPath != null ? new File(configPath) : null;

      if (stsTokenConfigFile == null || stsTokenConfigFile.exists() == false)
      {
         log.info("Did not find picketlink-sts.xml. We will install default configuration");
         sts.installDefaultConfiguration();
      }
      else
         sts.installDefaultConfiguration(configPath);

      if (this.signOutgoingMessages)
      {
         KeyProviderType keyProvider = this.idpConfiguration.getKeyProvider();
         if (keyProvider == null)
            throw new LifecycleException("Key Provider is null for context=" + context.getName());

         try
         {
            this.keyManager = CoreConfigUtil.getTrustKeyManager(keyProvider);

            List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
            keyManager.setAuthProperties(authProperties);
            keyManager.setValidatingAlias(keyProvider.getValidatingAlias());
         }
         catch (Exception e)
         {
            log.error("Exception reading configuration:", e);
            throw new LifecycleException(e.getLocalizedMessage());
         }
         if (trace)
            log.trace("Key Provider=" + keyProvider.getClassName());
      }

      try
      {
         //Get the handlers
         String handlerConfigFileName = GeneralConstants.HANDLER_CONFIG_FILE_LOCATION;
         handlers = ConfigurationUtil.getHandlers(context.getServletContext()
               .getResourceAsStream(handlerConfigFileName));
         chain.addAll(HandlerUtil.getHandlers(handlers));

         Map<String, Object> chainConfigOptions = new HashMap<String, Object>();
         chainConfigOptions.put(GeneralConstants.ROLE_GENERATOR, roleGenerator);
         chainConfigOptions.put(GeneralConstants.CONFIGURATION, idpConfiguration);
         chainConfigOptions.put(GeneralConstants.CANONICALIZATION_METHOD, canonicalizationMethod);
         if (this.keyManager != null)
            chainConfigOptions.put(GeneralConstants.KEYPAIR, keyManager.getSigningKeyPair());

         SAML2HandlerChainConfig handlerChainConfig = new DefaultSAML2HandlerChainConfig(chainConfigOptions);

         Set<SAML2Handler> samlHandlers = chain.handlers();

         for (SAML2Handler handler : samlHandlers)
         {
            handler.initChainConfig(handlerChainConfig);
         }
      }
      catch (Exception e)
      {
         log.error("Exception dealing with handler configuration:", e);
         throw new LifecycleException(e.getLocalizedMessage());
      }

      //Add some keys to the attibutes
      String[] ak = new String[]
      {"mail", "cn", "commonname", "givenname", "surname", "employeeType", "employeeNumber", "facsimileTelephoneNumber"};

      this.attributeKeys.addAll(Arrays.asList(ak));

      //The Identity Server on the servlet context gets set
      //in the implementation of IdentityServer
      //Create an Identity Server and set it on the context
      IdentityServer identityServer = (IdentityServer) context.getServletContext().getAttribute(
            GeneralConstants.IDENTITY_SERVER);
      if (identityServer == null)
      {
         identityServer = new IdentityServer();
         context.getServletContext().setAttribute(GeneralConstants.IDENTITY_SERVER, identityServer);
         if (StringUtil.isNotNull(this.identityParticipantStack))
         {
            try
            {
               Class<?> stackClass = SecurityActions.getContextClassLoader().loadClass(this.identityParticipantStack);
               identityServer.setStack((IdentityParticipantStack) stackClass.newInstance());
            }
            catch (ClassNotFoundException e)
            {
               log.error("Unable to set the Identity Participant Stack Class. Will just use the default", e);
            }
            catch (InstantiationException e)
            {
               log.error("Unable to set the Identity Participant Stack Class. Will just use the default", e);
            }
            catch (IllegalAccessException e)
            {
               log.error("Unable to set the Identity Participant Stack Class. Will just use the default", e);
            }
         }
      }
   }

   /**
    * Gracefully terminate the active use of the public methods of this
    * component.  This method should be the last one called on a given
    * instance of this component.
    *
    * @exception LifecycleException if this component detects a fatal error
    *  that needs to be reported
    */
   public void stop() throws LifecycleException
   {
      // Validate and update our current component state
      if (!started)
         throw new LifecycleException("IDPWebBrowserSSOValve NotStarted");
      lifecycle.fireLifecycleEvent(STOP_EVENT, null);
      started = false;
   }

   //Private Methods 

   protected static class SessionHolder
   {
      String samlRequest;

      String signature;

      String sigAlg;

      public SessionHolder(String req, String sig, String alg)
      {
         this.samlRequest = req;
         this.signature = sig;
         this.sigAlg = alg;
      }
   }

   private void recycle(Response response)
   {
      /**
       * Since the container finished authentication, it will try to locate
       * index.jsp or index.html. We need to recycle whatever is in the 
       * response object such that we direct it to the html that is being
       * created as part of the HTTP/POST binding
       */
      response.recycle();
   }
}