/*
 * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust;

import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * WS-Trust Client
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Aug 29, 2009
 */
public class STSClient
{
   private final ThreadLocal<Dispatch<Source>> dispatchLocal = new InheritableThreadLocal<Dispatch<Source>>();

   private final String targetNS = "http://org.picketlink.trust/sts/";

   private String wsaIssuerAddress;

   private String wspAppliesTo;

   /**
    * Indicates whether the request is a batch request - will be read from the {@link STSClientConfig}
    */
   private boolean isBatch = false;

   /**
    * Constructor
    * @see {@link #setDispatch(Dispatch)} for the setting of the {@link Dispatch} object
    */
   public STSClient()
   {
   }

   /**
    * <p>
    * Constructor that creates the {@link Dispatch} for use.
    * </p>
    * <p>
    * If you need to customize the ws properties, it is suggested to preconstruct a {@link Dispatch}
    * object and use the default no-arg constructor followed by a {@linkplain #setDispatch(Dispatch)} call
    * </p>
    * @param config
    */
   public STSClient(STSClientConfig config)
   {
      QName service = new QName(targetNS, config.getServiceName());
      QName portName = new QName(targetNS, config.getPortName());

      isBatch = config.isBatch();

      wsaIssuerAddress = config.getWsaIssuer();
      wspAppliesTo = config.getWspAppliesTo();

      Service jaxwsService = Service.create(service);
      jaxwsService.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, config.getEndPointAddress());
      Dispatch<Source> dispatch = jaxwsService.createDispatch(portName, Source.class, Mode.PAYLOAD);

      Map<String, Object> reqContext = dispatch.getRequestContext();
      String username = config.getUsername();
      if (username != null)
      {
         // add the username and password to the request context.
         reqContext.put(BindingProvider.USERNAME_PROPERTY, config.getUsername());
         reqContext.put(BindingProvider.PASSWORD_PROPERTY, config.getPassword());
      }
      setDispatch(dispatch);
   }

   /**
    * Set the {@link Dispatch} object for use
    * @param dispatch
    */
   public void setDispatch(Dispatch<Source> dispatch)
   {
      if (dispatch == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "dispatch");

      dispatchLocal.set(dispatch);
   }

   /**
    * Issue a token
    * @param tokenType
    * @return
    * @throws WSTrustException
    */
   public Element issueToken(String tokenType) throws WSTrustException
   {
      // create a custom token request message.
      RequestSecurityToken request = new RequestSecurityToken();
      setTokenType(tokenType, request);

      if (wsaIssuerAddress != null)
      {
         request.setIssuer(WSTrustUtil.createIssuer(wsaIssuerAddress));
      }
      if (wspAppliesTo != null)
      {
         request.setAppliesTo(WSTrustUtil.createAppliesTo(wspAppliesTo));
      }
      // send the token request to JBoss STS and get the response.
      return issueToken(request);
   }

   /**
    * Issues a Security Token for the ultimate recipient of the token.
    * 
    * @param endpointURI
    *           - The ultimate recipient of the token. This will be set at the AppliesTo for the RequestSecurityToken
    *           which is an optional element so it may be null.
    * @return Element - The Security Token Element which will be of the TokenType configured for the endpointURI passed
    *         in.
    * @throws WSTrustException
    */
   public Element issueTokenForEndpoint(String endpointURI) throws WSTrustException
   {
      RequestSecurityToken request = new RequestSecurityToken();
      if (wsaIssuerAddress != null)
      {
         request.setIssuer(WSTrustUtil.createIssuer(wsaIssuerAddress));
      }
      setAppliesTo(endpointURI, request);
      return issueToken(request);
   }

   /**
    * Issues a Security Token from the STS. This methods has the option of specifying one or both of
    * endpointURI/tokenType but at least one must specified.
    * 
    * @param endpointURI
    *           - The ultimate recipient of the token. This will be set at the AppliesTo for the RequestSecurityToken
    *           which is an optional element so it may be null.
    * @param tokenType
    *           - The type of security token to be issued.
    * @return Element - The Security Token Element issued.
    * @throws IllegalArgumentException
    *            If neither endpointURI nor tokenType was specified.
    * @throws WSTrustException
    */
   public Element issueToken(String endpointURI, String tokenType) throws WSTrustException
   {
      if (endpointURI == null && tokenType == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "endpointURI or tokenType");

      RequestSecurityToken request = new RequestSecurityToken();
      if (wsaIssuerAddress != null)
      {
         request.setIssuer(WSTrustUtil.createIssuer(wsaIssuerAddress));
      }
      setAppliesTo(endpointURI, request);
      setTokenType(tokenType, request);
      return issueToken(request);
   }

   /**
    * <p>
    * Issues a security token on behalf of the specified principal.
    * </p>
    * 
    * @param endpointURI
    *           the ultimate recipient of the token. This will be set at the AppliesTo for the RequestSecurityToken
    *           which is an optional element so it may be null.
    * @param tokenType
    *           the type of the token to be issued.
    * @param principal
    *           the {@code Principal} to whom the token will be issued.
    * @return an {@code Element} representing the issued security token.
    * @throws IllegalArgumentException
    *            If neither endpointURI nor tokenType was specified.
    * @throws WSTrustException
    *            if an error occurs while issuing the security token.
    */
   public Element issueTokenOnBehalfOf(String endpointURI, String tokenType, Principal principal)
         throws WSTrustException
   {
      if (endpointURI == null && tokenType == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "endpointURI or tokenType");

      RequestSecurityToken request = new RequestSecurityToken();
      if (wsaIssuerAddress != null)
      {
         request.setIssuer(WSTrustUtil.createIssuer(wsaIssuerAddress));
      }
      setAppliesTo(endpointURI, request);
      setTokenType(tokenType, request);
      setOnBehalfOf(principal, request);
      return issueToken(request);
   }

   private RequestSecurityToken setAppliesTo(String endpointURI, RequestSecurityToken rst)
   {
      if (StringUtil.isNotNull(wspAppliesTo))
      {
         rst.setAppliesTo(WSTrustUtil.createAppliesTo(wspAppliesTo));
      }
      else if (endpointURI != null)
         rst.setAppliesTo(WSTrustUtil.createAppliesTo(endpointURI));
      return rst;
   }

   private RequestSecurityToken setTokenType(String tokenType, RequestSecurityToken rst)
   {
      if (tokenType != null)
         rst.setTokenType(URI.create(tokenType));
      return rst;
   }

   private RequestSecurityToken setOnBehalfOf(Principal principal, RequestSecurityToken request)
   {
      if (principal != null)
         request.setOnBehalfOf(WSTrustUtil.createOnBehalfOfWithUsername(principal.getName(), "ID"));
      return request;
   }

   /**
    * Issue a token
    * @param request
    * @return
    * @throws WSTrustException
    */
   public Element issueToken(RequestSecurityToken request) throws WSTrustException
   {
      if (request.getRequestType() == null)
      {
         if (isBatch)
            request.setRequestType(URI.create(WSTrustConstants.BATCH_ISSUE_REQUEST));
         else
            request.setRequestType(URI.create(WSTrustConstants.ISSUE_REQUEST));
      }

      if (request.getContext() == null)
         request.setContext("default-context");

      validateDispatch();
      DOMSource requestSource = this.createSourceFromRequest(request);
      Source response = dispatchLocal.get().invoke(requestSource);

      NodeList nodes;
      try
      {
         Node documentNode = DocumentUtil.getNodeFromSource(response);

         Document responseDoc = documentNode instanceof Document ? (Document) documentNode : documentNode
               .getOwnerDocument();

         nodes = null;
         if (responseDoc instanceof SOAPPart)
         {
            SOAPPart soapPart = (SOAPPart) responseDoc;
            SOAPEnvelope env = soapPart.getEnvelope();
            SOAPBody body = env.getBody();
            Node data = body.getFirstChild();
            nodes = ((Element) data).getElementsByTagNameNS(WSTrustConstants.BASE_NAMESPACE, "RequestedSecurityToken");
            if (nodes == null || nodes.getLength() == 0)
               nodes = ((Element) data).getElementsByTagName("RequestedSecurityToken");
         }
         else
         {
            nodes = responseDoc.getElementsByTagNameNS(WSTrustConstants.BASE_NAMESPACE, "RequestedSecurityToken");
            if (nodes == null || nodes.getLength() == 0)
               nodes = responseDoc.getElementsByTagName("RequestedSecurityToken");
         }
      }
      catch (Exception e)
      {
         throw new WSTrustException(ErrorCodes.PROCESSING_EXCEPTION + " issuing token:", e);
      }

      if (nodes == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "NodeList");

      Node rstr = nodes.item(0);
      if (rstr == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "RSTR in the payload");

      return (Element) rstr.getFirstChild();
   }

   /**
    * Renew a token
    * @param tokenType
    * @param token
    * @return
    * @throws WSTrustException
    */
   public Element renewToken(String tokenType, Element token) throws WSTrustException
   {
      validateDispatch();
      RequestSecurityToken request = new RequestSecurityToken();
      request.setContext("context");

      request.setTokenType(URI.create(WSTrustConstants.STATUS_TYPE));
      request.setRequestType(URI.create(WSTrustConstants.RENEW_REQUEST));
      RenewTargetType renewTarget = new RenewTargetType();
      renewTarget.add(token);
      request.setRenewTarget(renewTarget);

      // send the token request to JBoss STS and get the response.
      DOMSource requestSource = this.createSourceFromRequest(request);
      Source response = dispatchLocal.get().invoke(requestSource);
      NodeList nodes;
      try
      {
         Node documentNode = DocumentUtil.getNodeFromSource(response);
         Document responseDoc = documentNode instanceof Document ? (Document) documentNode : documentNode
               .getOwnerDocument();

         nodes = null;
         if (responseDoc instanceof SOAPPart)
         {
            SOAPPart soapPart = (SOAPPart) responseDoc;
            SOAPEnvelope env = soapPart.getEnvelope();
            SOAPBody body = env.getBody();
            Node data = body.getFirstChild();
            nodes = ((Element) data).getElementsByTagNameNS(WSTrustConstants.BASE_NAMESPACE, "RequestedSecurityToken");
            if (nodes == null || nodes.getLength() == 0)
               nodes = ((Element) data).getElementsByTagName("RequestedSecurityToken");
         }
         else
         {
            nodes = responseDoc.getElementsByTagNameNS(WSTrustConstants.BASE_NAMESPACE, "RequestedSecurityToken");
            if (nodes == null || nodes.getLength() == 0)
               nodes = responseDoc.getElementsByTagName("RequestedSecurityToken");
         }
      }
      catch (Exception e)
      {
         throw new WSTrustException(ErrorCodes.PROCESSING_EXCEPTION + "renewing token:", e);
      }

      if (nodes == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "NodeList");

      Node rstr = nodes.item(0);

      return (Element) rstr.getFirstChild();
   }

   /**
    * Validate a token
    * @param token
    * @return
    * @throws WSTrustException
    */
   public boolean validateToken(Element token) throws WSTrustException
   {
      validateDispatch();
      RequestSecurityToken request = new RequestSecurityToken();
      request.setContext("context");

      request.setTokenType(URI.create(WSTrustConstants.STATUS_TYPE));
      request.setRequestType(URI.create(WSTrustConstants.VALIDATE_REQUEST));
      ValidateTargetType validateTarget = new ValidateTargetType();
      validateTarget.add(token);
      request.setValidateTarget(validateTarget);

      DOMSource requestSource = this.createSourceFromRequest(request);

      Source response = dispatchLocal.get().invoke(requestSource);
      try
      {
         InputStream stream = DocumentUtil.getNodeAsStream(DocumentUtil.getNodeFromSource(response));
         RequestSecurityTokenResponseCollection responseCollection = (RequestSecurityTokenResponseCollection) new WSTrustParser()
               .parse(stream);
         RequestSecurityTokenResponse tokenResponse = responseCollection.getRequestSecurityTokenResponses().get(0);

         StatusType status = tokenResponse.getStatus();
         if (status != null)
         {
            String code = status.getCode();
            return WSTrustConstants.STATUS_CODE_VALID.equals(code);
         }
         return false;
      }
      catch (Exception e)
      {
         throw new WSTrustException(ErrorCodes.PARSING_ERROR + "WS-Trust response: " + e.getMessage(), e);
      }
   }

   /**
    * <p>
    * Cancels the specified security token by sending a WS-Trust cancel message to the STS.
    * </p>
    * 
    * @param securityToken
    *           the security token to be canceled.
    * @return {@code true} if the token has been canceled by the STS; {@code false} otherwise.
    * @throws WSTrustException
    *            if an error occurs while processing the cancel request.
    */
   public boolean cancelToken(Element securityToken) throws WSTrustException
   {
      validateDispatch();
      // create a WS-Trust cancel request containing the specified token.
      RequestSecurityToken request = new RequestSecurityToken();
      request.setRequestType(URI.create(WSTrustConstants.CANCEL_REQUEST));
      CancelTargetType cancelTarget = new CancelTargetType();
      cancelTarget.add(securityToken);
      request.setCancelTarget(cancelTarget);
      request.setContext("context");

      DOMSource requestSource = this.createSourceFromRequest(request);
      Source response = dispatchLocal.get().invoke(requestSource);
      // get the WS-Trust response and check for presence of the RequestTokenCanceled element.
      try
      {
         InputStream stream = DocumentUtil.getNodeAsStream(DocumentUtil.getNodeFromSource(response));
         RequestSecurityTokenResponseCollection responseCollection = (RequestSecurityTokenResponseCollection) new WSTrustParser()
               .parse(stream);
         RequestSecurityTokenResponse tokenResponse = responseCollection.getRequestSecurityTokenResponses().get(0);
         if (tokenResponse.getRequestedTokenCancelled() != null)
            return true;
         return false;
      }
      catch (Exception e)
      {
         throw new WSTrustException(ErrorCodes.PARSING_ERROR + "WS-Trust response: " + e.getMessage(), e);
      }
   }

   /**
    * Get the dispatch object
    * @return
    */
   public Dispatch<Source> getDispatch()
   {
      return dispatchLocal.get();
   }

   private DOMSource createSourceFromRequest(RequestSecurityToken request) throws WSTrustException
   {
      try
      {
         DOMResult result = new DOMResult(DocumentUtil.createDocument());
         WSTrustRequestWriter writer = new WSTrustRequestWriter(result);
         writer.write(request);
         return new DOMSource(result.getNode());
      }
      catch (Exception e)
      {
         throw new WSTrustException(
               ErrorCodes.PROCESSING_EXCEPTION + "creating source from request: " + e.getMessage(), e);
      }
   }

   /**
    * Validate that we have a {@code Dispatch} to work with
    */
   private void validateDispatch()
   {
      if (getDispatch() == null)
         throw new RuntimeException(ErrorCodes.INJECTED_VALUE_MISSING + "Dispatch");
   }
}