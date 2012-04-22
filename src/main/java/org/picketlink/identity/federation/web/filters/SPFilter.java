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
package org.picketlink.identity.federation.web.filters;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.exceptions.AssertionExpiredException;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.factories.SAML2HandlerChainFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler.HANDLER_TYPE;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest.GENERATE_REQUEST_TYPE;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.HandlerUtil;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.interfaces.IRoleValidator;
import org.picketlink.identity.federation.web.roles.DefaultRoleValidator;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A service provider filter for web container agnostic
 * providers
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public class SPFilter implements Filter
{
   private static Logger log = Logger.getLogger(SPFilter.class);

   private final boolean trace = log.isTraceEnabled();

   protected SPType spConfiguration = null;

   protected String configFile = GeneralConstants.CONFIG_FILE_LOCATION;

   protected String serviceURL = null;

   protected String identityURL = null;

   private TrustKeyManager keyManager;

   private ServletContext context = null;

   private transient SAML2HandlerChain chain = null;

   protected boolean ignoreSignatures = false;

   private IRoleValidator roleValidator = new DefaultRoleValidator();

   private String logOutPage = GeneralConstants.LOGOUT_PAGE_NAME;

   protected String canonicalizationMethod = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

   public void destroy()
   {
   }

   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
         throws IOException, ServletException
   {
      HttpServletRequest request = (HttpServletRequest) servletRequest;
      HttpServletResponse response = (HttpServletResponse) servletResponse;

      boolean postMethod = "POST".equalsIgnoreCase(request.getMethod());

      HttpSession session = request.getSession();

      Principal userPrincipal = (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);

      String samlRequest = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
      String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

      //Eagerly look for Global LogOut
      String gloStr = request.getParameter(GeneralConstants.GLOBAL_LOGOUT);
      boolean logOutRequest = isNotNull(gloStr) && "true".equalsIgnoreCase(gloStr);

      if (!postMethod && !logOutRequest)
      {
         //Check if we are already authenticated 
         if (userPrincipal != null)
         {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
         }

         //We need to send request to IDP
         if (userPrincipal == null)
         {
            String relayState = null;
            try
            {
               //TODO: use the handlers to generate the request
               AuthnRequestType authnRequest = createSAMLRequest(serviceURL, identityURL);
               sendRequestToIDP(authnRequest, relayState, response);
            }
            catch (Exception e)
            {
               throw new ServletException(e);
            }
            return;
         }
      }
      else
      {
         if (!isNotNull(samlRequest) && !isNotNull(samlResponse))
         {
            //Neither saml request nor response from IDP
            //So this is a user request

            //Ask the handler chain to generate the saml request
            Set<SAML2Handler> handlers = chain.handlers();

            IssuerInfoHolder holder = new IssuerInfoHolder(this.serviceURL);
            ProtocolContext protocolContext = new HTTPContext(request, response, context);
            //Create the request/response
            SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext,
                  holder.getIssuer(), null, HANDLER_TYPE.SP);

            SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

            saml2HandlerResponse.setDestination(identityURL);

            //Reset the state
            try
            {
               for (SAML2Handler handler : handlers)
               {
                  handler.reset();
                  if (saml2HandlerResponse.isInError())
                  {
                     response.sendError(saml2HandlerResponse.getErrorCode());
                     break;
                  }

                  if (logOutRequest)
                     saml2HandlerRequest.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.LOGOUT);
                  else
                     saml2HandlerRequest.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);
                  handler.generateSAMLRequest(saml2HandlerRequest, saml2HandlerResponse);
               }
            }
            catch (ProcessingException pe)
            {
               throw new RuntimeException(pe);
            }
            Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
            String relayState = saml2HandlerResponse.getRelayState();

            String destination = saml2HandlerResponse.getDestination();

            if (destination != null && samlResponseDocument != null)
            {
               try
               {
                  this.sendToDestination(samlResponseDocument, relayState, destination, response,
                        saml2HandlerResponse.getSendRequest());
               }
               catch (Exception e)
               {
                  if (trace)
                     log.trace("Exception:", e);
                  throw new ServletException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION + "Server Error");
               }
               return;
            }
         }

         //See if we got a response from IDP
         if (isNotNull(samlResponse))
         {
            boolean isValid = false;
            try
            {
               isValid = this.validate(request);
            }
            catch (Exception e)
            {
               throw new ServletException(e);
            }
            if (!isValid)
               throw new ServletException(ErrorCodes.VALIDATION_CHECK_FAILED + "Validity check failed");

            //deal with SAML response from IDP
            byte[] base64DecodedResponse = PostBindingUtil.base64Decode(samlResponse);
            InputStream is = new ByteArrayInputStream(base64DecodedResponse);

            //Are we going to send Request to IDP?
            boolean willSendRequest = true;

            try
            {
               SAML2Response saml2Response = new SAML2Response();

               SAML2Object samlObject = saml2Response.getSAML2ObjectFromStream(is);
               SAMLDocumentHolder documentHolder = saml2Response.getSamlDocumentHolder();

               if (!ignoreSignatures)
               {
                  if (!verifySignature(documentHolder))
                     throw new ServletException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Cannot verify sender");
               }

               Set<SAML2Handler> handlers = chain.handlers();
               IssuerInfoHolder holder = new IssuerInfoHolder(this.serviceURL);
               ProtocolContext protocolContext = new HTTPContext(request, response, context);
               //Create the request/response
               SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext,
                     holder.getIssuer(), documentHolder, HANDLER_TYPE.SP);
               if (keyManager != null)
                  saml2HandlerRequest.addOption(GeneralConstants.DECRYPTING_KEY, keyManager.getSigningKey());

               SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

               //Deal with handler chains
               for (SAML2Handler handler : handlers)
               {
                  if (saml2HandlerResponse.isInError())
                  {
                     response.sendError(saml2HandlerResponse.getErrorCode());
                     break;
                  }
                  if (samlObject instanceof RequestAbstractType)
                  {
                     handler.handleRequestType(saml2HandlerRequest, saml2HandlerResponse);
                     willSendRequest = false;
                  }
                  else
                  {
                     handler.handleStatusResponseType(saml2HandlerRequest, saml2HandlerResponse);
                  }
               }

               Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
               String relayState = saml2HandlerResponse.getRelayState();

               String destination = saml2HandlerResponse.getDestination();

               if (destination != null && samlResponseDocument != null)
               {
                  this.sendToDestination(samlResponseDocument, relayState, destination, response, willSendRequest);
                  return;
               }

               //See if the session has been invalidated
               try
               {
                  session.isNew();
               }
               catch (IllegalStateException ise)
               {
                  //we are invalidated.
                  RequestDispatcher dispatch = context.getRequestDispatcher(this.logOutPage);
                  if (dispatch == null)
                     log.error("Cannot dispatch to the logout page: no request dispatcher:" + this.logOutPage);
                  else
                     dispatch.forward(request, response);
                  return;
               }
               filterChain.doFilter(request, servletResponse);
            }
            catch (Exception e)
            {
               log.error("Server Exception:", e);
               throw new ServletException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION);
            }

         }

         if (isNotNull(samlRequest))
         {
            //we got a logout request

            //deal with SAML response from IDP
            byte[] base64DecodedRequest = PostBindingUtil.base64Decode(samlRequest);
            InputStream is = new ByteArrayInputStream(base64DecodedRequest);

            //Are we going to send Request to IDP?
            boolean willSendRequest = false;

            try
            {
               SAML2Request saml2Request = new SAML2Request();
               SAML2Object samlObject = saml2Request.getSAML2ObjectFromStream(is);
               SAMLDocumentHolder documentHolder = saml2Request.getSamlDocumentHolder();

               if (!ignoreSignatures)
               {
                  if (!verifySignature(documentHolder))
                     throw new ServletException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Cannot verify sender");
               }

               Set<SAML2Handler> handlers = chain.handlers();
               IssuerInfoHolder holder = new IssuerInfoHolder(this.serviceURL);
               ProtocolContext protocolContext = new HTTPContext(request, response, context);
               //Create the request/response
               SAML2HandlerRequest saml2HandlerRequest = new DefaultSAML2HandlerRequest(protocolContext,
                     holder.getIssuer(), documentHolder, HANDLER_TYPE.SP);
               if (keyManager != null)
                  saml2HandlerRequest.addOption(GeneralConstants.DECRYPTING_KEY, keyManager.getSigningKey());

               SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse();

               //Deal with handler chains
               for (SAML2Handler handler : handlers)
               {
                  if (saml2HandlerResponse.isInError())
                  {
                     response.sendError(saml2HandlerResponse.getErrorCode());
                     break;
                  }
                  if (samlObject instanceof RequestAbstractType)
                  {
                     handler.handleRequestType(saml2HandlerRequest, saml2HandlerResponse);
                     willSendRequest = false;
                  }
                  else
                  {
                     handler.handleStatusResponseType(saml2HandlerRequest, saml2HandlerResponse);
                  }
               }

               Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
               String relayState = saml2HandlerResponse.getRelayState();

               String destination = saml2HandlerResponse.getDestination();

               if (destination != null && samlResponseDocument != null)
               {
                  this.sendToDestination(samlResponseDocument, relayState, destination, response, willSendRequest);
                  return;
               }
            }
            catch (Exception e)
            {
               if (trace)
                  log.trace("Server Exception:", e);
               throw new ServletException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION + "Server Exception");
            }
         }
      }
   }

   public void init(FilterConfig filterConfig) throws ServletException
   {
      this.context = filterConfig.getServletContext();
      InputStream is = context.getResourceAsStream(configFile);
      if (is == null)
         throw new RuntimeException(ErrorCodes.SERVICE_PROVIDER_CONF_FILE_MISSING + configFile + " missing");
      try
      {
         spConfiguration = ConfigurationUtil.getSPConfiguration(is);
         this.identityURL = spConfiguration.getIdentityURL();
         this.serviceURL = spConfiguration.getServiceURL();
         this.canonicalizationMethod = spConfiguration.getCanonicalizationMethod();

         log.info("SPFilter:: Setting the CanonicalizationMethod on XMLSignatureUtil::" + canonicalizationMethod);
         XMLSignatureUtil.setCanonicalizationMethodType(canonicalizationMethod);

         log.trace("Identity Provider URL=" + this.identityURL);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      //Get the Role Validator if configured
      String roleValidatorName = filterConfig.getInitParameter(GeneralConstants.ROLE_VALIDATOR);
      if (roleValidatorName != null && !"".equals(roleValidatorName))
      {
         try
         {
            Class<?> clazz = SecurityActions.loadClass(getClass(), roleValidatorName);
            this.roleValidator = (IRoleValidator) clazz.newInstance();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }

      Map<String, String> options = new HashMap<String, String>();
      String roles = filterConfig.getInitParameter(GeneralConstants.ROLES);
      if (trace)
         log.trace("Found Roles in SPFilter config=" + roles);
      if (roles != null)
      {
         options.put("ROLES", roles);
      }
      this.roleValidator.intialize(options);

      String samlHandlerChainClass = filterConfig.getInitParameter("SAML_HANDLER_CHAIN_CLASS");

      //Get the chain from config 
      if (StringUtil.isNullOrEmpty(samlHandlerChainClass))
         chain = SAML2HandlerChainFactory.createChain();
      else
         try
         {
            chain = SAML2HandlerChainFactory.createChain(samlHandlerChainClass);
         }
         catch (ProcessingException e1)
         {
            throw new ServletException(e1);
         }
      try
      {
         //Get the handlers
         String handlerConfigFileName = GeneralConstants.HANDLER_CONFIG_FILE_LOCATION;
         Handlers handlers = ConfigurationUtil.getHandlers(context.getResourceAsStream(handlerConfigFileName));
         chain.addAll(HandlerUtil.getHandlers(handlers));

         Map<String, Object> chainConfigOptions = new HashMap<String, Object>();
         chainConfigOptions.put(GeneralConstants.CONFIGURATION, spConfiguration);
         chainConfigOptions.put(GeneralConstants.ROLE_VALIDATOR, roleValidator);
         chainConfigOptions.put(GeneralConstants.CANONICALIZATION_METHOD, canonicalizationMethod);

         SAML2HandlerChainConfig handlerChainConfig = new DefaultSAML2HandlerChainConfig(chainConfigOptions);
         Set<SAML2Handler> samlHandlers = chain.handlers();

         for (SAML2Handler handler : samlHandlers)
         {
            handler.initChainConfig(handlerChainConfig);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      String ignoreSigString = filterConfig.getInitParameter(GeneralConstants.IGNORE_SIGNATURES);
      if (ignoreSigString != null && !"".equals(ignoreSigString))
      {
         this.ignoreSignatures = Boolean.parseBoolean(ignoreSigString);
      }

      if (ignoreSignatures == false)
      {
         KeyProviderType keyProvider = this.spConfiguration.getKeyProvider();
         if (keyProvider == null)
            throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyProvider");
         try
         {
            String keyManagerClassName = keyProvider.getClassName();
            if (keyManagerClassName == null)
               throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyManager class name");

            Class<?> clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);
            this.keyManager = (TrustKeyManager) clazz.newInstance();

            List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
            keyManager.setAuthProperties(authProperties);

            keyManager.setValidatingAlias(keyProvider.getValidatingAlias());
         }
         catch (Exception e)
         {
            log.error("Exception reading configuration:", e);
            throw new RuntimeException(e.getLocalizedMessage());
         }
         log.trace("Key Provider=" + keyProvider.getClassName());
      }

      //see if a global logout page has been configured
      String gloPage = filterConfig.getInitParameter(GeneralConstants.LOGOUT_PAGE);
      if (gloPage != null && !"".equals(gloPage))
         this.logOutPage = gloPage;
   }

   /**
    * Create a SAML2 auth request
    * @param serviceURL URL of the service
    * @param identityURL URL of the identity provider
    * @return   
    * @throws ConfigurationException 
    */
   private AuthnRequestType createSAMLRequest(String serviceURL, String identityURL) throws ConfigurationException
   {
      if (serviceURL == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "serviceURL");
      if (identityURL == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "identityURL");

      SAML2Request saml2Request = new SAML2Request();
      String id = IDGenerator.create("ID_");
      return saml2Request.createAuthnRequestType(id, serviceURL, identityURL, serviceURL);
   }

   protected void sendRequestToIDP(AuthnRequestType authnRequest, String relayState, HttpServletResponse response)
         throws IOException, SAXException, GeneralSecurityException
   {
      SAML2Request saml2Request = new SAML2Request();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      saml2Request.marshall(authnRequest, baos);

      String samlMessage = PostBindingUtil.base64Encode(baos.toString());
      String destination = authnRequest.getDestination().toASCIIString();
      PostBindingUtil.sendPost(new DestinationInfoHolder(destination, samlMessage, relayState), response, true);
   }

   protected void sendToDestination(Document samlDocument, String relayState, String destination,
         HttpServletResponse response, boolean request) throws IOException, SAXException, GeneralSecurityException
   {
      if (!ignoreSignatures)
      {
         SAML2Signature samlSignature = new SAML2Signature();

         KeyPair keypair = keyManager.getSigningKeyPair();
         samlSignature.signSAMLDocument(samlDocument, keypair);
      }
      String samlMessage = PostBindingUtil.base64Encode(DocumentUtil.getDocumentAsString(samlDocument));
      PostBindingUtil.sendPost(new DestinationInfoHolder(destination, samlMessage, relayState), response, request);
   }

   protected boolean validate(HttpServletRequest request) throws IOException, GeneralSecurityException
   {
      return request.getParameter("SAMLResponse") != null;
   }

   protected boolean verifySignature(SAMLDocumentHolder samlDocumentHolder) throws IssuerNotTrustedException
   {
      Document samlResponse = samlDocumentHolder.getSamlDocument();
      SAML2Object samlObject = samlDocumentHolder.getSamlObject();

      String issuerID = null;
      if (samlObject instanceof StatusResponseType)
      {
         issuerID = ((StatusResponseType) samlObject).getIssuer().getValue();
      }
      else
      {
         issuerID = ((RequestAbstractType) samlObject).getIssuer().getValue();
      }

      if (issuerID == null)
         throw new IssuerNotTrustedException(ErrorCodes.NULL_VALUE + "IssuerID missing");

      URL issuerURL;
      try
      {
         issuerURL = new URL(issuerID);
      }
      catch (MalformedURLException e1)
      {
         throw new IssuerNotTrustedException(e1);
      }

      try
      {
         PublicKey publicKey = keyManager.getValidatingKey(issuerURL.getHost());
         log.trace("Going to verify signature in the saml response from IDP");
         boolean sigResult = XMLSignatureUtil.validate(samlResponse, publicKey);
         log.trace("Signature verification=" + sigResult);
         return sigResult;
      }
      catch (TrustKeyConfigurationException e)
      {
         log.error("Unable to verify signature", e);
      }
      catch (TrustKeyProcessingException e)
      {
         log.error("Unable to verify signature", e);
      }
      catch (MarshalException e)
      {
         log.error("Unable to verify signature", e);
      }
      catch (XMLSignatureException e)
      {
         log.error("Unable to verify signature", e);
      }
      return false;
   }

   protected void isTrusted(String issuer) throws IssuerNotTrustedException
   {
      try
      {
         URL url = new URL(issuer);
         String issuerDomain = url.getHost();
         TrustType idpTrust = spConfiguration.getTrust();
         if (idpTrust != null)
         {
            String domainsTrusted = idpTrust.getDomains();
            if (domainsTrusted.indexOf(issuerDomain) < 0)
               throw new IssuerNotTrustedException(issuer);
         }
      }
      catch (Exception e)
      {
         throw new IssuerNotTrustedException(e.getLocalizedMessage(), e);
      }
   }

   protected ResponseType decryptAssertion(ResponseType responseType)
   {
      throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "This filter does not handle encryption");
   }

   /**
    * Handle the SAMLResponse from the IDP
    * @param request entire request from IDP
    * @param responseType ResponseType that has been generated
    * @param serverEnvironment tomcat,jboss etc
    * @return   
    * @throws AssertionExpiredException 
    */
   public Principal handleSAMLResponse(HttpServletRequest request, ResponseType responseType)
         throws ConfigurationException, AssertionExpiredException
   {
      if (request == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "request");
      if (responseType == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "response type");

      StatusType statusType = responseType.getStatus();
      if (statusType == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + "Status Type from the IDP");

      String statusValue = statusType.getStatusCode().getValue().toASCIIString();
      if (JBossSAMLURIConstants.STATUS_SUCCESS.get().equals(statusValue) == false)
         throw new SecurityException(ErrorCodes.IDP_AUTH_FAILED + "IDP forbid the user");

      List<org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType> assertions = responseType
            .getAssertions();
      if (assertions.size() == 0)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "No assertions in reply from IDP");

      AssertionType assertion = assertions.get(0).getAssertion();
      //Check for validity of assertion
      boolean expiredAssertion = AssertionUtil.hasExpired(assertion);
      if (expiredAssertion)
         throw new AssertionExpiredException(ErrorCodes.EXPIRED_ASSERTION);

      SubjectType subject = assertion.getSubject();
      /*JAXBElement<NameIDType> jnameID = (JAXBElement<NameIDType>) subject.getContent().get(0);
      NameIDType nameID = jnameID.getValue();*/
      NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();

      final String userName = nameID.getValue();
      List<String> roles = new ArrayList<String>();

      //Let us get the roles
      AttributeStatementType attributeStatement = (AttributeStatementType) assertion.getStatements().iterator().next();
      List<ASTChoiceType> attList = attributeStatement.getAttributes();
      for (ASTChoiceType obj : attList)
      {
         AttributeType attr = obj.getAttribute();
         String roleName = (String) attr.getAttributeValue().get(0);
         roles.add(roleName);
      }

      Principal principal = new Principal()
      {
         public String getName()
         {
            return userName;
         }
      };

      //Validate the roles
      boolean validRole = roleValidator.userInRole(principal, roles);
      if (!validRole)
      {
         if (trace)
            log.trace("Invalid role:" + roles);
         principal = null;
      }
      return principal;
   }
}