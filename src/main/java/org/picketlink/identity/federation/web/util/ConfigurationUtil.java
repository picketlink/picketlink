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

import java.io.InputStream;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.parsers.config.SAMLConfigParser;

/**
 * Deals with Configuration
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public class ConfigurationUtil
{
   /**
    * Get the IDP Configuration
    * from the passed configuration
    * @param is
    * @return 
    * @throws ParsingException 
    */
   public static IDPType getIDPConfiguration(InputStream is) throws ParsingException
   {
      if (is == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "inputstream");

      SAMLConfigParser parser = new SAMLConfigParser();
      return (IDPType) parser.parse(is);
   }

   /**
    * Get the SP Configuration from the
    * passed inputstream
    * @param is
    * @return 
    * @throws ParsingException 
    */
   public static SPType getSPConfiguration(InputStream is) throws ParsingException
   {
      if (is == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "inputstream");
      return (SPType) (new SAMLConfigParser()).parse(is);
   }

   /**
    * Get the Handlers from the configuration
    * @param is
    * @return 
    * @throws ParsingException 
    */
   public static Handlers getHandlers(InputStream is) throws ParsingException
   {
      if (is == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "inputstream");
      return (Handlers) (new SAMLConfigParser()).parse(is);
   }
}