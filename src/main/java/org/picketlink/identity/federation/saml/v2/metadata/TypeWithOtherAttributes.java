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
package org.picketlink.identity.federation.saml.v2.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Abstract base class for types that can have extra attributes
 * @author Anil.Saldhana@redhat.com
 * @since Dec 10, 2010
 */
public abstract class TypeWithOtherAttributes
{
   protected Map<QName, String> otherAttributes = new HashMap<QName, String>();
   
   /**
    * Add other attribute
    * @param qame
    * @param value
    */
   public void addOtherAttribute( QName qame, String value )
   {
      otherAttributes.put(qame, value);
   }
   
   /**
    * Remove other attribute
    * @param qame
    * @param value
    */
   public void removeOtherAttribute( QName qame )
   {
      otherAttributes.remove( qame );
   }
   
   /**
    * Gets a map that contains attributes that aren't bound to any typed property on this class.
    *  
    * 
    * @return
    *     always non-null
    */
   public Map<QName, String> getOtherAttributes() 
   {
       return Collections.unmodifiableMap( otherAttributes );
   }
}