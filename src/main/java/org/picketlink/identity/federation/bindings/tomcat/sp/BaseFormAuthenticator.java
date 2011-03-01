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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.metadata.MetaDataExtractor;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.factories.SAML2HandlerChainFactory;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.util.HandlerUtil;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.newmodel.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.newmodel.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.newmodel.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.newmodel.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.newmodel.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;

/**
 * Base Class for Service Provider Form Authenticators
 * @author Anil.Saldhana@redhat.com
 * @since Jun 9, 2009
 */
public abstract class BaseFormAuthenticator extends FormAuthenticator
{
   private static Logger log = Logger.getLogger(BaseFormAuthenticator.class);

   private final boolean trace = log.isTraceEnabled();

   protected SPType spConfiguration = null;

   protected String serviceURL = null;

   protected String identityURL = null;

   protected String issuerID = null;

   protected String configFile = GeneralConstants.CONFIG_FILE_LOCATION;

   /**
    * If the service provider is configured with an IDP metadata file,
    * then this certificate can be picked up from the metadata
    */
   protected transient X509Certificate idpCertificate = null;

   protected transient SAML2HandlerChain chain = null;

   protected transient String samlHandlerChainClass = null;

   protected Map<String, Object> chainConfigOptions = new HashMap<String, Object>();

   //Whether the authenticator has to to save and restore request
   protected boolean saveRestoreRequest = true;

   /**
    * A Lock for Handler operations in the chain
    */
   protected Lock chainLock = new ReentrantLock();

   protected String canonicalizationMethod = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

   public BaseFormAuthenticator()
   {
      super();
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public void setSamlHandlerChainClass(String samlHandlerChainClass)
   {
      this.samlHandlerChainClass = samlHandlerChainClass;
   }

   public void setServiceURL(String serviceURL)
   {
      this.serviceURL = serviceURL;
   }

   public void setSaveRestoreRequest(boolean saveRestoreRequest)
   {
      this.saveRestoreRequest = saveRestoreRequest;
   }

   /**
    * Set a separate issuer id
    * @param issuerID
    */
   public void setIssuerID(String issuerID)
   {
      this.issuerID = issuerID;
   }

   /**
    * Perform validation os the request object
    * @param request
    * @return
    * @throws IOException
    * @throws GeneralSecurityException
    */
   protected boolean validate(Request request) throws IOException, GeneralSecurityException
   {
      return request.getParameter("SAMLResponse") != null;
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
      throw new RuntimeException("Response was not of type catalina response");
   }

   @Override
   public void start() throws LifecycleException
   {
      super.start();
      processStart();
   }

   /**
    * Get the Identity URL
    * @return
    */
   public String getIdentityURL()
   {
      return identityURL;
   }

   /**
    * Get the {@link X509Certificate} of the IDP
    * if provided via the IDP metadata file
    * @return {@link X509Certificate} or null
    */
   public X509Certificate getIdpCertificate()
   {
      return idpCertificate;
   }

   //Mock test purpose
   public void testStart() throws LifecycleException
   {
      this.saveRestoreRequest = false;
      if (context == null)
         throw new RuntimeException("Catalina Context not set up");
      processStart();
   }

   private void processStart() throws LifecycleException
   {
      Handlers handlers = null;

      //Get the chain from config 
      if (StringUtil.isNullOrEmpty(samlHandlerChainClass))
      {
         chain = SAML2HandlerChainFactory.createChain();
      }
      else
      {
         try
         {
            chain = SAML2HandlerChainFactory.createChain(this.samlHandlerChainClass);
         }
         catch (ProcessingException e1)
         {
            throw new LifecycleException(e1);
         }
      }

      ServletContext servletContext = context.getServletContext();

      this.processConfiguration();

      try
      {
         //Get the handlers
         String handlerConfigFileName = GeneralConstants.HANDLER_CONFIG_FILE_LOCATION;
         handlers = ConfigurationUtil.getHandlers(servletContext.getResourceAsStream(handlerConfigFileName));
         chain.addAll(HandlerUtil.getHandlers(handlers));

         this.populateChainConfig();
         this.initializeHandlerChain();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Return the SAML Binding that this authenticator supports
    * @see {@link JBossSAMLURIConstants#SAML_HTTP_POST_BINDING}
    * @see {@link JBossSAMLURIConstants#SAML_HTTP_REDIRECT_BINDING}
    * @return
    */
   protected abstract String getBinding();

   /**
    * Attempt to process a metadata file available locally 
    */
   protected void processIDPMetadataFile(String idpMetadataFile)
   {
      ServletContext servletContext = context.getServletContext();
      InputStream is = servletContext.getResourceAsStream(idpMetadataFile);
      if (is == null)
         return;

      SAMLParser parser = new SAMLParser();
      Object metadata = null;
      try
      {
         metadata = parser.parse(is);
      }
      catch (ParsingException e)
      {
         throw new RuntimeException(e);
      }
      IDPSSODescriptorType idpSSO = null;
      if (metadata instanceof EntitiesDescriptorType)
      {
         EntitiesDescriptorType entities = (EntitiesDescriptorType) metadata;
         idpSSO = handleMetadata(entities);
      }
      else
      {
         idpSSO = handleMetadata((EntityDescriptorType) metadata);
      }
      if (idpSSO == null)
      {
         log.error("Unable to obtain the IDP SSO Descriptor from metadata");
         return;
      }
      List<EndpointType> endpoints = idpSSO.getSingleSignOnService();
      for (EndpointType endpoint : endpoints)
      {
         if (getBinding().equals(endpoint.getBinding().toString()))
         {
            identityURL = endpoint.getLocation().toString();
            break;
         }
      }
      List<KeyDescriptorType> keyDescriptors = idpSSO.getKeyDescriptor();
      if (keyDescriptors.size() > 0)
      {
         this.idpCertificate = MetaDataExtractor.getCertificate(keyDescriptors.get(0));
      }
   }

   /**
    * Process the configuration from the configuration file
    */
   protected void processConfiguration()
   {
      ServletContext servletContext = context.getServletContext();
      InputStream is = servletContext.getResourceAsStream(configFile);
      if (is == null)
         throw new RuntimeException(configFile + " missing");
      try
      {
         spConfiguration = ConfigurationUtil.getSPConfiguration(is);

         if (StringUtil.isNotNull(spConfiguration.getIdpMetadataFile()))
         {
            processIDPMetadataFile(spConfiguration.getIdpMetadataFile());
         }
         else
         {
            this.identityURL = spConfiguration.getIdentityURL();
         }
         this.serviceURL = spConfiguration.getServiceURL();
         this.canonicalizationMethod = spConfiguration.getCanonicalizationMethod();

         log.info("BaseFormAuthenticator:: Setting the CanonicalizationMethod on XMLSignatureUtil::"
               + canonicalizationMethod);
         XMLSignatureUtil.setCanonicalizationMethodType(canonicalizationMethod);

         if (trace)
            log.trace("Identity Provider URL=" + this.identityURL);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected IDPSSODescriptorType handleMetadata(EntitiesDescriptorType entities)
   {
      IDPSSODescriptorType idpSSO = null;

      List<Object> entityDescs = entities.getEntityDescriptor();
      for (Object entityDescriptor : entityDescs)
      {
         if (entityDescriptor instanceof EntitiesDescriptorType)
         {
            idpSSO = getIDPSSODescriptor(entities);
         }
         else
            idpSSO = handleMetadata((EntityDescriptorType) entityDescriptor);
         if (idpSSO != null)
            break;
      }
      return idpSSO;
   }

   protected IDPSSODescriptorType handleMetadata(EntityDescriptorType entityDescriptor)
   {
      return CoreConfigUtil.getIDPDescriptor(entityDescriptor);
   }

   protected IDPSSODescriptorType getIDPSSODescriptor(EntitiesDescriptorType entities)
   {
      List<Object> entityDescs = entities.getEntityDescriptor();
      for (Object entityDescriptor : entityDescs)
      {

         if (entityDescriptor instanceof EntitiesDescriptorType)
         {
            return getIDPSSODescriptor((EntitiesDescriptorType) entityDescriptor);
         }
         return CoreConfigUtil.getIDPDescriptor((EntityDescriptorType) entityDescriptor);
      }
      return null;
   }

   protected void initializeHandlerChain() throws ConfigurationException, ProcessingException
   {
      populateChainConfig();
      SAML2HandlerChainConfig handlerChainConfig = new DefaultSAML2HandlerChainConfig(chainConfigOptions);

      Set<SAML2Handler> samlHandlers = chain.handlers();

      for (SAML2Handler handler : samlHandlers)
      {
         handler.initChainConfig(handlerChainConfig);
      }
   }

   protected void populateChainConfig() throws ConfigurationException, ProcessingException
   {
      chainConfigOptions.put(GeneralConstants.CONFIGURATION, spConfiguration);
      chainConfigOptions.put(GeneralConstants.CANONICALIZATION_METHOD, canonicalizationMethod);
      chainConfigOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "false"); //No validator as tomcat realm does validn   
   }
}