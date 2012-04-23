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
package org.picketlink.identity.federation.bindings.jboss.auth;

import java.security.Principal;
import java.security.acl.Group;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;
import org.picketlink.identity.federation.bindings.tomcat.sp.holder.ServiceProviderSAMLContext;
import org.picketlink.identity.federation.core.util.StringUtil;

/**
 * Login Module that is capable of dealing with SAML2 cases
 * <p>
 * The password sent to this module should be 
 * {@link ServiceProviderSAMLContext#EMPTY_PASSWORD}
 * </p>
 * <p>
 * The username is available from {@link ServiceProviderSAMLContext#getUserName()}
 * and roles is available from {@link ServiceProviderSAMLContext#getRoles()}.
 * If the roles is null, then plugged in login modules in the stack have to provide
 * the roles.
 * </p>
 * @author Anil.Saldhana@redhat.com
 * @since Feb 13, 2009
 */
public abstract class SAML2CommonLoginModule extends UsernamePasswordLoginModule
{

   protected String groupName = "Roles";

   /*
    * (non-Javadoc)
    * @see org.jboss.security.auth.spi.AbstractServerLoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
    */
   @Override
   public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
         Map<String, ?> options)
   {
      super.initialize(subject, callbackHandler, sharedState, options);
      String groupNameStr = (String) options.get("groupPrincipalName");
      if (StringUtil.isNotNull(groupNameStr))
      {
         groupName = groupNameStr.trim();
      }
   }

   @Override
   protected Principal getIdentity()
   {
      return new SimplePrincipal(ServiceProviderSAMLContext.getUserName());
   }

   @Override
   protected Group[] getRoleSets() throws LoginException
   {
      Group group = new SimpleGroup(groupName);

      List<String> roles = ServiceProviderSAMLContext.getRoles();
      if (roles != null)
      {
         for (String role : roles)
         {
            group.addMember(new SimplePrincipal(role));
         }
      }
      return new Group[]
      {group};
   }

   @Override
   protected String getUsersPassword() throws LoginException
   {
      return ServiceProviderSAMLContext.EMPTY_PASSWORD;
   }
}