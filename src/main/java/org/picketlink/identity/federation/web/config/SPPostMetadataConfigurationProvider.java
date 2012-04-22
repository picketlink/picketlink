/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.web.config;

import java.io.InputStream;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

/**
 * <p>
 * An instance of {@link SAMLConfigurationProvider} that can be used to generate
 * the SP configuration for the HTTP-POST binding using SAML2 Metadata.
 * </p>
 * <p>
 * This provider uses the following in sequence whichever is available:
 * <ol>
 * <li> a sp-metadata.xml file available in its immediate class path.</li>
 * <li> </li>
 * </ol>
 * </p>
 * @author Anil Saldhana
 * @since Feb 15, 2012
 */
public class SPPostMetadataConfigurationProvider extends AbstractSAMLConfigurationProvider
      implements
         SAMLConfigurationProvider
{
   public static final String SP_MD_FILE = "sp-metadata.xml";

   public static final String bindingURI = JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get();

   /**
    * @see SAMLConfigurationProvider#getIDPConfiguration()
    */
   public IDPType getIDPConfiguration() throws ProcessingException
   {
      throw new RuntimeException(ErrorCodes.ILLEGAL_METHOD_CALLED);
   }

   /**
    * @see SAMLConfigurationProvider#getSPConfiguration()
    */
   public SPType getSPConfiguration() throws ProcessingException
   {
      SPType spType = null;
      if (fileAvailable())
      {
         try
         {
            EntitiesDescriptorType entities = parseMDFile();
            spType = CoreConfigUtil.getSPConfiguration(entities, bindingURI);
         }
         catch (ParsingException e)
         {
            throw new ProcessingException(e);
         }
      }
      if (configParsedSPType != null)
      {
         spType.importFrom(configParsedSPType);
      }
      return spType;
   }

   private boolean fileAvailable()
   {
      InputStream is = SecurityActions.loadStream(getClass(), SP_MD_FILE);
      return is != null;
   }

   private EntitiesDescriptorType parseMDFile() throws ParsingException
   {
      InputStream is = SecurityActions.loadStream(getClass(), SP_MD_FILE);

      if (is == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + SP_MD_FILE);

      SAMLParser parser = new SAMLParser();
      return (EntitiesDescriptorType) parser.parse(is);
   }
}