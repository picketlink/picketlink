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
import org.picketlink.identity.federation.core.parsers.config.SAMLConfigParser;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

/**
 * An abstact class to hold the common functionality across providers
 * @author Anil Saldhana
 * @since Feb 22, 2012
 */
public abstract class AbstractSAMLConfigurationProvider implements SAMLConfigurationProvider
{
   public static final String VALIDATING_ALIAS = "ValidatingAlias";

   protected IDPType configParsedIDPType = null;

   protected SPType configParsedSPType = null;

   public void setConfigFile(InputStream is) throws ParsingException
   {
      if (is == null)
      {
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT);
      }

      SAMLConfigParser parser = new SAMLConfigParser();
      Object parsedObject = parser.parse(is);
      if (parsedObject instanceof IDPType)
         configParsedIDPType = (IDPType) parsedObject;
      else
         configParsedSPType = (SPType) parsedObject;
   }

   public abstract IDPType getIDPConfiguration() throws ProcessingException;

   public abstract SPType getSPConfiguration() throws ProcessingException;
}