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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

/**
 * <p>Java class for EndpointType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EndpointType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="Binding" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Location" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="ResponseLocation" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class EndpointType extends TypeWithOtherAttributes
{

   protected List<Object> any = new ArrayList<Object>();

   protected URI binding;

   protected URI location;

   protected URI responseLocation;

   public EndpointType(URI binding, URI location)
   {
      this.binding = binding;
      this.location = location;
   }

   /**
    * Add an object
    * @param obj
    */
   public void addObject(Object obj)
   {
      this.any.add(obj);
   }

   /**
    * remove an object
    * @param obj
    */
   public void removeObject(Object obj)
   {
      this.any.remove(obj);
   }

   /**
    * Gets the value of the any property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Element }
    * {@link Object }
    * 
    * 
    */
   public List<Object> getAny()
   {
      return Collections.unmodifiableList(this.any);
   }

   /**
    * Gets the value of the binding property.
    * 
    * @return
    *     possible object is
    *     {@link URI }
    *     
    */
   public URI getBinding()
   {
      return binding;
   }

   /**
    * Gets the value of the location property.
    * 
    * @return
    *     possible object is
    *     {@link URI }
    *     
    */
   public URI getLocation()
   {
      return location;
   }

   /**
    * Gets the value of the responseLocation property.
    * 
    * @return
    *     possible object is
    *     {@link URI }
    *     
    */
   public URI getResponseLocation()
   {
      return responseLocation;
   }

   /**
    * Sets the value of the responseLocation property.
    * 
    * @param value
    *     allowed object is
    *     {@link URI }
    *     
    */
   public void setResponseLocation(URI value)
   {
      this.responseLocation = value;
   }
}