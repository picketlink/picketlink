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
package org.picketlink.identity.federation.saml.v2.assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>Java class for AuthzDecisionStatementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthzDecisionStatementType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}StatementAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Action" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Evidence" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Resource" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Decision" use="required" type="{urn:oasis:names:tc:SAML:2.0:assertion}DecisionType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class AuthzDecisionStatementType
extends StatementAbstractType
{ 
   private static final long serialVersionUID = 1L;
   protected List<ActionType> action = new ArrayList<ActionType>(); 
   protected EvidenceType evidence; 
   protected String resource; 
   protected DecisionType decision;

   /**
    * Get the list of actions (read-only list)
    * @return {@link List} read only 
    */
   public List<ActionType> getAction() 
   { 
      return Collections.unmodifiableList( this.action );
   }

   /**
    * Add an action
    * @param actionType
    */
   public void addAction( ActionType actionType )
   {
      action.add(actionType); 
   }
   
   /**
    * Remove an action
    * @param actionType
    */
   public void removeAction( ActionType actionType )
   {
      action.remove(actionType); 
   }

   /**
    * Gets the value of the evidence property.
    * 
    * @return
    *     possible object is
    *     {@link EvidenceType }
    *     
    */
   public EvidenceType getEvidence() {
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
   public void setEvidence(EvidenceType value) {
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
   public String getResource() {
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
   public void setResource(String value) {
      this.resource = value;
   }

   /**
    * Gets the value of the decision property.
    * 
    * @return
    *     possible object is
    *     {@link DecisionType }
    *     
    */
   public DecisionType getDecision() {
      return decision;
   }

   /**
    * Sets the value of the decision property.
    * 
    * @param value
    *     allowed object is
    *     {@link DecisionType }
    *     
    */
   public void setDecision(DecisionType value) {
      this.decision = value;
   }
}