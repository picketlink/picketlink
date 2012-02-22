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
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.metadata.MetaDataExtractor;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.factories.SAML2HandlerChainFactory;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.HandlerUtil;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.util.SystemPropertiesUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.web.config.AbstractSAMLConfigurationProvider;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;
import org.w3c.dom.Document;

/**
 * Base Class for Service Provider Form Authenticators
 * @author Anil.Saldhana@redhat.com
 * @since Jun 9, 2009
 */
public abstract class BaseFormAuthenticator extends FormAuthenticator
{
   protected static Logger log = Logger.getLogger(BaseFormAuthenticator.class);

   protected final boolean trace = log.isTraceEnabled();

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

   protected String logOutPage = GeneralConstants.LOGOUT_PAGE_NAME;

   /**
    * The user can inject a fully qualified name of a {@link SAMLConfigurationProvider}
    */
   protected SAMLConfigurationProvider configProvider = null;

   /**
    * Servlet3 related changes forced Tomcat to change the authenticate method
    * signature in the FormAuthenticator. For now, we use reflection for forward
    * compatibility.  This has to be changed in future.
    */
   private Method theSuperRegisterMethod = null;

   /**
    * If it is determined that we are running in a Tomcat6/JBAS5 environment,
    * there is no need to seek the super.register method that conforms to
    * the servlet3 spec changes
    */
   private boolean seekSuperRegisterMethod = true;

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

   public void setConfigProvider(String cp)
   {
      if (cp == null)
         throw new IllegalStateException(ErrorCodes.NULL_ARGUMENT + cp);
      Class<?> clazz = SecurityActions.loadClass(getClass(), cp);
      if (clazz == null)
         throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + cp);
      try
      {
         configProvider = (SAMLConfigurationProvider) clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(ErrorCodes.CANNOT_CREATE_INSTANCE + cp + ":" + e.getMessage());
      }
   }

   /**
    * Set a separate issuer id
    * @param issuerID
    */
   public void setIssuerID(String issuerID)
   {
      this.issuerID = issuerID;
   }

   public void setLogOutPage(String logOutPage)
   {
      this.logOutPage = logOutPage;
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

   @Override
   public void start() throws LifecycleException
   {
      super.start();
      SystemPropertiesUtil.ensure();
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

   /**
    * This method is a hack!!!
    * Tomcat on account of Servlet3 changed their authenticator method signatures
    * We utilize Java Reflection to identify the super register method on the first
    * call and save it. Subsquent invocations utilize the saved {@link Method}
    * @see org.apache.catalina.authenticator.AuthenticatorBase#register(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response, java.security.Principal, java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   protected void register(Request request, Response response, Principal principal, String arg3, String arg4,
         String arg5)
   {
      //Try the JBossAS6 version
      if (theSuperRegisterMethod == null && seekSuperRegisterMethod)
      {
         Class<?>[] args = new Class[]
         {Request.class, HttpServletResponse.class, Principal.class, String.class, String.class, String.class};
         Class<?> superClass = getAuthenticatorBaseClass();
         theSuperRegisterMethod = SecurityActions.getMethod(superClass, "register", args);
      }
      try
      {
         if (theSuperRegisterMethod != null)
         {
            Object[] callArgs = new Object[]
            {request, response, principal, arg3, arg4, arg5};
            theSuperRegisterMethod.invoke(this, callArgs);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      //Try the older version
      if (theSuperRegisterMethod == null)
      {
         seekSuperRegisterMethod = false; //Don't try to seek super register method on next invocation
         super.register(request, response, principal, arg3, arg4, arg5);
         return;
      }
   }

   /**
    * Fall back on local authentication at the service provider side
    * @param request
    * @param response
    * @param loginConfig
    * @return
    * @throws IOException
    */
   protected boolean localAuthentication(Request request, Response response, LoginConfig loginConfig)
         throws IOException
   {
      if (request.getUserPrincipal() == null)
      {
         log.error("Falling back on local Form Authentication if available");//fallback
         try
         {
            return super.authenticate(request, response, loginConfig);
         }
         catch (NoSuchMethodError e)
         {
            //Use Reflection
            try
            {
               Method method = super.getClass().getMethod("authenticate", new Class[]
               {HttpServletRequest.class, HttpServletResponse.class, LoginConfig.class});
               return (Boolean) method.invoke(this, new Object[]
               {request.getRequest(), response.getResponse(), loginConfig});
            }
            catch (Exception ex)
            {
               throw new IOException(ErrorCodes.UNABLE_LOCAL_AUTH, ex);
            }
         }
      }
      else
         return true;
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

      Object metadata = null;
      try
      {
         Document samlDocument = DocumentUtil.getDocument(is);
         SAMLParser parser = new SAMLParser();
         metadata = parser.parse(DocumentUtil.getNodeAsStream(samlDocument));
      }
      catch (Exception e)
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
         throw new RuntimeException(ErrorCodes.SERVICE_PROVIDER_CONF_FILE_MISSING + configFile);
      try
      {
         if (configProvider != null)
         {
            spConfiguration = configProvider.getSPConfiguration();
            if (configProvider instanceof AbstractSAMLConfigurationProvider)
            {
               ((AbstractSAMLConfigurationProvider) configProvider).setConfigFile(is);
            }
         }
         else
         {
            spConfiguration = ConfigurationUtil.getSPConfiguration(is);
         }

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

   protected void sendToLogoutPage(Request request, Response response, Session session) throws IOException,
         ServletException
   {
      //we are invalidated.
      RequestDispatcher dispatch = context.getServletContext().getRequestDispatcher(this.logOutPage);
      if (dispatch == null)
         log.error("Cannot dispatch to the logout page: no request dispatcher:" + this.logOutPage);
      else
      {
         session.expire();
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

   private Class<?> getAuthenticatorBaseClass()
   {
      Class<?> myClass = getClass();
      do
      {
         myClass = myClass.getSuperclass();
      }
      while (myClass != AuthenticatorBase.class);
      return myClass;
   }
}