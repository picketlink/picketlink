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
package org.picketlink.identity.federation.web.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

/**
 * A properties file based {@link SAMLConfigurationProvider}.
 * For the IDP configuration, a idp_config.properties is expected.
 * For the SP configuration, a sp_config.properties is expected.
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Aug 9, 2011
 */
public class PropertiesConfigurationProvider implements SAMLConfigurationProvider
{
   public static final String IDP_FILE = "idp_config.properties";

   public static final String SP_FILE = "sp_config.properties";

   public IDPType getIDPConfiguration() throws ProcessingException
   {
      InputStream is = SecurityActions.loadStream(getClass(), IDP_FILE);
      if (is == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + IDP_FILE);
      Properties props = new Properties();
      try
      {
         props.load(is);
      }
      catch (IOException e)
      {
         throw new ProcessingException(e);
      }
      IDPType idp = new IDPType();
      idp.setIdentityURL(props.getProperty("idp.url"));
      String domains = props.getProperty("domains");
      if (StringUtil.isNotNull(domains))
      {
         TrustType trustType = new TrustType();
         trustType.setDomains(domains);
         idp.setTrust(trustType);
      }

      return idp;
   }

   public SPType getSPConfiguration() throws ProcessingException
   {
      InputStream is = SecurityActions.loadStream(getClass(), SP_FILE);
      if (is == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + SP_FILE);
      Properties props = new Properties();
      try
      {
         props.load(is);
      }
      catch (IOException e)
      {
         throw new ProcessingException(e);
      }
      SPType sp = new SPType();
      sp.setIdentityURL(props.getProperty("idp.url"));
      sp.setServiceURL("service.url");
      String domains = props.getProperty("domains");
      if (StringUtil.isNotNull(domains))
      {
         TrustType trustType = new TrustType();
         trustType.setDomains(domains);
         sp.setTrust(trustType);
      }

      return sp;
   }
}