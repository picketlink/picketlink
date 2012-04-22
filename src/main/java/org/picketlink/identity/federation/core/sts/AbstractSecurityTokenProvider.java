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

import java.util.Map;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.sts.registry.DefaultRevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.DefaultTokenRegistry;
import org.picketlink.identity.federation.core.sts.registry.FileBasedRevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.FileBasedTokenRegistry;
import org.picketlink.identity.federation.core.sts.registry.JPABasedRevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.RevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry;

/**
 * Base Class for instances of {@code SecurityTokenProvider}
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public abstract class AbstractSecurityTokenProvider implements SecurityTokenProvider
{
   protected static Logger logger = Logger.getLogger(AbstractSecurityTokenProvider.class);

   protected static final String TOKEN_REGISTRY = "TokenRegistry";

   protected static final String TOKEN_REGISTRY_FILE = "TokenRegistryFile";

   protected static final String REVOCATION_REGISTRY = "RevocationRegistry";

   protected static final String REVOCATION_REGISTRY_FILE = "RevocationRegistryFile";

   protected static final String REVOCATION_REGISTRY_JPA_CONFIG = "RevocationRegistryJPAConfig";

   protected static final String ATTRIBUTE_PROVIDER = "AttributeProvider";

   protected SecurityTokenRegistry tokenRegistry = new DefaultTokenRegistry();

   protected RevocationRegistry revocationRegistry = new DefaultRevocationRegistry();

   protected Map<String, String> properties;

   public void initialize(Map<String, String> properties)
   {
      this.properties = properties;

      //Check for token registry
      String tokenRegistryOption = this.properties.get(TOKEN_REGISTRY);
      if (tokenRegistryOption == null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Security Token registry option not specified: Issued Tokens will not be persisted!");
      }
      else
      {
         // if a file is to be used as registry, check if the user has specified the file name.
         if ("FILE".equalsIgnoreCase(tokenRegistryOption))
         {
            String tokenRegistryFile = this.properties.get(TOKEN_REGISTRY_FILE);
            if (tokenRegistryFile != null)
               this.tokenRegistry = new FileBasedTokenRegistry(tokenRegistryFile);
            else
               this.tokenRegistry = new FileBasedTokenRegistry();
         }
         // the user has specified its own registry implementation class.
         else
         {
            try
            {
               Class<?> clazz = SecurityActions.loadClass(getClass(), tokenRegistryOption);
               if (clazz != null)
               {
                  Object object = clazz.newInstance();
                  if (object instanceof RevocationRegistry)
                     this.tokenRegistry = (SecurityTokenRegistry) object;
                  else
                  {
                     logger.warn(tokenRegistryOption
                           + " is not an instance of SecurityTokenRegistry - using default registry");
                  }
               }
            }
            catch (Exception pae)
            {
               logger.warn("Error instantiating revocation registry class - using default registry");
               pae.printStackTrace();
            }
         }
      }
      if (this.tokenRegistry == null)
         tokenRegistry = new DefaultTokenRegistry();

      // check if a revocation registry option has been set.
      String registryOption = this.properties.get(REVOCATION_REGISTRY);
      if (registryOption == null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Revocation registry option not specified: cancelled ids will not be persisted!");
      }
      else
      {
         // if a file is to be used as registry, check if the user has specified the file name.
         if ("FILE".equalsIgnoreCase(registryOption))
         {
            String registryFile = this.properties.get(REVOCATION_REGISTRY_FILE);
            if (registryFile != null)
               this.revocationRegistry = new FileBasedRevocationRegistry(registryFile);
            else
               this.revocationRegistry = new FileBasedRevocationRegistry();
         }
         // another option is to use the default JPA registry to store the revoked ids.
         else if ("JPA".equalsIgnoreCase(registryOption))
         {
            String configuration = this.properties.get(REVOCATION_REGISTRY_JPA_CONFIG);
            if (configuration != null)
               this.revocationRegistry = new JPABasedRevocationRegistry(configuration);
            else
               this.revocationRegistry = new JPABasedRevocationRegistry();
         }
         // the user has specified its own registry implementation class.
         else
         {
            try
            {
               Class<?> clazz = SecurityActions.loadClass(getClass(), registryOption);
               if (clazz != null)
               {
                  Object object = clazz.newInstance();
                  if (object instanceof RevocationRegistry)
                     this.revocationRegistry = (RevocationRegistry) object;
                  else
                  {
                     logger.warn(registryOption
                           + " is not an instance of RevocationRegistry - using default registry");
                  }
               }
            }
            catch (Exception pae)
            {
               logger.warn("Error instantiating revocation registry class - using default registry");
               pae.printStackTrace();
            }
         }
      }

      if (this.revocationRegistry == null)
         this.revocationRegistry = new DefaultRevocationRegistry();
   }
}