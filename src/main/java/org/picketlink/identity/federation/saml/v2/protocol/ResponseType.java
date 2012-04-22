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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedAssertionType;

/**
 * <p>Java class for ResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}StatusResponseType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Assertion"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedAssertion"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ResponseType extends StatusResponseType
{
   private static final long serialVersionUID = 1L;

   protected List<RTChoiceType> assertions = new ArrayList<ResponseType.RTChoiceType>();

   public ResponseType(String id, XMLGregorianCalendar issueInstant)
   {
      super(id, issueInstant);
   }

   public ResponseType(StatusResponseType srt)
   {
      super(srt);
   }

   /**
    * Add an assertion
    * @param choice
    */
   public void addAssertion(RTChoiceType choice)
   {
      assertions.add(choice);
   }

   /**
    * Remove an assertion
    * @param choice
    */
   public void removeAssertion(RTChoiceType choice)
   {
      assertions.remove(choice);
   }

   /**
    * Replace the first assertion with the passed assertion
    * @param id id of the old assertion
    * @param newAssertion
    */
   public void replaceAssertion(String id, RTChoiceType newAssertion)
   {
      int index = 0;
      if (id != null && !id.isEmpty())
      {
         for (RTChoiceType assertion : assertions)
         {
            if (assertion.getID().equals(id))
            {
               break;
            }
            index++;
         }
      }
      assertions.remove(index);
      assertions.add(index, newAssertion);
   }

   /**
    * Gets a read only list of assertions
    */
   public List<RTChoiceType> getAssertions()
   {
      return Collections.unmodifiableList(assertions);
   }

   public static class RTChoiceType
   {
      private AssertionType assertion;

      private EncryptedAssertionType encryptedAssertion;

      private String id;

      public RTChoiceType(AssertionType assertion)
      {
         this.assertion = assertion;
         this.id = assertion.getID();
      }

      public RTChoiceType(EncryptedAssertionType encryptedAssertion)
      {
         this.encryptedAssertion = encryptedAssertion;

      }

      public AssertionType getAssertion()
      {
         return assertion;
      }

      public EncryptedAssertionType getEncryptedAssertion()
      {
         return encryptedAssertion;
      }

      public String getID()
      {
         return id;
      }
   }
}