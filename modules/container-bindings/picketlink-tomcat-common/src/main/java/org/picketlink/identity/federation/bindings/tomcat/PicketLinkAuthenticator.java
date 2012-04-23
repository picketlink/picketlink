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
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.FormAuthenticator;
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
public class PicketLinkAuthenticator extends FormAuthenticator
{
   protected static Logger log = Logger.getLogger(PicketLinkAuthenticator.class);

   protected boolean trace = log.isTraceEnabled();

   /**
    * This is the auth method used in the register method
    */
   protected String authMethod = "SECURITY_DOMAIN";

   /**
    * The authenticator may not be aware of the user name until after
    * the underlying security exercise is complete. The Subject
    * will have the proper user name. Hence we may need to perform
    * an additional authentication now with the user name we have obtained.
    */
   protected boolean needSubjectPrincipalSubstitution = true;

   protected SubjectSecurityInteraction subjectInteraction = null;

   protected String subjectInteractionClassName = "org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkJBossSubjectInteraction";

   public PicketLinkAuthenticator()
   {
      if (trace)
      {
         log.trace("PicketLinkAuthenticator Created");
      }
   }

   /**
    * Set the auth method via WEB-INF/context.xml (JBoss AS)
    * @param authMethod
    */
   public void setAuthMethod(String authMethod)
   {
      this.authMethod = authMethod;
   }

   public void setNeedSubjectPrincipalSubstitution(String needSubjectPrincipalSubstitutionVal)
   {
      this.needSubjectPrincipalSubstitution = Boolean.valueOf(needSubjectPrincipalSubstitutionVal);
   }

   /**
    * Set this if you want to override the default {@link SubjectSecurityInteraction}
    * @param subjectRetrieverClassName
    */
   public void setSubjectInteractionClassName(String subjectRetrieverClassName)
   {
      this.subjectInteractionClassName = subjectRetrieverClassName;
   }

   @Override
   public boolean authenticate(Request request, Response response, LoginConfig loginConfig) throws IOException
   {
      log.trace("Authenticating user");

      Principal principal = request.getUserPrincipal();
      if (principal != null)
      {
         if (trace)
            log.trace("Already authenticated '" + principal.getName() + "'");
         return true;
      }

      Session session = request.getSessionInternal(true);
      String userName = UUID.randomUUID().toString();
      String password = userName;
      Realm realm = context.getRealm();

      principal = realm.authenticate(userName, password);
      Principal originalPrincipal = principal;

      if (principal != null)
      {
         if (needSubjectPrincipalSubstitution)
         {
            principal = getSubjectPrincipal();
            if (principal == null)
               throw new RuntimeException("Principal from subject is null");
            principal = realm.authenticate(principal.getName(), password);
         }
         session.setNote(Constants.SESS_USERNAME_NOTE, principal.getName());
         session.setNote(Constants.SESS_PASSWORD_NOTE, password);
         request.setUserPrincipal(principal);
         register(request, response, principal, this.authMethod, principal.getName(), password);
         if (originalPrincipal != null && needSubjectPrincipalSubstitution)
         {
            subjectInteraction.cleanup(originalPrincipal);
         }
         return true;
      }

      return false;
   }

   public boolean authenticate(HttpServletRequest request, HttpServletResponse response, LoginConfig loginConfig)
         throws IOException
   {
      return authenticate((Request) request, (Response) response, loginConfig);
   }

   protected Principal getSubjectPrincipal()
   {
      if (subjectInteraction == null)
      {
         Class<?> clazz = loadClass(getClass(), subjectInteractionClassName);
         try
         {
            subjectInteraction = (SubjectSecurityInteraction) clazz.newInstance();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }

      Subject subject = subjectInteraction.get();
      if (subject != null)
      {
         Set<Principal> principals = subject.getPrincipals();
         if (!principals.isEmpty())
         {
            return subject.getPrincipals().iterator().next();
         }
      }
      return null;
   }

   Class<?> loadClass(final Class<?> theClass, final String fqn)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Class<?>>()
      {
         public Class<?> run()
         {
            ClassLoader classLoader = theClass.getClassLoader();

            Class<?> clazz = loadClass(classLoader, fqn);
            if (clazz == null)
            {
               classLoader = Thread.currentThread().getContextClassLoader();
               clazz = loadClass(classLoader, fqn);
            }
            return clazz;
         }
      });
   }

   Class<?> loadClass(final ClassLoader cl, final String fqn)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Class<?>>()
      {
         public Class<?> run()
         {
            try
            {
               return cl.loadClass(fqn);
            }
            catch (ClassNotFoundException e)
            {
            }
            return null;
         }
      });
   }
}