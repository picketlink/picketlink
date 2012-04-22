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
package org.picketlink.identity.federation.web.roles;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.web.interfaces.IRoleValidator;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public class DefaultRoleValidator implements IRoleValidator
{
   private static Logger log = Logger.getLogger(DefaultRoleValidator.class);
   private boolean trace = log.isTraceEnabled();
   
   private Set<String> roleNames = new HashSet<String>();
   
   public void intialize(Map<String, String> options)
   {
      String csv = options.get("ROLES");
      if(csv == null)
      {
         if(trace)
            log.trace("There is no ROLES config");
      }
      else
      {
         //Get the comma separated role names 
         StringTokenizer st = new StringTokenizer(csv,",");
         while(st != null && st.hasMoreTokens())
         {
            roleNames.add(st.nextToken());
         }  
      }
   }

   public boolean userInRole(Principal userPrincipal, List<String> roles)
   {
      for(String roleName: roles)
      {
         if(roleNames.contains(roleName))
            return true;
      }
      return false;
   }
}