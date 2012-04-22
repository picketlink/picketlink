/*
 * JBoss, Home of Professional Open Source. Copyright 2009, Red Hat Middleware LLC, and individual contributors as
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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.annotation.Resource;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.STSType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.parsers.sts.STSConfigParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants.WSSE;
import org.picketlink.identity.federation.core.wstrust.wrappers.BaseRequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenCollection;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustResponseWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Default implementation of the {@code SecurityTokenService} interface.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
@WebServiceProvider(serviceName = "PicketLinkSTS", portName = "PicketLinkSTSPort", targetNamespace = "urn:picketlink:identity-federation:sts", wsdlLocation = "WEB-INF/wsdl/PicketLinkSTS.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)
public class PicketLinkSTS implements Provider<SOAPMessage>// SecurityTokenService
{
   private static Logger logger = Logger.getLogger(PicketLinkSTS.class);

   private static final String SEPARATOR = AccessController.doPrivileged(new PrivilegedAction<String>()
   {
      public String run()
      {
         return System.getProperty("file.separator");
      }
   });

   private static final String STS_CONFIG_FILE = "picketlink-sts.xml";

   private static final String STS_CONFIG_DIR = "picketlink-store" + SEPARATOR + "sts" + SEPARATOR;

   @Resource
   protected WebServiceContext context;

   protected STSConfiguration config;

   //If the SOAP Message contained a wsse:binaryToken, all the providers can have access to it
   public static ThreadLocal<BinaryToken> binaryToken = new InheritableThreadLocal<BinaryToken>();

   public static class BinaryToken
   {
      public Node token;
   }

   public SOAPMessage invoke(SOAPMessage request)
   {
      String valueType = null;
      Node binaryToken = null;
      //Check headers
      try
      {
         SOAPHeader soapHeader = request.getSOAPHeader();
         binaryToken = getBinaryToken(soapHeader);
         if (binaryToken != null)
         {
            NamedNodeMap namedNodeMap = binaryToken.getAttributes();
            int length = namedNodeMap != null ? namedNodeMap.getLength() : 0;
            for (int i = 0; i < length; i++)
            {
               Node nodeValueType = namedNodeMap.getNamedItem(WSSE.VALUE_TYPE);
               if (nodeValueType != null)
               {
                  valueType = nodeValueType.getNodeValue();
                  break;
               }
            }
         }
      }
      catch (SOAPException e)
      {
         throw new WebServiceException(e);
      }
      Node payLoad;
      BaseRequestSecurityToken baseRequest;
      try
      {
         payLoad = SOAPUtil.getSOAPData(request);

         WSTrustParser parser = new WSTrustParser();

         baseRequest = (BaseRequestSecurityToken) parser.parse(DocumentUtil.getNodeAsStream(payLoad));
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }

      if (baseRequest instanceof RequestSecurityToken)
      {
         RequestSecurityToken req = (RequestSecurityToken) baseRequest;
         try
         {
            req.setRSTDocument((Document) payLoad);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

         if (binaryToken != null)
         {
            req.setBinaryToken(binaryToken);
         }

         if (valueType != null)
         {
            req.setBinaryValueType(URI.create(valueType));
         }
         Source theResponse = this.handleTokenRequest(req);
         return convert(theResponse);
      }
      else if (baseRequest instanceof RequestSecurityTokenCollection)
      {
         return convert(this.handleTokenRequestCollection((RequestSecurityTokenCollection) baseRequest));
      }
      else
         throw new WebServiceException(ErrorCodes.STS_INVALID_TOKEN_REQUEST);
   }

   private SOAPMessage convert(Source theResponse)
   {
      try
      {
         SOAPMessage response = SOAPUtil.create();
         Document theResponseDoc = (Document) DocumentUtil.getNodeFromSource(theResponse);
         response.getSOAPBody().addDocument(theResponseDoc);
         return response;
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

   private Node getBinaryToken(SOAPHeader soapHeader)
   {
      if (soapHeader != null)
      {
         NodeList children = soapHeader.getChildNodes();
         int length = children != null ? children.getLength() : 0;
         for (int i = 0; i < length; i++)
         {
            Node child = children.item(i);
            if (child.getNodeName().contains(WSSE.BINARY_SECURITY_TOKEN))
            {
               return child;
            }
         }
      }
      return null;
   }

   /**
    * <p>
    * Process a security token request.
    * </p>
    * 
    * @param request
    *           a {@code RequestSecurityToken} instance that contains the request information.
    * @return a {@code Source} instance representing the marshalled response.
    * @throws WebServiceException
    *            Any exception encountered in handling token
    */
   protected Source handleTokenRequest(RequestSecurityToken request)
   {
      if (context == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "WebServiceContext");
      if (this.config == null)
         try
         {
            if (logger.isInfoEnabled())
               logger.info("Loading STS configuration");
            this.config = this.getConfiguration();
         }
         catch (ConfigurationException e)
         {
            throw new WebServiceException(ErrorCodes.STS_CONFIGURATION_EXCEPTION, e);
         }

      WSTrustRequestHandler handler = this.config.getRequestHandler();
      if (handler == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "WSTrustRequestHandler");

      String requestType = request.getRequestType().toString();
      if (logger.isDebugEnabled())
         logger.debug("STS received request of type " + requestType);

      try
      {
         if (requestType.equals(WSTrustConstants.ISSUE_REQUEST))
         {
            Source source = this.marshallResponse(handler.issue(request, this.context.getUserPrincipal()));
            Document doc = handler.postProcess((Document) ((DOMSource) source).getNode(), request);
            return new DOMSource(doc);
         }
         else if (requestType.equals(WSTrustConstants.RENEW_REQUEST))
         {
            Source source = this.marshallResponse(handler.renew(request, this.context.getUserPrincipal()));
            // we need to sign/encrypt renewed tokens.
            Document document = handler.postProcess((Document) ((DOMSource) source).getNode(), request);
            return new DOMSource(document);
         }
         else if (requestType.equals(WSTrustConstants.CANCEL_REQUEST))
            return this.marshallResponse(handler.cancel(request, this.context.getUserPrincipal()));
         else if (requestType.equals(WSTrustConstants.VALIDATE_REQUEST))
            return this.marshallResponse(handler.validate(request, this.context.getUserPrincipal()));
         else
            throw new WSTrustException(ErrorCodes.STS_INVALID_REQUEST_TYPE + requestType);
      }
      catch (WSTrustException we)
      {
         throw new WebServiceException(ErrorCodes.STS_EXCEPTION_HANDLING_TOKEN_REQ + we.getMessage(), we);
      }
   }

   /**
    * <p>
    * Process a collection of security token requests.
    * </p>
    * 
    * @param requestCollection
    *           a {@code RequestSecurityTokenCollection} containing the various requests information.
    * @return a {@code Source} instance representing the marshalled response.
    */
   protected Source handleTokenRequestCollection(RequestSecurityTokenCollection requestCollection)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * <p>
    * Marshalls the specified {@code RequestSecurityTokenResponse} into a {@code Source} instance.
    * </p>
    * 
    * @param response
    *           the {@code RequestSecurityTokenResponse} to be marshalled.
    * @return the resulting {@code Source} instance.
    */
   protected Source marshallResponse(RequestSecurityTokenResponse response)
   {
      // add the single response to a RequestSecurityTokenResponse collection, as per the specification.
      RequestSecurityTokenResponseCollection responseCollection = new RequestSecurityTokenResponseCollection();
      responseCollection.addRequestSecurityTokenResponse(response);

      try
      {
         DOMResult result = new DOMResult(DocumentUtil.createDocument());
         WSTrustResponseWriter writer = new WSTrustResponseWriter(result);
         writer.write(responseCollection);
         return new DOMSource(result.getNode());
      }
      catch (Exception e)
      {
         throw new WebServiceException(ErrorCodes.STS_RESPONSE_WRITING_ERROR + e.getMessage(), e);
      }
   }

   /**
    * <p>
    * Obtains the STS configuration options.
    * </p>
    * 
    * @return an instance of {@code STSConfiguration} containing the STS configuration properties.
    */
   protected STSConfiguration getConfiguration() throws ConfigurationException
   {
      URL configurationFileURL = null;

      try
      {
         // check the user home for a configuration file generated by the picketlink console.
         String configurationFilePath = System.getProperty("user.home") + SEPARATOR + STS_CONFIG_DIR + STS_CONFIG_FILE;
         File configurationFile = new File(configurationFilePath);
         if (configurationFile.exists())
            configurationFileURL = configurationFile.toURI().toURL();
         else
            // if not configuration file was found in the user home, check the context classloader.
            configurationFileURL = SecurityActions.loadResource(getClass(), STS_CONFIG_FILE);

         // if no configuration file was found, log a warn message and use default configuration values.
         if (configurationFileURL == null)
         {
            logger.warn(STS_CONFIG_FILE + " configuration file not found. Using default configuration values");
            return new PicketLinkSTSConfiguration();
         }

         InputStream stream = configurationFileURL.openStream();
         STSType stsConfig = (STSType) new STSConfigParser().parse(stream);
         STSConfiguration configuration = new PicketLinkSTSConfiguration(stsConfig);
         if (logger.isInfoEnabled())
            logger.info(STS_CONFIG_FILE + " configuration file loaded");
         return configuration;
      }
      catch (Exception e)
      {
         throw new ConfigurationException(ErrorCodes.STS_CONFIGURATION_FILE_PARSING_ERROR + configurationFileURL + "]",
               e);
      }
   }
}