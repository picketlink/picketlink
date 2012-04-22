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
package org.picketlink.identity.xmlsec.w3.xmlenc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="DataReference" type="{http://www.w3.org/2001/04/xmlenc#}ReferenceType"/>
 *         &lt;element name="KeyReference" type="{http://www.w3.org/2001/04/xmlenc#}ReferenceType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class ReferenceList 
{
   public static class References
   {
      private ReferenceType dataReference;
      private ReferenceType keyReference;
      
      public References(ReferenceType dataReference, ReferenceType keyReference)
      {
         this.dataReference = dataReference;
         this.keyReference = keyReference;
      }

      public ReferenceType getDataReference()
      {
         return dataReference;
      }

      public ReferenceType getKeyReference()
      {
         return keyReference;
      } 
   }
   
   private List<References> referencesList = new ArrayList<References>();
   
   public void add( References ref )
   {
      this.referencesList.add(ref);
   }
   
   public void addAll( List<References> refs )
   {
      this.referencesList.addAll(refs);
   }
   
   public void remove( References ref )
   {
      this.referencesList.remove( ref );
   }
   
   public List<References> getReferences()
   {
      return Collections.unmodifiableList( referencesList );
   }
}