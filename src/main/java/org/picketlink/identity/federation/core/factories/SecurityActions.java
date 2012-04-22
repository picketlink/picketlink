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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextFactory;
import org.picketlink.identity.federation.core.ErrorCodes;

/**
 * Privileged blocks
 * 
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @version $Revision: 1 $
 */
class SecurityActions
{
   static SecurityContext createSecurityContext() throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>()
      {
         public SecurityContext run() throws Exception
         {
            return SecurityContextFactory.createSecurityContext("CLIENT");
         }
      });
   }

   static MBeanServer getJBossMBeanServer()
   {
      return AccessController.doPrivileged(new PrivilegedAction<MBeanServer>()
      {
         public MBeanServer run()
         {
            //Differences in JBAS5.1, 6.0 with the "jboss" mbean server.
            MBeanServer cached = null;

            for (Iterator<MBeanServer> i = MBeanServerFactory.findMBeanServer(null).iterator(); i.hasNext();)
            {
               MBeanServer server = i.next();

               String defaultDomain = server.getDefaultDomain();

               if (defaultDomain != null)
               {
                  if (defaultDomain.contains("Default"))
                     cached = server;

                  if (defaultDomain.equals("jboss"))
                  {
                     return server;
                  }
               }
            }
            if (cached != null)
               return cached; //We did not find one with jboss but there is "DefaultDomain" which is the norm in AS6
            throw new IllegalStateException(ErrorCodes.NULL_VALUE + "No 'jboss' MBeanServer found!");
         }
      });

   }
}