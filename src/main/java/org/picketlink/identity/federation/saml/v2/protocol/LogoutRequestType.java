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

import org.picketlink.identity.federation.saml.v2.assertion.BaseIDAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;

/**
 * <p>Java class for LogoutRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LogoutRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}BaseID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *         &lt;/choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}SessionIndex" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Reason" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="NotOnOrAfter" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class LogoutRequestType extends RequestAbstractType
{
   private static final long serialVersionUID = 1L;

   protected BaseIDAbstractType baseID;

   protected NameIDType nameID;

   protected EncryptedElementType encryptedID;

   protected List<String> sessionIndex = new ArrayList<String>();

   protected String reason;

   protected XMLGregorianCalendar notOnOrAfter;

   public LogoutRequestType(String id, XMLGregorianCalendar instant)
   {
      super(id, instant);
   }

   /**
     * Gets the value of the baseID property.
     * 
     * @return
     *     possible object is
     *     {@link BaseIDAbstractType }
     *     
     */
   public BaseIDAbstractType getBaseID()
   {
      return baseID;
   }

   /**
    * Sets the value of the baseID property.
    * 
    * @param value
    *     allowed object is
    *     {@link BaseIDAbstractType }
    *     
    */
   public void setBaseID(BaseIDAbstractType value)
   {
      this.baseID = value;
   }

   /**
    * Gets the value of the nameID property.
    * 
    * @return
    *     possible object is
    *     {@link NameIDType }
    *     
    */
   public NameIDType getNameID()
   {
      return nameID;
   }

   /**
    * Sets the value of the nameID property.
    * 
    * @param value
    *     allowed object is
    *     {@link NameIDType }
    *     
    */
   public void setNameID(NameIDType value)
   {
      this.nameID = value;
   }

   /**
    * Gets the value of the encryptedID property.
    * 
    * @return
    *     possible object is
    *     {@link EncryptedElementType }
    *     
    */
   public EncryptedElementType getEncryptedID()
   {
      return encryptedID;
   }

   /**
    * Sets the value of the encryptedID property.
    * 
    * @param value
    *     allowed object is
    *     {@link EncryptedElementType }
    *     
    */
   public void setEncryptedID(EncryptedElementType value)
   {
      this.encryptedID = value;
   }

   /**
    * Add session index
    * @param index
    */
   public void addSessionIndex(String index)
   {
      this.sessionIndex.add(index);
   }

   /**
    * Remove session index
    * @param index
    */
   public void removeSessionIndex(String index)
   {
      this.sessionIndex.remove(index);
   }

   /**
    * Gets the value of the sessionIndex property.
    *  
    */
   public List<String> getSessionIndex()
   {
      return Collections.unmodifiableList(this.sessionIndex);
   }

   /**
    * Gets the value of the reason property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getReason()
   {
      return reason;
   }

   /**
    * Sets the value of the reason property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setReason(String value)
   {
      this.reason = value;
   }

   /**
    * Gets the value of the notOnOrAfter property.
    * 
    * @return
    *     possible object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public XMLGregorianCalendar getNotOnOrAfter()
   {
      return notOnOrAfter;
   }

   /**
    * Sets the value of the notOnOrAfter property.
    * 
    * @param value
    *     allowed object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public void setNotOnOrAfter(XMLGregorianCalendar value)
   {
      this.notOnOrAfter = value;
   }

}
