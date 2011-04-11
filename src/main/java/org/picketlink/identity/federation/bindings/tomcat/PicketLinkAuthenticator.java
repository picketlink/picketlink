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
package org.picketlink.identity.federation.bindings.tomcat;

import java.io.IOException;
import java.security.Principal;

import org.apache.catalina.Realm;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.log4j.Logger;

/**
 * An authenticator that delegates actual authentication to a realm, and in turn to a security
 * manager, by presenting a "conventional" identity. The security manager must accept the
 * conventional identity and generate the real identity for the authenticated principal.
 * 
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 * @author Anil.Saldhana@redhat.com
 * @since Apr 11, 2011
 */
public class PicketLinkAuthenticator extends AuthenticatorBase
{
   protected static Logger log = Logger.getLogger(PicketLinkAuthenticator.class);

   protected boolean trace = log.isTraceEnabled();

   /**
    * The {@link Realm} requires an user name
    */
   protected String userName = "custom-authenticator-user";

   /**
    * The {@link Realm} requires a password
    */
   protected String password = "custom-authenticator-password";

   /**
    * This is the auth method used in the register method
    */
   protected String authMethod = "SECURITY_DOMAIN";

   public PicketLinkAuthenticator()
   {
      if (trace)
      {
         log.trace("PicketLinkAuthenticator Created");
      }
   }

   /**
    * Set the user name via WEB-INF/context.xml (JBoss AS)
    * @param defaultUserName
    */
   public void setUserName(String defaultUserName)
   {
      this.userName = defaultUserName;
   }

   /**
    * Set the password via WEB-INF/context.xml (JBoss AS)
    * @param defaultPassword
    */
   public void setPassword(String defaultPassword)
   {
      this.password = defaultPassword;
   }

   /**
    * Set the auth method via WEB-INF/context.xml (JBoss AS)
    * @param authMethod
    */
   public void setAuthMethod(String authMethod)
   {
      this.authMethod = authMethod;
   }

   @Override
   protected boolean authenticate(Request request, Response response, LoginConfig loginConfig) throws IOException
   {
      Realm realm = context.getRealm();

      Principal principal = realm.authenticate(this.userName, this.password);

      if (principal != null)
      {
         register(request, response, principal, this.authMethod, null, null);
      }

      return true;
   }
}