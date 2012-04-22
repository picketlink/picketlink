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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Java class for SubjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;choice>
 *             &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}BaseID"/>
 *             &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *             &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *           &lt;/choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}SubjectConfirmation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}SubjectConfirmation" maxOccurs="unbounded"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class SubjectType implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected List<SubjectConfirmationType> subjectConfirmation = new ArrayList<SubjectConfirmationType>();

   protected STSubType subType;

   /**
    * Get the {@link STSubType}
    * @return
    */
   public STSubType getSubType()
   {
      return subType;
   }

   /**
    * Set the {@link STSubType}
    * @param subType
    */
   public void setSubType(STSubType subType)
   {
      this.subType = subType;
   }

   /**
    * Get the size of subject confirmations
    * @return
    */
   public int getCount()
   {
      return subjectConfirmation.size();
   }

   /**
    * Get a list of subject confirmations
    * @return {@link} read only list of subject confirmation
    */
   public List<SubjectConfirmationType> getConfirmation()
   {
      return Collections.unmodifiableList(subjectConfirmation);
   }

   /**
    * Add a subject confirmation
    * @param con
    */
   public void addConfirmation(SubjectConfirmationType con)
   {
      subjectConfirmation.add(con);
   }

   /**
    * Remove a subject confirmation
    * @param con
    */
   public void removeConfirmation(SubjectConfirmationType con)
   {
      subjectConfirmation.remove(con);
   }

   public static class STSubType implements Serializable
   {
      private static final long serialVersionUID = -4073731807610876524L;

      private BaseIDAbstractType baseID;

      private EncryptedElementType encryptedID;

      protected List<SubjectConfirmationType> subjectConfirmation = new ArrayList<SubjectConfirmationType>();

      public void addBaseID(BaseIDAbstractType base)
      {
         this.baseID = base;
      }

      public BaseIDAbstractType getBaseID()
      {
         return baseID;
      }

      public EncryptedElementType getEncryptedID()
      {
         return encryptedID;
      }

      public void setEncryptedID(EncryptedElementType encryptedID)
      {
         this.encryptedID = encryptedID;
      }

      public void addConfirmation(SubjectConfirmationType con)
      {
         subjectConfirmation.add(con);
      }

      public int getCount()
      {
         return subjectConfirmation.size();
      }

      public List<SubjectConfirmationType> getConfirmation()
      {
         return Collections.unmodifiableList(subjectConfirmation);
      }
   }
}