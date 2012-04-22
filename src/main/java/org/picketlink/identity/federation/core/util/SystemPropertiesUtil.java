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
package org.picketlink.identity.federation.core.util;

import javax.xml.XMLConstants;

/**
 * Utility dealing with the system properties at the JVM level
 * for PicketLink
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SystemPropertiesUtil
{
   static
   {
      //XML Signature
      String xmlSec = "org.apache.xml.security.ignoreLineBreaks";
      if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(xmlSec, "")))
      {
         SecurityActions.setSystemProperty(xmlSec, "true");
      }

      //For JAXP Validation
      String schemaFactoryProperty = "javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
      if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(schemaFactoryProperty, "")))
      {
         SecurityActions.setSystemProperty(schemaFactoryProperty, "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
      }

      //For the XACML Engine
      String xacmlValidation = "org.jboss.security.xacml.schema.validation";
      if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(xacmlValidation, "")))
      {
         SecurityActions.setSystemProperty(xacmlValidation, "false");
      }
   };

   /**
    * No-op call such that the default system properties are set
    */
   public static void ensure()
   {
   }
}