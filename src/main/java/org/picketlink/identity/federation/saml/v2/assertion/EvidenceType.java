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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>Java class for EvidenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EvidenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AssertionIDRef"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AssertionURIRef"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Assertion"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedAssertion"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class EvidenceType implements Serializable
{
   private static final long serialVersionUID = 1L;
   protected List<ChoiceType> evidences = new ArrayList<ChoiceType>();

   /**
    * Add an evidence
    * @param evidence
    */
   public void addEvidence( ChoiceType evidence )
   {
      evidences.add( evidence );
   }
   
   /**
    * Remove an evidence
    * @param evidence
    */
   public void removeEvidence( ChoiceType evidence )
   {
      evidences.remove( evidence );
   }

   /**
    * Get the list of evidences as a read only list
    * @return
    */
   public List<ChoiceType> evidences()
   {
      return Collections.unmodifiableList( evidences );
   }

   public static class ChoiceType implements Serializable
   {
      private static final long serialVersionUID = 1L;
      private String AssertionIDRef;
      private URI AssertionURIRef;
      private AssertionType assertion;
      private EncryptedAssertionType encryptedAssertion;

      public ChoiceType(String assertionIDRef)
      { 
         AssertionIDRef = assertionIDRef;
      }

      public ChoiceType(URI assertionURIRef)
      { 
         AssertionURIRef = assertionURIRef;
      }

      public ChoiceType(AssertionType assertion)
      { 
         this.assertion = assertion;
      }

      public ChoiceType(EncryptedAssertionType encryptedAssertion)
      { 
         this.encryptedAssertion = encryptedAssertion;
      }

      public String getAssertionIDRef()
      {
         return AssertionIDRef;
      }

      public URI getAssertionURIRef()
      {
         return AssertionURIRef;
      }

      public AssertionType getAssertion()
      {
         return assertion;
      }

      public EncryptedAssertionType getEncryptedAssertion()
      {
         return encryptedAssertion;
      } 
   }
}