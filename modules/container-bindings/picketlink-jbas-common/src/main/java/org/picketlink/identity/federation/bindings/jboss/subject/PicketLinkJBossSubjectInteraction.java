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
package org.picketlink.identity.federation.bindings.jboss.subject;

import java.security.Principal;
import java.util.Calendar;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;
import org.picketlink.identity.federation.bindings.tomcat.SubjectSecurityInteraction;
import org.picketlink.identity.federation.core.factories.JBossAuthCacheInvalidationFactory;
import org.picketlink.identity.federation.core.factories.JBossAuthCacheInvalidationFactory.TimeCacheExpiry;

/**
 * An implementation of {@link SubjectSecurityInteraction} for JBoss AS
 * @author Anil.Saldhana@redhat.com
 * @since Sep 13, 2011
 */
public class PicketLinkJBossSubjectInteraction implements SubjectSecurityInteraction
{
   protected static Logger log = Logger.getLogger(PicketLinkJBossSubjectInteraction.class);

   protected boolean trace = log.isTraceEnabled();

   /**
    * @see org.picketlink.identity.federation.bindings.tomcat.SubjectSecurityInteraction#cleanup(java.security.Principal)
    */
   public boolean cleanup(Principal principal)
   {
      try
      {
         String securityDomain = getSecurityDomain();
         if (trace)
         {
            log.trace("Determined Security Domain=" + securityDomain);
         }
         TimeCacheExpiry cacheExpiry = JBossAuthCacheInvalidationFactory.getCacheExpiry();
         Calendar calendar = Calendar.getInstance();
         calendar.add(Calendar.SECOND, 10);//Add 25 seconds
         if (trace)
         {
            log.trace("Will expire from cache in 10 seconds, principal=" + principal);
         }
         cacheExpiry.register(securityDomain, calendar.getTime(), principal);
         //Additional expiry of simple principal
         cacheExpiry.register(securityDomain, calendar.getTime(), new SimplePrincipal(principal.getName()));
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }

      return false;
   }

   /**
    * @see org.picketlink.identity.federation.bindings.tomcat.SubjectSecurityInteraction#get()
    */
   public Subject get()
   {
      try
      {
         return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
      }
      catch (PolicyContextException e)
      {
         throw new RuntimeException(e);
      }
   }

   private String getSecurityDomain() throws NamingException
   {
      //Get the SecurityManagerService from JNDI
      InitialContext ctx = new InitialContext();
      SubjectSecurityManager ssm = (SubjectSecurityManager) ctx.lookup("java:comp/env/security/securityMgr");
      if (ssm == null)
         throw new RuntimeException("Unable to get the subject security manager");
      return ssm.getSecurityDomain();
   }
}