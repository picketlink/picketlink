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
package org.picketlink.identity.federation.core.factories;

import java.security.Principal;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * <p>
 * A factory that is used to obtain an expiration policy of type {@link TimeCacheExpiry}
 * </p>
 * <p>
 * Primarily used to expire the SAML Principal in the JAAS Subject cached in the JBoss Auth Cache.
 * </p>
 * @author Anil.Saldhana@redhat.com
 * @since Feb 7, 2011
 */
public class JBossAuthCacheInvalidationFactory
{
   /**
    * Get an instance of {@link TimeCacheExpiry}
    * @return
    */
   public static TimeCacheExpiry getCacheExpiry()
   {
      return ExpiringPrincipalCacheInvalidation.get();
   }

   public interface TimeCacheExpiry
   {
      /**
       * Register a Principal that has an expiry at {@link Date}
       * @param securityDomain the security domain under which the principal may be cached in a subject
       * @param expiry when to expire the principal and hence the subject
       * @param principal the principal which needs to be expired
       */
      void register(String securityDomain, Date expiry, Principal principal);
   }

   protected static class ExpiringPrincipalCacheInvalidation implements TimeCacheExpiry
   {
      private static Logger log = Logger.getLogger(ExpiringPrincipalCacheInvalidation.class);

      private final boolean trace = log.isTraceEnabled();

      protected static ExpiringPrincipalCacheInvalidation _instance = null;

      protected static String objectName = "jboss.security:service=JaasSecurityManager";

      protected static Timer timer = new Timer();

      protected ExpiringPrincipalCacheInvalidation()
      {
      }

      protected static ExpiringPrincipalCacheInvalidation get()
      {
         if (_instance == null)
            _instance = new ExpiringPrincipalCacheInvalidation();
         return _instance;
      }

      protected static void setObjectName(String oName)
      {
         objectName = oName;
      }

      public void register(final String securityDomain, final Date expiry, final Principal principal)
      {
         try
         {
            timer.purge();
         }
         catch (Exception e)
         {
            if (trace)
            {
               log.trace("Exception in purging timer tasks:", e);
            }
         }
         try
         {
            timer.schedule(new TimerTask()
            {
               @Override
               public void run()
               {
                  try
                  {
                     ObjectName on = new ObjectName(objectName);
                     MBeanServer server = SecurityActions.getJBossMBeanServer();
                     Object[] obj = new Object[]
                     {securityDomain, principal};
                     String[] sig = new String[]
                     {"java.lang.String", "java.security.Principal"};

                     //Flush the Authentication Cache
                     server.invoke(on, "flushAuthenticationCache", obj, sig);
                  }
                  catch (Exception e)
                  {
                     if (trace)
                     {
                        log.trace("Exception in scheduling timer:", e);
                     }
                  }
               }
            }, expiry);
         }
         catch (Exception e)
         {
            if (trace)
            {
               log.trace("Exception in scheduling timer:", e);
            }
         }
      }
   }
}