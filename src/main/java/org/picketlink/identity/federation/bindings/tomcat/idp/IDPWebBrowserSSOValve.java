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
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

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
import org.picketlink.identity.federation.bindings.tomcat.TomcatRoleGenerator;
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
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssueInstantMissingException;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
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
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler.HANDLER_TYPE;
import org.picketlink.identity.federation.core.saml.v2.util.HandlerUtil;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil;
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
   private static Logger log =  Logger.getLogger(IDPWebBrowserSSOValve.class); 
   private boolean trace = log.isTraceEnabled();
   
   protected IDPType idpConfiguration = null;
   
   private RoleGenerator roleGenerator = new TomcatRoleGenerator();

   private long assertionValidity = 5000; // 5 seconds in miliseconds
   
   private String identityURL = null;
   
   private TrustKeyManager keyManager;
   
   private Boolean ignoreIncomingSignatures = true;

   private Boolean signOutgoingMessages = true;

   private transient DelegatedAttributeManager attribManager = new DelegatedAttributeManager();
   private List<String> attributeKeys = new ArrayList<String>();
   
   private transient SAML2HandlerChain chain = null;
   
   private Context context = null;
   
   private transient String samlHandlerChainClass = null;  
   
   /**
    * A Lock for Handler operations in the chain
    */
   private Lock chainLock = new ReentrantLock();
   
   //Set a list of attributes we are interested in separated by comma
   public void setAttributeList(String attribList)
   { 
      if(attribList != null && !"".equals(attribList))
      {
         this.attributeKeys.clear();
         StringTokenizer st = new StringTokenizer(attribList,",");
         while(st != null && st.hasMoreTokens())
         {
            this.attributeKeys.add(st.nextToken());
         }
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

   @Override
   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      String referer = request.getHeader("Referer"); 
      String relayState = request.getParameter(GeneralConstants.RELAY_STATE);

      if(isNotNull(relayState))
         relayState = RedirectBindingUtil.urlDecode(relayState);
      
      String samlRequestMessage = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
      String samlResponseMessage = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

      String signature = request.getParameter("Signature");
      String sigAlg = request.getParameter("SigAlg"); 
      
      boolean containsSAMLRequestMessage =  isNotNull(samlRequestMessage);
      boolean containsSAMLResponseMessage =  isNotNull(samlResponseMessage);
      
      Session session = request.getSessionInternal(); 
      
      if(containsSAMLRequestMessage || containsSAMLResponseMessage)
      {
         if(trace) log.trace("Storing the SAMLRequest/SAMLResponse and RelayState in session");
         if(isNotNull(samlRequestMessage))
           session.setNote(GeneralConstants.SAML_REQUEST_KEY, samlRequestMessage);
         if(isNotNull(samlResponseMessage))
            session.setNote(GeneralConstants.SAML_RESPONSE_KEY, samlResponseMessage); 
         if(isNotNull(relayState))
            session.setNote(GeneralConstants.RELAY_STATE, relayState.trim());
         if(isNotNull(signature))
            session.setNote("Signature", signature.trim());
         if(isNotNull(sigAlg))
            session.setNote("sigAlg", sigAlg.trim());
      } 
      
      //Lets check if the user has been authenticated
      Principal userPrincipal = request.getPrincipal();
      if(userPrincipal == null)
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
            if(trace)
               log.trace("Referer in finally block="+ referer + ":user principal=" + userPrincipal); 
         }
      }
      
      
      IDPWebRequestUtil webRequestUtil = new IDPWebRequestUtil(request, idpConfiguration, keyManager);
      webRequestUtil.setAttributeManager(this.attribManager);
      webRequestUtil.setAttributeKeys(attributeKeys);
      
      Document samlErrorResponse = null;
      //Look for unauthorized status
      if(response.getStatus() == HttpServletResponse.SC_FORBIDDEN)
      {
         try
         {
            samlErrorResponse =
              webRequestUtil.getErrorResponse(referer, 
                  JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                  this.identityURL, this.signOutgoingMessages); 
         
            if(this.signOutgoingMessages)
               webRequestUtil.send(samlErrorResponse, referer, relayState, response, true, 
                     this.keyManager.getSigningKey(), false);
            else
               webRequestUtil.send(samlErrorResponse, referer,relayState, response, false,null, false);
            
         } 
         catch (GeneralSecurityException e)
         {
            throw new ServletException(e);
         }  
         return;
      }  
      
      if(userPrincipal != null)
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
         
         if(trace) 
         {
            StringBuilder builder = new StringBuilder();
            builder.append("Retrieved saml messages and relay state from session");
            builder.append("saml Request message=").append(samlRequestMessage);
            builder.append("::").append("SAMLResponseMessage=");
            builder.append(samlResponseMessage).append(":").append("relay state=").append(relayState);
            
            builder.append("Signature=").append(signature).append("::sigAlg=").append(sigAlg);
            log.trace(builder.toString());
         }
         
         if(isNotNull(samlRequestMessage))
           session.removeNote(GeneralConstants.SAML_REQUEST_KEY);
         if(isNotNull(samlResponseMessage))
            session.removeNote(GeneralConstants.SAML_RESPONSE_KEY);
          
         if(isNotNull(relayState))
            session.removeNote(GeneralConstants.RELAY_STATE);
         
         if(isNotNull(signature))
            session.removeNote("Signature");
         if(isNotNull(sigAlg))
            session.removeNote("sigAlg");

         boolean willSendRequest = false;

         SAMLDocumentHolder samlDocumentHolder = null;
         SAML2Object samlObject = null;

         Document samlResponse = null;
         String destination = null;
         
         
         //Send valid saml response after processing the request
         if(samlRequestMessage != null)
         {
            //Get the SAML Request Message
            RequestAbstractType requestAbstractType =  null; 
            
               try
               {
                  samlDocumentHolder = webRequestUtil.getSAMLDocumentHolder(samlRequestMessage);
                  samlObject = (SAML2Object) samlDocumentHolder.getSamlObject();
                  
                  
                  boolean isPost = webRequestUtil.hasSAMLRequestInPostProfile();
                  boolean isValid = validate(request.getRemoteAddr(),
                        request.getQueryString(),
                        new SessionHolder(samlRequestMessage, signature, sigAlg), isPost);
                  
                  if(!isValid)
                     throw new GeneralSecurityException("Validation check failed");

                  String issuer = null;
                  IssuerInfoHolder idpIssuer = new IssuerInfoHolder(this.identityURL);
                  ProtocolContext protocolContext = new HTTPContext(request,response, context.getServletContext());
                  //Create the request/response
                  SAML2HandlerRequest saml2HandlerRequest = 
                     new DefaultSAML2HandlerRequest(protocolContext,
                           idpIssuer.getIssuer(), samlDocumentHolder, 
                           HANDLER_TYPE.IDP);
                  saml2HandlerRequest.setRelayState(relayState);
                  
                  //Set the options on the handler request
                  Map<String, Object> requestOptions = new HashMap<String, Object>();
                  requestOptions.put(GeneralConstants.ROLE_GENERATOR, roleGenerator);
                  requestOptions.put(GeneralConstants.ASSERTIONS_VALIDITY, this.assertionValidity);
                  requestOptions.put(GeneralConstants.CONFIGURATION, this.idpConfiguration);
                  
                  if(this.keyManager != null)
                  {
                     PublicKey validatingKey = CoreConfigUtil.getValidatingKey(keyManager, request.getRemoteAddr());
                     requestOptions.put(GeneralConstants.SENDER_PUBLIC_KEY, validatingKey);
                  }
                  
                  Map<String,Object> attribs  = this.attribManager.getAttributes(userPrincipal, attributeKeys);
                  requestOptions.put(GeneralConstants.ATTRIBUTES, attribs);
                  
                  saml2HandlerRequest.setOptions(requestOptions); 
                  
                  List<String> roles =  roleGenerator.generateRoles(userPrincipal); 
                  session.getSession().setAttribute(GeneralConstants.ROLES_ID, roles);
                  
                  SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse(); 

                  Set<SAML2Handler> handlers = chain.handlers();
                  
                  if(samlObject instanceof RequestAbstractType)
                  {
                     requestAbstractType = (RequestAbstractType) samlObject;
                     issuer = requestAbstractType.getIssuer().getValue();
                     webRequestUtil.isTrusted(issuer);
                     
                     if(handlers != null)
                     { 
                        try
                        {
                           chainLock.lock();
                           for(SAML2Handler handler: handlers)
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
                  
               }
               catch (IssuerNotTrustedException e)
               {
                  if(trace) log.trace("Exception in processing request:",e);
                   
                  samlResponse =
                        webRequestUtil.getErrorResponse(referer, 
                            JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get(), 
                            this.identityURL, this.signOutgoingMessages);  
               }
               catch (ParsingException e)
               {
                  if(trace) log.trace("Exception in processing request:",e);
                   
                  samlResponse =
                     webRequestUtil.getErrorResponse(referer, 
                         JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                         this.identityURL, this.signOutgoingMessages); 
               }
               catch (ConfigurationException e)
               {
                  if(trace) log.trace("Exception in processing request:",e);
                   
                  samlResponse =
                     webRequestUtil.getErrorResponse(referer, 
                         JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                         this.identityURL, this.signOutgoingMessages);
               }
               catch (IssueInstantMissingException e)
               {
                  if(trace) log.trace("Exception in processing request:",e); 
                  
                  samlResponse =
                     webRequestUtil.getErrorResponse(referer, 
                         JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                         this.identityURL, this.signOutgoingMessages); 
               } 
               catch(GeneralSecurityException e)
               {
                  if(trace) log.trace("Exception in processing request:", e);
                  
                  samlResponse =
                     webRequestUtil.getErrorResponse(referer, 
                         JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                         this.identityURL, this.signOutgoingMessages); 
               }
               catch(Exception e)
               {
                  if(trace) log.trace("Exception in processing request:",e);
                  
                  samlResponse =
                     webRequestUtil.getErrorResponse(referer, 
                         JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                         this.identityURL, this.signOutgoingMessages); 
               }
               finally
               {
                  try
                  {
                     if(webRequestUtil.hasSAMLRequestInPostProfile())
                        recycle(response);
                     
                     if(this.signOutgoingMessages)
                        webRequestUtil.send(samlResponse, destination,relayState, response, true, 
                              this.keyManager.getSigningKey(), willSendRequest);
                     else
                        webRequestUtil.send(samlResponse, destination, relayState, response, false,null, 
                              willSendRequest);
                  }
                  catch (ParsingException e)
                  {
                     if(trace) log.trace("Parsing exception:", e);
                  } 
                  catch (GeneralSecurityException e)
                  {
                     if(trace) log.trace("Security Exception:",e);
                  }
               } 
            return;
         }
         else if(isNotNull(samlResponseMessage))
         {
            StatusResponseType statusResponseType = null;
            try
            {
               samlDocumentHolder = webRequestUtil.getSAMLDocumentHolder(samlResponseMessage);
               samlObject = (SAML2Object) samlDocumentHolder.getSamlObject();
               
               boolean isPost = webRequestUtil.hasSAMLRequestInPostProfile();
               boolean isValid = validate(request.getRemoteAddr(),
                     request.getQueryString(),
                     new SessionHolder(samlResponseMessage, signature, sigAlg), isPost);
               
               if(!isValid)
                  throw new GeneralSecurityException("Validation check failed");

               String issuer = null;
               IssuerInfoHolder idpIssuer = new IssuerInfoHolder(this.identityURL);
               ProtocolContext protocolContext = new HTTPContext(request,response, context.getServletContext());
               //Create the request/response
               SAML2HandlerRequest saml2HandlerRequest = 
                  new DefaultSAML2HandlerRequest(protocolContext,
                        idpIssuer.getIssuer(), samlDocumentHolder, 
                        HANDLER_TYPE.IDP);
               saml2HandlerRequest.setRelayState(relayState);
               
               SAML2HandlerResponse saml2HandlerResponse = new DefaultSAML2HandlerResponse(); 

               Set<SAML2Handler> handlers = chain.handlers();
               
               if(samlObject instanceof StatusResponseType)
               {
                  statusResponseType = (StatusResponseType) samlObject;
                  issuer = statusResponseType.getIssuer().getValue();
                  webRequestUtil.isTrusted(issuer);
                  
                  if(handlers != null)
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
            }
            catch (IssuerNotTrustedException e)
            {
               if(trace) log.trace("Exception in processing request:",e);
                
               samlResponse =
                     webRequestUtil.getErrorResponse(referer, 
                         JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get(), 
                         this.identityURL, this.signOutgoingMessages);  
            }
            catch (ParsingException e)
            {
               if(trace) log.trace("Exception in processing request:",e);
                
               samlResponse =
                  webRequestUtil.getErrorResponse(referer, 
                      JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                      this.identityURL, this.signOutgoingMessages); 
            }
            catch (ConfigurationException e)
            {
               if(trace) log.trace("Exception in processing request:",e);
                
               samlResponse =
                  webRequestUtil.getErrorResponse(referer, 
                      JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                      this.identityURL, this.signOutgoingMessages);
            }
            catch (IssueInstantMissingException e)
            {
               if(trace) log.trace("Exception in processing request:",e); 
               
               samlResponse =
                  webRequestUtil.getErrorResponse(referer, 
                      JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                      this.identityURL, this.signOutgoingMessages); 
            } 
            catch(GeneralSecurityException e)
            {
               if(trace) log.trace("Exception in processing request:", e);
               
               samlResponse =
                  webRequestUtil.getErrorResponse(referer, 
                      JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                      this.identityURL, this.signOutgoingMessages); 
            }
            catch(Exception e)
            {
               if(trace) log.trace("Exception in processing request:",e);
               
               samlResponse =
                  webRequestUtil.getErrorResponse(referer, 
                      JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), 
                      this.identityURL, this.signOutgoingMessages); 
            }
            finally
            {
               try
               {
                  if(webRequestUtil.hasSAMLRequestInPostProfile())
                     recycle(response);
                  
                  if(this.signOutgoingMessages)
                     webRequestUtil.send(samlResponse, destination,relayState, response, true, 
                           this.keyManager.getSigningKey(), willSendRequest);
                  else
                     webRequestUtil.send(samlResponse, destination, relayState, response, false,null, 
                           willSendRequest);
               }
               catch (ParsingException e)
               {
                  if(trace) log.trace("Parsing exception:", e);
               } 
               catch (GeneralSecurityException e)
               {
                  if(trace) log.trace("Security Exception:",e);
               }
            } 
         return;
         }
         else
         {
            log.error("No SAML Request or Response Message");
            if(trace) log.trace("Referer="+referer);
            
            try
            {
               sendErrorResponseToSP(referer, response, relayState, webRequestUtil);
            }
            catch (ConfigurationException e)
            {
               if(trace) log.trace(e);
            } 
         } 
      } 
   }
   
   protected void sendErrorResponseToSP(String referrer, Response response, String relayState,
         IDPWebRequestUtil webRequestUtil) throws ServletException, IOException, ConfigurationException
   {
      if(trace) log.trace("About to send error response to SP:" + referrer);
      
      Document samlResponse =   
         webRequestUtil.getErrorResponse(referrer, JBossSAMLURIConstants.STATUS_RESPONDER.get(),
               this.identityURL, this.signOutgoingMessages);
      try
      {
         if(webRequestUtil.hasSAMLRequestInPostProfile())
            recycle(response);
         
         if(this.signOutgoingMessages)
            webRequestUtil.send(samlResponse, referrer, relayState, response, true, 
                  this.keyManager.getSigningKey(), false);
         else
            webRequestUtil.send(samlResponse, referrer, relayState, response, false,null, false);
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
   
   protected boolean validate(String remoteAddress, 
         String queryString,
         SessionHolder holder, boolean isPost) throws IOException, GeneralSecurityException
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
         if(sigValue == null)
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
           throw new LifecycleException
               ("IDPRedirectValve already Started");
       lifecycle.fireLifecycleEvent(START_EVENT, null);
       started = true;
       
       //Get the chain from config
       if(StringUtil.isNullOrEmpty(samlHandlerChainClass))
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
       if(is == null)
          throw new RuntimeException(configFile + " missing");
       try
       {
          idpConfiguration = ConfigurationUtil.getIDPConfiguration(is);
          this.identityURL = idpConfiguration.getIdentityURL(); 
          if(trace) log.trace("Identity Provider URL=" + this.identityURL); 
          this.assertionValidity = idpConfiguration.getAssertionValidity();
          //Get the attribute manager
          String attributeManager = idpConfiguration.getAttributeManager();
          if(attributeManager != null && !"".equals(attributeManager))
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
       
       if(this.signOutgoingMessages)
       {
          KeyProviderType keyProvider = this.idpConfiguration.getKeyProvider();
          if(keyProvider == null)
             throw new LifecycleException("Key Provider is null for context=" + context.getName());
          
          try
          {
             this.keyManager = CoreConfigUtil.getTrustKeyManager(keyProvider);
             keyManager.setAuthProperties(keyProvider.getAuth());
             keyManager.setValidatingAlias(keyProvider.getValidatingAlias());
          }
          catch(Exception e)
          {
             log.error("Exception reading configuration:",e);
             throw new LifecycleException(e.getLocalizedMessage());
          }
          if(trace) log.trace("Key Provider=" + keyProvider.getClassName()); 
       }
       
       try
       {
          //Get the handlers
          String handlerConfigFileName = GeneralConstants.HANDLER_CONFIG_FILE_LOCATION;
          handlers = ConfigurationUtil.getHandlers(context.getServletContext().getResourceAsStream(handlerConfigFileName));
          chain.addAll(HandlerUtil.getHandlers(handlers)); 
          
          Map<String, Object> chainConfigOptions = new HashMap<String, Object>();
          chainConfigOptions.put(GeneralConstants.ROLE_GENERATOR, roleGenerator);
          chainConfigOptions.put(GeneralConstants.CONFIGURATION, idpConfiguration);
          if(this.keyManager != null)
            chainConfigOptions.put(GeneralConstants.KEYPAIR, keyManager.getSigningKeyPair());
          
          SAML2HandlerChainConfig handlerChainConfig = new DefaultSAML2HandlerChainConfig(chainConfigOptions);
          Set<SAML2Handler> samlHandlers = chain.handlers();
          
          for(SAML2Handler handler: samlHandlers)
          {
             handler.initChainConfig(handlerChainConfig);
          }  
       }
       catch(Exception e)
       {
          log.error("Exception dealing with handler configuration:",e);
          throw new LifecycleException(e.getLocalizedMessage());
       }
       
       //Add some keys to the attibutes
       String[] ak = new String[] {"mail","cn","commonname","givenname",
             "surname","employeeType",
             "employeeNumber",
             "facsimileTelephoneNumber"};
       
       this.attributeKeys.addAll(Arrays.asList(ak));
       
       //The Identity Server on the servlet context gets set
       //in the implementation of IdentityServer
       //Create an Identity Server and set it on the context
       IdentityServer identityServer = (IdentityServer) context.getServletContext().getAttribute(GeneralConstants.IDENTITY_SERVER);
       if(identityServer == null)
       {
          identityServer = new IdentityServer();
          context.getServletContext().setAttribute(GeneralConstants.IDENTITY_SERVER, identityServer); 
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
           throw new LifecycleException
               ("IDPRedirectValve NotStarted");
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