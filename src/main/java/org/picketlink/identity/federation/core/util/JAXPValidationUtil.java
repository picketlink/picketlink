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
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Utility class associated with JAXP Validation
 * @author Anil.Saldhana@redhat.com
 * @since Jun 30, 2011
 */
public class JAXPValidationUtil
{
   protected static Logger log = Logger.getLogger(JAXPValidationUtil.class);

   protected static boolean trace = log.isTraceEnabled();

   protected static Validator validator;

   protected static SchemaFactory schemaFactory;

   public static void validate(String str) throws SAXException, IOException
   {
      validator().validate(new StreamSource(str));
   }

   public static void validate(InputStream stream) throws SAXException, IOException
   {
      validator().validate(new StreamSource(stream));
   }

   /**
    * Based on system property "picketlink.schema.validate" set to "true",
    * do schema validation
    * @param samlDocument
    * @throws ProcessingException
    */
   public static void checkSchemaValidation(Node samlDocument) throws ProcessingException
   {
      if (SecurityActions.getSystemProperty("picketlink.schema.validate", "false").equalsIgnoreCase("true"))
      {
         try
         {
            JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(samlDocument));
         }
         catch (Exception e)
         {
            throw new ProcessingException(e);
         }
      }
   }

   public static Validator validator() throws SAXException, IOException
   {
      SystemPropertiesUtil.ensure();

      if (validator == null)
      {
         Schema schema = getSchema();
         if (schema == null)
            throw new RuntimeException(ErrorCodes.NULL_VALUE + "schema");

         validator = schema.newValidator();
         validator.setErrorHandler(new CustomErrorHandler());
      }
      return validator;
   }

   private static Schema getSchema() throws IOException
   {
      schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

      schemaFactory.setResourceResolver(new IDFedLSInputResolver());
      schemaFactory.setErrorHandler(new CustomErrorHandler());
      Schema schemaGrammar = null;
      try
      {
         schemaGrammar = schemaFactory.newSchema(sources());
      }
      catch (SAXException e)
      {
         log.error("Cannot get schema", e);
      }
      return schemaGrammar;
   }

   private static Source[] sources() throws IOException
   {
      List<String> schemas = SchemaManagerUtil.getSchemas();

      Source[] sourceArr = new Source[schemas.size()];

      int i = 0;
      for (String schema : schemas)
      {
         URL url = SecurityActions.loadResource(JAXPValidationUtil.class, schema);
         if (url == null)
            throw new RuntimeException(ErrorCodes.NULL_VALUE + "schema url:" + schema);
         sourceArr[i++] = new StreamSource(url.openStream());
      }
      return sourceArr;
   }

   private static class CustomErrorHandler implements ErrorHandler
   {
      public void error(SAXParseException ex) throws SAXException
      {
         logException(ex);
         if (ex.getMessage().contains("null") == false)
         {
            throw ex;
         }
      }

      public void fatalError(SAXParseException ex) throws SAXException
      {
         logException(ex);
         throw ex;
      }

      public void warning(SAXParseException ex) throws SAXException
      {
         logException(ex);
      }

      private void logException(SAXParseException sax)
      {
         StringBuilder builder = new StringBuilder();

         if (trace)
         {
            builder.append("[line:").append(sax.getLineNumber()).append(",").append("::col=")
                  .append(sax.getColumnNumber()).append("]");
            builder.append("[publicID:").append(sax.getPublicId()).append(",systemId=").append(sax.getSystemId())
                  .append("]");
            builder.append(":").append(sax.getLocalizedMessage());
            log.trace(builder.toString());
         }
      }
   };
}