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
package org.picketlink.trust.jbossws.util;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;
 

/**
 * Utility class that uses reflection on the
 * JBossWS Native Stack as backup strategy
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Jul 13, 2011
 */
public class JBossWSNativeStackUtil
{
   protected static Logger log = Logger.getLogger(JBossWSNativeStackUtil.class);
   protected static boolean trace = log.isTraceEnabled();
   
   /**
    * It is unfortunate that the {@link MessageContext} does not contain the port name.
    * We will use reflection on the JBoss WS Native stack
    * @param msgContext
    * @return
    */
   public static QName getPortNameViaReflection(Class<?> callingClazz, MessageContext msgContext)
   {
      try
      {
         Class<?> clazz = SecurityActions.getClassLoader(callingClazz).loadClass("org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS");
         Method endpointMDMethod = clazz.getMethod("getEndpointMetaData", new Class[0]);
         Object endpointMD = endpointMDMethod.invoke(msgContext, new Object[0]);
         
         clazz = SecurityActions.getClassLoader(callingClazz).loadClass("org.jboss.ws.metadata.umdm.EndpointMetaData");
         Method portNameMethod = clazz.getMethod("getPortName", new Class[0]);
          
         return (QName) portNameMethod.invoke(endpointMD, new Object[0]);
      }
      catch (Exception e)
      { 
         if(trace)
            log.trace("Exception using backup method to get port name=",e);
      } 
      return null;
   } 
}