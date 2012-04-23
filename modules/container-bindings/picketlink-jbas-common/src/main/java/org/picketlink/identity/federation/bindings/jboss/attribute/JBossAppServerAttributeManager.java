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
package org.picketlink.identity.federation.bindings.jboss.attribute;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.identity.Attribute;
import org.jboss.security.mapping.MappingContext;
import org.jboss.security.mapping.MappingManager;
import org.jboss.security.mapping.MappingType;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;

/**
 * An attribute manager implementation for JBAS
 * @author Anil.Saldhana@redhat.com
 * @since Sep 8, 2009
 */
public class JBossAppServerAttributeManager implements AttributeManager
{
   private static Logger log = Logger.getLogger(JBossAppServerAttributeManager.class);
   private boolean trace = log.isTraceEnabled();
   
   /**
    * @see AttributeManager#getAttributes(Principal, List)
    */
   public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys)
   { 
      Map<String,Object> attributeMap = new HashMap<String, Object>();
      
      SecurityContext sc = SecurityActions.getSecurityContext();
      if(sc != null)
      {
         String mappingType = MappingType.ATTRIBUTE.name();
         MappingManager mm = sc.getMappingManager();
         MappingContext<List<Attribute<Object>>> mc = mm.getMappingContext(mappingType) ;
         
         if(mc == null)
         {
            log.error("Mapping Context returned is null");
            return attributeMap;
         }
         
         Map<String, Object> contextMap = new HashMap<String, Object>();
         contextMap.put(SecurityConstants.PRINCIPAL_IDENTIFIER, userPrincipal);
         
         
         List<Attribute<Object>> attList = new ArrayList<Attribute<Object>>();
         
         try
         {
            mc.performMapping(contextMap, attList); 
         }
         catch(Exception e)
         {
            log.error("Exception in attribute mapping:", e);
         }
         attList = (List<Attribute<Object>>) mc.getMappingResult().getMappedObject(); 
         
         if(attList != null)
         {
            for(Attribute<Object> attribute: attList)
            {
               attributeMap.put(attribute.getName(),attribute.getValue());
            } 
         } 
      }
      else
      {
         if(trace)
         {
            log.trace("Could not obtain security context.");
         }
      }
      
      if(trace && attributeMap != null)
         log.trace("Final attribute map size:" + attributeMap.size());
      
      return attributeMap;
   }
}