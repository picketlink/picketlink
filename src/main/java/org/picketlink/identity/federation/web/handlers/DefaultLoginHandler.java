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
package org.picketlink.identity.federation.web.handlers;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.web.interfaces.ILoginHandler;

/**
 * Default LoginHandler that uses a properties file
 * in the classpath called as users.properties whose
 * format is 
 * username=password
 * @author Anil.Saldhana@redhat.com
 * @since Aug 18, 2009
 */
public class DefaultLoginHandler implements ILoginHandler
{
   private static Properties props = new Properties();

   static
   {
      try
      {
         URL url = SecurityActions.loadResource(DefaultLoginHandler.class, "users.properties");
         if (url == null)
            throw new RuntimeException(ErrorCodes.RESOURCE_NOT_FOUND + "users.properties not found");
         props.load(url.openStream());
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public boolean authenticate(String username, Object credential) throws LoginException
   {
      String pass = null;
      if (credential instanceof byte[])
      {
         pass = new String((byte[]) credential);
      }
      else if (credential instanceof String)
      {
         pass = (String) credential;
      }
      else
         throw new RuntimeException(ErrorCodes.UNSUPPORTED_TYPE + "Unknown credential type:" + credential.getClass());

      String storedPass = (String) props.get(username);
      return storedPass != null ? storedPass.equals(pass) : false;
   }

}