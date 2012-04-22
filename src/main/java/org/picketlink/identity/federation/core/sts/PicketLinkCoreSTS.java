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
package org.picketlink.identity.federation.core.sts;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.STSType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.parsers.sts.STSConfigParser;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTSConfiguration;
import org.picketlink.identity.federation.core.wstrust.STSConfiguration;

/**
 * <p>
 * Generic STS Core.
 * </p>
 * <p>
 * This is a Singleton Class.
 * </p>
 * @see {@code #instance()}
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Dec 27, 2010
 */
public class PicketLinkCoreSTS
{
   private static final Logger logger = Logger.getLogger(PicketLinkCoreSTS.class);

   public static final RuntimePermission rte = new RuntimePermission("org.picketlink.sts");

   protected STSCoreConfig configuration;

   private static PicketLinkCoreSTS _instance = null;

   private static final String SEPARATOR = AccessController.doPrivileged(new PrivilegedAction<String>()
   {
      public String run()
      {
         return System.getProperty("file.separator");
      }
   });

   private static final String STS_CONFIG_DIR = "picketlink-store" + SEPARATOR + "sts" + SEPARATOR;

   protected PicketLinkCoreSTS()
   {
   }

   public static PicketLinkCoreSTS instance()
   {
      if (_instance == null)
         _instance = new PicketLinkCoreSTS();

      return _instance;
   }

   public void initialize(STSCoreConfig config)
   {
      if (this.configuration != null)
      {
         this.configuration.copy(config);
      }
      else
         this.configuration = config;
   }

   public void installDefaultConfiguration(String... configFileName)
   {
      String fileName = "core-sts.xml";

      if (configFileName != null && configFileName.length > 0)
         fileName = configFileName[0];

      if (configuration == null)
      {
         if (logger.isDebugEnabled())
            logger.debug("[InstallDefaultConfiguration] Configuration is null. Creating a new configuration");
         configuration = new PicketLinkSTSConfiguration();
      }

      try
      {

         if (logger.isDebugEnabled())
            logger.debug("[InstallDefaultConfiguration] Configuration file name=" + fileName);

         STSConfiguration config = getConfiguration(fileName);
         configuration.copy(config);
      }
      catch (ConfigurationException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Issue a security token
    * @param protocolContext
    * @throws ProcessingException
    * @throws {@link SecurityException} if the caller does not have a runtime permission for "org.picketlink.sts"
    */
   public void issueToken(ProtocolContext protocolContext) throws ProcessingException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(rte);

      SecurityTokenProvider provider = getProvider(protocolContext);

      if (provider == null)
         throw new ProcessingException(ErrorCodes.STS_NO_TOKEN_PROVIDER + configuration + "][ProtoCtx="
               + protocolContext + "]");

      if (logger.isDebugEnabled())
      {
         logger.debug("issueToken::provider=" + provider);
      }

      provider.issueToken(protocolContext);
   }

   /**
    * <p>
    * Renews the security token contained in the specified request context. This method is used when a previously
    * generated token has expired, generating a new version of the same token with different expiration semantics.
    * </p>
    * 
    * @param protocolContext the {@code ProtocolContext} that contains the token to be renewed.
    * @throws ProcessingException if an error occurs while renewing the security token.
    * @throws {@link SecurityException} if the caller does not have a runtime permission for "org.picketlink.sts"
    */
   public void renewToken(ProtocolContext protocolContext) throws ProcessingException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(rte);

      SecurityTokenProvider provider = null;

      if (provider == null)
         provider = getProviderBasedOnQName(protocolContext);

      if (provider == null)
         throw new ProcessingException(ErrorCodes.STS_NO_TOKEN_PROVIDER + configuration + "[ProtoCtx=]"
               + protocolContext);

      if (logger.isDebugEnabled())
      {
         logger.debug("renewToken::provider=" + provider);
      }
      provider.renewToken(protocolContext);
   }

   /**
    * <p>
    * Cancels the token contained in the specified request context. A security token is usually canceled when one wants
    * to make sure that the token will not be used anymore. A security token can't be renewed once it has been canceled.
    * </p>
    * 
    * @param protocolContext the {@code ProtocolContext} that contains the token to be canceled.
    * @throws ProcessingException if an error occurs while canceling the security token.
    * @throws {@link SecurityException} if the caller does not have a runtime permission for "org.picketlink.sts"
    */
   public void cancelToken(ProtocolContext protocolContext) throws ProcessingException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(rte);

      SecurityTokenProvider provider = null;

      if (provider == null)
         provider = getProviderBasedOnQName(protocolContext);

      if (provider == null)
         throw new ProcessingException(ErrorCodes.STS_NO_TOKEN_PROVIDER + protocolContext);

      if (logger.isDebugEnabled())
      {
         logger.debug("cancelToken::provider=" + provider);
      }

      provider.cancelToken(protocolContext);
   }

   /**
    * <p>
    * Evaluates the validity of the token contained in the specified request context and sets the result in the context
    * itself. The result can be a status, a new token, or both.
    * </p>
    * 
    * @param protocolContext the {@code ProtocolContext} that contains the token to be validated.
    * @throws ProcessingException if an error occurs while validating the security token.
    * @throws {@link SecurityException} if the caller does not have a runtime permission for "org.picketlink.sts"
    */
   public void validateToken(ProtocolContext protocolContext) throws ProcessingException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(rte);

      SecurityTokenProvider provider = null;

      if (provider == null)
         provider = getProviderBasedOnQName(protocolContext);

      if (provider == null)
         throw new ProcessingException(ErrorCodes.STS_NO_TOKEN_PROVIDER + configuration + "[ProtoCtx=]"
               + protocolContext);

      if (logger.isDebugEnabled())
      {
         logger.debug("validateToken::provider=" + provider);
      }

      provider.validateToken(protocolContext);
   }

   private SecurityTokenProvider getProvider(ProtocolContext protocolContext)
   {
      if (configuration == null)
         throw new RuntimeException(ErrorCodes.STS_CONFIGURATION_NOT_SET);

      SecurityTokenProvider provider = null;

      //Special Case: WST Applies To
      String serviceName = protocolContext.serviceName();
      if (serviceName != null)
      {
         provider = this.configuration.getProviderForService(serviceName);
      }

      if (provider == null)
      {
         //lets get the provider based on token type
         String tokenType = protocolContext.tokenType();
         if (tokenType != null)
            provider = this.configuration.getProviderForTokenType(protocolContext.tokenType());
      }
      return provider;
   }

   private SecurityTokenProvider getProviderBasedOnQName(ProtocolContext protocolContext) throws ProcessingException
   {
      SecurityTokenProvider provider = null;

      QName qname = null;
      if (provider == null)
      {
         qname = protocolContext.getQName();
         if (qname == null)
            throw new ProcessingException(ErrorCodes.NULL_VALUE + "QName of the token type");
         provider = this.configuration.getProviderForTokenElementNS(protocolContext.family(), qname);
      }

      if (provider == null)
         throw new ProcessingException(ErrorCodes.STS_NO_TOKEN_PROVIDER + qname.getNamespaceURI() + ":"
               + qname.getLocalPart());

      return provider;
   }

   /**
    * <p>
    * Obtains the STS configuration options.
    * </p>
    * 
    * @return an instance of {@code STSConfiguration} containing the STS configuration properties.
    */
   protected STSConfiguration getConfiguration(String fileName) throws ConfigurationException
   {
      URL configurationFileURL = null;

      try
      {
         // check the user home for a configuration file generated by the picketlink console.
         String configurationFilePath = System.getProperty("user.home") + SEPARATOR + STS_CONFIG_DIR + fileName;
         File configurationFile = new File(configurationFilePath);
         if (configurationFile.exists())
            configurationFileURL = configurationFile.toURI().toURL();
         else
         {
            // if not configuration file was found in the user home, check the context classloader. 
            configurationFileURL = SecurityActions.loadResource(getClass(), fileName);
         }

         // if no configuration file was found, log a warn message and use default configuration values.
         if (configurationFileURL == null)
         {
            logger.warn(fileName + " configuration file not found using TCCL");
            ClassLoader clazzLoader = SecurityActions.getClassLoader(getClass());
            configurationFileURL = clazzLoader.getResource(fileName);
         }

         // if no configuration file was found, log a warn message and use default configuration values.
         if (configurationFileURL == null)
         {
            logger.warn(fileName + " configuration file not found using classloader");
            try
            {
               configurationFileURL = new URL(fileName);
            }
            catch (Exception e)
            {
               return new PicketLinkSTSConfiguration();
            }
            finally
            {
               if (configurationFileURL == null)
               {
                  logger.warn(fileName + " configuration file not found using URL. Using default configuration values");
                  return new PicketLinkSTSConfiguration();
               }
            }
         }

         InputStream stream = configurationFileURL.openStream();
         STSType stsConfig = (STSType) new STSConfigParser().parse(stream);
         STSConfiguration configuration = new PicketLinkSTSConfiguration(stsConfig);
         if (logger.isInfoEnabled())
            logger.info(fileName + " configuration file loaded");
         return configuration;
      }
      catch (Exception e)
      {
         throw new ConfigurationException(ErrorCodes.STS_CONFIGURATION_FILE_PARSING_ERROR, e);
      }
   }
}