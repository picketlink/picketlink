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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Utility to obtain JAXB2 marshaller/unmarshaller etc
 * @author Anil.Saldhana@redhat.com
 * @since May 26, 2009
 */
public class JAXBUtil
{
   private static Logger log = Logger.getLogger(JAXBUtil.class);

   private static boolean trace = log.isTraceEnabled();

   public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

   private static HashMap<String, JAXBContext> jaxbContextHash = new HashMap<String, JAXBContext>();

   static
   {
      //Useful on Sun VMs.  Harmless on other VMs.
      SecurityActions.setSystemProperty("com.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot", "true");
   }

   /**
    * Get the JAXB Marshaller
    * @param pkgName The package name for the jaxb context
    * @param schemaLocation location of the schema to validate against 
    * @return Marshaller 
    * @throws JAXBException 
    * @throws SAXException 
    */
   public static Marshaller getValidatingMarshaller(String pkgName, String schemaLocation) throws JAXBException,
         SAXException
   {
      Marshaller marshaller = getMarshaller(pkgName);

      //Validate against schema
      Schema schema = getJAXPSchemaInstance(schemaLocation);
      marshaller.setSchema(schema);

      return marshaller;
   }

   /**
    * Get the JAXB Marshaller
    * @param pkgName The package name for the jaxb context  
    * @return Marshaller 
    * @throws JAXBException 
    */
   public static Marshaller getMarshaller(String pkgName) throws JAXBException
   {
      if (pkgName == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "pkgName");

      JAXBContext jc = getJAXBContext(pkgName);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE); //Breaks signatures
      return marshaller;
   }

   /**
    * Get the JAXB Unmarshaller
    * @param pkgName The package name for the jaxb context 
    * @return unmarshaller
    * @throws JAXBException  
    */
   public static Unmarshaller getUnmarshaller(String pkgName) throws JAXBException
   {
      if (pkgName == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "pkgName");
      JAXBContext jc = getJAXBContext(pkgName);
      return jc.createUnmarshaller();
   }

   /**
    * Get the JAXB Unmarshaller for a selected set
    * of package names
    * @param pkgNames
    * @return
    * @throws JAXBException
    */
   public static Unmarshaller getUnmarshaller(String... pkgNames) throws JAXBException
   {
      if (pkgNames == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "pkgName");
      int len = pkgNames.length;
      if (len == 0)
         return getUnmarshaller(pkgNames[0]);

      JAXBContext jc = getJAXBContext(pkgNames);
      return jc.createUnmarshaller();
   }

   /**
    * Get the JAXB Unmarshaller
    * @param pkgName The package name for the jaxb context
    * @param schemaLocation location of the schema to validate against 
    * @return unmarshaller
    * @throws JAXBException 
    * @throws SAXException  
    */
   public static Unmarshaller getValidatingUnmarshaller(String pkgName, String schemaLocation) throws JAXBException,
         SAXException
   {
      Unmarshaller unmarshaller = getUnmarshaller(pkgName);
      Schema schema = getJAXPSchemaInstance(schemaLocation);
      unmarshaller.setSchema(schema);

      return unmarshaller;
   }

   public static Unmarshaller getValidatingUnmarshaller(String[] pkgNames, String[] schemaLocations)
         throws JAXBException, SAXException, IOException
   {
      StringBuilder builder = new StringBuilder();
      int len = pkgNames.length;
      if (len == 0)
         throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + "Packages are empty");

      for (String pkg : pkgNames)
      {
         builder.append(pkg);
         builder.append(":");
      }

      Unmarshaller unmarshaller = getUnmarshaller(builder.toString());

      SchemaFactory schemaFactory = getSchemaFactory();

      //Get the sources
      Source[] schemaSources = new Source[schemaLocations.length];

      int i = 0;
      for (String schemaLocation : schemaLocations)
      {
         URL schemaURL = SecurityActions.loadResource(JAXBUtil.class, schemaLocation);
         if (schemaURL == null)
            throw new IllegalStateException(ErrorCodes.NULL_VALUE + "Schema URL :" + schemaLocation);

         schemaSources[i++] = new StreamSource(schemaURL.openStream());
      }

      Schema schema = schemaFactory.newSchema(schemaSources);
      unmarshaller.setSchema(schema);

      return unmarshaller;
   }

   private static Schema getJAXPSchemaInstance(String schemaLocation) throws SAXException
   {
      URL schemaURL = SecurityActions.loadResource(JAXBUtil.class, schemaLocation);
      if (schemaURL == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "Schema URL :" + schemaLocation);
      SchemaFactory scFact = getSchemaFactory();
      Schema schema = scFact.newSchema(schemaURL);
      return schema;
   }

   private static SchemaFactory getSchemaFactory()
   {
      SchemaFactory scFact = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);

      //Always install the resolver unless the system property is set
      if (SecurityActions.getSystemProperty("org.picketlink.identity.federation.jaxb.ls", null) == null)
         scFact.setResourceResolver(new IDFedLSInputResolver());

      scFact.setErrorHandler(new ErrorHandler()
      {
         public void error(SAXParseException exception) throws SAXException
         {
            StringBuilder builder = new StringBuilder();
            builder.append("Line Number=").append(exception.getLineNumber());
            builder.append(" Col Number=").append(exception.getColumnNumber());
            builder.append(" Public ID=").append(exception.getPublicId());
            builder.append(" System ID=").append(exception.getSystemId());
            builder.append(" exc=").append(exception.getLocalizedMessage());

            if (trace)
               log.trace("SAX Error:" + builder.toString());
         }

         public void fatalError(SAXParseException exception) throws SAXException
         {
            StringBuilder builder = new StringBuilder();
            builder.append("Line Number=").append(exception.getLineNumber());
            builder.append(" Col Number=").append(exception.getColumnNumber());
            builder.append(" Public ID=").append(exception.getPublicId());
            builder.append(" System ID=").append(exception.getSystemId());
            builder.append(" exc=").append(exception.getLocalizedMessage());

            log.error("SAX Fatal Error:" + builder.toString());
         }

         public void warning(SAXParseException exception) throws SAXException
         {
            StringBuilder builder = new StringBuilder();
            builder.append("Line Number=").append(exception.getLineNumber());
            builder.append(" Col Number=").append(exception.getColumnNumber());
            builder.append(" Public ID=").append(exception.getPublicId());
            builder.append(" System ID=").append(exception.getSystemId());
            builder.append(" exc=").append(exception.getLocalizedMessage());

            if (trace)
               log.trace("SAX Warn:" + builder.toString());
         }
      });
      return scFact;
   }

   public static JAXBContext getJAXBContext(String path) throws JAXBException
   {
      JAXBContext jx = jaxbContextHash.get(path);
      if (jx == null)
      {
         jx = JAXBContext.newInstance(path);
         jaxbContextHash.put(path, jx);
      }
      return jx;
   }

   public static JAXBContext getJAXBContext(String... paths) throws JAXBException
   {
      int len = paths.length;
      if (len == 0)
         return getJAXBContext(paths[0]);

      StringBuilder builder = new StringBuilder();
      for (String path : paths)
      {
         builder.append(path).append(":");
      }

      String finalPath = builder.toString();

      JAXBContext jx = jaxbContextHash.get(finalPath);
      if (jx == null)
      {
         jx = JAXBContext.newInstance(finalPath);
         jaxbContextHash.put(finalPath, jx);
      }
      return jx;
   }

   public static JAXBContext getJAXBContext(Class<?> clazz) throws JAXBException
   {
      String clazzName = clazz.getName();

      JAXBContext jx = jaxbContextHash.get(clazzName);
      if (jx == null)
      {
         jx = JAXBContext.newInstance(clazz);
         jaxbContextHash.put(clazzName, jx);
      }
      return jx;
   }
}