/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.wstrust;

import java.util.Map;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;

/**
 * <p>
 * Factory class used for instantiating pluggable services, such as the {@code WSTrustRequestHandler} and
 * {@code SecurityTokenProvider} implementations.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class WSTrustServiceFactory
{

   private static final WSTrustServiceFactory factory = new WSTrustServiceFactory();

   /**
    * <p>
    * Creates the {@code WSTrustConfigurationFactory} singleton instance.
    * </p>
    */
   private WSTrustServiceFactory()
   {
   }

   /**
    * <p>
    * Obtains a reference to the singleton instance.
    * </p>
    * 
    * @return the {@code WSTrustConfigurationFactory} singleton.
    */
   public static WSTrustServiceFactory getInstance()
   {
      return factory;
   }

   /**
    * <p>
    * Constructs and returns the {@code WSTrustRequestHandler} that will be used to handle WS-Trust requests.
    * </p>
    * 
    * @param configuration a reference to the {@code STSConfiguration}.
    * @return a reference to the constructed {@code WSTrustRequestHandler} object.
    */
   public WSTrustRequestHandler createRequestHandler(String handlerClassName, STSConfiguration configuration)
   {
      try
      {
         Class<?> clazz = SecurityActions.loadClass(getClass(), handlerClassName);
         if (clazz == null)
            throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + handlerClassName);
         WSTrustRequestHandler handler = (WSTrustRequestHandler) clazz.newInstance();
         handler.initialize(configuration);
         return handler;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

   /**
    * <p>
    * Constructs and returns a {@code SecurityTokenProvider} from the specified class name. 
    * </p>
    * 
    * @param providerClass the FQN of the {@code SecurityTokenProvider} to be instantiated.
    * @param properties a {@code Map<String, String>} containing the properties that have been configured for the
    * token provider.
    * @return a reference to the constructed {@code SecurityTokenProvider} object.
    */
   public SecurityTokenProvider createTokenProvider(String providerClass, Map<String, String> properties)
   {
      try
      {
         Class<?> clazz = SecurityActions.loadClass(getClass(), providerClass);
         if (clazz == null)
            throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + providerClass);
         SecurityTokenProvider tokenProvider = (SecurityTokenProvider) clazz.newInstance();
         tokenProvider.initialize(properties);
         return tokenProvider;
      }
      catch (Exception pae)
      {
         throw new RuntimeException(ErrorCodes.CANNOT_CREATE_INSTANCE + providerClass, pae);
      }
   }

   /**
    * <p>
    * Constructs and returns a {@code ClaimsProcessor} from the specified class name. The processor is initialized
    * with the specified properties map. 
    * </p>
    * 
    * @param processorClass the FQN of the {@code ClaimsProcessor} to be instantiated.
    * @param properties a {@code Map<String, String>} containing the properties that have been configured for the 
    * claims processor.
    * @return a reference to the constructed {@code ClaimsProcessor} object.
    */
   public ClaimsProcessor createClaimsProcessor(String processorClass, Map<String, String> properties)
   {
      try
      {
         Class<?> clazz = SecurityActions.loadClass(getClass(), processorClass);
         if (clazz == null)
            throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + processorClass);
         ClaimsProcessor claimsProcessor = (ClaimsProcessor) clazz.newInstance();
         claimsProcessor.initialize(properties);
         return claimsProcessor;
      }
      catch (Exception pae)
      {
         throw new RuntimeException(ErrorCodes.CANNOT_CREATE_INSTANCE + "claims processor " + processorClass, pae);
      }
   }
}