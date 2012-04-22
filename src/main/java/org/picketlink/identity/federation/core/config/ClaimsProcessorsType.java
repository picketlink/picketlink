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
package org.picketlink.identity.federation.core.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * 				The claims processors specify the classes that are capable of processing specific claims dialects.
 * 			
 * 
 * <p>Java class for ClaimsProcessorsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ClaimsProcessorsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ClaimsProcessor" type="{urn:picketlink:identity-federation:config:1.0}ClaimsProcessorType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ClaimsProcessorsType
{

   protected List<ClaimsProcessorType> claimsProcessor = new ArrayList<ClaimsProcessorType>();

   public void add(ClaimsProcessorType claim)
   {
      this.claimsProcessor.add(claim);
   }

   public void remove(ClaimsProcessorType claim)
   {
      this.claimsProcessor.remove(claim);
   }

   /**
    * Gets the value of the claimsProcessor property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link ClaimsProcessorType }
    * 
    * 
    */
   public List<ClaimsProcessorType> getClaimsProcessor()
   {
      return Collections.unmodifiableList(this.claimsProcessor);
   }

}