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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.realm.GenericPrincipal;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;

/**
 * Generate roles from Tomcat Principal
 * @author Anil.Saldhana@redhat.com
 * @since Jan 21, 2009
 */
public class TomcatRoleGenerator implements RoleGenerator
{
   /**
    * @see RoleGenerator#generateRoles(Principal)
    * @throws IllegalArgumentException if principal is not of type GenericPrincipal or User
    */
   public List<String> generateRoles(Principal principal)
   {
      String className = principal.getClass().getCanonicalName();

      if (principal instanceof GenericPrincipal == false && principal instanceof User == false)
         throw new IllegalArgumentException(ErrorCodes.WRONG_TYPE + "principal is not tomcat principal:" + className);
      List<String> userRoles = new ArrayList<String>();

      if (principal instanceof GenericPrincipal)
      {
         GenericPrincipal gp = (GenericPrincipal) principal;
         String[] roles = gp.getRoles();
         if (roles.length > 0)
            userRoles.addAll(Arrays.asList(roles));
      }
      else if (principal instanceof User)
      {
         User tomcatUser = (User) principal;
         Iterator<?> iter = tomcatUser.getRoles();
         while (iter.hasNext())
         {
            Role tomcatRole = (Role) iter.next();
            userRoles.add(tomcatRole.getRolename());
         }
      }
      return userRoles;
   }
}