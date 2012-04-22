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

package org.picketlink.identity.federation.ws.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.picketlink.identity.federation.ws.addressing.AnyAddressingType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}AppliesTo"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}Policy"/>
 *           &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}PolicyReference"/>
 *         &lt;/choice>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
{"appliesTo", "policyOrPolicyReference", "any"})
@XmlRootElement(name = "PolicyAttachment")
public class PolicyAttachment extends AnyAddressingType
{
   protected AppliesTo appliesTo;

   protected List<PolicyChoice> theChoices = new ArrayList<PolicyChoice>();

   public static class PolicyChoice
   {
      private Policy thePolicy;

      private PolicyReference thePolicyRef;

      public PolicyChoice(Policy p)
      {
         thePolicy = p;
      }

      public PolicyChoice(PolicyReference pr)
      {
         thePolicyRef = pr;
      }

      public Policy getPolicy()
      {
         return thePolicy;
      }

      public PolicyReference getPolicyReference()
      {
         return thePolicyRef;
      }
   }

   /**
    * Gets the value of the appliesTo property.
    * 
    * @return
    *     possible object is
    *     {@link AppliesTo }
    *     
    */
   public AppliesTo getAppliesTo()
   {
      return appliesTo;
   }

   /**
    * Sets the value of the appliesTo property.
    * 
    * @param value
    *     allowed object is
    *     {@link AppliesTo }
    *     
    */
   public void setAppliesTo(AppliesTo value)
   {
      this.appliesTo = value;
   }

   /**
    * Add a {@link PolicyChoice}
    * @param pc
    */
   public void addChoice(PolicyChoice pc)
   {
      this.theChoices.add(pc);
   }

   /**
    * Gets the value of the policyOrPolicyReference property.
    *  
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link PolicyReference }
    * {@link Policy }
    */
   public List<PolicyChoice> getPolicyOrPolicyReference()
   {
      return Collections.unmodifiableList(this.theChoices);
   }
}