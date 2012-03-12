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
package org.picketlink.identity.federation.bindings.tomcat.sp;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.bindings.tomcat.sp.holder.ServiceProviderSAMLContext;
import org.picketlink.identity.federation.bindings.util.ValveUtil;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.exceptions.AssertionExpiredException;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.process.ServiceProviderBaseProcessor;
import org.picketlink.identity.federation.web.process.ServiceProviderSAMLRequestProcessor;
import org.picketlink.identity.federation.web.process.ServiceProviderSAMLResponseProcessor;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil.RedirectBindingUtilDestHolder;
import org.picketlink.identity.federation.web.util.ServerDetector;
import org.w3c.dom.Document;

/**
 * Authenticator at the Service Provider
 * that handles HTTP/Redirect binding of SAML 2
 * but falls back on Form Authentication
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Dec 12, 2008
 */
public class SPRedirectFormAuthenticator extends BaseFormAuthenticator
{
   protected static Logger log = Logger.getLogger(SPRedirectFormAuthenticator.class);

   protected boolean jbossEnv = false;
   
   /**
    * The SAML Web Browser SSO Profile says that the IDP cannot send
    * response back in Redirect Binding.  The user should use this
    * parameter to adhere to that requirement.
    */
   protected boolean idpPostBinding = false;
   
   /**
    * Set the Authenticator to expect a post response from IDP
    * @param idpPostBinding
    */
   public void setIdpPostBinding(Boolean idpPostBinding)
   {
      this.idpPostBinding = idpPostBinding;
   }

   public SPRedirectFormAuthenticator()
   {
      super();
      ServerDetector detector = new ServerDetector();
      jbossEnv = detector.isJboss();
   }

   /**
    * Authenticate the request
    * @param request
    * @param response
    * @param config
    * @return
    * @throws IOException
    * @throws {@link RuntimeException} when the response is not of type catalina response object
    */
   public boolean authenticate(Request request, HttpServletResponse response, LoginConfig config) throws IOException
   {
      if (response instanceof Response)
      {
         Response catalinaResponse = (Response) response;
         return authenticate(request, catalinaResponse, config);
      }
      throw new RuntimeException(ErrorCodes.SERVICE_PROVIDER_NOT_CATALINA_RESPONSE);
   }

   @Override
   public boolean authenticate(Request request, Response response, LoginConfig loginConfig) throws IOException
   {
      Session session = request.getSessionInternal(true);

      //Eagerly look for Local LogOut
      String lloStr = request.getParameter(GeneralConstants.LOCAL_LOGOUT);
      boolean localLogout = isNotNull(lloStr) && "true".equalsIgnoreCase(lloStr);
      if (localLogout)
      {
         try
         {
            sendToLogoutPage(request, response, session);
         }
         catch (ServletException e)
         {
            log.error("Exception in logout::", e);
            throw new IOException(e);
         }
         return false;
      }

      //Eagerly look for Global LogOut
      String gloStr = request.getParameter(GeneralConstants.GLOBAL_LOGOUT);
      boolean logOutRequest = isNotNull(gloStr) && "true".equalsIgnoreCase(gloStr);

      String samlRequest = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
      String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

      Principal principal = request.getUserPrincipal();

      //If we have already authenticated the user and there is no request from IDP or logout from user
      if (principal != null && !(logOutRequest || isNotNull(samlRequest) || isNotNull(samlResponse)))
         return true;

      //General User Request
      if (!isNotNull(samlRequest) && !isNotNull(samlResponse))
      {
         return generalUserRequest(request, response, loginConfig);
      }

      //See if we got a response from IDP
      if (isNotNull(samlResponse))
      {
         return handleSAMLResponse(request, response, loginConfig);
      }

      //Handle SAML Requests from IDP
      if (isNotNull(samlRequest))
      {
         return handleSAMLRequest(request, response, loginConfig);
      }//end if

      return localAuthentication(request, response, loginConfig);
   }

   /**
    * Handle the SAML Request message from IDP
    * @param request
    * @param response
    * @param loginConfig
    * @return
    * @throws IOException
    */
   protected boolean handleSAMLRequest(Request request, Response response, LoginConfig loginConfig) throws IOException
   {
      String samlRequest = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
      HTTPContext httpContext = new HTTPContext(request, response, context.getServletContext());

      Set<SAML2Handler> handlers = chain.handlers();

      //we got a logout request
      try
      {
         ServiceProviderSAMLRequestProcessor requestProcessor = new ServiceProviderSAMLRequestProcessor(false,
               this.serviceURL);
         boolean result = requestProcessor.process(samlRequest, httpContext, handlers, chainLock);

         // If response is already commited, we need to stop with processing of HTTP request
         if (response.isCommitted() || response.isAppCommitted())
            return false;

         if (result)
            return result;
      }
      catch (Exception e)
      {
         log.error("Server Exception:", e);
         throw new IOException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION);
      }
      return localAuthentication(request, response, loginConfig);
   }

   /**
    * Handle the IDP Response
    * @param request
    * @param response
    * @param loginConfig
    * @return
    * @throws IOException
    */
   protected boolean handleSAMLResponse(Request request, Response response, LoginConfig loginConfig) throws IOException
   {
      Session session = request.getSessionInternal(true);

      String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

      Principal principal = request.getUserPrincipal();

      String relayState = request.getParameter(GeneralConstants.RELAY_STATE);
      HTTPContext httpContext = new HTTPContext(request, response, context.getServletContext());

      Set<SAML2Handler> handlers = chain.handlers();
      boolean isValid = false;
      try
      {
         isValid = this.validate(request);
      }
      catch (Exception e)
      {
         log.error("Exception:", e);
         throw new IOException();
      }
      if (!isValid)
         throw new IOException(ErrorCodes.VALIDATION_CHECK_FAILED);

      try
      {
         ServiceProviderSAMLResponseProcessor responseProcessor = new ServiceProviderSAMLResponseProcessor(false,
               serviceURL);
         if(idpPostBinding)
            responseProcessor.setIdpPostBinding(true);
         initializeSAMLProcessor(responseProcessor);

         SAML2HandlerResponse saml2HandlerResponse = null;

         try
         {
            saml2HandlerResponse = responseProcessor.process(samlResponse, httpContext, handlers, chainLock);
         }
         catch (ProcessingException pe)
         {
            Throwable te = pe.getCause();
            if (te instanceof AssertionExpiredException)
            {
               //We need to reissue redirect to IDP
               ServiceProviderBaseProcessor baseProcessor = new ServiceProviderBaseProcessor(false, serviceURL);
               initializeSAMLProcessor(baseProcessor);

               saml2HandlerResponse = baseProcessor.process(httpContext, handlers, chainLock);
               saml2HandlerResponse.setDestination(identityURL);
            }
            else
               throw pe;
         }
         Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
         relayState = saml2HandlerResponse.getRelayState();

         String destination = saml2HandlerResponse.getDestination();

         if (destination != null && samlResponseDocument != null)
         {
            boolean areWeSendingRequest = saml2HandlerResponse.getSendRequest();
            String samlMsg = DocumentUtil.getDocumentAsString(samlResponseDocument);

            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMsg.getBytes("UTF-8"));

            String destinationQuery = getDestinationQueryString(base64Request, relayState, areWeSendingRequest);

            RedirectBindingUtilDestHolder holder = new RedirectBindingUtilDestHolder();
            holder.setDestination(destination).setDestinationQueryString(destinationQuery);

            String destinationURL = RedirectBindingUtil.getDestinationURL(holder);

            HTTPRedirectUtil.sendRedirectForRequestor(destinationURL, response);
         }
         else
         {
            //See if the session has been invalidated 
            boolean sessionValidity = session.isValid();
            if (!sessionValidity)
            {
               sendToLogoutPage(request, response, session);
               return false;
            }

            //We got a response with the principal
            List<String> roles = saml2HandlerResponse.getRoles();
            if (principal == null)
               principal = (Principal) session.getSession().getAttribute(GeneralConstants.PRINCIPAL_ID);

            String username = principal.getName();
            String password = ServiceProviderSAMLContext.EMPTY_PASSWORD;

            //Map to JBoss specific principal
            if ((new ServerDetector()).isJboss() || jbossEnv)
            {
               //Push a context
               ServiceProviderSAMLContext.push(username, roles);
               principal = context.getRealm().authenticate(username, password);
               ServiceProviderSAMLContext.clear();
            }
            else
            {
               //tomcat env   
               SPUtil spUtil = new SPUtil();
               principal = spUtil.createGenericPrincipal(request, principal.getName(), roles);
            }

            session.setNote(Constants.SESS_USERNAME_NOTE, username);
            session.setNote(Constants.SESS_PASSWORD_NOTE, password);
            request.setUserPrincipal(principal);

            if (saveRestoreRequest)
            {
               this.restoreRequest(request, session);
            }
            register(request, response, principal, Constants.FORM_METHOD, username, password);

            return true;
         }
      }
      catch (ProcessingException pe)
      {
         Throwable t = pe.getCause();
         if (t != null && t instanceof AssertionExpiredException)
         {
            log.error("Assertion has expired. Asking IDP for reissue");
            //Just issue a fresh request back to IDP
            return generalUserRequest(request, response, loginConfig);
         }
         log.error("Server Exception:", pe);
         throw new IOException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION + pe.getLocalizedMessage());
      }
      catch (Exception e)
      {
         log.error("Server Exception:", e);
         throw new IOException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION + e.getLocalizedMessage());
      }
      return localAuthentication(request, response, loginConfig);
   }

   /**
    * Handle the user invocation for the first time
    * @param request
    * @param response
    * @param loginConfig
    * @return
    * @throws IOException
    */
   protected boolean generalUserRequest(Request request, Response response, LoginConfig loginConfig) throws IOException
   {
      Session session = request.getSessionInternal(true);
      HTTPContext httpContext = new HTTPContext(request, response, context.getServletContext());
      Set<SAML2Handler> handlers = chain.handlers();

      String relayState = request.getParameter(GeneralConstants.RELAY_STATE);

      //Neither saml request nor response from IDP
      //So this is a user request
      SAML2HandlerResponse saml2HandlerResponse = null;
      try
      {
         ServiceProviderBaseProcessor baseProcessor = new ServiceProviderBaseProcessor(false, serviceURL);

         initializeSAMLProcessor(baseProcessor);

         saml2HandlerResponse = baseProcessor.process(httpContext, handlers, chainLock);
         saml2HandlerResponse.setDestination(identityURL);
      }
      catch (ProcessingException pe)
      {
         log.error("Processing Exception:", pe);
         throw new RuntimeException(pe);
      }
      catch (ParsingException pe)
      {
         log.error("Parsing Exception:", pe);
         throw new RuntimeException(pe);
      }
      catch (ConfigurationException pe)
      {
         log.error("Config Exception:", pe);
         throw new RuntimeException(pe);
      }

      Document samlResponseDocument = saml2HandlerResponse.getResultingDocument();
      relayState = saml2HandlerResponse.getRelayState();

      String destination = saml2HandlerResponse.getDestination();

      if (destination != null && samlResponseDocument != null)
      {
         try
         {
            String samlMsg = DocumentUtil.getDocumentAsString(samlResponseDocument);
            if (trace)
               log.trace("SAML Document=" + samlMsg);

            boolean areWeSendingRequest = saml2HandlerResponse.getSendRequest();

            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMsg.getBytes("UTF-8"));

            String destinationQuery = getDestinationQueryString(base64Request, relayState, areWeSendingRequest);

            RedirectBindingUtilDestHolder holder = new RedirectBindingUtilDestHolder();
            holder.setDestination(destination).setDestinationQueryString(destinationQuery);

            String destinationURL = RedirectBindingUtil.getDestinationURL(holder);

            if (trace)
            {
               log.trace("URL used for sending:" + destinationURL);
            }

            if (saveRestoreRequest)
            {
               this.saveRequest(request, session);
            }

            HTTPRedirectUtil.sendRedirectForRequestor(destinationURL, response);
            return false;
         }
         catch (Exception e)
         {
            log.error("Server Exception:", e);
            throw new IOException(ErrorCodes.SERVICE_PROVIDER_SERVER_EXCEPTION);
         }
      }
      return localAuthentication(request, response, loginConfig);
   }

   protected String createSAMLRequestMessage(String relayState, Response response) throws ServletException,
         ConfigurationException, IOException, ProcessingException
   {
      //create a saml request
      if (this.serviceURL == null)
         throw new ServletException(ErrorCodes.NULL_VALUE + "serviceURL");

      SAML2Request saml2Request = new SAML2Request();

      SPUtil spUtil = new SPUtil();
      AuthnRequestType authnRequest = spUtil.createSAMLRequest(serviceURL, identityURL);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      saml2Request.marshall(authnRequest, baos);

      String base64Request = RedirectBindingUtil.deflateBase64URLEncode(baos.toByteArray());
      String destination = authnRequest.getDestination().toASCIIString();

      String destinationQueryString = getDestinationQueryString(base64Request, relayState, true);

      RedirectBindingUtilDestHolder holder = new RedirectBindingUtilDestHolder();
      holder.setDestinationQueryString(destinationQueryString).setDestination(destination);
      return RedirectBindingUtil.getDestinationURL(holder);
   }

   @Override
   protected String getBinding()
   {
      return JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.get();
   }

   protected String getDestinationQueryString(String urlEncodedRequest, String urlEncodedRelayState, boolean sendRequest)
   {
      return RedirectBindingUtil.getDestinationQueryString(urlEncodedRequest, urlEncodedRelayState, sendRequest);
   }

   protected void isTrusted(String issuer) throws IssuerNotTrustedException
   {
      try
      {
         String issuerDomain = ValveUtil.getDomain(issuer);
         TrustType spTrust = spConfiguration.getTrust();
         if (spTrust != null)
         {
            String domainsTrusted = spTrust.getDomains();
            if (trace)
               log.trace("Domains that SP trusts=" + domainsTrusted + " and issuer domain=" + issuerDomain);
            if (domainsTrusted.indexOf(issuerDomain) < 0)
            {
               //Let us do string parts checking
               StringTokenizer st = new StringTokenizer(domainsTrusted, ",");
               while (st != null && st.hasMoreTokens())
               {
                  String uriBit = st.nextToken();
                  if (trace)
                     log.trace("Matching uri bit=" + uriBit);
                  if (issuerDomain.indexOf(uriBit) > 0)
                  {
                     if (trace)
                        log.trace("Matched " + uriBit + " trust for " + issuerDomain);
                     return;
                  }
               }
               throw new IssuerNotTrustedException(issuer);
            }
         }
      }
      catch (Exception e)
      {
         throw new IssuerNotTrustedException(e.getLocalizedMessage(), e);
      }
   }

   /**
    * Initialize the {@code ServiceProviderBaseProcessor}
    * @param processor
    */
   protected void initializeSAMLProcessor(ServiceProviderBaseProcessor processor)
   {
      if (issuerID != null)
         processor.setIssuer(issuerID);

      processor.setConfiguration(spConfiguration);
   }

   /**
    * Subclasses should provide the implementation
    * @param responseType ResponseType that contains the encrypted assertion
    * @return response type with the decrypted assertion
    */
   protected ResponseType decryptAssertion(ResponseType responseType) throws IOException, GeneralSecurityException,
         ConfigurationException, ParsingException
   {
      throw new RuntimeException(ErrorCodes.AUTHENTICATOR_DOES_NOT_HANDLE_ENC);
   }
}