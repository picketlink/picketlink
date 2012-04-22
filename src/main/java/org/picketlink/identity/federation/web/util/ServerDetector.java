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
package org.picketlink.identity.federation.web.util;

/**
 * Utility Class to detect which server
 * we are currently operating in
 * @author Anil.Saldhana@redhat.com
 * @since Sep 11, 2009
 */
public class ServerDetector
{
   private boolean jboss = false;

   private boolean tomcat = false;

   public ServerDetector()
   {
      this.detectServer();
   }

   public boolean isJboss()
   {
      return jboss;
   }

   public boolean isTomcat()
   {
      return tomcat;
   }

   private void detectServer()
   {
      //Detect JBoss 
      Class<?> me = getClass();
      Class<?> clazz = null;
      try
      {
         clazz = SecurityActions.loadClass(me, "org.jboss.system.Service");
         if (clazz != null)
         {
            jboss = true;
            return;
         }
      }
      catch (Exception e)
      {
      }

      //If class is null - try the JBossAS7 and beyond
      try
      {
         clazz = SecurityActions.loadClass(me, "org.jboss.as.web.WebServer");
         if (clazz != null)
         {
            jboss = true;
            return;
         }
      }
      catch (Exception e)
      {
      }

      //Tomcat
      try
      {
         clazz = SecurityActions.loadClass(getClass(), "org.apache.catalina.Server");
         if (clazz != null)
         {
            tomcat = true;
            return;
         }
      }
      catch (Exception e)
      {
         //ignore  
      }
   }
}