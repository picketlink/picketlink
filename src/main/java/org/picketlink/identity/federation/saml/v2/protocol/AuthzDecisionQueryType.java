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
package org.picketlink.identity.federation.saml.v2.protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.v2.assertion.ActionType;
import org.picketlink.identity.federation.saml.v2.assertion.EvidenceType;

/**
 * <p>Java class for AuthzDecisionQueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthzDecisionQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}SubjectQueryAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Action" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Evidence" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Resource" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre> 
 */
public class AuthzDecisionQueryType extends SubjectQueryAbstractType
{
   private static final long serialVersionUID = 1L;

   protected List<ActionType> action = new ArrayList<ActionType>();

   protected EvidenceType evidence;

   protected URI resource;

   public AuthzDecisionQueryType(String id, XMLGregorianCalendar instant)
   {
      super(id, instant);
   }

   /**
    * Add an action
    * @param act
    */
   public void addAction(ActionType act)
   {
      this.action.add(act);
   }

   /**
    * Remove an action
    * @param act
    */
   public void removeAction(ActionType act)
   {
      this.action.remove(act);
   }

   /**
    * Gets the value of the action property.  
    */
   public List<ActionType> getAction()
   {
      return Collections.unmodifiableList(this.action);
   }

   /**
    * Gets the value of the evidence property.
    * 
    * @return
    *     possible object is
    *     {@link EvidenceType }
    *     
    */
   public EvidenceType getEvidence()
   {
      return evidence;
   }

   /**
    * Sets the value of the evidence property.
    * 
    * @param value
    *     allowed object is
    *     {@link EvidenceType }
    *     
    */
   public void setEvidence(EvidenceType value)
   {
      this.evidence = value;
   }

   /**
    * Gets the value of the resource property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public URI getResource()
   {
      return resource;
   }

   /**
    * Sets the value of the resource property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setResource(URI value)
   {
      this.resource = value;
   }
}