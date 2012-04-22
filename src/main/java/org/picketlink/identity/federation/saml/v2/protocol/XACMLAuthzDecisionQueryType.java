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

import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.security.xacml.core.model.context.RequestType;

/**
 * <p>Java class for XACMLAuthzDecisionQueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XACMLAuthzDecisionQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:context:schema:os}Request"/>
 *       &lt;/sequence>
 *       &lt;attribute name="InputContextOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="ReturnContext" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class XACMLAuthzDecisionQueryType extends RequestAbstractType
{
   private static final long serialVersionUID = 1L;

   protected RequestType request;

   protected Boolean inputContextOnly;

   protected Boolean returnContext;

   public XACMLAuthzDecisionQueryType(String id, XMLGregorianCalendar instant)
   {
      super(id, instant);
   }

   /**
    * Gets the value of the request property.
    * 
    * @return
    *     possible object is
    *     {@link RequestType }
    *     
    */
   public RequestType getRequest()
   {
      return request;
   }

   /**
    * Sets the value of the request property.
    * 
    * @param value
    *     allowed object is
    *     {@link RequestType }
    *     
    */
   public void setRequest(RequestType value)
   {
      this.request = value;
   }

   /**
    * Gets the value of the inputContextOnly property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public boolean isInputContextOnly()
   {
      if (inputContextOnly == null)
      {
         return false;
      }
      else
      {
         return inputContextOnly;
      }
   }

   /**
    * Sets the value of the inputContextOnly property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setInputContextOnly(Boolean value)
   {
      this.inputContextOnly = value;
   }

   /**
    * Gets the value of the returnContext property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public boolean isReturnContext()
   {
      if (returnContext == null)
      {
         return false;
      }
      else
      {
         return returnContext;
      }
   }

   /**
    * Sets the value of the returnContext property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setReturnContext(Boolean value)
   {
      this.returnContext = value;
   }

}