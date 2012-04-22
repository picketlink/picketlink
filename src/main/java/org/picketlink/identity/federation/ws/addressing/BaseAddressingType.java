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
package org.picketlink.identity.federation.ws.addressing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * @author Anil.Saldhana@redhat.com
 * @since May 17, 2011
 */
public class BaseAddressingType
{
   protected final Map<QName, String> otherAttributes = new HashMap<QName, String>();

   /**
    * Add an other attribute
    * @param qname
    * @param str
    */
   public void addOtherAttribute(QName qname, String str)
   {
      otherAttributes.put(qname, str);
   }

   public void addOtherAttributes(Map<QName, String> otherMap)
   {
      otherAttributes.putAll(otherMap);
   }

   /**
    * Gets a map that contains attributes that aren't bound to any typed property on this class.
    * 
    * <p>
    * the map is keyed by the name of the attribute and 
    * the value is the string value of the attribute.
    * 
    * the map returned by this method is live, and you can add new attribute
    * by updating the map directly. Because of this design, there's no setter.
    * 
    * 
    * @return
    *     always non-null
    */
   public Map<QName, String> getOtherAttributes()
   {
      return Collections.unmodifiableMap(otherAttributes);
   }
}