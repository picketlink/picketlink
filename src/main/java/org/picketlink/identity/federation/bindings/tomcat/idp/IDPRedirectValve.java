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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.valves.ValveBase;
import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.bindings.tomcat.TomcatRoleGenerator;
import org.picketlink.identity.federation.bindings.util.ValveUtil;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssueInstantMissingException;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.xml.sax.SAXException;

/**
 * Valve at the IDP that supports the HTTP/Redirect Binding
 * @author Anil.Saldhana@redhat.com
 * @since Dec 9, 2008
 */
public class IDPRedirectValve extends ValveBase implements Lifecycle
{ 
   private static Logger log =  Logger.getLogger(IDPRedirectValve.class); 
   private boolean trace = log.isTraceEnabled();
   
   protected IDPType idpConfiguration = null;
   
   private RoleGenerator rg = new TomcatRoleGenerator();

   private long assertionValidity = 5000; // 5 seconds in miliseconds
   
   private String identityURL = null; 
   
   public IDPRedirectValve()
   {
      super();
   }
   
   public void setRoleGenerator(String rgName)
   {
      try
      {
         Class<?> clazz = SecurityActions.getContextClassLoader().loadClass(rgName);
         rg = (RoleGenerator) clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      } 
   }

   @Override
   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      boolean containsSAMLRequestMessage = this.hasSAMLRequestMessage(request); 
      
      //Lets check if the user has been authenticated
      Principal userPrincipal = request.getPrincipal();
      if(userPrincipal == null)
      {
         //Send it for user authentication
         try
         {
            //Next in the invocation chain
            getNext().invoke(request, response);
         }
         finally
         { 
            String referer = request.getHeader("Referer");
            
            if(response.getStatus() == HttpServletResponse.SC_FORBIDDEN)
            {
               ResponseType errorResponseType = this.getErrorResponse(referer, JBossSAMLURIConstants.STATUS_AUTHNFAILED.get()); 
               try
               {
                  send(errorResponseType, 
                        request.getParameter(GeneralConstants.RELAY_STATE), response);
               }
               catch (ParsingException e)
               {
                 log.error(e);
               }
               catch (ProcessingException e)
               {
                  log.error(e);
               }  
               return;
            } 

            //User is authenticated as we are on the return path
            userPrincipal = request.getPrincipal();
            if(userPrincipal != null)
            {
               //Send valid saml response after processing the request
               if(containsSAMLRequestMessage)
               {
                  RequestAbstractType requestAbstractType =  null;
                  try
                  {
                     requestAbstractType = getSAMLRequest(request); 
                     boolean isValid = this.validate(request);
                     if(!isValid)
                        throw new GeneralSecurityException("Validity Checks Failed");
                     
                     this.isTrusted(requestAbstractType.getIssuer().getValue());
                     
                     ResponseType responseType = this.getResponse(request, userPrincipal);
                     send(responseType, request.getParameter(GeneralConstants.RELAY_STATE), response); 
                  }
                  catch (Exception e)
                  { 
                     log.error("Exception:" ,e); 
                     if(requestAbstractType != null)
                        referer = requestAbstractType.getIssuer().getValue();
                     ResponseType errorResponseType = this.getErrorResponse(referer, JBossSAMLURIConstants.STATUS_RESPONDER.get());
                     try
                     {
                        send(errorResponseType, request.getParameter(GeneralConstants.RELAY_STATE), response);
                     }
                     catch (ParsingException e1)
                     {
                        log.error(e1);
                     }
                     catch (ProcessingException e1)
                     {
                        log.error(e1);
                     }
                  } 
               }
               else
               {
                  log.error("No SAML Request Message");
                  if(trace)
                     log.trace("Referer="+referer);
                  throw new ServletException("No SAML Request Message");                
               }
            }
         }
      }   
   }
   
   /**
    * Verify that the issuer is trusted
    * @param issuer
    * @throws IssuerNotTrustedException
    */
   protected void isTrusted(String issuer) throws IssuerNotTrustedException
   {
      try
      {
         String issuerDomain = ValveUtil.getDomain(issuer);
         TrustType idpTrust =  idpConfiguration.getTrust();
         if(idpTrust != null)
         {
            String domainsTrusted = idpTrust.getDomains();
            if(domainsTrusted.indexOf(issuerDomain) < 0)
               throw new IssuerNotTrustedException(issuer); 
         }
      }
      catch (Exception e)
      {
         throw new IssuerNotTrustedException(e.getLocalizedMessage(),e);
      }
   }
   
   protected void send(ResponseType responseType, String relayState, Response response) 
   throws ParsingException, ProcessingException 
   {
      try
      {
         SAML2Response saml2Response = new SAML2Response();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            saml2Response.marshall(responseType, baos);

            String urlEncodedResponse = RedirectBindingUtil.deflateBase64URLEncode(baos.toByteArray());

            String destination = responseType.getDestination();
            if(trace) log.trace("IDP:Destination=" + destination);
             
            if(isNotNull(relayState))
               relayState = RedirectBindingUtil.urlEncode(relayState);
            
            String finalDest = destination + this.getDestination(urlEncodedResponse, relayState);
            HTTPRedirectUtil.sendRedirectForResponder(finalDest, response);
      }
      catch (JAXBException e)
      {
         throw new ParsingException(e);
      }
      catch (SAXException e)
      {
         throw new ParsingException(e);
      }
      catch (IOException e)
      {
         throw new ProcessingException(e);
      }   
   }
   
   /**
    * Generate a Destination URL for the HTTPRedirect binding
    * with the saml response and relay state
    * @param urlEncodedResponse
    * @param urlEncodedRelayState
    * @return
    */
   protected String getDestination(String urlEncodedResponse, String urlEncodedRelayState)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("?SAMLResponse=").append(urlEncodedResponse);
      if(isNotNull(urlEncodedRelayState))
         sb.append("&RelayState=").append(urlEncodedRelayState);
      return sb.toString();
   }

   /**
    * Validate the incoming Request
    * @param request
    * @return 
    */
   protected boolean validate(Request request) throws IOException,GeneralSecurityException
   {
     return this.hasSAMLRequestMessage(request);     
   }
   
   private boolean hasSAMLRequestMessage(Request request)
   {
      return request.getParameter(GeneralConstants.SAML_REQUEST_KEY) != null;
   }
   
   private RequestAbstractType getSAMLRequest(Request request) 
   throws ParsingException, ConfigurationException, ProcessingException
   {
      String samlMessage = getSAMLMessage(request);
      InputStream is = RedirectBindingUtil.base64DeflateDecode(samlMessage); 
      SAML2Request saml2Request = new SAML2Request();  
      return saml2Request.getRequestType(is); 
   }

    
   /**
    * Create a response type
    * @param request
    * @param userPrincipal
    * @return
    * @throws ParsingException  
    * @throws ConfigurationException 
    * @throws ProcessingException 
    */
   protected ResponseType getResponse(Request request, Principal userPrincipal) 
   throws ParsingException, ConfigurationException, ProcessingException
   {
      ResponseType responseType = null;

      String samlMessage = getSAMLMessage(request);
      InputStream is = RedirectBindingUtil.base64DeflateDecode(samlMessage); 
      SAML2Request saml2Request = new SAML2Request();
      
      AuthnRequestType authnRequestType = null;
      try
      {
         authnRequestType = saml2Request.getAuthnRequestType(is);
      }
      catch (JAXBException e2)
      {
         throw new ParsingException(e2);
      }
      catch (SAXException e2)
      {
         throw new ParsingException(e2);
      }
      if(authnRequestType == null)
         throw new IllegalStateException("AuthnRequest is null"); 

      if(log.isTraceEnabled())
      {
         StringWriter sw = new StringWriter();
         try
         {
            saml2Request.marshall(authnRequestType, sw);
         }
         catch (SAXException e)
         {
            log.trace(e);
         }
         catch (JAXBException e)
         {
            log.trace(e);
         }
         log.trace("IDPRedirectValve::AuthnRequest="+sw.toString()); 
      }
      SAML2Response saml2Response = new SAML2Response();
            
      //Create a response type
      String id = IDGenerator.create("ID_");

      IssuerInfoHolder issuerHolder = new IssuerInfoHolder(this.identityURL); 
      issuerHolder.setStatusCode(JBossSAMLURIConstants.STATUS_SUCCESS.get());

      IDPInfoHolder idp = new IDPInfoHolder();
      idp.setNameIDFormatValue(userPrincipal.getName());
      idp.setNameIDFormat(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());

      SPInfoHolder sp = new SPInfoHolder();
      sp.setResponseDestinationURI(authnRequestType.getAssertionConsumerServiceURL());
      responseType = saml2Response.createResponseType(id, sp, idp, issuerHolder);
      //Add information on the roles
      List<String> roles = rg.generateRoles(userPrincipal);
      AssertionType assertion = (AssertionType) responseType.getAssertionOrEncryptedAssertion().get(0);

      AttributeStatementType attrStatement = StatementUtil.createAttributeStatement(roles);
      assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement().add(attrStatement);
      
      //Add timed conditions
      try
      {
         saml2Response.createTimedConditions(assertion, this.assertionValidity);
      }
      catch (IssueInstantMissingException e1)
      {
         log.error(e1);
      }
 
      //Lets see how the response looks like 
      if(log.isTraceEnabled())
      {
         StringWriter sw = new StringWriter();
         try
         {
            saml2Response.marshall(responseType, sw);
         }
         catch (JAXBException e)
         {
            log.trace(e);
         }
         catch (SAXException e)
         {
            log.trace(e);
         }
         log.trace("IDPRedirectValve::Response="+sw.toString()); 
      }

      return responseType; 
   }
   
   private ResponseType getErrorResponse(String responseURL, String status) throws ServletException
   {
      try
      {
         ResponseType responseType = null; 
         
         SAML2Response saml2Response = new SAML2Response();
               
         //Create a response type
         String id = IDGenerator.create("ID_");

         IssuerInfoHolder issuerHolder = new IssuerInfoHolder(this.identityURL); 
         issuerHolder.setStatusCode(status);
         
         IDPInfoHolder idp = new IDPInfoHolder();
         idp.setNameIDFormatValue(null);
         idp.setNameIDFormat(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());

         SPInfoHolder sp = new SPInfoHolder();
         sp.setResponseDestinationURI(responseURL);
         responseType = saml2Response.createResponseType(id, sp, idp, issuerHolder); 

         //Lets see how the response looks like 
         if(log.isTraceEnabled())
         {
            log.trace("ResponseType = ");
            StringWriter sw = new StringWriter();
            saml2Response.marshall(responseType, sw);
            log.trace("IDPRedirectValve::Response="+sw.toString()); 
         }

         return responseType;       
      }  
      catch(Exception e)
      {
         log.error("Exception in getErrorResponse::",e);
         throw new ServletException(e.getLocalizedMessage());
      }
   }
   
   private String getSAMLMessage(Request request)
   {
      return request.getParameter(GeneralConstants.SAML_REQUEST_KEY);
   }
   
   //***************Catalina Lifecyle methods
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
       // Validate and update our current component state
       if (started)
           throw new LifecycleException
               ("IDPRedirectValve already Started");
       lifecycle.fireLifecycleEvent(START_EVENT, null);
       started = true;
       
       String configFile = GeneralConstants.CONFIG_FILE_LOCATION; 
       Context context = (Context) getContainer();
       InputStream is = context.getServletContext().getResourceAsStream(configFile);
       if(is == null)
          throw new RuntimeException(configFile + " missing");
       try
       {
          idpConfiguration = ConfigurationUtil.getIDPConfiguration(is);
          this.identityURL = idpConfiguration.getIdentityURL();
          if(trace)
             log.trace("Identity Provider URL=" + this.identityURL); 
          this.assertionValidity = idpConfiguration.getAssertionValidity();
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
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
}