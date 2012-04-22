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
package org.picketlink.identity.xmlsec.w3.xmldsig;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import org.w3c.dom.Element;


/**
 * <p>Java class for TransformType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransformType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;any/>
 *         &lt;element name="XPath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *       &lt;attribute name="Algorithm" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class TransformType 
{

   protected List<Object> content = new ArrayList<Object>();
   protected URI algorithm; 

   public TransformType(URI algorithm)
   { 
      this.algorithm = algorithm;
   }
   
   public void addTransform( Object obj )
   {
      this.content.add(obj);
   }
   
   public void removeTransform( Object obj )
   {
      this.content.remove(obj);
   }

   /**
    * Gets the value of the content property.
    *  
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Element }
    * {@link String }
    * {@link Object }  
    * 
    */
   public List<Object> getContent() { 
      return Collections.unmodifiableList( this.content );
   }

   /**
    * Gets the value of the algorithm property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public URI getAlgorithm() {
      return algorithm;
   }

}